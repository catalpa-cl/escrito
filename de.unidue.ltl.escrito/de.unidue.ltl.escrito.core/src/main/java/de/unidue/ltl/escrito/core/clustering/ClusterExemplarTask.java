
package de.unidue.ltl.escrito.core.clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Clusters the training data, selects an exemplar/centroid,
 * uses thesee in the following test task as new training data.
 */
public class ClusterExemplarTask
extends ExecutableTaskBase
implements Constants
{

	/**
	 * Public name of the output folder for the new training data
	 */
	public static final String ADAPTED_TRAINING_DATA = "train.new";

	@Discriminator
	private List<String> clusteringArguments;
	@Discriminator
	private String featureMode;
	@Discriminator
	private String learningMode;


	@Discriminator(name = "dimension_number_of_training_instances")
	public static int[] NUMBER_OF_TRAINING_INSTANCES;



	@Override
	public void execute(TaskContext aContext)
			throws Exception
	{
		System.out.println("ClusterExemplarTask");
		if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
			throw new IllegalArgumentException("Cannot use multi-label setup in clustering.");
		}
		boolean multiLabel = false;
		

		for (int numberOfClusters : NUMBER_OF_TRAINING_INSTANCES ){

			File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
					FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);

			Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);

			Clusterer abstractClusterer = AbstractClusterer.forName(clusteringArguments.get(0), clusteringArguments
					.subList(1, clusteringArguments.size()).toArray(new String[0]));
			// we assume that only this method has been used - breaks modularity, but need results fast ... :/
			// TODO: allow for other clusteirng methods?
			SimpleKMeans clusterer = (SimpleKMeans) abstractClusterer;
			clusterer.setNumClusters(numberOfClusters);

			Instances copyTrainDataWithId = new Instances(trainData);
			Instances selectedTrainDataWithId = new Instances(trainData);
			selectedTrainDataWithId.clear();
			
			trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
			Instances copyTrainData = new Instances(trainData);

			// generate data for clusterer (w/o class)
			Remove filter = new Remove();
			filter.setAttributeIndices("" + (trainData.classIndex() + 1));
			filter.setInputFormat(trainData);
			Instances clusterTrainData = Filter.useFilter(trainData, filter);

			clusterer.buildClusterer(clusterTrainData);
			Instances centroids = clusterer.getClusterCentroids();

			//        Add addFilter = new Add();
			//        addFilter.setAttributeIndex(new Integer(numTestLabels + i + 1).toString());
			//        addFilter.setNominalLabels("0,1");
			//        addFilter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
			//        addFilter.setInputFormat(testData);

			trainData.clear();

			Enumeration<Instance> centroidInstances = centroids.enumerateInstances();
			while (centroidInstances.hasMoreElements()) {
				Instance centroidInstance = centroidInstances.nextElement();

				// centroidInstance is usually not a real instance, but a virtual centroid
				// we need to find the closest point in the training data
				double minDistance = Double.POSITIVE_INFINITY;
				int offset = 0;
				int minOffset = 0;
				Enumeration<Instance> trainInstances = clusterTrainData.enumerateInstances();
				while (trainInstances.hasMoreElements()) {
					Instance trainInstance = trainInstances.nextElement();

					double dist = distance(centroidInstance, trainInstance);
					if (dist < minDistance) {
						minDistance = dist;
						minOffset = offset;
					}
					offset++;
				}

				// add selected instance to instances
				trainData.add(copyTrainData.get(minOffset));	
				selectedTrainDataWithId.add(copyTrainDataWithId.get(minOffset));
			}


			// write the new training data (that will be used by the test task instead of the original one)    
			File predictionFile = WekaUtils.getFile(aContext,"", ADAPTED_TRAINING_DATA, AccessMode.READWRITE);
			DataSink.write(predictionFile.getPath() + "/training-data_"+numberOfClusters+".arff.gz", trainData);

			File trainItemIds = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
					AccessMode.READWRITE)
					.getPath()
					+ "/" + Constants.EVAL_FILE_NAME + "_" + numberOfClusters + "_itemIds.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(trainItemIds));
			for (Instance inst : selectedTrainDataWithId){
				bw.write(inst.stringValue(0)+"\n");
			}
			bw.close();
		}
	}

	private double distance(Instance i1, Instance i2) {
		double dist = 0.0;
		for (int i=0; i<i1.numAttributes(); i++) {
			dist += Math.abs(i1.value(i) - i2.value(i));
		}
		return dist / i1.numAttributes();
	}
}
