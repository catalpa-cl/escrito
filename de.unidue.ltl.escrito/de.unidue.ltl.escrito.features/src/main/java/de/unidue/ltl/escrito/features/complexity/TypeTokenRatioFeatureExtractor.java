package de.unidue.ltl.escrito.features.complexity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TypeTokenRatioFeatureExtractor
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{
	public static final String FN_TTR = "TypeTokenRatio";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException
	{

		FrequencyDistribution<String> fd = new FrequencyDistribution<String>();

		for (Token token : JCasUtil.select(jcas, Token.class)) {
			fd.inc(token.getCoveredText().toLowerCase());
		}
		double ttr = 0.0;
		if (fd.getN() > 0) {
			ttr = (double) fd.getB() / fd.getN();
		}

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FN_TTR, ttr, FeatureType.NUMERIC));

		return features;
	}
}


