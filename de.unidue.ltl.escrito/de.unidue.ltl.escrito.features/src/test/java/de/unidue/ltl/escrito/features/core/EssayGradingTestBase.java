package de.unidue.ltl.escrito.features.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class EssayGradingTestBase {

	public AnalysisEngine getPreprocessingEngine()
			throws ResourceInitializationException
	{
		AnalysisEngineDescription description = createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				// TODO: reintegrate that
			//	createEngineDescription(LanguageToolChecker.class,
			//			LanguageToolChecker.PARAM_LANGUAGE, "en"),
				createEngineDescription(OpenNlpPosTagger.class),
	//			createEngineDescription(OpenNlpChunker.class),
				createEngineDescription(MateLemmatizer.class),
			createEngineDescription(
					BerkeleyParser.class,
					BerkeleyParser.PARAM_LANGUAGE,"de",
					//BerkeleyParser.PARAM_LANGUAGE,"en",
					BerkeleyParser.PARAM_WRITE_PENN_TREE,true
					)
			);
		return createEngine(description);
	}
}
