package de.unidue.ltl.escrito.features.errors;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import java.util.Iterator;
import java.util.Set;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Assert;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;


public class NumberOfSpellingErrorsTest extends EssayGradingTestBase {

	@Test
	public void spellCheckingFeatureExtractorTest_DE() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",false);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Es ist anonym das Mund-zu-Mund 4-5 & ? ! < > desinterresse der US-Bürger. Test soll passen");
		engine.process(jcas);

		NumberOfSpellingErrors extractor = FeatureUtil.createResource(
				NumberOfSpellingErrors.class,
				NumberOfSpellingErrors.PARAM_DICT_PATH, "src/main/resources/lists/de/germanDictionary_Task1_Task2.txt",
				NumberOfSpellingErrors.PARAM_UNIQUE_EXTRACTOR_NAME, "SpellCheckingDFE"
		);
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());

		Iterator<Feature> iter = features.iterator();
		// 1 error/ 20 tokens= 0.05
		assertFeature(NumberOfSpellingErrors.NR_OF_SPELLINGMISTAKES, 0.05, iter.next());
		
	}
	
	
	
	@Test
	public void spellCheckingFeatureExtractorTest_EN() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en");

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("It's a wonderful day. It keps gettin better.");
		engine.process(jcas);

		NumberOfSpellingErrors extractor = FeatureUtil.createResource(
				NumberOfSpellingErrors.class,
				NumberOfSpellingErrors.PARAM_DICT_PATH, "src/main/resources/lists/en/en_US_dict.txt",
				NumberOfSpellingErrors.PARAM_UNIQUE_EXTRACTOR_NAME, "SpellCheckingDFE"
		);
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(1, features.size());

		Iterator<Feature> iter = features.iterator();
		// 2 errors/ 10 tokens= 0.2
		assertFeature(NumberOfSpellingErrors.NR_OF_SPELLINGMISTAKES, 0.3, iter.next());
		
	}
	
	
	
	
}
