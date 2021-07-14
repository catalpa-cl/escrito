package de.unidue.ltl.escrito.features.complexity;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;

public class WordWeb1TFrequencyDFETest extends EssayGradingTestBase
{
	@Test
	public void WordWeb1TFrequencyDFETest_de() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de", ParserType.noParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Das ist ein Test. Das ist eine tolle Testung. Das ist ein Test, der viel zu zeigen hat.");
		engine.process(jcas);

		WordWeb1TFrequency extractor = new WordWeb1TFrequency();
		extractor.init(DkproContext.getContext().getWorkspace("corpora").getAbsolutePath()+"/googleNGrams/de");
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(2, features.size());

		Iterator<Feature> iter = features.iterator();
		assertFeature(WordWeb1TFrequency.AVG_TOKEN_WEB1TFREQUENCY, 18.670910271521326, iter.next());
		assertFeature(WordWeb1TFrequency.MEDIAN_TOKEN_WEB1TFREQUENCY, 19.342148818721554, iter.next());
	}
	@Test
	public void WordWeb1TFrequencyDFETest_en() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en", ParserType.noParser);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("This is a test. It's a nice testing. This is a test that shows plenty things.");
		engine.process(jcas);

		WordWeb1TFrequency extractor = new WordWeb1TFrequency();
		extractor.init(DkproContext.getContext().getWorkspace("corpora").getAbsolutePath()+"/googleNGrams/wiki_en");
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(2, features.size());

		Iterator<Feature> iter = features.iterator();
		assertFeature(WordWeb1TFrequency.AVG_TOKEN_WEB1TFREQUENCY, 13.71341430422406, iter.next());
		assertFeature(WordWeb1TFrequency.MEDIAN_TOKEN_WEB1TFREQUENCY, 14.292260132072075, iter.next());
	}

}

