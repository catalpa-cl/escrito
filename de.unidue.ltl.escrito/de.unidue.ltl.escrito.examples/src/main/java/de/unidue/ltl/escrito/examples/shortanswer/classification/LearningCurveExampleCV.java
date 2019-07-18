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

public class LearningCurveExampleCV extends Experiments_ImplBase implements Constants {

	public static void main(String[] args) throws Exception{
		runLearningCurveCVExperiment("PG_LearningCurve_CV_Example", 
				System.getenv("DKPRO_HOME")+"/datasets/powergrading//train_70.txt", 
				"en", 
				1);
	}

	protected static void runLearningCurveCVExperiment(String experimentName, String trainData, 
			String languageCode, Integer... questionIds) throws Exception {
		for (int id : questionIds) {
			System.out.println("Prompt: "+id);
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(PowerGradingReader.class,
					PowerGradingReader.PARAM_INPUT_FILE, trainData,
					PowerGradingReader.PARAM_PROMPT_IDS, id);
			runLearningCurveExperiment(experimentName + "_" + id + "", readerTrain, languageCode);
		}
	}



	@SuppressWarnings("unchecked")
	private static void runLearningCurveExperiment(String experimentName, 
			CollectionReaderDescription readerTrain, 
			String languageCode)
					throws Exception
	{     
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getWekaLearningCurveClassificationArgsDim();
		int[] NUMBER_OF_TRAINING_INSTANCES = new int[] {10,20,40,80,160,320};
		
		ParameterSpace pSpace = new ParameterSpace(
				Dimension.createBundle("readers", dimReaders),
				learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
				Dimension.create("dimension_iterations", 10),
				Dimension.create("dimension_number_of_training_instances", NUMBER_OF_TRAINING_INSTANCES),
				FeatureSettings.getFeatureSetsDimBaseline(),
				learningsArgsDims
				);

		runLearningCurveCV(pSpace, experimentName, languageCode, 2);
	}








}
