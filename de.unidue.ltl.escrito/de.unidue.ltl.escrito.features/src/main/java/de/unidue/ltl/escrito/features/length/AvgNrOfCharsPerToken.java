package de.unidue.ltl.escrito.features.length;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Extracts the average number of characters per sentence
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class AvgNrOfCharsPerToken
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     * Public name of the feature "number of characters"
     */
    public static final String AVG_NR_OF_CHARS_PER_TOKEN = "avgNumCharsToken";
    public static final String STANDARD_DEVIATION_OF_CHARS_PER_TOKEN = "standardDevCharsPerToken";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> featList = new HashSet<Feature>();
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		double numOfTokens = 0;
		double tempSize = 0;

		// Calculate AvgLength
		for (Token token : tokens) {
			// ignore puctuation marks
			if (token.getPos() == null){
				System.err.println("No POS for token "+token.getCoveredText()+" in essay "+jcas.getDocumentText().substring(0, 100));
			} else {
				if (!token.getPos().getPosValue().equals("$.")&& !token.getPos().getPosValue().equals(".")) {
					tempSize += token.getCoveredText().length();
					numOfTokens++;
				}
			}
		}
		//Normalization on total count of words
		double avgSize = tempSize / numOfTokens;

		// set temp to 0 again
		tempSize = 0;

		// Calculate stndDeviation
		for (Token token : tokens) {
			// ignore puctuation marks
			if (token.getPos() == null){
				System.err.println("No POS for token "+token.getCoveredText()+" in essay "+jcas.getDocumentText().substring(0, 100));
			} else {
				if (!token.getPos().getPosValue().equals("$.")&& !token.getPos().getPosValue().equals(".")) {
					double tempAdd = token.getCoveredText().length() - avgSize;
					tempSize += Math.pow(tempAdd, 2);
				}
			}
		}

		double stndDeviation = Math.sqrt(tempSize / numOfTokens);
		featList.add(new Feature(AVG_NR_OF_CHARS_PER_TOKEN, avgSize, FeatureType.NUMERIC));
		featList.add(new Feature(STANDARD_DEVIATION_OF_CHARS_PER_TOKEN,
				stndDeviation, FeatureType.NUMERIC));
		return featList;
	}

}
