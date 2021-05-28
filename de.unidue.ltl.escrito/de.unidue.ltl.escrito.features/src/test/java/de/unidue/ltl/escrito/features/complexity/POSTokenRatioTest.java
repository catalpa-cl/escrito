package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Assert;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

public class POSTokenRatioTest extends EssayGradingTestBase
{
	@Test
	public void lexicalVariationFeatureExtractorTest_de() 
			throws Exception
	{
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Das ist ein Test und das ein schönes Beispiel.");
		engine.process(jcas);

		TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
				jcas.getDocumentText().length());

		POSTokenRatio extractor = new POSTokenRatio();
		List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));
		
		
		//Test: returns 3 features
		Assert.assertEquals(3, features.size());

		//Test: feature values
		for(Feature f : features) {
			System.out.println(f.toString());
			if(f.getName().equals(POSTokenRatio.FN_NounRatio)) {
				assertFeature(POSTokenRatio.FN_NounRatio, 0.2, f);
			}
			if (f.getName().equals(POSTokenRatio.FN_VerbRatio)) {
				assertFeature(POSTokenRatio.FN_VerbRatio, 0.1, f);
			}
		}
	}

	@Test
	public void lexicalVariationFeatureExtractorTest_en() 
			throws Exception
	{
		AnalysisEngine engine = getPreprocessingEngine("en",ParserType.noParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("This is a test and this a nice example.");
		engine.process(jcas);

		TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
				jcas.getDocumentText().length());

		POSTokenRatio extractor = new POSTokenRatio();
		List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));
		
		
		//Test: returns 3 features
		Assert.assertEquals(3, features.size());

		//Test: feature values
		for(Feature f : features) {
			System.out.println(f.toString());
			if(f.getName().equals(POSTokenRatio.FN_NounRatio)) {
				assertFeature(POSTokenRatio.FN_NounRatio, 0.2, f);
			}
			if (f.getName().equals(POSTokenRatio.FN_VerbRatio)) {
				assertFeature(POSTokenRatio.FN_VerbRatio, 0.1, f);
			}
		}
	}
	

}