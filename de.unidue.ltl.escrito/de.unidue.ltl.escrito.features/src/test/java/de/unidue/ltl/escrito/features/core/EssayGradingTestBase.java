package de.unidue.ltl.escrito.features.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
//import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class EssayGradingTestBase {

	public AnalysisEngine getPreprocessingEngine(String lang, boolean useParser)
			throws ResourceInitializationException
	{
		AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription posTagger = createEngineDescription(OpenNlpPosTagger.class);
		AnalysisEngineDescription chunker = createEngineDescription(OpenNlpChunker.class);
		AnalysisEngineDescription lemmatizer = createEngineDescription(MateLemmatizer.class);
		//Berkeley Parser
		
		AnalysisEngineDescription parserDE = createEngineDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_LANGUAGE,"de",BerkeleyParser.PARAM_WRITE_PENN_TREE,true);
		AnalysisEngineDescription parserEN = createEngineDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_LANGUAGE,"en",BerkeleyParser.PARAM_WRITE_PENN_TREE,true);
		
		//Standford Parser
		/*
		AnalysisEngineDescription standfordParserDE = createEngineDescription(StanfordParser.class,
				StanfordParser.PARAM_LANGUAGE,"de",
				StanfordParser.PARAM_WRITE_PENN_TREE, true);
		AnalysisEngineDescription standfordParserEN = createEngineDescription(StanfordParser.class,
				StanfordParser.PARAM_LANGUAGE,"en",StanfordParser.PARAM_WRITE_PENN_TREE, true);*/

		if (lang.equals("de")&&!useParser){
			System.out.println("Build test engine WITHOUT parser for German...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer);
			return createEngine(description);
		}else if(lang.equals("de")&&useParser){
			System.out.println("Build test engine WITH parser for German...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,lemmatizer,parserDE);	
			return createEngine(description);
		}else if (lang.equals("en")&&!useParser){
			System.out.println("Build test engine WITHOUT parser for English...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,chunker, lemmatizer);
			return createEngine(description);
		}else if(lang.equals("en")&&useParser){
			System.out.println("Build test engine WITH parser for English...");
			AnalysisEngineDescription description = createEngineDescription(segmenter,posTagger,chunker,lemmatizer,parserEN);	
			return createEngine(description);
		} else {
			return null;
		}

	}

}
