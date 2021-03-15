package de.unidue.ltl.escrito.features.complexity;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase.ParserType;
import de.unidue.ltl.escrito.features.length.NrOfTokens;

import org.junit.Assert;

public class LexicalVariationTest extends EssayGradingTestBase
{
	@Test
	public void lexicalVariationFeatureExtractorTest() 
			throws Exception
	{
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);


		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Das ist ein Test und das ist ein Beispiel.");
		engine.process(jcas);

		TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
				jcas.getDocumentText().length());

		LexicalVariation extractor = new LexicalVariation();
		List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));

		
		//Test: returns 2 features
		Assert.assertEquals(2, features.size());

		//Test: feature values
		for(Feature f : features) {
			System.out.println(f.toString());
			if(f.getName().equals(LexicalVariation.FN_LV)) {
				assertFeature(LexicalVariation.FN_LV, 0.75, f);
			}
			if (f.getName().equals(LexicalVariation.FN_VV)) {
				assertFeature(LexicalVariation.FN_VV, 0.5, f);
			}
		}
	}


}