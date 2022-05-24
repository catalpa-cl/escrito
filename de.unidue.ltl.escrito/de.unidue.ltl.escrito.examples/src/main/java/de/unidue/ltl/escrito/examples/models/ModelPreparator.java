package de.unidue.ltl.escrito.examples.models;

import static java.util.Arrays.asList;

import java.io.File;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.dkpro.tc.ml.base.TcTrainer;
import org.dkpro.tc.ml.weka.core.WekaTrainer;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.io.shortanswer.Asap2Reader;
import weka.classifiers.functions.SMO;

public class ModelPreparator {

	/*
	 * Example that shows how to train and store a model for ASAP prompt 1
	 */
	

	static File modelFileAsap1 = new File("target/pretrainedModels/Asap1.model");
	
	static File luceneFolderAsap1 = new File("target/pretrainedModels/luceneFolderASAP1");
	

	public static void main(String[] args) throws Exception{
		trainModel(1);
		System.out.println("Finished training model");
	}

	

	// trains a model, model is stored in modelOut
	private static void trainModel(int promptId) throws Exception {
		//String trainPath = "/users/andrea/dkpro/datasets/ASAP/train.tsv";
		String trainPath = System.getenv("DKPRO_HOME")+"/datasets/ASAP/train.tsv";
		trainModel(trainPath, modelFileAsap1, luceneFolderAsap1, promptId);
	}

	private static void trainModel(String trainPath, File modelOut, File luceneFolder, int promptId) throws Exception {
		String outputPathTrain = "target/tn_raw_output/train";
		Utils.ensureFolderExistence(outputPathTrain);

		Object[] ngramParameter = new Object[] { WordNGram.PARAM_NGRAM_USE_TOP_K, "10000",
				WordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123", WordNGram.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString() };

		// Extract features from training data - this steps requires building the Lucene index for
		// the ngram feature
		runTrainingMetaCollection(trainPath, promptId, ngramParameter);
		// method shared between model training and model application
		File extractedTrainData = Utils.runFeatureExtractionAsap(trainPath, ngramParameter,
				outputPathTrain, true, promptId);

		TcTrainer trainer = new WekaTrainer();
		trainer.train(extractedTrainData, modelOut, asList(SMO.class.getName()));
	}




	private static void runTrainingMetaCollection(String train, int promptId, Object[] ngramParameter)
			throws Exception
	{
		// Features such as WordNgram (or any other Ngram-based features) build a Lucene index
		// first, this is done by executing this piece of code. If no Ngram features are used this
		// step is not necessary
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReaderDescription(Asap2Reader.class,
						Asap2Reader.PARAM_INPUT_FILE, train,
						Asap2Reader.PARAM_PROMPT_IDS, promptId),
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(WordNGramMC.class, ngramParameter));
	}
}
