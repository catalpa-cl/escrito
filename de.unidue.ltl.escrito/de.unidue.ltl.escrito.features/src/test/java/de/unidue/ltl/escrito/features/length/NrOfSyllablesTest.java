package de.unidue.ltl.escrito.features.length;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase.ParserType;
import de.unidue.ltl.escrito.features.fachsprache.SubstantivierungExtractor;

import org.junit.Assert;

public class NrOfSyllablesTest extends EssayGradingTestBase

{
    @Test
    public void nrOfTokensFeatureExtractorTest()
        throws Exception
    {
    	AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Das ist ein kurzer Beispielsatz.");
        engine.process(jcas);

        TextClassificationTarget target = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        NrOfSyllables extractor = FeatureUtil.createResource(
        		NrOfSyllables.class,
        		NrOfSyllables.PARAM_UNIQUE_EXTRACTOR_NAME, "NrOfSyllables"
		);
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, target));

        Assert.assertEquals(3, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfSyllables.FN_NUM_SYLLABLES, 1.6, iter.next());
        assertFeature(NrOfSyllables.FN_NUM_SIMPLE, .8, iter.next());
        assertFeature(NrOfSyllables.FN_NUM_COMPLEX, .2, iter.next());
        
    }


}