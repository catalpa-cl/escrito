package de.unidue.ltl.escrito.features.complexity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
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
 * Ratio of a particular POS to total number of tokens
 * Currently implemented: Noun ratio, verb ratio
 */

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class POSTokenRatio
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{
	public static final String FN_NounRatio = "NounRatio";
	public static final String FN_VerbRatio = "VerbRatio";
	public static final String FN_AdjectivRatio = "AdjectivRatio";
	

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException
	{
		
		int numberOfTokens = JCasUtil.select(jcas, POS.class).size();
		
		//Nouns, Verbs
		int numberOfNouns = 0;
		int numberOfVerbs = 0;
		int numberOfAdjectives = 0;
		
		for (POS pos : JCasUtil.select(jcas, POS.class)) {
			System.out.println(pos.getCoarseValue());
			if (pos.getCoarseValue().startsWith("N")){
				numberOfNouns++;
			}
			if (pos.getCoarseValue().equals("VERB")){
				numberOfVerbs++;
			}
			if (pos.getCoarseValue().equals("VERB")){
				numberOfAdjectives++;
			}
		}

		
		double nr = (1.0*numberOfNouns)/numberOfTokens;
		double vr = (1.0*numberOfVerbs)/numberOfTokens;
		double ar = (1.0*numberOfAdjectives)/numberOfTokens;

		
		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FN_NounRatio, nr, FeatureType.NUMERIC));
		features.add(new Feature(FN_VerbRatio, vr, FeatureType.NUMERIC));
		features.add(new Feature(FN_AdjectivRatio, ar, FeatureType.NUMERIC));
		return features;
	}

}

