package de.unidue.ltl.escrito.core.learningcurve;

import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import java.io.File;
import java.util.List;

import meka.core.Result;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.unsupervised.attribute.Remove;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.util.MultilabelResult;
import org.dkpro.tc.ml.weka.util.WekaUtils;

/**
 * Base class for test task and save model tasks
 */
public class TrainingDataSelectionTestTask
extends ExecutableTaskBase
implements Constants
{

	@Discriminator(name=DIM_CLASSIFICATION_ARGS)
	protected List<String> classificationArguments;
	@Discriminator(name=DIM_FEATURE_SEARCHER_ARGS)
	protected List<String> featureSearcher;
	@Discriminator(name=DIM_ATTRIBUTE_EVALUATOR_ARGS)
	protected List<String> attributeEvaluator;
	@Discriminator(name=DIM_LABEL_TRANSFORMATION_METHOD)
	protected String labelTransformationMethod;
	@Discriminator(name=DIM_NUM_LABELS_TO_KEEP)
	protected int numLabelsToKeep;
	@Discriminator(name=DIM_APPLY_FEATURE_SELECTION)
	protected boolean applySelection;
	@Discriminator(name=DIM_FEATURE_MODE)
	protected String featureMode;
	@Discriminator(name=DIM_LEARNING_MODE)
	protected String learningMode;
	@Discriminator(name=DIM_BIPARTITION_THRESHOLD)
	protected String threshold;

	public static final String evaluationBin = "evaluation.bin";


	@Discriminator(name = "dimension_number_of_training_instances")
	public static int[] NUMBER_OF_TRAINING_INSTANCES;


	@Override
	public void execute(TaskContext aContext)
			throws Exception
	{
		boolean multiLabel = false;

		for (int numberOfClusters : NUMBER_OF_TRAINING_INSTANCES ){

			File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
					AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);
			File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
					AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);

			String train = arffFileTrain.getAbsolutePath();
			System.out.println(train);
			train = train.replaceAll(".arff.gz", "_"+numberOfClusters+".arff.gz");
			System.out.println(train);
			arffFileTrain = new File(train);

			Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
			Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);

			// do not balance in regression experiments
			if (!learningMode.equals(Constants.LM_REGRESSION)) {
				testData = WekaUtils.makeOutcomeClassesCompatible(trainData, testData, multiLabel);
			}

			Instances copyTestData = new Instances(testData);
			trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
			testData = WekaUtils.removeInstanceId(testData, multiLabel);


			// FEATURE SELECTION
			if (attributeEvaluator != null && labelTransformationMethod != null
					&& numLabelsToKeep > 0) {
				Remove attSel = WekaUtils.featureSelectionMultilabel(aContext, trainData,
						attributeEvaluator, labelTransformationMethod, numLabelsToKeep);
				if (applySelection) {
					Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
					trainData = WekaUtils.applyAttributeSelectionFilter(trainData, attSel);
					testData = WekaUtils.applyAttributeSelectionFilter(testData, attSel);
				}
			}

			// build classifier
			Classifier cl = WekaUtils.getClassifier(learningMode, classificationArguments);

			// evaluation & prediction generation

			// file to hold prediction results
			File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
					AccessMode.READWRITE)
					.getPath()
					+ "/" + Constants.EVAL_FILE_NAME + "_" + numberOfClusters);
				// train the classifier on the train set split - not necessary in multilabel setup, but
				// in single label setup
				cl.buildClassifier(trainData);
				weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
						WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
				testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
				testData = WekaUtils.addInstanceId(testData, copyTestData, false);
				
			// TODO anpassen	
			// Write out the predictions
			File predictionFile = WekaUtils.getFile(aContext,"", AdapterNameEntries.predictionsFile, AccessMode.READWRITE);

			String predictions = predictionFile.getAbsolutePath();
			System.out.println(predictions);
			predictions = predictions.replaceAll(".arff", "_"+numberOfClusters+".arff");
			System.out.println(predictions);
			predictionFile = new File(predictions);

			DataSink.write(predictionFile.getAbsolutePath(), testData);
		}

	}

}