package de.unidue.ltl.escrito.features.occurence;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.unidue.ltl.escrito.features.occurence.NrOfRutaPatternMatches;





public class RutaFeatureExtractorTest {
	//der klappt nicht wegen dem TextClassificationTarget
	@Test
    public void rutaFeatureExtractorTest_en()
        throws Exception
    {
//		File specFile =new File("src/test/resources/uimaRutaScripts/TierEngine.xml");
//		XMLInputSource in = new XMLInputSource(specFile);
//		ResourceSpecifier spec = UIMAFramework.getXMLParser().parseResourceSpecifier(in);
//		AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(spec);
		
		
		
		AnalysisEngine engine = AnalysisEngineFactory.createEngine("MainEngine");
		
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("I am very utterly happy with the outcome.");
		engine.process(jcas);
//		CAS cas = engine.newCAS();
//		cas.setDocumentText(jcas.getDocumentText());
		ArrayList<String> list = new ArrayList<String>();
		
		
//		for (AnnotationFS tier : CasUtil.select(cas, cas.getTypeSystem().getType("Tier.Tierart"))) {
//			list.add(tier.getCoveredText());
//			System.out.println("Found: " + tier.getCoveredText());
//			}
		
		NrOfRutaPatternMatches extractor = new NrOfRutaPatternMatches();
		Set<Feature> features = extractor.extract(jcas, null);
		assertEquals(1, features.size());
		Feature f = features.iterator().next();
		assertEquals(2, f.getValue());	
    }
	
	//der klappt
//	@Test
//	public void tierTest() throws InvalidXMLException,
//			ResourceInitializationException, IOException,
//			AnalysisEngineProcessException {
//		AnalysisEngine engine = AnalysisEngineFactory
//				.createEngine("TierEngine");
//		CAS cas = engine.newCAS();
//
//		cas.setDocumentText("Ich mag Katzen und Kater");
//		engine.process(cas);
//
//		AnnotationFS[] satz = CasUtil.select(cas,
//				cas.getTypeSystem().getType(NrOfRutaPatternMatches.TIER_TYPE)).toArray(
//				new AnnotationFS[0]);
//		assertEquals(2, satz.length);
//
//		assertEquals("Katzen", satz[0].getCoveredText());
//		assertEquals("Kater", satz[1].getCoveredText());
//	}
	
	
//	@Test
//    public void rutaFeatureExtractorTest_en()
//        throws Exception
//    {
		//AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class));
		//AnalysisEngine engine=createEngine(description);
        		
		//JCas jcas = engine.newJCas();
		//jcas.setDocumentLanguage("en");
		//jcas.setDocumentText("To improve her English, she practised every day.");
		//engine.process(jcas);

		//NrOfCommas extractor = new NrOfCommas();
		//Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

		//Assert.assertEquals(1, features.size());

		//Iterator<Feature> iter = features.iterator();
        //1 comma/ 10 tokens= 0.2
		//assertFeature(NrOfCommas.NR_OF_COMMAS, 0.1, iter.next());
    }
	
	
}
