package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;


public class SyntaxTreeDepthTest 
	extends EssayGradingTestBase
{

	@Test
	public void syntaxTreeDepthFeatureExtractorTest_DE()
	        throws Exception
	  {
		AnalysisEngine engine = getPreprocessingEngine("de",true);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Dies ist ein Satz, der immer verschachltelt und lanweilig zu lesen. Er ist dafür kurz."
        		);
        engine.process(jcas);

        SyntaxTreeDepth extractor = FeatureUtil.createResource(
        		SyntaxTreeDepth.class,
        		SyntaxTreeDepth.LANGUAGE,"de",
        		SyntaxTreeDepth.PARAM_UNIQUE_EXTRACTOR_NAME, "SyntaxTreeDepthDFE");
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(2, features.size());

        assertFeatures(SyntaxTreeDepth.AVG_SYNTAX_TREE_DEPTH, 4.0, features, 0.0001);
        assertFeatures(SyntaxTreeDepth.TOTAL_SYNTAX_TREE_DEPTH, 8.0, features, 0.0001);
    }

	
	
	@Test
	public void syntaxTreeDepthFeatureExtractorTest_EN()
	        throws Exception
	    {
			AnalysisEngine engine = getPreprocessingEngine("en");

	        JCas jcas = engine.newJCas();
	        jcas.setDocumentLanguage("en");
	        jcas.setDocumentText("This is a sentence that is nested and boring to read. But it is short.");
	        engine.process(jcas);

	        SyntaxTreeDepth extractor = FeatureUtil.createResource(
	        		SyntaxTreeDepth.class,
	        		SyntaxTreeDepth.LANGUAGE,"en",
	        		SyntaxTreeDepth.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy");
	        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

	        Assert.assertEquals(2, features.size());

	        
	        assertFeatures(SyntaxTreeDepth.AVG_SYNTAX_TREE_DEPTH, 6.0, features, 0.0001);
	        assertFeatures(SyntaxTreeDepth.TOTAL_SYNTAX_TREE_DEPTH, 12.0, features, 0.0001);
	    }
	
	
}
