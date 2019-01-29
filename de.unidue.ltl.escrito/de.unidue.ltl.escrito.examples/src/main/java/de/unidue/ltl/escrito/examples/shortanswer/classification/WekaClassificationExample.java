package de.unidue.ltl.escrito.examples.shortanswer.classification;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;

import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.examples.basics.FeatureSettings;
import de.unidue.ltl.escrito.io.shortanswer.Asap2Reader;
import de.unidue.ltl.escrito.io.shortanswer.PowerGradingReader;



public class WekaClassificationExample extends Experiments_ImplBase implements Constants {

	public static void main(String[] args) throws Exception{
//		runAsapBaselineExperiment("ASAP_Train_Test_Example", 
//				System.getenv("DKPRO_HOME")+"/datasets/asap/originalData/train_repaired.txt", 
//				System.getenv("DKPRO_HOME")+"/datasets/asap/originalData/test_public.txt", 
//				"en", 
//				1);
		runPowergradingBaselineExperiment("PG_Train_Test_Example", 
					System.getenv("DKPRO_HOME")+"/datasets/powergrading//train_70.txt", 
					System.getenv("DKPRO_HOME")+"/datasets/powergrading//test_30.txt", 
					"en", 
					2);
	}



	protected static void runAsapBaselineExperiment(String experimentName, String trainData, String testData,
			String languageCode, Integer... questionIds) throws Exception {
		for (int id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(Asap2Reader.class,
					Asap2Reader.PARAM_INPUT_FILE, trainData,
					Asap2Reader.PARAM_PROMPT_IDS, id);

			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(Asap2Reader.class,
					Asap2Reader.PARAM_INPUT_FILE, testData, 
					Asap2Reader.PARAM_PROMPT_IDS, id);
			runBaselineExperiment(experimentName + "_" + id + "", readerTrain, readerTest, languageCode);
		}
	}
	
	
	protected static void runPowergradingBaselineExperiment(String experimentName, String trainData, String testData,
			String languageCode, Integer... questionIds) throws Exception {
		for (int id : questionIds) {
			System.out.println("Prompt: "+id);
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(PowerGradingReader.class,
					PowerGradingReader.PARAM_INPUT_FILE, trainData,
					PowerGradingReader.PARAM_PROMPT_IDS, id);

			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(PowerGradingReader.class,
					PowerGradingReader.PARAM_INPUT_FILE, testData, 
					PowerGradingReader.PARAM_PROMPT_IDS, id);
			runBaselineExperiment(experimentName + "_" + id + "", readerTrain, readerTest, languageCode);
		}
	}

	private static void runBaselineExperiment(String experimentName, CollectionReaderDescription readerTrain,
			CollectionReaderDescription readerTest, String languageCode) throws Exception {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();

		ParameterSpace pSpace = null;
//		if (useCV) {
//			pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), learningDims,
//					Dimension.create(DIM_FEATURE_MODE, FM_UNIT), Dimension.create("DIMENSION_ITERATIONS", 1000),
//					Dimension.create("DIMENSION_NUMBER_OF_TRAINING_INSTANCES", 270),
//					FeatureSettings.getFeatureSetsDimBaseline(),
//					// FeatureSettings.getFeatureSetsDimBaselineStacking(),
//					learningsArgsDims);
//			this.runCrossValidation(pSpace, experimentName, getPreprocessing(languageCode), 10);
//		} else {
			pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), learningDims,
					Dimension.create(DIM_FEATURE_MODE, FM_UNIT), FeatureSettings.getFeatureSetsDimBaseline(),
					learningsArgsDims);
			runTrainTest(pSpace, experimentName, getPreprocessing(languageCode));
//		}
	}
}
