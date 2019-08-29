package de.unidue.ltl.escrito.features.ngrams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts POS n-grams.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneMixedNGram
    extends org.dkpro.tc.features.ngram.meta.base.LuceneFeatureExtractorBase
    implements FeatureExtractor
{

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    protected boolean useCanonicalTags;

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {

        Set<Feature> features = new HashSet<Feature>();
        FrequencyDistribution<String> documentPOSNgrams = null;
        documentPOSNgrams = NgramUtils.getDocumentMixedNgrams(view, classificationUnit, ngramMinN,
                ngramMaxN, useCanonicalTags);

        for (String topNgram : topKSet.getKeys()) {
            if (documentPOSNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1, FeatureType.NUMERIC));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true, FeatureType.NUMERIC));
            }
        }
        return features;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
                throws ResourceInitializationException
    {
        return Arrays.asList(
                new MetaCollectorConfiguration(LuceneMixedNGramMetaCollector.class, parameterSettings)
                        .addStorageMapping(LuceneMixedNGramMetaCollector.PARAM_TARGET_LOCATION,
                                LuceneMixedNGram.PARAM_SOURCE_LOCATION,
                                LuceneMixedNGramMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return LuceneMixedNGramMetaCollector.LUCENE_POS_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "mixedngram";
    }
    
    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}
