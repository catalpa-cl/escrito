package de.unidue.ltl.escrito.features.ngrams;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.features.ngram.meta.CharacterNGramMC;


import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class CharNGramsNormalizedFeatureExtractor extends CharacterNGram {

	@Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
         FrequencyDistribution<String> documentCharNgrams = CharacterNGramMC.getAnnotationCharacterNgrams(
                target, ngramLowerCase, ngramMinN, ngramMaxN, '^', '$');

        for (String topNgram : topKSet.getKeys()) {
            if (documentCharNgrams.getKeys().contains(topNgram)) {
            	int numberOfOccurences = (int) documentCharNgrams.getCount(topNgram);
            	int n = topNgram.length();
            	int numberOfNgramsInEssay = jcas.getDocumentText().length()-n+1;	
       //     	System.out.println("Char: \t"+topNgram+"\t"+numberOfOccurences+"\t"+numberOfNgramsInEssay+"\t"+1.0*numberOfOccurences/numberOfNgramsInEssay);
                features.add(new Feature(getFeaturePrefix() + "_normalized_" + topNgram, 1.0*numberOfOccurences/numberOfNgramsInEssay, FeatureType.NUMERIC));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_normalized_" + topNgram, 0, FeatureType.NUMERIC));
            }
        }
        return features;
    }
	
	
	
	
}
