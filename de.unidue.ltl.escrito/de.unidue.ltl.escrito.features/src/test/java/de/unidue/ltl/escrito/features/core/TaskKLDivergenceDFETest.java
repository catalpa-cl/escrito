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
//import org.apache.uima.analysis_engine.AnalysisEngine;
//import org.apache.uima.collection.CollectionReader;
//import org.apache.uima.fit.factory.JCasFactory;
//import org.apache.uima.jcas.JCas;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.type.TextClassificationTarget;
//import org.junit.Test;
//
//import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
//import de.tudarmstadt.ukp.dkpro.core.io.tiger.TigerXmlReader;
//import de.unidue.ltl.edu.scoring.features.essay.core.TaskKLDivergenceDFE;
//
//public class TaskKLDivergenceDFETest extends EssayGradingTestBase {
//	
//	//TODO: fix
//	
////	@Test
////	public void taskKLDivergenceFETest() throws Exception {
////		
////		CollectionReader reader = createReader(TextReader.class,
////				TigerXmlReader.PARAM_SOURCE_LOCATION, "src/main/resources/originalTexts/01_Texte_der_2._Nachcodierung",
////				TigerXmlReader.PARAM_PATTERNS, "*.txt",
////				TigerXmlReader.PARAM_LANGUAGE, "de");
////		JCas jcas = JCasFactory.createJCas();
////		reader.getNext(jcas.getCas());
////		
////		AnalysisEngine engine = getPreprocessingEngine();
////		engine.process(jcas);
////
////		TaskKLDivergenceDFE extractor = new TaskKLDivergenceDFE();
////		extractor.init("src/main/resources/originalTexts/01_Texte_der_2._Nachcodierung");
////		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
////
////		Assert.assertEquals(4, features.size());
////
////		Iterator<Feature> iter = features.iterator();
////		assertFeature(TaskKLDivergenceDFE.TASK_TOKEN_KL_DIVERGENCE, 0.0, iter.next());
////		assertFeature(TaskKLDivergenceDFE.TASK_POS_KL_DIVERGENCE, 0.0, iter.next());
////		assertFeature(TaskKLDivergenceDFE.TASK_BIGRAM_KL_DIVERGENCE, 0.0, iter.next());
////		assertFeature(TaskKLDivergenceDFE.TASK_TRIGRAM_KL_DIVERGENCE, 0.0, iter.next());
////	}
//}
