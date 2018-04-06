package de.unidue.ltl.escrito.features.length;

import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Extracts the number of characters in the unit (basically, its length).
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfChars
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     * Public name of the feature "number of characters"
     */
    public static final String NR_OF_CHARS = "NrofChars";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        int nrOfChars = target.getEnd() - target.getBegin();
        return new Feature(NR_OF_CHARS, nrOfChars, FeatureType.NUMERIC).asSet();
    }
}