package de.unidue.ltl.escrito.features.length;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.junit.Assert;

public class AvgNrOfCharsPerTokenTest
{
	@Test
	public void tokenLengthFeatureExtractorTest()
			throws Exception
	{
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class), createEngineDescription(OpenNlpPosTagger.class));
		AnalysisEngine engine=createEngine(description);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Sie ist gut.");
		engine.process(jcas);

		AvgNrOfCharsPerToken extractor = new AvgNrOfCharsPerToken();
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(2, features.size());

		Iterator<Feature> iter = features.iterator();
		assertFeature(AvgNrOfCharsPerToken.STANDARD_DEVIATION_OF_CHARS_PER_TOKEN, 0.0, iter.next());
		assertFeature(AvgNrOfCharsPerToken.AVG_NR_OF_CHARS_PER_TOKEN, 3.0, iter.next());
	}
}