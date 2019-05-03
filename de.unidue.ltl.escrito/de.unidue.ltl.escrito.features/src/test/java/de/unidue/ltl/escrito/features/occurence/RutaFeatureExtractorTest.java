package de.unidue.ltl.escrito.features.occurence;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.occurence.NrOfCommas;

public class RutaFeatureExtractorTest {
	@Test
    public void nrOfCommasFeatureExtractorTest_de()
        throws Exception
    {
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class));
        AnalysisEngine engine=createEngine(description);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Ich wohne, esse und lebe in einem Haus, das klein, neu, schön und rund ist.");
        engine.process(jcas);

        NrOfCommas extractor = new NrOfCommas();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        //4 commas/ 20 tokens= 0.2
        assertFeature(NrOfCommas.NR_OF_COMMAS, 0.2, iter.next());
    }
	
	
	@Test
    public void nrOfCommasFeatureExtractorTest_en()
        throws Exception
    {
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class));
        AnalysisEngine engine=createEngine(description);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("To improve her English, she practised every day.");
        engine.process(jcas);

        NrOfCommas extractor = new NrOfCommas();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        //1 comma/ 10 tokens= 0.2
        assertFeature(NrOfCommas.NR_OF_COMMAS, 0.1, iter.next());
    }
	
	
}
