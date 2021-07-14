package de.unidue.ltl.escrito.examples.basicexamples.essay;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.examples.basics.FeatureSettings;

public class EssayGradingExperiment
	extends Experiments_ImplBase implements Constants
{

    public static final int NUM_FOLDS = 10;

    public static void main(String[] args)
        throws Exception
    { 	
    	runASAP();
    }
   
    
    public static void runASAP() throws Exception{	
    	CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, "src/main/resources/bincas/asap_aes_small/",
				BinaryCasReader.PARAM_LANGUAGE, "en",
				BinaryCasReader.PARAM_PATTERNS, "*.bin"
				);
    	CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
				BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, "src/main/resources/bincas/asap_aes_small/",
				BinaryCasReader.PARAM_LANGUAGE, "en",
				BinaryCasReader.PARAM_PATTERNS, "*.bin"
				);
    	runEssayBaselineExperiment(readerTrain, readerTest);
    }
    
    
    
	public static void runEssayBaselineExperiment(CollectionReaderDescription readerTrain, CollectionReaderDescription readerTest) throws Exception{ 	
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
				learningsArgsDims
		);

    	EssayGradingExperiment experiment = new EssayGradingExperiment();
        experiment.runTrainTest(pSpace, "ASAP_TestTask", getEmptyPreprocessing());
    }
	
	
	
	
}
