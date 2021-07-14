package de.unidue.ltl.escrito.examples.basicexamples.essay;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.examples.basics.FeatureSettings;

public class AsapAesExperiment extends Experiments_ImplBase implements Constants
{

	public static final int NUM_FOLDS = 10;

	public static void main(String[] args)
			throws Exception
	{ 	
		runASAP();
	}


	public static void runASAP() throws Exception{	
		for (int i = 1; i<=8; i++){
			CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
					BinaryCasReader.class,
					BinaryCasReader.PARAM_SOURCE_LOCATION, System.getenv("DKPRO_HOME")+"/datasets/asap_essays/asap_bincas/"+i+"/90",
					BinaryCasReader.PARAM_LANGUAGE, "en",
					BinaryCasReader.PARAM_PATTERNS, "*.bin"
					);
			CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
					BinaryCasReader.class,
					BinaryCasReader.PARAM_SOURCE_LOCATION, System.getenv("DKPRO_HOME")+"/datasets/asap_essays/asap_bincas/"+i+"/10",
					BinaryCasReader.PARAM_LANGUAGE, "en",
					BinaryCasReader.PARAM_PATTERNS, "*.bin"
					);
			runEssayBaselineExperiment(readerTrain, readerTest, "full_prompt_"+i);
		}
	}



	public static void runEssayBaselineExperiment(CollectionReaderDescription readerTrain, CollectionReaderDescription readerTest, String experimentName) throws Exception{ 	
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);
		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();

		ParameterSpace pSpace = new ParameterSpace(
				Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
								FeatureSettings.getFeatureSetsEssayFull(),
				//				FeatureSettings.getFeatureSetNGrams(),
				//FeatureSettings.getFeatureSetsEssayNoNgrams(),
				learningsArgsDims
				);

		EssayGradingExperiment experiment = new EssayGradingExperiment();
		experiment.runTrainTest(pSpace, experimentName, getEmptyPreprocessing());
	}




}
