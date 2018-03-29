/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.unidue.ltl.escrito.core.clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Clusters the training data, the performs label propagation 
 * and takes the purest clusters as new training material.
 * Evaluates on the test data in the next task.
 */
public class ClusterLabelPropagationTask
    extends ExecutableTaskBase
    implements Constants
{
	
     @Discriminator
    private List<String> clusteringArguments;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;
    
    @Discriminator(name = "dimension_number_of_clusters")
	public static int NUMBER_OF_CLUSTERS;
	

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
        	throw new IllegalArgumentException("Cannot use multi-label setup in clustering.");
        }
        boolean multiLabel = false;

        File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
        		FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);

        Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
        
        // get number of outcomes
		List<String> trainOutcomeValues = WekaUtils.getClassLabels(trainData, multiLabel);
		
        SimpleKMeans clusterer = (SimpleKMeans) AbstractClusterer.forName(clusteringArguments.get(0), clusteringArguments
                .subList(1, clusteringArguments.size()).toArray(new String[0]));
        clusterer.setNumClusters(NUMBER_OF_CLUSTERS);
        
        Instances copyTrainData = new Instances(trainData);
        Instances clusterTrainData = ClusterUtils.removeInstanceIdAndClassLabel(trainData, multiLabel);
        		
        clusterer.buildClusterer(clusterTrainData);
        
        // get a mapping from clusterIDs to instance offsets in the ARFF
        Map<Integer, Set<Integer>> clusterMap = ClusterUtils.getClusterMap(clusterTrainData, clusterer);
        
        // get a CFD that stores the number of outcomes for each class indexed by the clusterID
        ConditionalFrequencyDistribution<Integer, String> clusterCfd = 
        		getClusterCfd(clusterMap, copyTrainData, trainOutcomeValues);
        
        Map<Integer, String> mostFrequentClassPerCluster = new HashMap<Integer, String>();
        Map<Integer, Double> clusterPurityMap = new HashMap<Integer, Double>();
        for (Integer clusterId : clusterMap.keySet()) {
        	FrequencyDistribution<String> fd = clusterCfd.getFrequencyDistribution(clusterId);
        	mostFrequentClassPerCluster.put(clusterId, fd.getSampleWithMaxFreq());
        	
        	double purity = (double) fd.getCount(fd.getSampleWithMaxFreq()) / fd.getN();
// attention - cannot simply use RMSE here - as smaller values are better unlike with purity
//        	double rmse = getRMSE(fd, trainOutcomeValues);
        	clusterPurityMap.put(clusterId, purity);
        }
        
        // sort clusters by score
        Map<Integer,Double> sortedClusters = new TreeMap<Integer,Double>(new ValueComparator(clusterPurityMap));
        sortedClusters.putAll(clusterPurityMap);
        
        // change the outcome values of instances according to the most frequent class in its cluster
        
        double avgPurity = 0.0;
        int n = 0;
        int correctlyLabeled = 0;
        for (Integer clusterId : sortedClusters.keySet()) { 	
        	n++;
        	avgPurity += clusterPurityMap.get(clusterId);
        	correctlyLabeled += clusterPurityMap.get(clusterId)*sortedClusters.keySet().size();
        	n+=sortedClusters.keySet().size();
        }
        double accuracy = correctlyLabeled/(n*1.0);
        System.out.println(correctlyLabeled+"\t"+n+"\t"+accuracy);
        System.out.println(avgPurity+"\t"+NUMBER_OF_CLUSTERS);
        avgPurity = avgPurity / NUMBER_OF_CLUSTERS;
        System.out.println("Average cluster purity: " + avgPurity);
        System.out.println("Average cluster accuracy: " + accuracy);
        File clusterEval = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
				AccessMode.READWRITE)
				.getPath()
				+ "/" + "cluster_evaluation.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(clusterEval));
        bw.write("Average cluster purity: " + avgPurity+"\n");
        bw.write("Average cluster accuracy: " + accuracy+"\n");
       bw.close();
    }
    
    
    private ConditionalFrequencyDistribution<Integer,String> getClusterCfd(Map<Integer, Set<Integer>> clusterMap, Instances data, List<String> outcomeValues) {
        ConditionalFrequencyDistribution<Integer,String> clusterAssignments = new ConditionalFrequencyDistribution<Integer,String>();

    	for (Integer clusterId : clusterMap.keySet()) {
        	for (Integer offset : clusterMap.get(clusterId)) {
        		
        		// get instance ID from instance
        		Instance instance = data.get(offset);
        		
        		Double classOffset = new Double(instance.value(data.classAttribute()));
                String label = outcomeValues.get(classOffset.intValue());
                
        		clusterAssignments.addSample(clusterId, label, 1);       
        	}
        }
    	
    	return clusterAssignments;
    }
    
    private double getRMSE(FrequencyDistribution<String> fd, List<String> outcomeStrings) {
    	Integer[] outcomeValues = new Integer[outcomeStrings.size()];
    	for (int i=0; i<outcomeStrings.size(); i++) {
    		outcomeValues[i] = Integer.parseInt(outcomeStrings.get(i));
    	}
    	List<Integer> ratingsA = new ArrayList<Integer>();
    	List<Integer> ratingsB = new ArrayList<Integer>();
    	
    	for (String key : fd.getKeys()) {
    		for (int i=0; i<fd.getCount(key); i++) {
        		ratingsA.add(Integer.parseInt(key));
        		ratingsB.add(Integer.parseInt(fd.getSampleWithMaxFreq()));
    		}
    	}
    	
    	int sum = 0;
    	for (int i=0; i<ratingsA.size(); i++) {
    		int distance = ratingsA.get(i) - ratingsB.get(i);
    		sum += distance*distance;
    	}
    	double rmse = Math.sqrt((double) sum / ratingsA.size());
    	
    	return rmse;
    }
    
    class ValueComparator
	    implements Comparator<Integer>
	{
	    Map<Integer,Double> base;
	
	    public ValueComparator(Map<Integer,Double> base)
	    {
	        this.base = base;
	    }
	
	    public int compare(Integer a, Integer b)
	    {
	
	        if (base.get(a) < base.get(b)) {
	            return 1;
	        }
	        else {
	            return -1;
	        }
	    }
	}
}
