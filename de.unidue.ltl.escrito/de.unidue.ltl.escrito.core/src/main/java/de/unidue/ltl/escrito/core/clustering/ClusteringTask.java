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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.ml.weka.core._eka;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.unidue.ltl.escrito.core.Utils;

/**
 * Clusters the training data and outputs some statistics.
 */
public class ClusteringTask
extends ExecutableTaskBase
implements Constants
{
	@Discriminator
	private List<String> clusteringArguments;
	@Discriminator
	private String featureMode;
	@Discriminator
	private String learningMode;

	@Discriminator(name = "dimension_number_of_clusters_min")
	public static int NUMBER_OF_CLUSTERS_MIN;
	@Discriminator(name = "dimension_number_of_clusters_max")
	public static int NUMBER_OF_CLUSTERS_MAX;

	//  @Discriminator(name = "instance_id_to_text_map")
	//	public static Map<String, String> INSTANCE_ID_TO_TEXT_MAP;

	@Override
	public void execute(TaskContext aContext)
			throws Exception
	{
		// TODO: für den normalen Betrieb wieder raus!
		for (int numClusters = NUMBER_OF_CLUSTERS_MIN; numClusters<=NUMBER_OF_CLUSTERS_MAX; numClusters++){
			if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
				throw new IllegalArgumentException("Cannot use multi-label setup in clustering.");
			}
			boolean multiLabel = false;

			File arffFileTrain = Utils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
					FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);
			Instances trainData = _eka.getInstances(arffFileTrain, multiLabel);

			// get number of outcomes
			List<String> trainOutcomeValues = _eka.getClassLabels(trainData, multiLabel);

			// TODO: complain if it is not KMeans

			Clusterer clusterer = AbstractClusterer.forName(clusteringArguments.get(0), clusteringArguments
					.subList(1, clusteringArguments.size()).toArray(new String[0]));
			((SimpleKMeans) clusterer).setNumClusters(numClusters);

			System.out.println(trainData.numAttributes());

			Instances copyTrainData = new Instances(trainData);
			Instances clusterTrainData = ClusterUtils.removeInstanceIdAndClassLabel(trainData, multiLabel); 
			clusterer.buildClusterer(clusterTrainData);

			// get a mapping from clusterIDs to instance offsets in the ARFF
			Map<Integer, Set<Integer>> clusterMap = ClusterUtils.getClusterMap(clusterTrainData, clusterer);

			Map<String, String> instanceId2TextMap = Utils.getInstanceId2TextMapTrain(aContext);

			ConditionalFrequencyDistribution<Integer, String> clusterAssignments = ClusterUtils.writeClusterAssignments(aContext,
					numClusters, trainOutcomeValues, copyTrainData, clusterMap, instanceId2TextMap);

			System.out.println("ID\tSIZE\tPURITY\tRMSE");
			for (Integer clusterId : clusterMap.keySet()) {
				FrequencyDistribution<String> fd = clusterAssignments.getFrequencyDistribution(clusterId);
				double purity = (double) fd.getCount(fd.getSampleWithMaxFreq()) / fd.getN();
				String purityString = String.format("%.2f", purity);
				double rmse = getRMSE(fd, trainOutcomeValues);
				String rmseString = String.format("%.2f", rmse);
				System.out.println(clusterId + "\t" + clusterMap.get(clusterId).size() + "\t" + purityString + "\t" + rmseString);
			}
			System.out.println();      
			double dbIndex = computeDaviesBouldinIndex(clusterMap, (SimpleKMeans) clusterer, clusterTrainData);
			double dunnIndex = computeDunnIndex(clusterMap, (SimpleKMeans) clusterer, clusterTrainData);
			System.out.println("Davies-Bouldin-Index: "+dbIndex);
			File clusterEval = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
					AccessMode.READWRITE)
					.getPath()
					+ "/" + "internal_cluster_evaluation_"+numClusters+".txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(clusterEval));
			bw.write("Davies-Bouldin-Index: "+dbIndex+"\n");
			bw.write("Dunn-Index: "+dunnIndex+"\n");
			bw.close();
		}
	}




