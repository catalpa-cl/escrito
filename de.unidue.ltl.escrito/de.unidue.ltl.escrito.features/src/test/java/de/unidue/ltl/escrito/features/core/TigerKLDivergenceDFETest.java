//package de.unidue.ltl.edu.scoring.features.core;
//
//import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
//import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
//
//import java.util.Iterator;
//import java.util.Set;
//
//import org.junit.Assert;
//
//import org.apache.uima.collection.CollectionReader;
//import org.apache.uima.fit.factory.JCasFactory;
//import org.apache.uima.jcas.JCas;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.type.TextClassificationTarget;
//import org.junit.Test;
//
//import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
//import de.tudarmstadt.ukp.dkpro.core.io.tiger.TigerXmlReader;
//import de.unidue.ltl.edu.scoring.features.essay.core.TigerKLDivergenceDFE;
//
//public class TigerKLDivergenceDFETest
//	extends EssayGradingTestBase
//{
//	@Test
//	public void tigerKLDivergenceFETest() throws Exception {
//		
//		CollectionReader reader = createReader(TigerXmlReader.class,
//				TigerXmlReader.PARAM_SOURCE_LOCATION, DkproContext.getContext().getWorkspace("corpora").getAbsolutePath()+"/tiger",
//				TigerXmlReader.PARAM_PATTERNS, "*.xml",
//				TigerXmlReader.PARAM_LANGUAGE, "de");
//		JCas jcas = JCasFactory.createJCas();
//		reader.getNext(jcas.getCas());
////		AnalysisEngine engine = getPreprocessingEngine();
////		engine.process(jcas);
//
//		TigerKLDivergenceDFE extractor = new TigerKLDivergenceDFE();
//		extractor.init("src/main/resources");
//		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
//
//		Assert.assertEquals(4, features.size());
//
//		Iterator<Feature> iter = features.iterator();
//		assertFeature(TigerKLDivergenceDFE.TIGER_TOKEN_KL_DIVERGENCE, 0.0, iter.next());
//		assertFeature(TigerKLDivergenceDFE.TIGER_POS_KL_DIVERGENCE, 0.0, iter.next());
//		assertFeature(TigerKLDivergenceDFE.TIGER_BIGRAM_KL_DIVERGENCE, 0.0, iter.next());
//		assertFeature(TigerKLDivergenceDFE.TIGER_TRIGRAM_KL_DIVERGENCE, 0.0, iter.next());
//	}
//}
