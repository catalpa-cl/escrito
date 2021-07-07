package de.unidue.ltl.escrito.features.ngrams;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.PosNGram;
import org.dkpro.tc.features.ngram.meta.PosNGramMC;


import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class PosNGramsNormalizedFeatureExtractor extends PosNGram {

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
			throws TextClassificationException
	{ 
		Set<Feature> features = new HashSet<Feature>();
		FrequencyDistribution<String> documentPOSNgrams = PosNGramMC.getDocumentPosNgrams(jcas, target, ngramMinN,
				ngramMaxN, useCanonicalTags);

		for (String topNgram : topKSet.getKeys()) {
			if (documentPOSNgrams.getKeys().contains(topNgram)) {
				int numberOfOccurences = (int) documentPOSNgrams.getCount(topNgram);
				int n =  StringUtils.countMatches(topNgram, "_");;
				int numberOfNgramsInEssay = JCasUtil.select(jcas, Token.class).size()-n;	
			//	System.out.println("POS: \t"+topNgram+"\t"+numberOfOccurences+"\t"+numberOfNgramsInEssay+"\t"+1.0*numberOfOccurences/numberOfNgramsInEssay);
				if (numberOfNgramsInEssay == 0){
					features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0.0, FeatureType.NUMERIC));
				} else{
					features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1.0*numberOfOccurences/numberOfNgramsInEssay, FeatureType.NUMERIC));
				}
			}
			else {
				features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, FeatureType.NUMERIC));
			}
		}
		return features;
	}


}
