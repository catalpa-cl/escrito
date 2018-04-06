package de.unidue.ltl.escrito.features.length;

import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Extracts the number of sentences in this classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfSentences
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    /**
     * Public name of the feature "number of sentences in this unit"
     */
    public static final String FN_NR_OF_SENTENCES = "NrofSentences";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {
        return new Feature(FN_NR_OF_SENTENCES, JCasUtil.selectCovered(jcas, Sentence.class,
                classificationUnit).size(), FeatureType.NUMERIC).asSet();
    }
}
