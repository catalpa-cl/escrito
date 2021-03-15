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
 * Lexical Variation: Type-Token-Ratio only for content words (NOUN, VERB, ADJ)
 * Verb Variation: Type-Token-Ratio only for verbs
 */

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LexicalVariation
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{
	public static final String FN_LV = "LexicalVariation";
	public static final String FN_VV = "VerbVariation";


	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException
	{
		//Lexical Variation
		int numberOfContentWords = 0;
		Set<String> contentWordTypes = new HashSet<String>();

		//Verb Variation
		int numberOfVerbs = 0;
		Set<String> verbTypes = new HashSet<String>();


		for (POS pos : JCasUtil.select(jcas, POS.class)) {
					
			if (isContentWord(pos.getCoarseValue())){
				numberOfContentWords++;
				contentWordTypes.add(pos.getCoveredText().toLowerCase());
			}
			if (pos.getCoarseValue().equals("VERB")){
				verbTypes.add(pos.getCoveredText().toLowerCase());
				numberOfVerbs++;
			}
		}

		double lv = (1.0*contentWordTypes.size())/numberOfContentWords;
		double vv = (1.0*verbTypes.size())/numberOfVerbs;

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FN_LV, lv, FeatureType.NUMERIC));
		features.add(new Feature(FN_VV, vv, FeatureType.NUMERIC));

		return features;
	}

	// TODO: Are those all content words? What about adverbs?
	private boolean isContentWord(String coarseValue) {
		if (coarseValue.equals("ADJ") || coarseValue.equals("VERB") || coarseValue.startsWith("N")){
			return true;
		} else {
			return false;
		}
	}
}

