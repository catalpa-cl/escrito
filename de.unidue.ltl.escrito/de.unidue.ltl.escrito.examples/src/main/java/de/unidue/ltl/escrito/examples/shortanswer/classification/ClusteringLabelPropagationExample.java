package de.unidue.ltl.escrito.examples.shortanswer.classification;

import java.util.Arrays;
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
import weka.clusterers.SimpleKMeans;

import org.dkpro.tc.core.Constants;

public class ClusteringLabelPropagationExample extends Experiments_ImplBase implements Constants {

	public static void main(String[] args) throws Exception{
		//		runAsapBaselineExperiment("ASAP_LearningCurve_Example", 
		//				System.getenv("DKPRO_HOME")+"/datasets/asap/originalData/train_repaired.txt", 
		//				System.getenv("DKPRO_HOME")+"/datasets/asap/originalData/test_public.txt", 
		//				"en", 
		//				1);
		runPowergradingBaselineExperiment("PG_LearningCurve_Example", 
				System.getenv("DKPRO_HOME")+"/datasets/powergrading//train_70.txt", 
				System.getenv("DKPRO_HOME")+"/datasets/powergrading//test_30.txt", 
				"en", 
				1);
	}



	protected static void runAsapBaselineExperiment(String experimentName, String trainData, String testData,
			String languageCode, Integer... questionIds) throws Exception {
		for (int id : questionIds) {
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(Asap2Reader.class,
					Asap2Reader.PARAM_INPUT_FILE, trainData,
					Asap2Reader.PARAM_PROMPT_IDS, id);

			runClusteringLabelPropagationExperiment(experimentName + "_" + id + "", readerTrain, languageCode);
		}
	}


	protected static void runPowergradingBaselineExperiment(String experimentName, String trainData, String testData,
			String languageCode, Integer... questionIds) throws Exception {
		for (int id : questionIds) {
			System.out.println("Prompt: "+id);
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(PowerGradingReader.class,
					PowerGradingReader.PARAM_INPUT_FILE, trainData,
					PowerGradingReader.PARAM_PROMPT_IDS, id);

			runClusteringLabelPropagationExperiment(experimentName + "_" + id + "", readerTrain, languageCode);
		}
	}



	@SuppressWarnings("unchecked")
	private static void runClusteringLabelPropagationExperiment(String experimentName, 
			CollectionReaderDescription readerTrain, String languageCode)
					throws Exception
	{     
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		
		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<List<String>> dimClusteringArgs = Dimension.create("clusteringArguments",
				Arrays.asList(new String[] { SimpleKMeans.class.getName() })
				); 
		
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim(); // TODO What is this used for?
		
		ParameterSpace pSpace = new ParameterSpace(
				Dimension.createBundle("readers", dimReaders),
				Dimension.create("dimension_number_of_clusters", 10),
				dimClusteringArgs,
				learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
				FeatureSettings.getFeatureSetsDimBaseline(),
				learningsArgsDims
				);
		runClusteringLabelPropagation(pSpace, experimentName, languageCode);
	}








}
