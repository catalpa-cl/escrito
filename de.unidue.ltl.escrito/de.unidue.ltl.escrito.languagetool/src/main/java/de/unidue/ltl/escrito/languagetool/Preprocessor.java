package de.unidue.ltl.escrito.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolChecker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.io.shortanswer.PowerGradingReader;

/**
 * 
 * This class is used to preprocess learner answers via languagetool. 
 * (We need to do so because the languagetool dependencies are incompatible with ngram feature extractors)
 * 
 * @author andrea
 *
 */

public class Preprocessor {


	public static void main(String[] args) throws UIMAException, IOException{
		String dkproHome = DkproContext.getContext().getWorkspace().getAbsolutePath();
		
		preprocessPG(dkproHome + "/datasets/powergrading/train_70.txt", "en");
		preprocessPG(dkproHome + "/datasets/powergrading/test_30.txt", "en");
	}

	private static void preprocessPG(String data, String languageCode) 
			throws UIMAException, IOException 
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				PowerGradingReader.class,
				PowerGradingReader.PARAM_INPUT_FILE, data,
				PowerGradingReader.PARAM_CORPUSNAME, "PG"
		);
		
		runLanguageTool("PG", reader, "en");
	}

	private static void runLanguageTool(String string, CollectionReaderDescription reader, String languageCode)
			throws UIMAException, IOException
	{
		AnalysisEngineDescription seg = createEngineDescription(
				OpenNlpSegmenter.class,
				OpenNlpSegmenter.PARAM_LANGUAGE, languageCode
		);
		AnalysisEngineDescription grammarChecker = createEngineDescription(
				LanguageToolChecker.class,
				LanguageToolChecker.PARAM_LANGUAGE, languageCode
		);

		AnalysisEngineDescription grammarMistakesAnalyzer = createEngineDescription(
				GrammarMistakesAnalyzer.class,
				GrammarMistakesAnalyzer.PARAM_OUTPUT_PATH, DkproContext.getContext().getWorkspace("datasets").getAbsolutePath()
		);

		SimplePipeline.runPipeline(reader, 
				seg, 
				grammarChecker,
				grammarMistakesAnalyzer
				);
	}
}
