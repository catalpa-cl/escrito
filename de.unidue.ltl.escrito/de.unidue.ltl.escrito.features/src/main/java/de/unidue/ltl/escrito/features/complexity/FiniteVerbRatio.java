package de.unidue.ltl.escrito.features.complexity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/*
 * Number of finite verbs divided by the total number of verbs
 * For English texts: PennTreebank Tagset assumed
 * For German texts: STTS Tagset assumed
 */

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class FiniteVerbRatio
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{
	public static final String FN_FiniteVerbRatio = "FiniteVerbRatio";
	
	
	public static final String PARAM_TAGSET = "tagset_name";
	@ConfigurationParameter(name = PARAM_TAGSET, mandatory = false)
	private String tagset_name;
	
	private String tagset;
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		if (tagset_name != null) {
			tagset = tagset_name.toLowerCase();
		}
		return true;
	}
	
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException
	{
				
		//if no tagset is given, assume STTS for German and PennTreebank for English
		if (tagset == null) {
			if (jcas.getDocumentLanguage() != null) {
				String lang = jcas.getDocumentLanguage();
				if (lang.equals("de")) {
					tagset = "stts";
				} else if (lang.equals("en")) {
					tagset = "ptb";
				}
			} else {
				System.out.println("No Tagset or language specified! Assuming PennTreebank Tagset now!");
				tagset = "ptb";
			}
		}

		int numberOfVerbs = 0;
		int numberOfFiniteVerbs = 0;
		
		//TODO Add further tagsets
		Set<String> finiteVerbTags = new HashSet<String>();
		if (tagset.equals("stts")){
			finiteVerbTags.add("VAFIN");
			finiteVerbTags.add("VMFIN");
			finiteVerbTags.add("VVFIN");
			
		}else if(tagset.equals("ptb")) {
			finiteVerbTags.add("VBD");
			finiteVerbTags.add("VBP");
			finiteVerbTags.add("VBZ");
		}
		
		
		for (POS pos : JCasUtil.select(jcas, POS.class)) {
			//System.out.println(pos.getPosValue());
			if (pos.getCoarseValue().equals("VERB")){
				numberOfVerbs++;
				if(finiteVerbTags.contains(pos.getPosValue())) {
					numberOfFiniteVerbs++;
				}
			}
		}
		
		double finiteRatio = (1.0*numberOfFiniteVerbs)/numberOfVerbs;

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FN_FiniteVerbRatio, finiteRatio, FeatureType.NUMERIC));

		return features;
	}

}

