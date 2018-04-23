package de.unidue.ltl.escrito.features.coherencecohesion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.lexical.string.GreedyStringTiling;

/**
 * calculates an average similarity between all pairs of following sentences
 * uses overlap of nouns and named entities + greeddy string tiling
 * 
 * @author Michael
 * 
 */
@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
		"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class PairwiseSentenceSimilarity 
extends	FeatureExtractorResource_ImplBase 
implements FeatureExtractor {

	public static final String PARAM_NR_OF_SAMPLES = "nrOfSamples";
	@ConfigurationParameter(name = PARAM_NR_OF_SAMPLES, mandatory = true, defaultValue = "5")
	protected int nrOfSampledValues;

	public static final String PAIRWISE_SENTENCE_SIMILARITY_NOUNS = "PairwiseSentenceSimilarityNouns";
	public static final String PAIRWISE_SENTENCE_SIMILARITY_GREEDY_TILE = "GreedyStringTilingSimilarity";
	public static final String PAIRWISE_GREEDY_TILING_SAMPLE_NR = "GreedyStringTilingSampleNr_";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Map<Integer, Double> greedyStringTilingDataPoints = new HashMap<Integer, Double>();
		Map<Integer, Double> greedyStringSampledValues = null;
		// Initialize startPoint TODO change value?
		greedyStringTilingDataPoints.put(0, 0.0);
		int nOfDataPoints = 0;

		GreedyStringTiling measure = new GreedyStringTiling(5);
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		Set<Feature> featList = new HashSet<Feature>();
		double sentenceSimilarity = 0;
		double greedyStringTilingSimilarity = 0;
		List<Sentence> sentenceList = new ArrayList<Sentence>(sentences);
		for (int i = 0; i < sentenceList.size(); i++) {
			if (i < sentences.size() - 1) {
				// comparison based on containing nouns
				sentenceSimilarity += compare(sentenceList.get(i),
						sentenceList.get(i + 1), jcas);
				// comparison based on greedy String Tiling
				try {
					double tempGSTSimilarity = measure.getSimilarity(
							sentenceList.get(i).getCoveredText(), sentenceList
							.get(i + 1).getCoveredText());
					greedyStringTilingSimilarity += tempGSTSimilarity;
					// store all samples
					greedyStringTilingDataPoints.put(nOfDataPoints + 1,
							tempGSTSimilarity);
					nOfDataPoints++;

				} catch (SimilarityException e) {
					e.printStackTrace();
				}
			}
		}
		//Normalization on total count of sentences
		if (sentences.size() > 1) {
			// -1 because we count pairs (last sentence has no partner)
			sentenceSimilarity = sentenceSimilarity / (sentences.size() - 1);
			greedyStringTilingSimilarity = greedyStringTilingSimilarity
					/ (sentences.size() - 1);

			greedyStringSampledValues = sample(greedyStringTilingDataPoints,
					nOfDataPoints);
		}
		//check if there had been at least two sentences
		if (greedyStringSampledValues != null) {
			// add a feature for each sample step
			for (int i : greedyStringSampledValues.keySet()) {
				featList.add(new Feature(PAIRWISE_GREEDY_TILING_SAMPLE_NR + i,
						greedyStringSampledValues.get(i), FeatureType.NUMERIC));
			}
			//put 0.0 else
		}else{
			for (int i=1;i<=nrOfSampledValues;i++){
				featList.add(new Feature(PAIRWISE_GREEDY_TILING_SAMPLE_NR + i,
						0.0, FeatureType.NUMERIC));
			}
		}

		featList.add(new Feature(PAIRWISE_SENTENCE_SIMILARITY_NOUNS,
				sentenceSimilarity, FeatureType.NUMERIC));
		featList.add(new Feature(PAIRWISE_SENTENCE_SIMILARITY_GREEDY_TILE,
				greedyStringTilingSimilarity, FeatureType.NUMERIC));
		return featList;
	}

	/**
	 * 
	 * sample values from given data points. If x of the current step is a direct
	 * match the value is chosen directly. Else the value is interpolated from
	 * the the points in direct neighborhood
	 * 
	 * @param greedyStringTilingDataPoints
	 * @param nrOfDataPoints
	 * @return
	 */
	private Map<Integer, Double> sample(
			Map<Integer, Double> greedyStringTilingDataPoints, int nOfDataPoints) {
		Map<Integer, Double> greedyStringSampledValues = new HashMap<Integer, Double>();
		double sampleStep = (double) (nOfDataPoints) / nrOfSampledValues;
		for (int i = 1; i <= nrOfSampledValues; i++) {
			double currentSampleStep = (double) (i) * sampleStep;
			// check for direct matches --> no interpolation necessary
			if ((currentSampleStep % 1) == 0
					&& greedyStringTilingDataPoints.containsKey(new Double(
							currentSampleStep).intValue())) {
				//				 System.out.println("Direct Match "+greedyStringTilingDataPoints.get(new
				//				 Double(currentSampleStep).intValue()));
				greedyStringSampledValues.put(i, greedyStringTilingDataPoints
						.get(new Double(currentSampleStep).intValue()));
			} else {
				greedyStringSampledValues
				.put(i,
						getLinearInterpolatedValue(
								greedyStringTilingDataPoints,
								currentSampleStep));
				//				 System.out.println("Interpolated "+getLinearInterpolatedValue(greedyStringTilingDataPoints,currentSampleStep));
			}
		}
		return greedyStringSampledValues;
	}

	/**
	 * gets linear interpolated values from points in direct neighborhood of the
	 * current sample step
	 * 
	 * @param greedyStringTilingDataPoints
	 * @param currentSampleStep
	 * @return
	 */
	private Double getLinearInterpolatedValue(
			Map<Integer, Double> greedyStringTilingDataPoints,
			double currentSampleStep) {
		// cut of all decimal places
		int lowPoint = new Double(currentSampleStep).intValue();
		int highPoint = lowPoint + 1;
		double x = currentSampleStep - (double) lowPoint;
		// value of low point+ x*(value of high Point-value of low Point)
		return greedyStringTilingDataPoints.get(lowPoint)
				+ x
				* (greedyStringTilingDataPoints.get(highPoint) - greedyStringTilingDataPoints
						.get(lowPoint));
	}

	/**
	 * computes the overlap of nouns and named entities of two sentences. The
	 * similarity is standardized by the number on nouns
	 * 
	 * @param sentenceA
	 * @param sentenceB
	 * @param jcas
	 * @return sentenceSimilarity
	 */
	private double compare(Sentence sentenceA, Sentence sentenceB, JCas jcas) {
		Collection<Token> tokensA = JCasUtil.selectCovered(jcas, Token.class,
				sentenceA);
		Collection<Token> tokensB = JCasUtil.selectCovered(jcas, Token.class,
				sentenceB);
		double nrOfNounsA = 0;
		double sentenceSimilarity = 0;
		for (Token tokenA : tokensA) {
			//System.out.println(tokenA.getPos().getPosValue());
			if (tokenA.getPos().getPosValue().equals("NN")
					|| tokenA.getPos().getPosValue().equals("NE")
					|| tokenA.getPos().getPosValue().equals("NNS")) {
				nrOfNounsA++;
				for (Token tokenB : tokensB) {
				//	System.out.println(tokenB.getPos().getPosValue());
					if (tokenB.getPos().getPosValue().equals("NN")
							|| tokenB.getPos().getPosValue().equals("NE")
							|| tokenB.getPos().getPosValue().equals("NNS")) {
						if (tokenB.getLemma().getValue()
								.equals(tokenA.getLemma().getValue())) {
							sentenceSimilarity++;
						}
					}
				}
			}
		}
		return sentenceSimilarity / nrOfNounsA;
	}
	/**
	 * for testing only
	 * @param nrOfSampledValues
	 */
	public void init(int nrOfSampledValues) {
		this.nrOfSampledValues = nrOfSampledValues;
	}
}
