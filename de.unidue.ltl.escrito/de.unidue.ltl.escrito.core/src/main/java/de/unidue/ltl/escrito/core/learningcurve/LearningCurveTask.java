package de.unidue.ltl.escrito.core.learningcurve;


import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.unidue.ltl.escrito.core.Utils;




/**
 * Builds the classifier from the training data and performs classification on the test data.
 * Repeats that for different amounts of training data in order to build a learning curve.
 * 
 */
public class LearningCurveTask
extends ExecutableTaskBase
//  extends WekaTestTask
implements Constants
{
	@Discriminator
	private List<String> classificationArguments;   
	@Discriminator
	private String featureMode;
	@Discriminator
	private String learningMode;
	
	// Should this be static? If not, how can I caccess it in the report?
	@Discriminator(name = "dimension_iterations")
	public static Integer ITERATIONS = 100;
	
	@Discriminator(name = "dimension_number_of_training_instances")
	public static int[] NUMBER_OF_TRAINING_INSTANCES = {4,8,16,32,64,128,256};
	
	
	@Override
	public void execute(TaskContext aContext)
			throws Exception
	{
		boolean multiLabel = false;
		// TODO comment in again
	//	Map<String, String> instanceId2TextMap = Utils.getInstanceId2TextMap(aContext);


		System.out.println("Execute LearningCurveTask");
		for (Integer numberInstances : NUMBER_OF_TRAINING_INSTANCES) {
			for (int iteration=0; iteration<ITERATIONS; iteration++) {
			//	System.out.println(numberInstances+"\t"+iteration);
				File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
						FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);
					File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
							FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);
			
				Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
				Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);


				if (numberInstances > trainData.size()) {
					System.out.println("Not enough training data!");
					continue;
				}

				Classifier cl = AbstractClassifier.forName(classificationArguments.get(0), classificationArguments
						.subList(1, classificationArguments.size()).toArray(new String[0]));

				Instances copyTestData = new Instances(testData);
				testData = WekaUtils.removeInstanceId(testData, multiLabel);

				
				Random generator = new Random();
				generator.setSeed(System.nanoTime());
				trainData.randomize(generator);

				// remove fraction of training data that should not be used for training
				for (int i = trainData.size() - 1; i >= numberInstances; i--) {
					trainData.delete(i);
				}
				Instances copyTrainData = new Instances(trainData);
				trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
				

				// file to hold prediction results
				File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
						AccessMode.READWRITE)
						.getPath()
						+ "/" + Constants.EVAL_FILE_NAME + "_" + numberInstances + "_" + iteration);

				File trainItemIds = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
						AccessMode.READWRITE)
						.getPath()
						+ "/" + Constants.EVAL_FILE_NAME + "_" + numberInstances + "_" + iteration + "_itemIds.txt");
				
					// train the classifier on the train set split - not necessary in multilabel setup, but
				// in single label setup
				cl.buildClassifier(trainData);

				weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
						WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
				testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
				testData = WekaUtils.addInstanceId(testData, copyTestData, multiLabel);
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(trainItemIds));
				for (Instance inst : copyTrainData){
//					bw.write(inst.stringValue(0)+"\n");
					String instanceId = inst.stringValue(copyTrainData.attribute(Constants.ID_FEATURE_NAME).index());
					instanceId = instanceId.substring(instanceId.indexOf("_0_")+3);
					// TODO comment in again
				//	bw.write(instanceId+"\t"+instanceId2TextMap.get(instanceId)+"\n");
				}
				bw.close();
				//                // Write out the predictions
				//                DataSink.write(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
				//                        .getAbsolutePath() + "/" + PREDICTIONS_FILENAME + "_" + trainPercent, testData); 
			} 	
		}
		File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
				FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);
		Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);
		File testItemIds = new File(aContext.getStorageLocation("",
				AccessMode.READWRITE)
				.getPath()
				+ "/" + "testItemIds.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(testItemIds));
		for (Instance inst : testData){
			bw.write(inst.value(0)+"\n");
		}
		bw.close();
	}

	
}
