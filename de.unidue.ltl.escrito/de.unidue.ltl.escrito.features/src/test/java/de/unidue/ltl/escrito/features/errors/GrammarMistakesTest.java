package de.unidue.ltl.escrito.features.errors;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolChecker;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;


public class GrammarMistakesTest {
	@Test
	public void grammarMistakesFeatureExtractorTest_DE()
			throws Exception
	{
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),createEngineDescription(LanguageToolChecker.class,
				LanguageToolChecker.PARAM_LANGUAGE, "de"));
		AnalysisEngine engine=createEngine(description);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Ich wohnen in Haus. Viele Haus sind in der Stadt.");
		engine.process(jcas);

		NumberOfGrammarMistakes extractor = new NumberOfGrammarMistakes();
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());

		Iterator<Feature> iter = features.iterator();
		//3 GrammarMistakes
		assertFeatures(NumberOfGrammarMistakes.FN_NR_OF_GRAMMAR_MISTAKES, 0.25, features, 0.001);
	}

	@Test
	public void grammarMistakesFeatureExtractorTest_EN()
			throws Exception
	{
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),createEngineDescription(LanguageToolChecker.class,
				LanguageToolChecker.PARAM_LANGUAGE, "en"));
		AnalysisEngine engine=createEngine(description);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("I living in house. Many house are in street.");
		engine.process(jcas);

		NumberOfGrammarMistakes extractor = new NumberOfGrammarMistakes();
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());

		Iterator<Feature> iter = features.iterator();
		//3 GrammarMistakes
		assertFeatures(NumberOfGrammarMistakes.FN_NR_OF_GRAMMAR_MISTAKES, 0.1818, features, 0.001);
	}

}
