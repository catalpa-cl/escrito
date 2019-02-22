package de.unidue.ltl.escrito.features.occurence;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;
import org.junit.Assert;
import org.junit.Ignore;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.occurence.NumberOfSubordinateClauses;


public class NumberOfSubordinateClausesTest 
	extends EssayGradingTestBase
{
	
	@Test @Ignore
    public void subOrdinateClauseFeatureExtractorTest_de()
        throws Exception
    {
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Ich weiß es, weil ich einen Grund habe und wenn ich aufgegessen habe. Hier ist nur ein Hauptsatz.");
        engine.process(jcas);

        NumberOfSubordinateClauses extractor = FeatureUtil.createResource(
        		NumberOfSubordinateClauses.class,
        		NumberOfSubordinateClauses.PARAM_LANGUAGE, "de",
        		NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/causalIndicators_de.txt",
        		NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/temporalIndicators_de.txt",
        		NumberOfSubordinateClauses.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy"
        );
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(3, features.size());

        assertFeatures(NumberOfSubordinateClauses.NR_OF_SUBORDINATECLAUSES, 1.0, features, 0.0001);
        assertFeatures(NumberOfSubordinateClauses.NR_OF_CAUSALCLAUSES, 0.5, features, 0.0001);
        assertFeatures(NumberOfSubordinateClauses.NR_OF_TEMPORALCLAUSES, 0.5, features, 0.0001);
    }
	
	
	
	@Test @Ignore
    public void subOrdinateClauseFeatureExtractorTest()
        throws Exception
    {
		AnalysisEngine engine = getPreprocessingEngine("en",ParserType.noParser);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("I know it because I have a reason as soon as I read that book.");
        engine.process(jcas);

        NumberOfSubordinateClauses extractor = FeatureUtil.createResource(
        		NumberOfSubordinateClauses.class,
        		NumberOfSubordinateClauses.PARAM_LANGUAGE,"en",
        		NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/causalIndicators_en.txt",
        		NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/temporalIndicators_en.txt",
        		NumberOfSubordinateClauses.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy"
        );
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(3, features.size());

        assertFeatures(NumberOfSubordinateClauses.NR_OF_SUBORDINATECLAUSES, 2.0, features, 0.0001);
        assertFeatures(NumberOfSubordinateClauses.NR_OF_CAUSALCLAUSES, 2.0, features, 0.0001);
        assertFeatures(NumberOfSubordinateClauses.NR_OF_TEMPORALCLAUSES,1.0,features,0.0001);
    }
	
}
