package de.unidue.ltl.escrito.features.occurence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * counts appearance of specified patterns like the structure of quotes or line indication
 * @author Michael
 *
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NumberOfQuotations 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor {

	public static final String NR_OF_QUOTES = "nrOfQuotes";
	public static final String NR_OF_LINEINDICATORS = "nrOfLineIndicators";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> featList = new HashSet<Feature>();
		
		//patterns
		Pattern quoteRegexPattern = Pattern.compile("\".*?\"");
		Pattern lineIndicatorRegexPattern = Pattern.compile("Z(eile)?(\\s)?\\.[0-9]+");
		//end of patterns
		
		double nrOfQuotes = 0;
		double nrOfLineIndicators = 0;
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		for (Sentence sentence : sentences) {
			Matcher quoteMatcher = quoteRegexPattern.matcher(sentence
					.getCoveredText());
			while (quoteMatcher.find()) {
				nrOfQuotes++;
			}
			Matcher lineIndicatorMatcher = lineIndicatorRegexPattern
					.matcher(sentence.getCoveredText());
			while (lineIndicatorMatcher.find()) {
			//	System.out.println("Found: "+lineIndicatorMatcher.group(0));
				nrOfLineIndicators++;
			}
		}
		//Normalization on total count of sentences
//		System.out.println("nrofQuotes: "+nrOfQuotes+"    nrOfLineIndicators: "+nrOfLineIndicators);
		nrOfQuotes = nrOfQuotes / sentences.size();
		nrOfLineIndicators = nrOfLineIndicators / sentences.size();
		featList.add(new Feature(NR_OF_QUOTES, nrOfQuotes, FeatureType.NUMERIC));
		featList.add(new Feature(NR_OF_LINEINDICATORS, nrOfLineIndicators, FeatureType.NUMERIC));
		return featList;
	}

}
