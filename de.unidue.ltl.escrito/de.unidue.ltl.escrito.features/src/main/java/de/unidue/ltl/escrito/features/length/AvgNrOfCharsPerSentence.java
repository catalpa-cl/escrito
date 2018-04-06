package de.unidue.ltl.escrito.features.length;

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
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class AvgNrOfCharsPerSentence
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     * Public name of the feature "number of characters"
     */
    public static final String AVG_NR_OF_CHARS_SENTENCE = "avgNumCharsSent";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        double nrOfSentences = JCasUtil.selectCovered(jcas, Sentence.class, target).size();
        double nrOfChars = target.getEnd() - target.getBegin();
        
        return new Feature(AVG_NR_OF_CHARS_SENTENCE, nrOfChars / nrOfSentences, FeatureType.NUMERIC).asSet();
    }
}