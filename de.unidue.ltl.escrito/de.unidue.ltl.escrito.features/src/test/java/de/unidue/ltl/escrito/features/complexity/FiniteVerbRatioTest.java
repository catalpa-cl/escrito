package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import java.util.Iterator;
import java.util.Set;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Assert;
import org.junit.Test;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;


public class FiniteVerbRatioTest extends EssayGradingTestBase {
	@Test
	public void finiteVerbRatioExtractorTest_de() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Ich möchte ein Beispiel testen.");
		engine.process(jcas);
		
		FiniteVerbRatio extractor = FeatureUtil.createResource(
				FiniteVerbRatio.class,
				FiniteVerbRatio.PARAM_TAGSET,"stts",
				FiniteVerbRatio.PARAM_UNIQUE_EXTRACTOR_NAME,"FiniteVerbRatio"
		);
		
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
		
		//Test: Number of features
		Assert.assertEquals(1, features.size());
		
		//Test feature value
		Iterator<Feature> iter = features.iterator();
        Feature f = iter.next();
        System.out.println(f.toString());
        assertFeature(FiniteVerbRatio.FN_FiniteVerbRatio, 0.5, f);
	}
	
	
	@Test
	public void finiteVerbRatioExtractorTest_en() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en",ParserType.noParser);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("I want to test an example.");
		engine.process(jcas);
		
		FiniteVerbRatio extractor = FeatureUtil.createResource(
				FiniteVerbRatio.class,
				FiniteVerbRatio.PARAM_TAGSET,"ptb",
				FiniteVerbRatio.PARAM_UNIQUE_EXTRACTOR_NAME,"FiniteVerbRatio"
		);
		
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
		
		//Test: Number of features
		Assert.assertEquals(1, features.size());
		
		//Test feature value
		Iterator<Feature> iter = features.iterator();
        Feature f = iter.next();
        System.out.println(f.toString());
        assertFeature(FiniteVerbRatio.FN_FiniteVerbRatio, 0.5, f);
	}
}
