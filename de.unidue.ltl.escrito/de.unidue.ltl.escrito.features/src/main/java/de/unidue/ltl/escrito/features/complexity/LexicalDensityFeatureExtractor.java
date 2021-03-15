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


@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LexicalDensityFeatureExtractor
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{
	public static final String FN_LD = "LexicalDensity";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException
	{

		int numberOfContentWords = 0;

		for (POS pos : JCasUtil.select(jcas, POS.class)) {
	//		System.out.println(pos.getCoarseValue());
			if (isContentWord(pos.getCoarseValue())){
				numberOfContentWords++;
			}
		}
	//	System.out.println(numberOfContentWords);
		double ld = (1.0*numberOfContentWords)/JCasUtil.select(jcas, POS.class).size();
	//	System.out.println(ld);
		
		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FN_LD, ld, FeatureType.NUMERIC));

		return features;
	}

	// TODO: Are those all content words? What about adverbs?
	// TODO: make parametrizable
	private boolean isContentWord(String coarseValue) {
		if (coarseValue.equals("ADJ") || coarseValue.equals("VERB") || coarseValue.startsWith("N")){
			return true;
		} else {
			return false;
		}
	}
}


