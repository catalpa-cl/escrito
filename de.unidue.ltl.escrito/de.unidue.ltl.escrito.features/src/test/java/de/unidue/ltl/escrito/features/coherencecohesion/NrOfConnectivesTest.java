package de.unidue.ltl.escrito.features.coherencecohesion;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;


public class NrOfConnectivesTest 
	extends EssayGradingTestBase
{
	@Test
	public void nrOfConnectivesFeatureExtractorTest_de() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de");

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		// Prepositions trotz nach zur während vor
		jcas.setDocumentText("Deshalb ist das so. Wir denken, denn wir leben. Obwohl es manchmal schwer ist.");
		engine.process(jcas);

		NrOfConnectives extractor = FeatureUtil.createResource(
				NrOfConnectives.class,
				NrOfConnectives.PARAM_CONNECTIVES_FILE_PATH, "src/main/resources/lists/de/connectives_de.txt",
				NrOfConnectives.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy"
		);
		
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());
		Iterator<Feature> iter = features.iterator();
		// 3 connectives/ 18 tokens= 0.16666666666666666 
		Feature f = iter.next();
		assertFeature(NrOfConnectives.NR_OF_CONNECTIVES, 0.16666666666666666, f);
		
	}
	
	
	@Test
	public void nrOfConnectivesFeatureExtractorTest_en() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en");

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		// Prepositions trotz nach zur während vor
		jcas.setDocumentText("Because I love connectives since I learned about them.");
		engine.process(jcas);

		NrOfConnectives extractor = FeatureUtil.createResource(
				NrOfConnectives.class,
				NrOfConnectives.PARAM_CONNECTIVES_FILE_PATH, "src/main/resources/lists/en/connectives_en.txt"
		);
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());

		Iterator<Feature> iter = features.iterator();
		// 2 connectives/ 10 tokens= 0.2 
		assertFeature(NrOfConnectives.NR_OF_CONNECTIVES, 0.2, iter.next());
		
	}
}
