package de.unidue.ltl.escrito.features.complexity;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class TypeTokenRatioTest {

	
	@Test
	public void TypeTokenRatioFeatureExtractorTest()
			throws Exception
	{
		String testDocument = "Ich bin ein Testtext. Ich wiederhole mich manchmal.";

		AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngine engine = createEngine(desc);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(testDocument);
		engine.process(jcas);

		TypeTokenRatioFeatureExtractor extractor = FeatureUtil.createResource(
				TypeTokenRatioFeatureExtractor.class,
				TypeTokenRatioFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy");

		List<Feature> features = new ArrayList<>(extractor.extract(jcas, TextClassificationTarget.get(jcas)));

		Assert.assertEquals(1, features.size());
		Iterator<Feature> iter = features.iterator();
		assertFeature(TypeTokenRatioFeatureExtractor.FN_TTR, 0.8, iter.next(), 0.1);

	}
	
	
	
	
}
