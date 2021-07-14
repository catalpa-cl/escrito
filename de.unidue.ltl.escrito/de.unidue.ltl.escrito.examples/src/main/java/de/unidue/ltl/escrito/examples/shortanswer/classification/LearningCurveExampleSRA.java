package de.unidue.ltl.escrito.examples.shortanswer.classification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;

import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.examples.basics.FeatureSettings;
import de.unidue.ltl.escrito.io.shortanswer.Asap2Reader;
import de.unidue.ltl.escrito.io.shortanswer.PowerGradingReader;
import de.unidue.ltl.escrito.io.shortanswer.SRAReader;

import org.dkpro.tc.core.Constants;

public class LearningCurveExampleSRA extends Experiments_ImplBase implements Constants {

	public static void main(String[] args) throws Exception{
//		runSraBaselineExperiment("SRA_LearningCurve_Example", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/beetle/", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/test/2way/beetle/test-unseen-answers/", 
//				"en", 
//				SRAReader.PromptSetIds_beetle);
		runSraBaselineExperimentSimilarity("SRA_LearningCurveSimilarity_Example", 
				System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/beetle/", 
				System.getenv("DKPRO_HOME")+"/datasets/SRA/test/2way/beetle/test-unseen-answers/", 
				"en", 
				SRAReader.PromptSetIds_beetle);
//		runSraBaselineExperimentPromptIndependent("SRA_LearningCurve_Example", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/beetle/", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/test/2way/beetle/test-unseen-answers/", 
//				"en", 
//				SRAReader.PromptSetIds_beetle);
//		runSraBaselineExperimentSimilarityPromptIndependent("SRA_LearningCurveSimilarity_Example", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/beetle/", 
//				System.getenv("DKPRO_HOME")+"/datasets/SRA/test/2way/beetle/test-unseen-answers/", 
//				"en", 
//				SRAReader.PromptSetIds_beetle);
	}


	protected static void runSraBaselineExperiment(String experimentName, String trainData, String testData,
			String languageCode, String... questionIds) throws Exception {
		for (String id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, trainData,
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
					);
			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, testData, 
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			runLearningCurveExperiment(experimentName + "_" + id + "", readerTrain, readerTest, languageCode);
		}
	}
	
	
	protected static void runSraBaselineExperimentPromptIndependent(String experimentName, String trainData, String testData,
			String languageCode, String... questionIds) throws Exception {
		for (String id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, trainData,
					SRAReader.PARAM_EXCLUDE_PROMPT_SET_ID, id,
					SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
					);
			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, testData, 
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			runLearningCurveExperiment(experimentName + "_" + id + "", readerTrain, readerTest, languageCode);
		}
	}
	
	
	protected static void runSraBaselineExperimentSimilarity(String experimentName, String trainData, String testData,
			String languageCode, String... questionIds) throws Exception {
		for (String id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, trainData,
					// across prompts, we do not train on a specific prompt
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
					);
			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, testData, 
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			runLearningCurveExperimentSimilarity(experimentName + "_" + id + "", readerTrain, readerTest, languageCode, "SRA");
		}
	}
	
	protected static void runSraBaselineExperimentSimilarityPromptIndependent(String experimentName, String trainData, String testData,
			String languageCode, String... questionIds) throws Exception {
		for (String id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, trainData,
					// across prompts, we do not train on a specific prompt
					SRAReader.PARAM_EXCLUDE_PROMPT_SET_ID, id,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
					);
			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, testData, 
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_CORPUSNAME, "SRA",
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			runLearningCurveExperimentSimilarity(experimentName + "_" + id + "", readerTrain, readerTest, languageCode, "SRA");
		}
	}
	


	@SuppressWarnings("unchecked")
	private static void runLearningCurveExperiment(String experimentName, 
			CollectionReaderDescription readerTrain, 
			CollectionReaderDescription readerTest, String languageCode)
					throws Exception
	{     
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getWekaLearningCurveClassificationArgsDim();
	//	Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();
		int[] NUMBER_OF_TRAINING_INSTANCES = new int[] {10,20,40,80,160,320,640,1280};
		
		ParameterSpace pSpace = new ParameterSpace(
				Dimension.createBundle("readers", dimReaders),
				learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
				Dimension.create("dimension_iterations", 100),
				Dimension.create("dimension_number_of_training_instances", NUMBER_OF_TRAINING_INSTANCES),
				FeatureSettings.getFeatureSetsDimBaseline(),
				learningsArgsDims
				);

		runLearningCurve(pSpace, experimentName, languageCode);
	}



	@SuppressWarnings("unchecked")
	private static void runLearningCurveExperimentSimilarity(String experimentName, 
			CollectionReaderDescription readerTrain, 
			CollectionReaderDescription readerTest, String languageCode, String corpusName)
					throws Exception
	{     
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getWekaLearningCurveClassificationArgsDim();
	//	Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();
		int[] NUMBER_OF_TRAINING_INSTANCES = new int[] {10,20,40,80,160,320,640,1280};
		
		ParameterSpace pSpace = new ParameterSpace(
				Dimension.createBundle("readers", dimReaders),
				learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
				Dimension.create("dimension_iterations", 100),
				Dimension.create("dimension_number_of_training_instances", NUMBER_OF_TRAINING_INSTANCES),
				FeatureSettings.getFeatureSetsSimilarity(corpusName),
				learningsArgsDims
				);

		runLearningCurve(pSpace, experimentName, languageCode);
	}




}
