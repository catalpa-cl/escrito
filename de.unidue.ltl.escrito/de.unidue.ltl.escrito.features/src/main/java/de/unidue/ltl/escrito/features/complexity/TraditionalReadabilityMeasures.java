package de.unidue.ltl.escrito.features.complexity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Computes the readability measures ari, coleman_liau, flesch, fog, kincaid, lix and smog as
 * implemented in de.tudarmstadt.ukp.dkpro.core.readability-asl s
 */
@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})
public class TraditionalReadabilityMeasures
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    // having all these parameters is not nice
    // better: several instances of extractor with measure as resource
    public static final String PARAM_ADD_KINCAID = "kincaid";
    @ConfigurationParameter(name = PARAM_ADD_KINCAID, mandatory = false, defaultValue = "true")
    protected boolean kincaid;

    public static final String PARAM_ADD_ARI = "ari";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = false, defaultValue = "true")
    protected boolean ari;

    public static final String PARAM_ADD_COLEMANLIAU = "coleman_liau";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = false, defaultValue = "true")
    protected boolean colemanLiau;

    public static final String PARAM_ADD_FLESH = "flesch";
    @ConfigurationParameter(name = PARAM_ADD_FLESH, mandatory = false, defaultValue = "true")
    protected boolean flesh;

    public static final String PARAM_ADD_FOG = "fog";
    @ConfigurationParameter(name = PARAM_ADD_FOG, mandatory = false, defaultValue = "true")
    protected boolean fog;

    public static final String PARAM_ADD_LIX = "lix";
    @ConfigurationParameter(name = PARAM_ADD_LIX, mandatory = false, defaultValue = "true")
    protected boolean lix;

    public static final String PARAM_ADD_SMOG = "smog";
    @ConfigurationParameter(name = PARAM_ADD_ARI, mandatory = false, defaultValue = "true")
    protected boolean smog;

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {

        ReadabilityMeasures readability = new ReadabilityMeasures();
        Set<Feature> featSet = new HashSet<Feature>();
        if (jcas.getDocumentLanguage() != null) {
            readability.setLanguage(jcas.getDocumentLanguage());
        }

        int nrOfSentences = JCasUtil.select(jcas, Sentence.class).size();
        List<String> words = new ArrayList<String>();
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            words.add(t.getCoveredText());
        }

        // only add features for selected readability measures
        Measures measure;
        if (ari) {
            measure = Measures.valueOf(PARAM_ADD_ARI);
            featSet.add(new Feature(PARAM_ADD_ARI, readability.getReadabilityScore(measure, words,
                    nrOfSentences), FeatureType.NUMERIC));
        }
        if (kincaid) {
            measure = Measures.valueOf(PARAM_ADD_KINCAID);
            featSet.add(new Feature(PARAM_ADD_KINCAID, readability.getReadabilityScore(measure,
                    words, nrOfSentences), FeatureType.NUMERIC));
        }
        if (colemanLiau) {
            measure = Measures.valueOf(PARAM_ADD_COLEMANLIAU);
            featSet.add(new Feature(PARAM_ADD_COLEMANLIAU, readability.getReadabilityScore(
                    measure, words, nrOfSentences), FeatureType.NUMERIC));
        }
        if (flesh) {
            measure = Measures.valueOf(PARAM_ADD_FLESH);
            featSet.add(new Feature(PARAM_ADD_FLESH, readability.getReadabilityScore(measure,
                    words, nrOfSentences), FeatureType.NUMERIC));
        }
        if (fog) {
            measure = Measures.valueOf(PARAM_ADD_FOG);
            featSet.add(new Feature(PARAM_ADD_FOG, readability.getReadabilityScore(measure, words,
                    nrOfSentences), FeatureType.NUMERIC));
        }
        if (smog) {
            measure = Measures.valueOf(PARAM_ADD_SMOG);
            featSet.add(new Feature(PARAM_ADD_SMOG, readability.getReadabilityScore(measure,
                    words, nrOfSentences), FeatureType.NUMERIC));
        }
        if (lix) {
            measure = Measures.valueOf(PARAM_ADD_LIX);
            featSet.add(new Feature(PARAM_ADD_LIX, readability.getReadabilityScore(measure, words,
                    nrOfSentences), FeatureType.NUMERIC));
        }
        return featSet;
    }

}
