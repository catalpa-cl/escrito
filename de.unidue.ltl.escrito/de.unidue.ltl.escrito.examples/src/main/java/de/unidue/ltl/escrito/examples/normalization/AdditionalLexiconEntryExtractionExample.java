package de.unidue.ltl.escrito.examples.normalization;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.unidue.ltl.escrito.core.normalization.JazzyChecker;
import de.unidue.ltl.escrito.core.normalization.UnigramExtractor;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;


/**
 * 
 * Example for how to extracts lexical entries which are not in a certain spelling dictionary from a specific prompt text. 
 * This lexical material can then be used in a next step to supplement a spellechecking dictionary
 * to make sure that unusual terms from the prompts are not flagged as errors
 * 
 * 
 * @author andrea
 *
 */


public class AdditionalLexiconEntryExtractionExample extends Experiments_ImplBase{
	public static final String LANGUAGE_CODE = "en";

	public static final Boolean[] toLowerCase = new Boolean[] { true };
	public static final boolean useUnigramExtractor = true;

	
	public static void main(String[] args) throws Exception	{
		String dictionaryPath = "src/main/resources/dictionary/hunspell_en_US.txt";
		String sourceText = "src/main/resources/exampleTexts/promptMaterial/examplePrompt_asap3.txt";
		runUnigramPipeline(sourceText, dictionaryPath);
	}

	private static void runUnigramPipeline(String sourceText, String dictionaryPath) throws Exception {
		CollectionReaderDescription readerSource = CollectionReaderFactory.createReaderDescription(
				TextReader.class,
				TextReader.PARAM_LANGUAGE,LANGUAGE_CODE,
				TextReader.PARAM_ENCODING,"UTF-8",
				TextReader.PARAM_SOURCE_LOCATION, sourceText);		
		AnalysisEngineDescription segmenter =createEngineDescription(
					ClearNlpSegmenter.class,
					ClearNlpSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE
					);	
		AnalysisEngineDescription spellChecker = createEngineDescription(
				JazzyChecker.class,
				JazzyChecker.PARAM_MODEL_LOCATION, dictionaryPath
				);
		AnalysisEngineDescription unigramExtractor = createEngineDescription(
				UnigramExtractor.class,
				UnigramExtractor.PARAM_OUTPUT_LOCATION, "src/main/resources/dictionaryExtensions/",
				UnigramExtractor.PARAM_NAME, "lexiconExtension.txt");
		
		SimplePipeline.runPipeline(readerSource, segmenter, spellChecker, unigramExtractor);
	}

}
