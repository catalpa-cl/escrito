package de.unidue.ltl.escrito.examples.normalization;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.core.normalization.LevenshteinChecker;
import de.unidue.ltl.escrito.core.normalization.SpellingErrorAnalyzer;
import de.unidue.ltl.escrito.core.normalization.SpellingErrorCorrector;
import de.unidue.ltl.escrito.generic.GenericDatasetReader;
import de.unidue.ltl.escrito.io.shortanswer.PowerGradingReader;


/**
 * 
 * Example for how to perform spellchecking using either the JazzyChecker or a Levenshtein Distance based checker.
 * 
 * @author andrea
 *
 */

public class NormalizationExampleStandardSpellchecking {


	public static void main(String[] args) throws UIMAException, IOException{
		String dictionaryPath = "src/main/resources/dictionary/hunspell_en_US.txt";
//		preprocessPG(System.getenv("DKPRO_HOME")+"/datasets/powergrading//train_70.txt", dictionaryPath, "en");
//		preprocessPG(System.getenv("DKPRO_HOME")+"/datasets/powergrading//test_30.txt", dictionaryPath, "en");
		preprocessGeneric("src/main/resources/exampleTexts/smallExampleDataset.tsv", dictionaryPath, "en");
	}

	private static void preprocessPG(String data, String spellingDictionaryPath, String languageCode) throws UIMAException, IOException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(PowerGradingReader.class,
				PowerGradingReader.PARAM_INPUT_FILE, data,
				PowerGradingReader.PARAM_CORPUSNAME, "PG");
		runSpellchecking("PG", reader, spellingDictionaryPath, "en");
	}
	
	private static void preprocessGeneric(String data, String spellingDictionaryPath, String languageCode) throws UIMAException, IOException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(GenericDatasetReader.class,
				GenericDatasetReader.PARAM_INPUT_FILE, data,
				GenericDatasetReader.PARAM_LANGUAGE, "en",
				GenericDatasetReader.PARAM_QUESTION_PREFIX, "Q",
				GenericDatasetReader.PARAM_TARGET_ANSWER_PREFIX, "TA",
				GenericDatasetReader.PARAM_CORPUSNAME, "Example");
		runSpellchecking("Example", reader, spellingDictionaryPath, "en");
	}

	private static void runSpellchecking(String string, CollectionReaderDescription reader, String spellingDictionaryPath, String languageCode) throws UIMAException, IOException {
		AnalysisEngineDescription seg = createEngineDescription(OpenNlpSegmenter.class,
				OpenNlpSegmenter.PARAM_LANGUAGE, languageCode);
//		AnalysisEngineDescription spellChecker = createEngineDescription(
//				JazzyChecker.class,
//				JazzyChecker.PARAM_MODEL_LOCATION, spellingDictionaryPath
//				);
		AnalysisEngineDescription spellChecker = createEngineDescription(
				LevenshteinChecker.class,
				LevenshteinChecker.PARAM_MODEL_LOCATION, spellingDictionaryPath,
				LevenshteinChecker.PARAM_SCORE_THRESHOLD, 3
				);
		
		AnalysisEngineDescription analyzer = createEngineDescription(SpellingErrorAnalyzer.class,
				SpellingErrorAnalyzer.PARAM_OUTPUT_TEXTS_LOCATION, "src/main/resources/spellingAnalyses", 
				SpellingErrorAnalyzer.PARAM_PROMPTID, "PG_all");
		AnalysisEngineDescription corrector = createEngineDescription(SpellingErrorCorrector.class,
				SpellingErrorCorrector.PARAM_OUTPUT_TEXTS_LOCATION, "src/main/resources/spellingCorrection",
				SpellingErrorCorrector.PARAM_PROMPTID, "PG_all");
		SimplePipeline.runPipeline(reader, 
				seg, 
				spellChecker,
				analyzer,
				corrector
				);
	}








}
