package de.unidue.ltl.escrito.features.ngrams;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.LuceneMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class LuceneMixedNGramMetaCollector
    extends LuceneMC
{
    public static final String LUCENE_POS_NGRAM_FIELD = "posngram";
    
    @ConfigurationParameter(name = LuceneMixedNGram.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = LuceneMixedNGram.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = LuceneMixedNGram.PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonical;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        return NgramUtils.getDocumentMixedNgrams(jcas, fullDoc, ngramMinN, ngramMaxN,
                useCanonical);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_POS_NGRAM_FIELD + featureExtractorName;
    }
    
    
  
    
   
    
}