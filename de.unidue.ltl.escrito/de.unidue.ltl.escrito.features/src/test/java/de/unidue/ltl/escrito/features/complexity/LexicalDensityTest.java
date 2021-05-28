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

public class LexicalDensityTest extends EssayGradingTestBase
{
    @Test
    public void lexicalDensityFeatureExtractorTest_de() 
        throws Exception
    {
    	AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);


        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Das ist ein Test.");
        engine.process(jcas);

        TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        LexicalDensityFeatureExtractor extractor = new LexicalDensityFeatureExtractor();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        Feature f = iter.next();
        System.out.println(f.toString());
        assertFeature(LexicalDensityFeatureExtractor.FN_LD, 0.4, f);
    }

    @Test
    public void lexicalDensityFeatureExtractorTest_en() 
        throws Exception
    {
    	AnalysisEngine engine = getPreprocessingEngine("en",ParserType.noParser);


        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);

        TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        LexicalDensityFeatureExtractor extractor = new LexicalDensityFeatureExtractor();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        Feature f = iter.next();
        System.out.println(f.toString());
        assertFeature(LexicalDensityFeatureExtractor.FN_LD, 0.4, f);
    }
    
    
}