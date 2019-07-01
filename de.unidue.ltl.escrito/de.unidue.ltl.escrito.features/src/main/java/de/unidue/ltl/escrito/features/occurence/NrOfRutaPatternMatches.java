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
    public static String TIER_TYPE = "Tier.Tierart";
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
		
		AnalysisEngine engine = null;
		try {
			engine = AnalysisEngineFactory.createEngine("uimaRutaScripts.TierEngine");
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
	
		CAS cas = null;
		try {
			cas = engine.newCAS();
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
		
		
		for (AnnotationFS tier : CasUtil.select(cas, cas.getTypeSystem().getType("Tier.Tierart"))) {
			list.add(tier.getCoveredText());
			System.out.println("Found: " + tier.getCoveredText());
		}
		System.out.println("Anzahl: " + list.size());
		
		int numberOfMatches = list.size();
		return new Feature(NR_OF_MATCHES+"_"+featureName, numberOfMatches, FeatureType.NUMERIC).asSet();
	}
}
