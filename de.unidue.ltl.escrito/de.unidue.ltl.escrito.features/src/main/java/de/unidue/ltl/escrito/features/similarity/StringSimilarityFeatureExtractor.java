package de.unidue.ltl.edu.scoring.features.similarity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.PairFeatureExtractor;

import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.lexical.string.LevenshteinComparator;
import dkpro.similarity.algorithms.lexical.string.GreedyStringTiling;

public class StringSimilarityFeatureExtractor 
extends FeatureExtractorResource_ImplBase
implements PairFeatureExtractor{

	/**
	 * 
	 * Feature extractor to compute string similarity features features: 
	 * - standard Levenshtein
	 * - Greedy String Tiling
	 * 
	 * TODO make parametrizable which metrics are used
	 * 
	 */


	public static final String FEAT_LEVENSHTEIN = "Levenshtein";	

	public static final String FEAT_GREEDY_STRING_TILING = "GreedyStringTiling";

	// TODO: make this parametrizable
	private int stringTilingMin=3;	
	private int stringTilingMax=7;	



	@Override
	public Set<Feature> extract(JCas view1, JCas view2)
			throws TextClassificationException {
		String text1 = view1.getDocumentText();
		String text2 = view2.getDocumentText();
		//	System.out.println("Comparing "+text1+"\t"+text2);
		Set<Feature> features = new HashSet<Feature>();
		LevenshteinComparator measure = new LevenshteinComparator();
		double levenshteinDistance = -1;
		try {
			levenshteinDistance = measure.getSimilarity(text1, text2);
		} catch (SimilarityException e) {
			// TODO. find out: what kind of exception is this, when does it occur?
			System.err.println("Problem while computing similarity between "+text1+" and "+text2);
			System.exit(-1);
		}
		//	System.out.println(levenshteinDistance);
		features.add(new Feature(FEAT_LEVENSHTEIN, levenshteinDistance));

		for (int minMatchLength = stringTilingMin; minMatchLength <= stringTilingMax; minMatchLength++){
			GreedyStringTiling gst = new GreedyStringTiling(minMatchLength);
			double gst_distance = -1;
			try {
				gst_distance = gst.getSimilarity(text1, text2);
			} catch (SimilarityException e) {
				System.err.println("Problem while computing greedy String Tiling similarity between "+text1+" and "+text2);
				System.exit(-1);
			}
			features.add(new Feature(FEAT_GREEDY_STRING_TILING+"_"+minMatchLength, gst_distance));
		}
		return features;
	}

}
