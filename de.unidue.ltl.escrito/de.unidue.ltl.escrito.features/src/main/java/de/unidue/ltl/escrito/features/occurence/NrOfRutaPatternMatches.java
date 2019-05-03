package de.unidue.ltl.escrito.features.occurence;

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

/**
 * Counts the appearance commas
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
	
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String,Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		// TODO initialize pattern
		return true;
	}
	
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		// TODO
		int numberOfMatches = 0;
		return new Feature(NR_OF_MATCHES+"_"+featureName, numberOfMatches, FeatureType.NUMERIC).asSet();
	}
}
