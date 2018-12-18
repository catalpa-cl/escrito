package de.unidue.ltl.escrito.examples.basics;

import org.dkpro.lab.task.Dimension;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.core.Constants;

public class FeatureSettings
	implements Constants
{

	/*
	 * Standard Baseline feature set for prompt-specific scoring 
	 */
	public static Dimension<TcFeatureSet> getFeatureSetsDimBaseline()
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
			//			TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(
								WordNGram.class,
								WordNGram.PARAM_NGRAM_MIN_N, 1,
								WordNGram.PARAM_NGRAM_MAX_N, 3,
								WordNGram.PARAM_NGRAM_USE_TOP_K, 10000
								),
						TcFeatureFactory.create(
								CharacterNGram.class,
								CharacterNGram.PARAM_NGRAM_MIN_N, 2,
								CharacterNGram.PARAM_NGRAM_MAX_N, 5,
								CharacterNGram.PARAM_NGRAM_USE_TOP_K, 10000
								)
						)
				);
		return dimFeatureSets;
	}
}
