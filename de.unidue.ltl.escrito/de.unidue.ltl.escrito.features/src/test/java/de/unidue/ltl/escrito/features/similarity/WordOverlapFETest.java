package de.unidue.ltl.escrito.features.similarity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

public class WordOverlapFETest extends EssayGradingTestBase {
	@Test @Ignore
	public void WordOverlapWithoutIdfTest() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);

		JCas jcas1 = engine.newJCas();
		jcas1.setDocumentLanguage("de");
		jcas1.setDocumentText("Ich bin eine Frau.");
		engine.process(jcas1);
		
		JCas jcas2 = engine.newJCas();
		jcas2.setDocumentLanguage("de");
		jcas2.setDocumentText("Ich bin eine Frau.");
		engine.process(jcas2);
		
		WordOverlapFeatureExtractor extractor = FeatureUtil.createResource(
				WordOverlapFeatureExtractor.class,
				WordOverlapFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,"wordOverlapFeatureExtractor"
				);
		Set<Feature> features = extractor.extract(jcas1,jcas2);
		Assert.assertEquals(2, features.size());
		assertFeatures("TokenOverlapTargetAnswer",1.0, features,0.0001);
		assertFeatures("TokenOverlapReferenceAnswer",1.0,features,0.0001);		
	}
	
	//TODO: test the word overlap FE with WordIdfCollector

}
