//package de.unidue.ltl.escrito.examples.essay.classification;
//
//import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.apache.uima.analysis_engine.AnalysisEngineDescription;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.dkpro.lab.Lab;
//import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
//import org.dkpro.lab.task.Dimension;
//import org.dkpro.lab.task.ParameterSpace;
//import org.dkpro.tc.api.features.TcFeatureFactory;
//import org.dkpro.tc.api.features.TcFeatureSet;
//import org.dkpro.tc.core.Constants;
//import org.dkpro.tc.features.length.NrOfTokens;
//import org.dkpro.tc.features.ngram.LuceneNGram;
//import org.dkpro.tc.features.ngram.WordNGram;
//import org.dkpro.tc.ml.ExperimentCrossValidation;
//import org.dkpro.tc.ml.ExperimentTrainTest;
//import org.dkpro.tc.ml.report.BatchCrossValidationReport;
//import org.dkpro.tc.ml.report.BatchTrainTestReport;
//import org.dkpro.tc.ml.weka.WekaClassificationAdapter;
//
//import weka.classifiers.functions.SMO;
//import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
//import de.unidue.ltl.escrito.core.report.CvEvaluationReport;
//import de.unidue.ltl.escrito.core.report.GradingEvaluationReport;
//
//public abstract class Experiments_ImplBase
//	implements Constants
//{
//
//	public static final Boolean[] toLowerCase = new Boolean[] { true };
//
//	public static final String stopwordList = "classpath:/stopwords/english_stopwords.txt";
//	//    public static final String stopwordList = "classpath:/stopwords/english_empty.txt";
//
//	public static final String SPELLING_VOCABULARY = "classpath:/vocabulary/en_US_dict.txt";
//
//	@SuppressWarnings("unchecked")
//	public static Dimension<List<String>> getClassificationArgsDim()
//	{
//		Dimension<List<String>> dimClassificationArgs = Dimension.create(
//				Constants.DIM_CLASSIFICATION_ARGS,
//				Arrays.asList(new String[] { SMO.class.getName() })
//		);
//		
//		return dimClassificationArgs;
//	}
//
//	public static Dimension<TcFeatureSet> getFeatureSetsDim()
//	{
//		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
//				DIM_FEATURE_SET,
//				new TcFeatureSet(
//			//			TcFeatureFactory.create(NrOfTokens.class),
//						TcFeatureFactory.create(
//								WordNGram.class,
//								WordNGram.PARAM_NGRAM_MIN_N, 1,
//								WordNGram.PARAM_NGRAM_MAX_N, 3,
//								WordNGram.PARAM_NGRAM_USE_TOP_K, 1000
//								)
//						)
//				);
//		return dimFeatureSets;
//	}
//
//	public static AnalysisEngineDescription getPreprocessing()
//			throws ResourceInitializationException
//	{
//		return createEngineDescription(
//				createEngineDescription(
//						BreakIteratorSegmenter.class
//				)
//		);
//	}
//
//	// ##### CV #####
//	protected void runCrossValidation(ParameterSpace pSpace, String name, int numFolds)
//			throws Exception
//	{
//		ExperimentCrossValidation batch = new ExperimentCrossValidation(name + "-CV",
//				WekaClassificationAdapter.class, numFolds);
//		batch.setPreprocessing(getPreprocessing());
//		batch.addInnerReport(GradingEvaluationReport.class);
//		batch.setParameterSpace(pSpace);
//		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
//		batch.addReport(BatchCrossValidationReport.class);
//		batch.addReport(CvEvaluationReport.class);
//
//		// Run
//		Lab.getInstance().run(batch);
//	}
//
//	// ##### TRAIN-TEST #####
//	protected void runTrainTest(ParameterSpace pSpace, String name)
//			throws Exception
//	{
//		ExperimentTrainTest batch = new ExperimentTrainTest(name + "-TrainTest",
//				WekaClassificationAdapter.class);
//		batch.setPreprocessing(getPreprocessing());
//		batch.addInnerReport(GradingEvaluationReport.class);
//		batch.setParameterSpace(pSpace);
//		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
//		batch.addReport(BatchTrainTestReport.class);
//
//		// Run
//		Lab.getInstance().run(batch);
//	}
//	
//	
//	
//}
