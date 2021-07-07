package de.unidue.ltl.escrito.features.occurence;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Iterator;
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
import de.unidue.ltl.escrito.features.occurence.SpeechThoughtWritingRepresentation;

public class SpeechThoughtWritingRepresentationDFETest extends EssayGradingTestBase{
	@Test 
	public void speechThoughtWritingRepresentationTest_DE() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",ParserType.noParser);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		// Prepositions trotz nach zur während vor
		jcas.setDocumentText("Heinz fragt, ob er schlau ist! Sie sprachen vom Dummsein. " +
				"Karin denkt:\"Heinz denkt, dass er schlau ist.\" Sie befahl uns zu arbeiten. " +
				"Heinz meinte, er sei dumm");
		engine.process(jcas);

		SpeechThoughtWritingRepresentation extractor = FeatureUtil.createResource(
				SpeechThoughtWritingRepresentation.class,
				SpeechThoughtWritingRepresentation.PARAM_LANGUAGE,"de",
				SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH,"src/main/resources/lists/de/reporting_verbs_krestel_de.txt",
				SpeechThoughtWritingRepresentation.PARAM_UNIQUE_EXTRACTOR_NAME, "SpeechThoughtWritingRepresentationDFE"
        );
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(3, features.size());

		// 1 direct representation/ 5 sentences= 0.2
		assertFeatures(SpeechThoughtWritingRepresentation.NR_OF_DIRECT_REPRESENTATION, 0.2, features, 0.0001);
		// 3 indirect representation/5 sentences = 0.8
		assertFeatures(SpeechThoughtWritingRepresentation.NR_OF_INDIRECT_REPRESENTATION, 0.6, features, 0.0001);
		// 1 reported representation/5 sentences = 0.2
		assertFeatures(SpeechThoughtWritingRepresentation.NR_OF_REPORTED_REPRESENTATION, 0.2, features, 0.0001);
	}
	
	
	@Test 
	public void speechThoughtWritingRepresentationDFETest_EN() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("en",ParserType.noParser);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		// Prepositions trotz nach zur während vor
		jcas.setDocumentText("Heinz asks how smart he is! They talked about stupidness. " +
				"Karin thinks:\"Heinz thinks that he is smart.\" They ordered us to work. " +
				"Heinz told he would be stupid");
		engine.process(jcas);

		SpeechThoughtWritingRepresentation extractor = FeatureUtil.createResource(
				SpeechThoughtWritingRepresentation.class,
				SpeechThoughtWritingRepresentation.PARAM_LANGUAGE,"en",
				SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH,"src/main/resources/lists/en/reporting_verbs_krestel_en.txt",
				SpeechThoughtWritingRepresentation.PARAM_UNIQUE_EXTRACTOR_NAME, "SpeechThoughtWritingRepresentationDFE"
        );
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		Assert.assertEquals(3, features.size());

		//TODO: errors are detected in some wrong way NR_OF_INDIRECT_REPRESENTATION vs NR_OF_REPORTED_REPRESENTATION
		Iterator<Feature> iter = features.iterator();
		// 1 direct representations/ 5 sentences= 0.2
		assertFeature(SpeechThoughtWritingRepresentation.NR_OF_DIRECT_REPRESENTATION, 0.2, iter.next());
		// 3 indirect representations/5 sentences = 0.6
		assertFeature(SpeechThoughtWritingRepresentation.NR_OF_INDIRECT_REPRESENTATION, 0.6, iter.next());
		// 2 reported representations/5 sentences = 0.4
		assertFeature(SpeechThoughtWritingRepresentation.NR_OF_REPORTED_REPRESENTATION, 0.4, iter.next());
		
	}
}
