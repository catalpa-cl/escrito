package de.unidue.ltl.escrito.features.length;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

import org.junit.Assert;

public class AvgNrOfTokensPerSentenceTest extends EssayGradingTestBase
{
    @Test
    public void nrOfTokensPerSentenceFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is a test.");
        engine.process(jcas);

        TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        AvgNrOfTokensPerSentence extractor = new AvgNrOfTokensPerSentence();
        Set<Feature> features = new HashSet<Feature>(extractor.extract(jcas, target));

        Assert.assertEquals(2, features.size());
        assertFeatures("NrofTokensPerSentence",5.0,features,0.0001);
        assertFeatures("standardDevTokensPerSentence",0.0,features,0.0001);
    }

}