// as distance between clusters we use the distance between centroids
	// as distance within cluster average distance to the centroid
	private double computeDunnIndex(Map<Integer, Set<Integer>> clusterMap, SimpleKMeans clusterer,
			Instances clusterTrainData) {
		double minInterClusterDistance = Double.MAX_VALUE;
		double maximalIntraClusterDistance = 0.0;
		Instances centroids = clusterer.getClusterCentroids();
		for (Instance c1 : centroids){
			for (Instance c2: centroids){
				if (c1.equals(c2)){
					continue;
				}
				double centroidDistance = clusterer.getDistanceFunction().distance(c1, c2);
				if (centroidDistance < minInterClusterDistance){
					minInterClusterDistance = centroidDistance;
				}
			}
		}
		System.out.println(minInterClusterDistance);
		for (int clusterId : clusterMap.keySet()){
				double distanceToCentroid = computeAverageDistanceToCentroid(centroids.get(clusterId), clusterMap.get(clusterId), clusterTrainData, clusterer);
				if (distanceToCentroid > maximalIntraClusterDistance){
					maximalIntraClusterDistance = distanceToCentroid; 
				}
		}	
		System.out.println(maximalIntraClusterDistance);
		return minInterClusterDistance/maximalIntraClusterDistance;
	}
	
	private double computeDaviesBouldinIndex(Map<Integer, Set<Integer>> clusterMap, SimpleKMeans clusterer, Instances copyTrainData) throws Exception {
		// cluster centroids are returned in the same order as cluster assignments by Weka
		Instances centroids = clusterer.getClusterCentroids();
		double sum = 0.0;
		int numClusters = clusterMap.size();
		for (int clusterId1 : clusterMap.keySet()){
			double max = 0.0;
			for (int clusterId2 : clusterMap.keySet()){
				if (clusterId1 == clusterId2){
					continue;
				}
				double centroidDistance = clusterer.getDistanceFunction().distance(centroids.get(clusterId1), centroids.get(clusterId2));
				double distanceToCentroid1 = computeAverageDistanceToCentroid(centroids.get(clusterId1), clusterMap.get(clusterId1), copyTrainData, clusterer);
				double distanceToCentroid2 = computeAverageDistanceToCentroid(centroids.get(clusterId2), clusterMap.get(clusterId2), copyTrainData, clusterer);
				double clusterValue = (distanceToCentroid1 + distanceToCentroid2)/centroidDistance;
				if (clusterValue > max){
					max = clusterValue;
				}
			} 
			sum+=max;
		}
		//	System.out.println("sum: "+sum);
		return sum/(1.0*numClusters);
	}

	private double computeAverageDistanceToCentroid(Instance centroid, Set<Integer> clusterItems, Instances copyTrainData, SimpleKMeans clusterer) {
		double sum = 0.0;
		for (int itemId : clusterItems){
			Instance instance = copyTrainData.get(itemId);
			sum += clusterer.getDistanceFunction().distance(centroid, instance);
		}
		return sum/(clusterItems.size()*1.0);
	}
	//
	//	private Map<Integer, Instance> assignCentroidsToClusters(Instances centroids,
	//			Map<Integer, Set<Integer>> clusterMap, Instances copyTrainData, SimpleKMeans clusterer) {
	//		Map<Integer, Instance> centroidPerCluster = new HashMap<Integer, Instance>();
	//		for (Instance centroid : centroids){
	//			System.out.println(centroid);
	//			double minDistance = Double.MAX_VALUE;
	//			int bestClusterId = -1;
	//			// compare with each cluster
	//			for (int clusterId : clusterMap.keySet()){
	//				System.out.println("Comparing to cluster "+clusterId);
	//				double sumDistances = 0.0;
	//				Set<Integer> itemsInCluster = clusterMap.get(clusterId);
	//				int closestItemId = -1;
	//				double minItemDistance = Double.MAX_VALUE;
	//				for (int itemId : itemsInCluster){
	//					Instance item = copyTrainData.get(itemId);
	//					//	System.out.println("Centroid: "+centroid.numAttributes());
	//					//	System.out.println("Item: "+item.numAttributes());
	//					double itemDistance = clusterer.getDistanceFunction().distance(centroid, item);
	//					sumDistances += itemDistance;
	//					if (itemDistance < minItemDistance){
	//						minItemDistance = itemDistance;
	//						closestItemId = itemId;
	//					}
	//				}
	//				System.out.println("closest Item: "+closestItemId+"\t"+minItemDistance);
	//				sumDistances = sumDistances/(itemsInCluster.size()*1.0);
	//				System.out.println("distance: "+sumDistances);
	//				if (sumDistances<minDistance){
	//					minDistance = sumDistances;
	//					bestClusterId = clusterId;
	//					System.out.println("new minimal distance: "+ minDistance);
	//				}
	//			}
	//			System.out.println("bestClusterId: "+bestClusterId+"\n");
	//			centroidPerCluster.put(bestClusterId, centroid);
	//		}
	//		if (centroidPerCluster.keySet().size() != centroids.size()){
	//			System.err.println("Expected "+centroids.size()+" clusters, but found only "+centroidPerCluster.keySet().size());
	//		}
	//		return centroidPerCluster;
	//	}






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


}
