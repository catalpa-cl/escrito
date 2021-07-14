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
import de.unidue.ltl.escrito.io.essay.AsapEssayReader;
import de.unidue.ltl.escrito.io.essay.AsapEssayReader.RatingBias;

public class BasicExperimentASAP extends Experiments_ImplBase implements Constants {

	public static void main(String[] args) throws Exception{

		String essayPath = "/Users/andrea/dkpro/datasets/asap_essays/training_set_rel3.tsv";
		System.out.println(essayPath);
		runBasicAsapExperiment(essayPath, essayPath);
	}


	private static void runBasicAsapExperiment(String trainData, String experimentName) throws Exception {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				AsapEssayReader.class,
				AsapEssayReader.PARAM_QUESTION_ID, 1,
				AsapEssayReader.PARAM_TARGET_LABEL, "score",
				AsapEssayReader.PARAM_RATING_BIAS, RatingBias.low,
                AsapEssayReader.PARAM_DO_SPARSECLASSMERGING, false,
                AsapEssayReader.PARAM_DO_NORMALIZATION, false,
				AsapEssayReader.PARAM_INPUT_FILE, trainData);
		runBaselineExperiment("ASAP1", reader, reader, "en");
	}




	private static void runBaselineExperiment(String experimentName, CollectionReaderDescription readerTrain,
			CollectionReaderDescription readerTest, String languageCode) throws Exception {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();


		ParameterSpace pSpace = null;
		pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT), 
				FeatureSettings.getFeatureSetsEssayFull(),
				learningsArgsDims); 

		runTrainTest(pSpace, experimentName, getPreprocessing(languageCode));
	}





}
