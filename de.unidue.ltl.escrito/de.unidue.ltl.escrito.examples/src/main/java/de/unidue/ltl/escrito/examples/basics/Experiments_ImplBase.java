package de.unidue.ltl.escrito.examples.basics;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchRuntimeReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpParser;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.unidue.ltl.escrito.core.learningcurve.LearningCurveAdapter;
import de.unidue.ltl.escrito.core.learningcurve.LearningCurveReport;
import de.unidue.ltl.escrito.core.report.GradingEvaluationReport;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;

public class Experiments_ImplBase implements Constants {


	@SuppressWarnings("unchecked")
	public static Dimension<Map<String, Object>> getStandardWekaClassificationArgsDim()
	{	
		Map<String, Object> config = new HashMap<>();
		config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapter(), SMO.class.getName()});
		config.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass().getName());
		config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());
		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);					
		return mlas;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Dimension<Map<String, Object>> getWekaLearningCurveClassificationArgsDim()
	{	
		Map<String, Object> config = new HashMap<>();
		config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new LearningCurveAdapter(), SMO.class.getName()});
		config.put(DIM_DATA_WRITER, new LearningCurveAdapter().getDataWriterClass().getName());
		config.put(DIM_FEATURE_USE_SPARSE, new LearningCurveAdapter().useSparseFeatures());
		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);					
		return mlas;
	}

	// ######### PREPROCESSING ##########//

	// TODO: make preprocessing dependent on the feature extraction used
	public static AnalysisEngineDescription getPreprocessing(String languageCode) throws ResourceInitializationException {
		AnalysisEngineDescription tagger       = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription lemmatizer   = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription parser       = createEngineDescription(NoOpAnnotator.class);

		tagger = createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, languageCode);
		lemmatizer = createEngineDescription(ClearNlpLemmatizer.class);
		parser = createEngineDescription(
				ClearNlpParser.class,
				ClearNlpParser.PARAM_LANGUAGE, languageCode,
				ClearNlpParser.PARAM_VARIANT, "ontonotes"
				);


		if (languageCode.equals("en")){
			return createEngineDescription(
					createEngineDescription(
							ClearNlpSegmenter.class
							),
					tagger,
					lemmatizer,
					parser
					);
		} else if (languageCode.equals("de")){
			return null;
		} else {
			System.err.println("Unknown language code "+languageCode+". We currently support: en, de");
			System.exit(-1);
		}
		return null;
	}




	// ######### EXPERIMENTAL SETUPS ##########
	// ##### TRAIN-TEST #####
	protected static void runTrainTest(ParameterSpace pSpace, String name, AnalysisEngineDescription aed)
			throws Exception
	{
		System.out.println("Running experiment "+name);
		ExperimentTrainTest batch = new ExperimentTrainTest(name + "-TrainTest");
		batch.setPreprocessing(aed);
		batch.addInnerReport(GradingEvaluationReport.class);
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchTrainTestReport.class);
		// Run
		Lab.getInstance().run(batch);
	}

	// ##### LEARNING-CURVE #####
	public static void runLearningCurve(ParameterSpace pSpace, String name, String languageCode)
			throws Exception
	{
		System.out.println("Running experiment "+name);
		ExperimentTrainTest batch = new ExperimentTrainTest(name + "-LearningCurve");
		batch.setPreprocessing(getPreprocessing(languageCode));
		batch.addInnerReport(LearningCurveReport.class);    
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		// TODO: wieso wird der nicht ausgeführt?
		batch.addReport(BatchRuntimeReport.class);
		// Run
		Lab.getInstance().run(batch);
	}



}
