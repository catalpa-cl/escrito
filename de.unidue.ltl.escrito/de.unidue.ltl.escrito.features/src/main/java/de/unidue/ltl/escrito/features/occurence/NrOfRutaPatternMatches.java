package de.unidue.ltl.escrito.features.occurence;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.ruta.engine.RutaEngine;
import org.apache.uima.util.InvalidXMLException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

//import Tier.Tierart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.CasUtil;


/**
 * Counts the appearance of a certain pattern
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})
public class NrOfRutaPatternMatches extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String NR_OF_MATCHES = "nrOfMatches";
	
	public static final String PARAM_PATTERN_FILE_PATH = "reportingVerbsFilePath";
    @ConfigurationParameter(name = PARAM_PATTERN_FILE_PATH, mandatory = true)
    private String patternFilePath;
    
    public static final String PARAM_FEATURE_NAME = "featureName";
    @ConfigurationParameter(name = PARAM_FEATURE_NAME, mandatory = true)
    private String featureName;
	
    ArrayList<String> list = new ArrayList<String>();
	ArrayList<String> annos = new ArrayList<String>();
	
    public static String TIER_TYPE = "Tier.Tierart";
	public static String ANNO_TYPE1 = "AmplifierWord.Amplifier";
	public static String ANNO_TYPE2 = "DowntonerWord.Downtoner";
	public static String ANNO_TYPE3 = "PronounWord.ItPronoun";
	public static String ANNO_TYPE4 = "BeWord.BeAsMainVerb";
	public static String ANNO_TYPE9 = "HedgeWord.Hedge";
	public static String ANNO_TYPE10 = "EmphaticWord.Emphatic";
	public static String ANNO_TYPE11 = "DemonstrativeWord.Demonstrative";
	public static String ANNO_TYPE12 = "PassivesWord.AgentlessPassive";
	public static String ANNO_TYPE13 = "PassivesWord.ByPassive";
	public static String ANNO_TYPE14 = "CausativeAdverbialSubordinatorWord.CausativeAdverbialSubordinator";
	public static String ANNO_TYPE15 = "ConcessiveAdverbialSubordinatorWord.ConcessiveAdverbialSubordinator";
	public static String ANNO_TYPE16 = "ConditionalAdverbialSubordinatorWord.ConditionalAdverbialSubordinator";
	public static String ANNO_TYPE17 = "OtherAdverbialSubordinatorWord.OtherAdverbialSubordinator";
	public static String ANNO_TYPE18 = "ThatComplementWord.ThatAdjectiveComplement";
	public static String ANNO_TYPE19 = "ThatComplementWord.ThatVerbComplement";
	public static String ANNO_TYPE20 = "InfinitiveWord.Infinitive";
	public static String ANNO_TYPE21 = "WHClauseWord.WH_Clause";
	

	@Override
	
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String,Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		// TODO initialize pattern
		
		
		return true;
	}
	
	//http://www.apache.org/dist/uima/eclipse-update-site/
	//-> uima ruta installation
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		// TODO
		//Annotationen laden
		annos.add(ANNO_TYPE1);
		annos.add(ANNO_TYPE2);
		annos.add(ANNO_TYPE3);
		annos.add(ANNO_TYPE4);
		annos.add(ANNO_TYPE9);
		annos.add(ANNO_TYPE10);
		annos.add(ANNO_TYPE11);
		annos.add(ANNO_TYPE12);
		annos.add(ANNO_TYPE13);
		annos.add(ANNO_TYPE14);
		annos.add(ANNO_TYPE15);
		annos.add(ANNO_TYPE16);
		annos.add(ANNO_TYPE17);
		annos.add(ANNO_TYPE18);
		annos.add(ANNO_TYPE19);
		annos.add(ANNO_TYPE20);
		annos.add(ANNO_TYPE21);
		
		AnalysisEngine engine = null;
		try {
			engine = AnalysisEngineFactory.createEngine("uimaRutaScripts.MainEngine");
		} catch (InvalidXMLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ResourceInitializationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		JCas cas = null;
		try {
			cas = engine.newJCas();
		} catch (ResourceInitializationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
		ArrayList<String> list = new ArrayList<String>();
		cas.setDocumentText(jcas.getDocumentText());
		try {
			engine.process(cas);
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//for (AnnotationFS found : JCasUtil.select(cas, Tierart.class)) {
		//for (AnnotationFS tier : JCasUtil.select(cas, cas.getTypeSystem().getType("Tier.Tierart"))) {
		for (String annotation : annos) {
			for (AnnotationFS found : JCasUtil.select(cas, annotation)) {
				list.add(found.getCoveredText());
				System.out.println("Found: " + found.getCoveredText());
			}
		}
		System.out.println("Anzahl: " + list.size());
		
		int numberOfMatches = list.size();
		return new Feature(NR_OF_MATCHES+"_"+featureName, numberOfMatches, FeatureType.NUMERIC).asSet();
	}
}
