//package de.unidue.ltl.escrito.core.tc.stacking;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.log4j.Logger;
//import org.dkpro.lab.engine.TaskContext;
//import org.dkpro.lab.storage.StorageService.AccessMode;
//import org.dkpro.lab.task.Discriminator;
//import org.dkpro.lab.task.impl.ExecutableTaskBase;
//import org.dkpro.tc.core.Constants;
//import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
//import org.dkpro.tc.ml.weka.util.MultilabelResult;
//import org.dkpro.tc.ml.weka.util.WekaUtils;
//
//import meka.core.Result;
//import weka.attributeSelection.AttributeSelection;
//import weka.classifiers.Classifier;
//import weka.classifiers.functions.SMO;
//import weka.classifiers.meta.FilteredClassifier;
//import weka.classifiers.meta.Stacking;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils.DataSink;
//import weka.filters.unsupervised.attribute.Remove;
//
///**
// * Base class for test task and save model tasks
// */
//public class WekaStackingTask
//extends ExecutableTaskBase
//implements Constants
//{
//
//	@Discriminator(name=DIM_CLASSIFICATION_ARGS)
//	protected List<String> classificationArguments;
//	@Discriminator(name=DIM_FEATURE_SEARCHER_ARGS)
//	protected List<String> featureSearcher;
//	@Discriminator(name=DIM_ATTRIBUTE_EVALUATOR_ARGS)
//	protected List<String> attributeEvaluator;
//	@Discriminator(name=DIM_LABEL_TRANSFORMATION_METHOD)
//	protected String labelTransformationMethod;
//	@Discriminator(name=DIM_NUM_LABELS_TO_KEEP)
//	protected int numLabelsToKeep;
//	@Discriminator(name=DIM_APPLY_FEATURE_SELECTION)
//	protected boolean applySelection;
//	@Discriminator(name=DIM_FEATURE_MODE)
//	protected String featureMode;
//	@Discriminator(name=DIM_LEARNING_MODE)
//	protected String learningMode;
//	@Discriminator(name=DIM_BIPARTITION_THRESHOLD)
//	protected String threshold;
//
//	public static final String evaluationBin = "evaluation.bin";
//
//	@Override
//	public void execute(TaskContext aContext)
//			throws Exception
//	{
//
//		System.out.println("Execute WEKA stacking task.");
//		boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
//
//		File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
//				AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);
//		File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
//				AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);
//
//		Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
//		Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);
//
//		System.out.println(trainData.numInstances()+" training data");
//		System.out.println(testData.numInstances()+" test data");
//		System.out.println(aContext.getId());
//
//		// do not balance in regression experiments
//		if (!learningMode.equals(Constants.LM_REGRESSION)) {
//			testData = WekaUtils.makeOutcomeClassesCompatible(trainData, testData, multiLabel);
//		}
//
//		Instances copyTestData = new Instances(testData);
//		trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
//		testData = WekaUtils.removeInstanceId(testData, multiLabel);
//
//
//		// FEATURE SELECTION
//		if (!learningMode.equals(Constants.LM_MULTI_LABEL)) {
//			if (featureSearcher != null && attributeEvaluator != null) {
//				AttributeSelection attSel = WekaUtils.featureSelectionSinglelabel(aContext,
//						trainData, featureSearcher, attributeEvaluator);
//				File file = WekaUtils.getFile(aContext, "",
//						AdapterNameEntries.featureSelectionFile, AccessMode.READWRITE);
//				FileUtils.writeStringToFile(file, attSel.toResultsString());
//				if (applySelection) {
//					Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
//					trainData = attSel.reduceDimensionality(trainData);
//					testData = attSel.reduceDimensionality(testData);
//				}
//			}
//		}
//		else {
//			if (attributeEvaluator != null && labelTransformationMethod != null
//					&& numLabelsToKeep > 0) {
//				Remove attSel = WekaUtils.featureSelectionMultilabel(aContext, trainData,
//						attributeEvaluator, labelTransformationMethod, numLabelsToKeep);
//				if (applySelection) {
//					Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
//					trainData = WekaUtils.applyAttributeSelectionFilter(trainData, attSel);
//					testData = WekaUtils.applyAttributeSelectionFilter(testData, attSel);
//				}
//			}
//		}
//
//		// sort the instances by stacking group
//		Map<Integer, Set<Integer>> stackingGroups = new HashMap<Integer, Set<Integer>>();
//		Set<Integer> unstackables = new HashSet<Integer>();
//		for (int i = 0; i<trainData.numAttributes(); i++){
//			String attrName = trainData.attribute(i).name();
//			System.out.println("Attribute: "+attrName);
//			int stackingGroupId = getStackingGroup(attrName);
//			if (stackingGroupId == -1 ){
//				if (attrName.equals("outcome")){
//
//				} else {
//					unstackables.add(i);
//				}
//			} else {
//				if (!(stackingGroups.containsKey(stackingGroupId))){
//					stackingGroups.put(stackingGroupId,  new HashSet<Integer>());
//				}
//				stackingGroups.get(stackingGroupId).add(i);
//			}
//		}
//
//
//		FeatureStacking cl = new FeatureStacking();
//		cl.setMetaClassifier(WekaUtils.getClassifier(learningMode, classificationArguments));
//	//	System.out.println("Found "+stackingGroups.size()+" stacking groups.");
//		Classifier[] stackoptions = new Classifier[stackingGroups.size()];
//		int classifierCounter = 0;
//		for (int stackingGroupId : stackingGroups.keySet()){
//			Set<Integer> stackingGroup = stackingGroups.get(stackingGroupId);
//			Remove filter1 = new Remove();
//			filter1.setInputFormat(trainData);
//			int[] attributes = new int[stackingGroup.size()+1];
//			int counter = 0;
//			for (Integer index : stackingGroup){
//				attributes[counter] = index;
//				counter++;
//			}
//			attributes[counter] = trainData.classIndex();
//			filter1.setAttributeIndicesArray(attributes);
//			filter1.setInvertSelection(true);
//			FilteredClassifier fc1 = new FilteredClassifier();
//			// TODO: we might want to set a different classifier for that
//			fc1.setClassifier(WekaUtils.getClassifier(learningMode, classificationArguments));
//			fc1.setFilter(filter1);
//			stackoptions[classifierCounter] = fc1;
//			classifierCounter++;
//		}
//		Integer[] unstackedFeatures = unstackables.toArray(new Integer[unstackables.size()]);
//		cl.setUnstackedFeatures(unstackedFeatures);
//		cl.setClassifiers(stackoptions);
//
//		// build classifier
//		cl.setMetaClassifier(WekaUtils.getClassifier(learningMode, classificationArguments));
//
//		// file to hold prediction results
//		File evalOutput = WekaUtils.getFile(aContext, "", evaluationBin, AccessMode.READWRITE);
//
//		// evaluation & prediction generation
//		if (multiLabel) {
//			// we don't need to build the classifier - meka does this
//			// internally
//			Result r = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, threshold);
//			WekaUtils.writeMlResultToFile(new MultilabelResult(r.allTrueValues(), r.allPredictions(),
//					threshold), evalOutput);
//			testData = WekaUtils.getPredictionInstancesMultiLabel(testData, cl,
//					WekaUtils.getMekaThreshold(threshold, r, trainData));
//			testData = WekaUtils.addInstanceId(testData, copyTestData, true);
//		}
//		else {
//			// train the classifier on the train set split - not necessary in multilabel setup, but
//			// in single label setup
//			cl.buildClassifier(trainData);
//			weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
//					WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
//			testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
//			testData = WekaUtils.addInstanceId(testData, copyTestData, false);
//		}
//
//		// Write out the predictions
//		File predictionFile = WekaUtils.getFile(aContext,"", AdapterNameEntries.predictionsFile, AccessMode.READWRITE);
//		DataSink.write(predictionFile.getAbsolutePath(), testData);
//
//		System.out.println(trainData.numInstances()+" training data");
//		System.out.println(testData.numInstances()+" test data");
//	}
//
//	private int getStackingGroup(String attrName) {
//		if (attrName.startsWith("stackingGroup:")){
//			String[] parts = attrName.split("_");
//			//	System.out.println(parts[0].substring(14, parts[0].length()));
//			return Integer.parseInt(parts[0].substring(14, parts[0].length()));
//		} else {
//			return -1;
//		}
//	}
//
//}
