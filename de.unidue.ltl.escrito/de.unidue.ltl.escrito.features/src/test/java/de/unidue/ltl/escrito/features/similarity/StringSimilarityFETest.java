package de.unidue.ltl.escrito.features.similarity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.junit.Assert;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

public class StringSimilarityFETest extends EssayGradingTestBase{
	@Test
	public void PrepositionalPhraseInEssayTest() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",false);

		JCas jcas1 = engine.newJCas();
		jcas1.setDocumentLanguage("de");
		jcas1.setDocumentText("Ich bin eine Frau.");
		engine.process(jcas1);
		
		JCas jcas2 = engine.newJCas();
		jcas2.setDocumentLanguage("de");
		jcas2.setDocumentText("Ich bin keine Frau.");
		engine.process(jcas2);
		
		StringSimilarityFeatureExtractor extractor = FeatureUtil.createResource(
				StringSimilarityFeatureExtractor.class,
				StringSimilarityFeatureExtractor.PARAM_STRING_TILING_MIN,"3",
				StringSimilarityFeatureExtractor.PARAM_STRING_TILING_MAX,"7",
				StringSimilarityFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,"StringSimilarityFeatureExtractor");
		
		Set<Feature> features = extractor.extract(jcas1,jcas2);
		Assert.assertEquals(6, features.size());
		assertFeatures("Levenshtein",1.0, features,0.0001);
		assertFeatures("GreedyStringTiling_3",1.0,features,0.0001);
	}

}
