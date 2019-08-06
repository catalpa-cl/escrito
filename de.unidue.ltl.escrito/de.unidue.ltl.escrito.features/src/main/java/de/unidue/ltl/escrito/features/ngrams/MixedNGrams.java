package de.unidue.ltl.escrito.features.ngrams;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MixedNGrams extends LuceneMixedNGram {

	
	
	  public static final String PARAM_NORAMLIZED = "normalizeFeature";
	    @ConfigurationParameter(name = PARAM_NORAMLIZED, mandatory = false, defaultValue = "false")
	    protected boolean normalizeFeature;
	
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
			throws TextClassificationException
	{ 
		Set<Feature> features = new HashSet<Feature>();
		FrequencyDistribution<String> documentMixedNgrams = NgramUtils.getDocumentMixedNgrams(jcas, target, ngramMinN,
				ngramMaxN, useCanonicalTags);

		for (String topNgram : topKSet.getKeys()) {
			if (documentMixedNgrams.getKeys().contains(topNgram)) {
				int numberOfOccurences = (int) documentMixedNgrams.getCount(topNgram);
				int n =  StringUtils.countMatches(topNgram, "_");;
				int numberOfNgramsInEssay = JCasUtil.select(jcas, Token.class).size()-n;	
			//	System.out.println("POS: \t"+topNgram+"\t"+numberOfOccurences+"\t"+numberOfNgramsInEssay+"\t"+1.0*numberOfOccurences/numberOfNgramsInEssay);
				if (normalizeFeature){
				features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1.0*numberOfOccurences/numberOfNgramsInEssay, FeatureType.NUMERIC));
				} else {
					features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1.0*numberOfOccurences, FeatureType.NUMERIC));
				}
			}
			else {
				features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true, FeatureType.NUMERIC));
			}
		}
		return features;
	}



}
