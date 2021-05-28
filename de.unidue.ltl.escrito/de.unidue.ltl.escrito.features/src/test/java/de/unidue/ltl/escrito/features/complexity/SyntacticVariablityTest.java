package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;



public class SyntacticVariablityTest
	extends EssayGradingTestBase
{
	
	@Test 
	public void SyntacticVariablityTest_en() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en", ParserType.constituentParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("This is a test. This is a great testing. "
				+ "This is a test which has a lot to show.");
		engine.process(jcas);

		SyntacticVariability extractor = new SyntacticVariability();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(6, features.size());

		// 2 types of sentences (phraseLevel) in 3 sentences = 0.6666666666666666
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_PHRASELEVEL, 0.6666666666666666, features, 0.0001);
		// 3 types of sentences (POS) in 3 sentences = 1.0
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_POSLEVEL, 1.0, features, 0.0001);
		// 2 types of sentences (POS) in 3 sentences = 0.66666666666666
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_SENTENCELEVEL, 0.6666666666666666, features, 0.0001);
		// 1 similar pair/ 2 pairs = 0.5 
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_PHRASELEVEL, 0.5, features, 0.0001);
		// 0 similar pair/ 2 pairs = 0
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_POSLEVEL, 0.0, features, 0.0001);
		// 1 similar pair/ 2 pairs = 0
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_SENTENCELEVEL, 0.5, features, 0.0001);
	}
	
	
	@Test
	public void SyntacticVariablityTest_de() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de", ParserType.constituentParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Das ist ein Test. Das ist eine tolle Testung. Das ist ein Test, der viel zu zeigen hat.");
		engine.process(jcas);

		SyntacticVariability extractor = new SyntacticVariability();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(6, features.size());

		// 2 types of sentences (phraseLevel) in 3 sentences = 0.6666666666666666
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_PHRASELEVEL, 0.6666666666666666, features, 0.0001);
		// 3 types of sentences (POS) in 3 sentences = 1.0
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_POSLEVEL, 1.0, features, 0.0001);
		// 2 types of sentences (POS) in 3 sentences = 0.66666666666666
		assertFeatures(SyntacticVariability.SYNTAX_TYPE_RATIO_SENTENCELEVEL, 0.6666666666666666, features, 0.0001);
		// 1 similar pair/ 2 pairs = 0.5 
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_PHRASELEVEL, 0.5, features, 0.0001);
		// 0 similar pair/ 2 pairs = 0
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_POSLEVEL, 0.0, features, 0.0001);
		// 1 similar pair/ 2 pairs = 0
		assertFeatures(SyntacticVariability.PAIRWISE_SYNTACTIC_SIMILARITY_SENTENCELEVEL, 0.5, features, 0.0001);
	}
}
