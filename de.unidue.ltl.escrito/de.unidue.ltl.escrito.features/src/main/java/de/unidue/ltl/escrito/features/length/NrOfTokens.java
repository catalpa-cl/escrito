package de.unidue.ltl.escrito.features.length;

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
 * Extracts the number of tokens in the classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfTokens
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    /**
     * Public name of the feature "number of tokens" in this classification unit
     */
    public static final String FN_NR_OF_TOKENS = "NrofTokens";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> featList = new HashSet<Feature>();

        double numTokens = JCasUtil.selectCovered(jcas, Token.class, target).size();

        featList.add(new Feature(FN_NR_OF_TOKENS, numTokens, FeatureType.NUMERIC));
        return featList;
    }
}
