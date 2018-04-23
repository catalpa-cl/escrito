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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Extracts the average number of characters per sentence
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})
public class AvgNrOfCharsPerSentence
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     * Public name of the feature "number of characters"
     */
    public static final String AVG_NR_OF_CHARS_SENTENCE = "avgNumCharsSent";
    public static final String STANDARD_DEVIATION_OF_CHARS_PER_SENTENCE = "standardDevCharsPerSentence";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException{
    	Set<Feature> featureList = new HashSet<Feature>();
    	Collection<Sentence> sentences = JCasUtil.selectCovered(jcas, Sentence.class,target);
        double nrOfSentences = sentences.size();
        double sumOfChars = 0;
        for(Sentence s:sentences){
        	double sentenceLength = s.getEnd()-s.getBegin();
        	sumOfChars+=sentenceLength;
        }
        double avgSize = sumOfChars / nrOfSentences;
        
        double varianceSum = 0;
        for(Sentence s:sentences){
        	double sentenceLength = s.getEnd()-s.getBegin();
        	double deviation = sentenceLength-avgSize;
        	varianceSum+=Math.pow(deviation,2);
        }
        double stndDeviation = Math.sqrt(varianceSum/nrOfSentences);
        
        
        featureList.add(new Feature(AVG_NR_OF_CHARS_SENTENCE, avgSize, FeatureType.NUMERIC));
        featureList.add(new Feature(STANDARD_DEVIATION_OF_CHARS_PER_SENTENCE,stndDeviation,FeatureType.NUMERIC));
        
        return featureList;
    }
}