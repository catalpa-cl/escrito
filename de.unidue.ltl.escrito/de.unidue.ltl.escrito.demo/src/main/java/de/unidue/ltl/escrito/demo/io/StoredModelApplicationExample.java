package de.unidue.ltl.escrito.demo.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.junit.rules.TemporaryFolder;

import de.unidue.ltl.escrito.core.types.LearnerAnswer;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.features.length.NrOfChars;
import de.unidue.ltl.escrito.io.shortanswer.MohlerMihalceaReader;
import weka.classifiers.bayes.NaiveBayes;

public class StoredModelApplicationExample extends Experiments_ImplBase{

	
	
	public static void main(String[] args) throws Exception{
		setDkproHome(StoredModelApplicationExample.class.getSimpleName());
		String exampleAnswer = "This is an exampleAnswer";
		File modelFolder = new File("src/main/resources/serializationExample/model");
		documentLoadModelSingleLabel(modelFolder, exampleAnswer);
	}

   
      
	
	
	public static ParameterSpace documentGetParameterSpaceSingleLabel()
			throws ResourceInitializationException
	{
		System.out.println("Starting setup");
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		String inputDataFile = System.getenv("DKPRO_HOME")+"/datasets/mohler_and_mihalcea/basicDataset/assign.txt";
		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				MohlerMihalceaReader.class,
				MohlerMihalceaReader.PARAM_INPUT_FILE, inputDataFile,
			//	MohlerMihalceaReader.PARAM_CORPUSNAME, "MohlerMihalcea",
				MohlerMihalceaReader.PARAM_PROMPT_IDS, 1//,
			//	MohlerMihalceaReader.PARAM_QUESTION_PREFIX, "Q",
			//	MohlerMihalceaReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
				);
		dimReaders.put(DIM_READER_TRAIN, readerTrain);

		@SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
				Arrays.asList(new Object[] { new WekaAdapter(), NaiveBayes.class.getName() }));

		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
				TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K, 500,
						WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N, 3),
				TcFeatureFactory.create(NrOfChars.class)));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets,
				Dimension.create(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass()),
				//Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
				dimClassificationArgs);
		System.out.println("Finished setup");
		return pSpace;
	}

	
	public static void documentRoundTripWekaSingleLabel(String exampleAnswer)
			throws Exception
	{
		File modelFolder = new File("/src/main/resources/serializationExample/model");
		ParameterSpace docParamSpace = documentGetParameterSpaceSingleLabel();
		documentWriteModel(docParamSpace, modelFolder);
		documentLoadModelSingleLabel(modelFolder, exampleAnswer);

		// verify created files

		File classifierFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
		//assertTrue(classifierFile.exists());

		File metaOverride = new File(modelFolder.getAbsolutePath() + "/" + META_COLLECTOR_OVERRIDE);
		//assertTrue(metaOverride.exists());

		File extractorOverride = new File(
				modelFolder.getAbsolutePath() + "/" + META_EXTRACTOR_OVERRIDE);
//		assertTrue(extractorOverride.exists());

		File modelMetaFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_META);
//		assertTrue(modelMetaFile.exists());

		File featureMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_MODE);
//		assertTrue(featureMode.exists());

		File learningMode = new File(modelFolder.getAbsolutePath() + "/" + MODEL_LEARNING_MODE);
//		assertTrue(learningMode.exists());

		File bipartitionThreshold = new File(
				modelFolder.getAbsolutePath() + "/" + MODEL_BIPARTITION_THRESHOLD);
//		assertTrue(bipartitionThreshold.exists());

		modelFolder.deleteOnExit();
	}

	private static void documentWriteModel(ParameterSpace paramSpace, File modelFolder)
			throws Exception
	{
		ExperimentSaveModel batch;
		batch = new ExperimentSaveModel();
		batch.setPreprocessing(Experiments_ImplBase.getPreprocessing("en"));
		batch.setParameterSpace(paramSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.setExperimentName("MohlerMihalceaTest");
		batch.setOutputFolder(modelFolder);
		Lab.getInstance().run(batch);
	}
	
	
	

	private static void documentLoadModelSingleLabel(File modelFile, String exampleAnswer)
			throws Exception
	{

		System.out.println("Path to model: "+modelFile.getAbsolutePath());
		AnalysisEngine preprocessing = AnalysisEngineFactory.createEngine(Experiments_ImplBase.getPreprocessing("en"));
		AnalysisEngine tcAnno = AnalysisEngineFactory.createEngine(TcAnnotator.class,
				TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, LearnerAnswer.class,
				// Achtung: It seems like you MAY NOT use the class TextClassificationTarget (as we do in the reader)
				// to indicate the unit to be considered
				// as far as I can see, a TextClassifcationTarget is produced by the classifier and we only want to have one in the end!
					TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFile.getAbsolutePath());

		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(exampleAnswer);
		jcas.setDocumentLanguage("en");

		LearnerAnswer unit = new LearnerAnswer(jcas, 0, jcas.getDocumentText().length());
		unit.addToIndexes();

		
		// redo the preprocessing
		preprocessing.process(jcas);
		tcAnno.process(jcas);
		
		// redo the processing done by the classifier

		List<TextClassificationOutcome> outcomes = new ArrayList<>(
				JCasUtil.select(jcas, TextClassificationOutcome.class));
		System.out.println(jcas.getDocumentText()+"\nOutcome: "+outcomes.get(0).getOutcome());
	}

	
	
	public static boolean setDkproHome(String experimentName) {
    	String dkproHome = "DKPRO_HOME";
    	Map<String, String> env = System.getenv();
    	if (!env.containsKey(dkproHome)) {
    		System.out.println("DKPRO_HOME not set.");
    		
        	File folder = new File("target/results/" + experimentName);
        	folder.mkdirs();
        	
        	System.setProperty(dkproHome, folder.getPath());
        	System.out.println("Setting DKPRO_HOME to: " + folder.getPath());
        	
        	return true;
    	}
    	else {
    		System.out.println("DKPRO_HOME already set to: " + env.get(dkproHome));
    		System.out.println("Keeping those settings.");
    		
    		return false;
    	}
    }
	
	
}
