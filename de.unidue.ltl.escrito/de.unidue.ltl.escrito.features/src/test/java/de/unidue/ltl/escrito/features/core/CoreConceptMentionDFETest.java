//package de.unidue.ltl.edu.scoring.features.core;
//
//import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
//
//import java.util.Iterator;
//import java.util.Set;
//
//import org.junit.Assert;
//
//import org.apache.uima.analysis_engine.AnalysisEngine;
//import org.apache.uima.jcas.JCas;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.features.util.FeatureUtil;
//import org.dkpro.tc.api.type.TextClassificationTarget;
//import org.junit.Test;
//
//import de.unidue.ltl.edu.scoring.features.essay.core.CoreConceptMentionDFE;
//import de.unidue.ltl.edu.scoring.features.essay.core.NrOfConnectivesDFE;
//
//public class CoreConceptMentionDFETest
//	extends EssayGradingTestBase
//{
//	@Test
//	public void coreConceptMentionFeatureExtractorTest() throws Exception {
//		AnalysisEngine engine = getPreprocessingEngine();
//		JCas jcas = engine.newJCas();
//		jcas.setDocumentLanguage("de");
//		jcas.setDocumentText("Hier gibt es einen \"hidden intellectualism\",einen \"hidden curriculum\" und einen \"heimlicher Lehrplan\".");
//		engine.process(jcas);
//
//		CoreConceptMentionDFE extractor = FeatureUtil.createResource(
//				CoreConceptMentionDFE.class,
//				CoreConceptMentionDFE.PARAM_CORECONCEPTS_FILE_PATH, "src/main/resources/lists/de/coreConcepts_de.txt",
//				NrOfConnectives.PARAM_UNIQUE_EXTRACTOR_NAME, "dummy"
//		);
//		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
//
//		Assert.assertEquals(1, features.size());
//
//		Iterator<Feature> iter = features.iterator();
//		// 3 mentions/21 tokens = 0,14285714285714285714285714285714
//		assertFeature(CoreConceptMentionDFE.NR_OF_MENTIONS, 0.14285714285714285, iter.next());
//		
//	}
//}
