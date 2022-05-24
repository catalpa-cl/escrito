package de.unidue.ltl.escrito.examples.models;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.io.shortanswer.Asap2Reader;

public class Utils {


	public static File runFeatureExtractionAsap(String inputFile, Object[] ngramParameter,
			String outputPathTrain, boolean isTraining, int promptId)
					throws Exception
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				ReaderNewAnswers.class,
				ReaderNewAnswers.PARAM_INPUT_FILE, inputFile);
		if (isTraining){
			reader = CollectionReaderFactory.createReaderDescription(
					Asap2Reader.class,
					Asap2Reader.PARAM_INPUT_FILE, inputFile,
					Asap2Reader.PARAM_PROMPT_IDS, promptId);
		}
		SimplePipeline.runPipeline(
				// Reader
				reader,
				// Preprocessing
				AnalysisEngineFactory.createEngineDescription(JCasIdSetter.class),
				createEngineDescription(OpenNlpSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en"),

				// Feature extraction
				AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
						ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputPathTrain,
						ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS,
						WekaDataWriter.class.getName(),
						ExtractFeaturesConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
						ExtractFeaturesConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
						ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, false,
						ExtractFeaturesConnector.PARAM_FEATURE_FILTERS, new String[] {},
						ExtractFeaturesConnector.PARAM_IS_TESTING, false,
						ExtractFeaturesConnector.PARAM_USE_SPARSE_FEATURES, false,
						// TODO adapt this when working on a new dataset to let the classifier knwo about possible labels!
						ExtractFeaturesConnector.PARAM_OUTCOMES,
						new String[] { "0", "1", "2", "3"},
						// adapt this to use different features
						ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS,
						asList(ExternalResourceFactory.createExternalResourceDescription(
								WordNGram.class, ngramParameter))));
		return new File(outputPathTrain, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
	}


	public static void ensureFolderExistence(String outputPathTrain)
	{
		File file = new File(outputPathTrain);
		if (file.exists()) {
			return;
		}
		boolean creationSuccessful = file.mkdirs();
		if (!creationSuccessful) {
			throw new IllegalStateException(
					"Could not create the folder path [" + file.getAbsolutePath() + "]");
		}
	}

}
