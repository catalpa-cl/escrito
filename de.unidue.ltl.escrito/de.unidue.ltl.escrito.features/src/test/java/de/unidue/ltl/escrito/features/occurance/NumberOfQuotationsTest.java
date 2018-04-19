package de.unidue.ltl.escrito.features.occurance;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.occurance.NumberOfQuotations;


public class NumberOfQuotationsTest 
	extends EssayGradingTestBase
{
	@Test
    public void regexMatcherFeatureExtractorTest()
        throws Exception
    {
		AnalysisEngine engine = getPreprocessingEngine("de");
       		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Ich bin ein \"FeatureExtractor\" (Z.344).");
        engine.process(jcas);

        NumberOfQuotations extractor = new NumberOfQuotations();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(2, features.size());

        assertFeatures(NumberOfQuotations.NR_OF_QUOTES, 1.0, features, 0.0001);
        assertFeatures(NumberOfQuotations.NR_OF_LINEINDICATORS, 1.0, features, 0.0001);
    }
}
