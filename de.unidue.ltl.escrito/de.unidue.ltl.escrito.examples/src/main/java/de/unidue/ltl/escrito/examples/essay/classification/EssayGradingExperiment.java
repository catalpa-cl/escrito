//package de.unidue.ltl.escrito.examples.essay.classification;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.uima.collection.CollectionReaderDescription;
//import org.apache.uima.fit.factory.CollectionReaderFactory;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.dkpro.lab.task.Dimension;
//import org.dkpro.lab.task.ParameterSpace;
//
//import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
//import de.unidue.ltl.escrito.io.essay.UdeEssayReader;
//
//public class EssayGradingExperiment
//	extends Experiments_ImplBase
//{
//
//    public static final int NUM_FOLDS = 3;
//
//    public static void main(String[] args)
//        throws Exception
//    { 	
//    	runUde();
//    }
//   
//    
//    public static void runUde() throws Exception{
//    	String inputFolder = DkproContext.getContext().getWorkspace("eduScoringData").getAbsolutePath();
//    	File essayFolder = new File(inputFolder, "essays/ude/1");
//    	if (!essayFolder.exists()) {
//    		throw new IllegalArgumentException("Cannot read data from folder: " + essayFolder.getAbsolutePath());
//    	}
//    	
//		// configure training and test data reader dimension
//		// train/test will use both, while cross-validation will only use the train part
//		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
//				UdeEssayReader.class,
//				UdeEssayReader.PARAM_SOURCE_LOCATION, essayFolder,
//                UdeEssayReader.PARAM_LANGUAGE_CODE, "de",
//                UdeEssayReader.PARAM_PATTERNS, "*.xml",
//                UdeEssayReader.PARAM_RATING_BIAS, UdeEssayReader.RatingBias.high,
//                UdeEssayReader.PARAM_DO_SPARSECLASSMERGING,false,
//                UdeEssayReader.PARAM_TARGET_LABEL, "SCHRA000001 Gesamteindruck"
//		);
//    	runEssayBaselineExperimentCV(readerTrain);
//    }
//    
//    
//    
//	public static void runEssayBaselineExperiment(CollectionReaderDescription readerTrain, CollectionReaderDescription readerTest) throws Exception{ 	
//		Map<String, Object> dimReaders = new HashMap<String, Object>();
//		dimReaders.put(DIM_READER_TRAIN, readerTrain);
//		dimReaders.put(DIM_READER_TEST, readerTest);
//
//		ParameterSpace pSpace = new ParameterSpace(
//				Dimension.createBundle("readers", dimReaders),
//				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
//				Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
//				getFeatureSetsDim(),
//				//getFeatureSetsDim(),
//				getClassificationArgsDim()
//		);
//
//    	EssayGradingExperiment experiment = new EssayGradingExperiment();
//       // experiment.runCrossValidation(pSpace, "Task1EssayGradingExperiment", 3);
//        experiment.runTrainTest(pSpace, "Task1EssayGradingExperiment");
//    }
//	
//	public static void runEssayBaselineExperimentCV(CollectionReaderDescription reader) throws Exception{ 	
//		Map<String, Object> dimReaders = new HashMap<String, Object>();
//		dimReaders.put(DIM_READER_TRAIN, reader);
//		
//		ParameterSpace pSpace = new ParameterSpace(
//				Dimension.createBundle("readers", dimReaders),
//				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
//				Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
//				getFeatureSetsDim(),
//				//getFeatureSetsDim(),
//				getClassificationArgsDim()
//		);
//
//    	EssayGradingExperiment experiment = new EssayGradingExperiment();
//        experiment.runCrossValidation(pSpace, "Task1EssayGradingExperiment", 3);
//    }
//	
//	
//}
