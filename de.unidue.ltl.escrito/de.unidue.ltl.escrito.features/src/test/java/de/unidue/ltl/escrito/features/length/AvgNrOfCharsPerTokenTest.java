package de.unidue.ltl.escrito.features.length;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Assert;

public class AvgNrOfCharsPerTokenTest
{
    @Test
    public void nrOfCharsFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is a test.");
        engine.process(jcas);
        
        TextClassificationTarget target = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());

        AvgNrOfCharsPerToken extractor = new AvgNrOfCharsPerToken();
        List<Feature> features = new ArrayList<>(extractor.extract(jcas,target));

        Assert.assertEquals(1, features.size());

        assertEquals(new Double(3.1), features.get(0).getValue());
    }
}