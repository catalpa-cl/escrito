package de.unidue.ltl.escrito.features.length;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Extracts the average number of tokens per sentence in the classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class AvgNrOfTokensPerSentence
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{

	/**
	 * Public name of the feature "number of tokens per sentence" in this classification unit
	 */
	public static final String FN_TOKENS_PER_SENTENCE = "NrofTokensPerSentence";
	public static final String STANDARD_DEVIATION_OF_TOKENS_PER_SENTENCE = "standardDevTokensPerSentence";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
			throws TextClassificationException
	{
		Set<Feature> featSet = new HashSet<Feature>();
		Collection<Sentence> sentences = JCasUtil.selectCovered(jcas, Sentence.class, classificationUnit);
		double numSentences = sentences.size();
		double numTokens = JCasUtil.selectCovered(jcas, Token.class, classificationUnit).size();
		double avgSize = numTokens/numSentences;
		
		double varianceSum = 0;
		for(Sentence s : sentences){
			double nrOfTokens = JCasUtil.selectCovered(jcas, Token.class, s).size();
			double deviation = nrOfTokens-avgSize;
			varianceSum +=Math.pow(deviation,2);
		}
		double stndDeviation = Math.sqrt(varianceSum/numSentences);

		featSet.add(new Feature(FN_TOKENS_PER_SENTENCE, avgSize, FeatureType.NUMERIC));
		//System.out.println(avgSize);
		featSet.add(new Feature(STANDARD_DEVIATION_OF_TOKENS_PER_SENTENCE, stndDeviation,FeatureType.NUMERIC));
		return featSet;
	}
}
