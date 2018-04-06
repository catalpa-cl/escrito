package de.unidue.ltl.escrito.features.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class EssayGradingTestBase {

	public AnalysisEngine getPreprocessingEngine()
			throws ResourceInitializationException
	{
		AnalysisEngineDescription description = createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(OpenNlpPosTagger.class),
//				createEngineDescription(StanfordParser.class,
//						StanfordParser.PARAM_WRITE_PENN_TREE, true),
				createEngineDescription(MateLemmatizer.class)
//				createEngineDescription(OpenNlpChunker.class),
				// TODO: reintegrate the parser
//				createEngineDescription(
//						StanfordParser.class,
//						StanfordParser.PARAM_WRITE_PENN_TREE,true,
//						StanfordParser.PARAM_PRINT_TAGSET,true,
//						StanfordParser.PARAM_MAX_ITEMS, 100,
//						StanfordParser.PARAM_VARIANT, "pcfg"
//				)
				);
		
		return createEngine(description);
	}
}
