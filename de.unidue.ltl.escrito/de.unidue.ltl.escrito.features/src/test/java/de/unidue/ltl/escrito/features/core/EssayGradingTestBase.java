package de.unidue.ltl.escrito.features.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
//import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
//import de.tudarmstadt.ukp.dkpro.core.matetools.MateParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class EssayGradingTestBase {

	public enum ParserType {
		noParser,
		constituentParser,
		dependencyParser
	}

	public AnalysisEngine getPreprocessingEngine(String lang, ParserType parser)
			throws ResourceInitializationException
	{
		AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription posTagger = createEngineDescription(OpenNlpPosTagger.class);
		AnalysisEngineDescription chunker = createEngineDescription(OpenNlpChunker.class);
		AnalysisEngineDescription lemmatizer = createEngineDescription(NoOpAnnotator.class);
		if (lang.equals("de")){
			lemmatizer = createEngineDescription(MateLemmatizer.class);
		}
		else if (lang.equals("en")){
			lemmatizer = createEngineDescription(CoreNlpLemmatizer.class);
		}
		//Berkeley Parser		
		AnalysisEngineDescription berkeleyParserDE = createEngineDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_LANGUAGE,"de",BerkeleyParser.PARAM_WRITE_PENN_TREE,true);
		AnalysisEngineDescription berkeleyparserEN = createEngineDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_LANGUAGE,"en",BerkeleyParser.PARAM_WRITE_PENN_TREE,true);
		//Mate Parser
		//		AnalysisEngineDescription mateParserDE = createEngineDescription(MateParser.class,
		//				MateParser.PARAM_LANGUAGE,"de", MateParser.PARAM_PRINT_TAGSET,true
		//				);
		//		AnalysisEngineDescription mateParserEN = createEngineDescription(MateParser.class,
		//				MateParser.PARAM_LANGUAGE,"en", MateParser.PARAM_PRINT_TAGSET,true
		//				);
		AnalysisEngineDescription mateParserEN = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription mateParserDE = createEngineDescription(NoOpAnnotator.class);


		if (lang.equals("de")&&parser.equals(ParserType.noParser)){
			System.out.println("Build test engine WITHOUT parser for German...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer);
			return createEngine(description);
		}else if(lang.equals("de")&&parser.equals(ParserType.constituentParser)){
			System.out.println("Build test engine WITH constituent parser for German...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer,berkeleyParserDE);	
			return createEngine(description);
		}else if(lang.equals("de")&&parser.equals(ParserType.dependencyParser)){
			System.out.println("Build test engine WITH dependency parser for German...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer,mateParserDE);	
			return createEngine(description);
		}else if(lang.equals("en")&&parser.equals(ParserType.noParser)){
			System.out.println("Build test engine WITHOUT parser for English...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,chunker, lemmatizer);
			return createEngine(description);
		}else if(lang.equals("en")&&parser.equals(ParserType.constituentParser)){
			System.out.println("Build test engine WITH parser for English...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,chunker,lemmatizer,berkeleyparserEN);	
			return createEngine(description);
		}else if(lang.equals("en")&&parser.equals(ParserType.dependencyParser)){
			System.out.println("Build test engine WITH dependency parser for English...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer,mateParserEN);	
			return createEngine(description);
		}else {
			return null;
		}
	}

}
