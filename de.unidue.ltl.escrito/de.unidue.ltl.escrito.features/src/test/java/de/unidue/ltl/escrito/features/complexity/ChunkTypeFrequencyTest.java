package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

import org.junit.Assert;

public class ChunkTypeFrequencyTest extends EssayGradingTestBase
{
    @Test
    public void chunkTypeFrequencyTest() 
        throws Exception
    {
    	AnalysisEngine engine = getPreprocessingEngine("en", ParserType.noParser);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test in a sentence.");
        engine.process(jcas);

        TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        ChunkTypeFrequency extractor = new ChunkTypeFrequency();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));

        Assert.assertEquals(3, features.size());

        Iterator<Feature> iter = features.iterator();
        Feature f = iter.next();
        System.out.println(f.toString());
        assertFeature(ChunkTypeFrequency.FN_PP_FREQ, 1., f);
        f = iter.next();
        System.out.println(f.toString());
        assertFeature(ChunkTypeFrequency.FN_NP_FREQ, 3., f);
        f = iter.next();
        System.out.println(f.toString());
        assertFeature(ChunkTypeFrequency.FN_VP_FREQ, 1., f);
        
    }

    
}