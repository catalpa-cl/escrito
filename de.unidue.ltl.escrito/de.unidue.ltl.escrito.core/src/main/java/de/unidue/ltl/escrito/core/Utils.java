package de.unidue.ltl.escrito.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.ml.weka.task.WekaTestTask;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.escrito.core.types.LearnerAnswerToken;
import meka.classifiers.multilabel.MultiLabelClassifier;
import mulan.dimensionalityReduction.BinaryRelevanceAttributeEvaluator;
import mulan.dimensionalityReduction.LabelPowersetAttributeEvaluator;
import mulan.dimensionalityReduction.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

public class Utils {

	public static Map<String, String> getInstanceId2TextMapTrain(TaskContext aContext)
			throws ResourceInitializationException{
		String path = aContext.getFolder(Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY).getPath()
				+"/documentMetaData.txt";
		return getInstanceId2TextMap(path);
	}

	
	public static Map<String, String> getInstanceId2TextMap(String path)
			throws ResourceInitializationException
	{	
		Map<String, String> instanceId2TextMap = new HashMap<String,String>();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			while (line != null){
				if (line.startsWith("#")){
					// skip
				} else {
					String[] parts = line.split("\t");
					instanceId2TextMap.put(parts[0], parts[1]);
			//		System.out.println(parts[0]+"\t"+parts[1]);
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return instanceId2TextMap;
	}

	public static void extendInstanceId2TextMapCV(Map<String, String> instanceId2TextMap, 
			StorageService store, String subcontextId)
					throws ResourceInitializationException
	{	
		File path = store.locateKey(subcontextId,
				Constants.TEST_TASK_INPUT_KEY_TEST_DATA+"/documentMetaData.txt");
		System.out.println(path);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			while (line != null){
				if (line.startsWith("#")){
					// skip
				} else {
					String[] parts = line.split("\t");
					instanceId2TextMap.put(parts[0], parts[1]);
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static File getFile(TaskContext aContext, String key, String entry, AccessMode mode){
		String path = aContext.getFolder(key, mode).getPath();
		String pathToArff = path + "/" + entry;
		return new File(pathToArff);
	}
	
	 /**
     * Evaluates a given single-label classifier on given train and test sets.
     */
    public static Evaluation getEvaluationSinglelabel(Classifier cl, Instances trainData,
            Instances testData)
        throws Exception
    {
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(cl, testData);
        return eval;
    }
    
    
    /**
     * Generates an instances object containing the predictions of a given single-label classifier
     * for a given test set
     *
     * @param testData
     *            test set
     * @param cl
     *            single-label classifier, needs to be trained beforehand, needs to be compatible
     *            with the test set trained classifier
     * @return instances object with additional attribute storing the predictions
     * @throws Exception an exception
     */
    public static Instances getPredictionInstancesSingleLabel(Instances testData, Classifier cl)
        throws Exception
    {

        StringBuffer classVals = new StringBuffer();
        for (int i = 0; i < testData.classAttribute().numValues(); i++) {
            if (classVals.length() > 0) {
                classVals.append(",");
            }
            classVals.append(testData.classAttribute().value(i));
        }

        // get predictions
        List<Double> labelPredictionList = new ArrayList<Double>();
        for (int i = 0; i < testData.size(); i++) {
            labelPredictionList.add(cl.classifyInstance(testData.instance(i)));
        }

        // add an attribute with the predicted values at the end off the attributes
        Add filter = new Add();
        filter.setAttributeName(WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
        if (classVals.length() > 0) {
            filter.setAttributeType(new SelectedTag(Attribute.NOMINAL, Add.TAGS_TYPE));
            filter.setNominalLabels(classVals.toString());
        }
        filter.setInputFormat(testData);
        testData = Filter.useFilter(testData, filter);

        // fill predicted values for each instance
        for (int i = 0; i < labelPredictionList.size(); i++) {
            testData.instance(i).setValue(testData.classIndex() + 1, labelPredictionList.get(i));
        }
        return testData;
    }
    
    
    public static Classifier getClassifier(String learningMode, List<Object> classificationArguments)
            throws Exception
        {
            boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

            Classifier cl;
            if (multiLabel) {
                List<Object> mlArgs = classificationArguments
                        .subList(1, classificationArguments.size());
                cl = AbstractClassifier.forName((String) classificationArguments.get(0), new String[] {});
                ((MultiLabelClassifier) cl).setOptions(mlArgs.toArray(new String[0]));
            }
            else {
                cl = AbstractClassifier.forName((String) classificationArguments.get(0), classificationArguments
                        .subList(1, classificationArguments.size()).toArray(new String[0]));
            }
            return cl;
        }

    /**
     * Suffix for class label names in the test data that have been adapted to match the training
     * data
     *
     * @see #makeOutcomeClassesCompatible(Instances, Instances, boolean)
     */
    public static final String COMPATIBLE_OUTCOME_CLASS = "_Comp";
    
    
    /**
     * Adapts the test data class labels to the training data. Class labels from the test data
     * unseen in the training data will be deleted from the test data. Class labels from the
     * training data unseen in the test data will be added to the test data. If training and test
     * class labels are equal, nothing will be done.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Instances makeOutcomeClassesCompatible(Instances trainData, Instances testData,
            boolean multilabel)
        throws Exception
    {
        // new (compatible) test data
        Instances compTestData = null;

        // ================ SINGLE LABEL BRANCH ======================
        if (!multilabel) {
            // retrieve class labels
            Enumeration trainOutcomeValues = trainData.classAttribute().enumerateValues();
            Enumeration testOutcomeValues = testData.classAttribute().enumerateValues();
            ArrayList trainLabels = Collections.list(trainOutcomeValues);
            ArrayList testLabels = Collections.list(testOutcomeValues);

            // add new outcome class attribute to test data
            Add addFilter = new Add();
            addFilter.setNominalLabels(StringUtils.join(trainLabels, ','));
            addFilter.setAttributeName(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS);
            addFilter.setInputFormat(testData);
            testData = Filter.useFilter(testData, addFilter);

            // fill NEW test data with values from old test data plus the new class attribute
            compTestData = new Instances(testData, testData.numInstances());
            for (int i = 0; i < testData.numInstances(); i++) {
                weka.core.Instance instance = testData.instance(i);
                String label = (String) testLabels.get((int) instance.value(testData
                        .classAttribute()));
                if (trainLabels.indexOf(label) != -1) {
                    instance.setValue(
                            testData.attribute(Constants.CLASS_ATTRIBUTE_NAME
                                    + COMPATIBLE_OUTCOME_CLASS), label);
                }
                else {
                    instance.setMissing(testData.classIndex());
                }
                compTestData.add(instance);
            }

            // remove old class attribute
            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(compTestData.attribute(
                    Constants.CLASS_ATTRIBUTE_NAME).index() + 1));
            remove.setInvertSelection(false);
            remove.setInputFormat(compTestData);
            compTestData = Filter.useFilter(compTestData, remove);

            // set new class attribute
            compTestData.setClass(compTestData.attribute(Constants.CLASS_ATTRIBUTE_NAME
                    + COMPATIBLE_OUTCOME_CLASS));
        }
        // ================ MULTI LABEL BRANCH ======================
        else {

            int numTrainLabels = trainData.classIndex();
            int numTestLabels = testData.classIndex();

            ArrayList<String> trainLabels = getLabels(trainData);
            // ArrayList<String> testLabels = getLabels(testData);

            // add new outcome class attributes to test data

            Add filter = new Add();
            for (int i = 0; i < numTrainLabels; i++) {
                // numTestLabels +i (because index starts from 0)
                filter.setAttributeIndex(new Integer(numTestLabels + i + 1).toString());
                filter.setNominalLabels("0,1");
                filter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
                filter.setInputFormat(testData);
                testData = Filter.useFilter(testData, filter);
            }

            // fill NEW test data with values from old test data plus the new class attributes
            compTestData = new Instances(testData, testData.numInstances());
            for (int i = 0; i < testData.numInstances(); i++) {
                weka.core.Instance instance = testData.instance(i);
                // fullfill with 0.
                for (int j = 0; j < numTrainLabels; j++) {
                    instance.setValue(j + numTestLabels, 0.);
                }
                // fill the real values:
                for (int j = 0; j < numTestLabels; j++) {
                    // part of train data: forget labels which are not part of the train data
                    if (trainLabels.indexOf(instance.attribute(j).name()) != -1) {
                        // class label found in test data
                        int index = trainLabels.indexOf(instance.attribute(j).name());
                        instance.setValue(index + numTestLabels, instance.value(j));
                    }
                }
                compTestData.add(instance);
            }

            // remove old class attributes
            for (int i = 0; i < numTestLabels; i++) {
                Remove remove = new Remove();
                remove.setAttributeIndices("1");
                remove.setInvertSelection(false);
                remove.setInputFormat(compTestData);
                compTestData = Filter.useFilter(compTestData, remove);
            }

            // adapt header and set new class label
            String relationTag = compTestData.relationName();
            compTestData.setRelationName(relationTag.substring(0, relationTag.indexOf("-C") + 2)
                    + " " + numTrainLabels + " ");
            compTestData.setClassIndex(numTrainLabels);
        }
        return compTestData;
    }
    

    private static ArrayList<String> getLabels(Instances data)
    {
        int numLabels = data.classIndex();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < numLabels; i++) {
            list.add(data.attribute(i).name());
        }
        return list;
    }
    
    public static List<String> getParameters(List<Object> classificationArguments)
    {
        List<String> o = new ArrayList<>();

        for (int i = 1; i < classificationArguments.size(); i++) {
            o.add((String) classificationArguments.get(i));
        }

        return o;
    }
    
//    /**
//     * Feature selection using Mulan.
//     */
//	public static Remove featureSelectionMultilabel(TaskContext aContext, Instances trainData,
//			List<String> attributeEvaluator, String labelTransformationMethod, int numLabelsToKeep) throws TextClassificationException {
//		// file to hold the results of attribute selection
//		File fsResultsFile = getFile(aContext, TEST_TASK_OUTPUT_KEY, AdapterNameEntries.featureSelectionFile,
//				AccessMode.READWRITE);
//
//		// filter for reducing dimension of attributes
//	    Remove filterRemove = new Remove();
//        try {
//            MultiLabelInstances mulanInstances = convertMekaInstancesToMulanInstances(trainData);
//
//            ASEvaluation eval = ASEvaluation.forName(attributeEvaluator.get(0), attributeEvaluator
//                    .subList(1, attributeEvaluator.size()).toArray(new String[0]));
//
//            AttributeEvaluator attributeSelectionFilter;
//
//            // We currently only support the following Mulan Transformation methods (configuration
//            // is complicated due to missing commandline support of mulan):
//            if (labelTransformationMethod.equals("LabelPowersetAttributeEvaluator")) {
//                attributeSelectionFilter = new LabelPowersetAttributeEvaluator(eval, mulanInstances);
//            }
//            else if (labelTransformationMethod.equals("BinaryRelevanceAttributeEvaluator")) {
//                attributeSelectionFilter = new BinaryRelevanceAttributeEvaluator(eval,
//                        mulanInstances, "max", "none", "rank");
//            }
//            else {
//                throw new TextClassificationException(
//                        "This Label Transformation Method is not supported.");
//            }
//
//            Ranker r = new Ranker();
//            int[] result = r.search(attributeSelectionFilter, mulanInstances);
//
//            // collect evaluation for *all* attributes and write to file
//            StringBuffer evalFile = new StringBuffer();
//            for (Attribute att : mulanInstances.getFeatureAttributes()) {
//                evalFile.append(att.name()
//                        + ": "
//                        + attributeSelectionFilter.evaluateAttribute(att.index()
//                                - mulanInstances.getNumLabels()) + "\n");
//            }
//            FileUtils.writeStringToFile(fsResultsFile, evalFile.toString());
//
//            // create a filter to reduce the dimension of the attributes
//            int[] toKeep = new int[numLabelsToKeep + mulanInstances.getNumLabels()];
//            System.arraycopy(result, 0, toKeep, 0, numLabelsToKeep);
//            int[] labelIndices = mulanInstances.getLabelIndices();
//            System.arraycopy(labelIndices, 0, toKeep, numLabelsToKeep,
//                    mulanInstances.getNumLabels());
//
//            filterRemove.setAttributeIndicesArray(toKeep);
//            filterRemove.setInvertSelection(true);
//            filterRemove.setInputFormat(mulanInstances.getDataSet());
//        }
//        catch (ArrayIndexOutOfBoundsException e) {
//            // less attributes than we want => no filtering
//            return null;
//        }
//        catch (Exception e) {
//            throw new TextClassificationException(e);
//        }
//		return filterRemove;
//	}
    
    
    private static boolean ignoreToken(LearnerAnswerToken t, boolean ignoreQuestionMaterial, boolean ignoreStopwords,
			boolean ignorePunctuation) {
		return ((t.getIsPunctuation() && ignorePunctuation)
				|| (t.getIsQuestionMaterial() && ignoreQuestionMaterial)
				|| (t.getIsStopWord() && ignoreStopwords));
	}

	
	public static List<String> extractAllLemmasFromView(JCas view, 
			boolean ignoreQuestionMaterial,
			boolean ignoreStopwords,
			boolean ignorePunctuation) {
		if (JCasUtil.exists(view, LearnerAnswerToken.class)){
			Collection<LearnerAnswerToken> tokens = JCasUtil.select(view, LearnerAnswerToken.class);
			List<String> words = new ArrayList<String>();
			Iterator<LearnerAnswerToken> iter = tokens.iterator();
			while (iter.hasNext()){
				LearnerAnswerToken t = iter.next();
				if (ignoreToken(t, ignoreQuestionMaterial, ignoreStopwords, ignorePunctuation)){
					System.out.println("Ignore token "+t.getCoveredText());
				} else {
					words.add(t.getToken().getLemma().getValue()); 
				}
			}
			return words;
		} else {
			//if we do not have learner answer tokens annotated, take all tokens
			Collection<Token> tokens = JCasUtil.select(view, Token.class);
			List<String> words = new ArrayList<String>();
			Iterator<Token> iter = tokens.iterator();
			while (iter.hasNext()){
				words.add(iter.next().getLemma().getValue()); 
			}
			return words;
		}
	}

}
