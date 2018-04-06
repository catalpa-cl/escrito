package de.unidue.ltl.edu.scoring.features.general;



import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

import static de.unidue.ltl.edu.scoring.features.general.DependencyUtils.getDocumentDependencies;

public class DependencyMetaCollector extends LuceneBasedMetaCollector {

	// TODO: Trees of differents sizes? Maybe later
	/*@ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int ngramMinN;

	@ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	private int ngramMaxN;*/

	@ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
	private String ngramStopwordsFile;

	@ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
	private boolean filterPartialStopwordMatches;

	@ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
	private String stringNgramLowerCase;

	boolean ngramLowerCase = true;

	private Set<String> stopwords;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);

		ngramLowerCase = Boolean.valueOf(stringNgramLowerCase);

		try {
			stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected FrequencyDistribution<String> getNgramsFD(JCas jcas) throws TextClassificationException{

		TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());

		FrequencyDistribution<String> fd = null;
		//	fd = NGramUtils.getDocumentNgrams(jcas, fullDoc, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
		fd = getDocumentDependencies(jcas, fullDoc, ngramLowerCase);
		System.out.println("Found "+fd.getKeys().size()+" triples");
		return fd;
	}


	@Override
	protected String getFieldName(){
		return "dependency_" + featureExtractorName;
	}

}
