package de.unidue.ltl.escrito.examples.preprocessing;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpDependencyParser;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.unidue.ltl.escrito.io.essay.AsapEssayReader;
import de.unidue.ltl.escrito.io.essay.AsapEssayReader.RatingBias;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

public class Preprocessing {

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException{
		testPrePro_de();
		testPrePro_en();
		//preprocess_ASAP_AES();
	}

	private static void testPrePro_de() throws ResourceInitializationException, AnalysisEngineProcessException { 
		String text = "Das ist eine Satz. Peter gibt Maria das Buch.";
		AnalysisEngine engine = createEngine(getPreprocessing("de"));
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(text);
		engine.process(jcas);
	}

	private static void testPrePro_en() throws ResourceInitializationException, AnalysisEngineProcessException { 
		String text = "Peter gives Mary a book. This is an sentence with seven words.";
		AnalysisEngine engine = createEngine(getPreprocessing("en"));
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText(text);
		engine.process(jcas);
	}

	private static void preprocess_ASAP_AES() throws ResourceInitializationException, UIMAException, IOException {
		for (int i = 1; i<=8; i++){
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					AsapEssayReader.class,
					AsapEssayReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/asap_essays/training_set_rel3.tsv/",
					AsapEssayReader.PARAM_TARGET_LABEL, "score",
					AsapEssayReader.PARAM_ASAP_NUMBER, 1,
					AsapEssayReader.PARAM_RATING_BIAS, RatingBias.low,
					AsapEssayReader.PARAM_DO_SPARSECLASSMERGING, false,
					AsapEssayReader.PARAM_DO_NORMALIZATION, false,
					AsapEssayReader.PARAM_QUESTION_ID, 1);

			SimplePipeline.runPipeline(reader, 
					getPreprocessingAndWriter("en", "target/output/asap-aes")
					);
		}
	}


	public static AnalysisEngineDescription getPreprocessingAndWriter(String languageCode, String outputPath) throws ResourceInitializationException {
		if (!outputPath.endsWith("/")){
			outputPath+="/";
		}
		AnalysisEngineDescription binCasWriter = createEngineDescription(
				BinaryCasWriter.class, 
				BinaryCasWriter.PARAM_FORMAT, "6+",
				BinaryCasWriter.PARAM_OVERWRITE, true,
				BinaryCasWriter.PARAM_TARGET_LOCATION, outputPath+"bincas"
				);
		AnalysisEngineDescription xmiWriter = createEngineDescription(
				XmiWriter.class, 
				XmiWriter.PARAM_OVERWRITE, true,
				XmiWriter.PARAM_TARGET_LOCATION, outputPath+"cas"
				);

		return createEngineDescription(
				getPreprocessing(languageCode),
				xmiWriter,
				binCasWriter
				);
	}


	public static AnalysisEngineDescription getPreprocessing(String languageCode) throws ResourceInitializationException {
		AnalysisEngineDescription seg = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription checker = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription posTagger = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription lemmatizer = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription chunker = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription constituentParser = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription dependencyParser = createEngineDescription(NoOpAnnotator.class);

		seg = createEngineDescription(CoreNlpSegmenter.class,
				CoreNlpSegmenter.PARAM_LANGUAGE, languageCode);
//		does not work with TC so we cannot use it here
//		checker = createEngineDescription(LanguageToolChecker.class,
//				CoreNlpSegmenter.PARAM_LANGUAGE, languageCode);
		constituentParser = createEngineDescription(StanfordParser.class,
				StanfordParser.PARAM_LANGUAGE, languageCode,
				StanfordParser.PARAM_WRITE_PENN_TREE, true,
				StanfordParser.PARAM_WRITE_POS, false,
				StanfordParser.PARAM_PRINT_TAGSET,false,
				StanfordParser.PARAM_VARIANT, "pcfg");
		// Achtung: Geht anscheinend nur mit dem alten Stanfordparser. Der neue CoreNLP-Parser wirft komische Ausnahmen.
		
		if (languageCode.equals("de")){
			posTagger = createEngineDescription(StanfordPosTagger.class,
					CoreNlpPosTagger.PARAM_LANGUAGE, "de");
			lemmatizer = createEngineDescription(MateLemmatizer.class,
					MateLemmatizer.PARAM_LANGUAGE, languageCode);
			dependencyParser = createEngineDescription(CoreNlpDependencyParser.class,
					CoreNlpDependencyParser.PARAM_LANGUAGE, languageCode,
					CoreNlpDependencyParser.PARAM_PRINT_TAGSET,false,
					CoreNlpDependencyParser.PARAM_VARIANT, "ud");
		} else if(languageCode.equals("en")){
			posTagger = createEngineDescription(CoreNlpPosTagger.class,
					CoreNlpPosTagger.PARAM_LANGUAGE, "en");
			chunker = createEngineDescription(OpenNlpChunker.class,
					OpenNlpChunker.PARAM_LANGUAGE, "en");
			lemmatizer = createEngineDescription(CoreNlpLemmatizer.class);
			dependencyParser = createEngineDescription(CoreNlpDependencyParser.class,
					CoreNlpDependencyParser.PARAM_LANGUAGE, languageCode,
					CoreNlpDependencyParser.PARAM_PRINT_TAGSET,false);
		}

		AnalysisEngineDescription analyzer = createEngineDescription(Analyzer.class);
		return createEngineDescription(
				seg, 
				checker,
				posTagger, 
				lemmatizer,
				chunker,
				dependencyParser,
				constituentParser,
				analyzer
				);
	}



}
