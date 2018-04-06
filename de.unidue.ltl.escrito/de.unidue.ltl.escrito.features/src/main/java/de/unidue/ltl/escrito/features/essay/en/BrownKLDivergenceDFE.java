package de.unidue.ltl.edu.scoring.features.essay.en;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.ltl.edu.scoring.features.essay.core.KLDivergenceFE_Base;

public class BrownKLDivergenceDFE 
	extends KLDivergenceFE_Base
{

	public static final String BROWN_TOKEN_KL_DIVERGENCE = "brownTokenKLDivergence";
	public static final String BROWN_POS_KL_DIVERGENCE = "brownPosKLDivergence";

	public static final String BROWN_BIGRAM_KL_DIVERGENCE = "brownBiGramKLDivergence";
	public static final String BROWN_TRIGRAM_KL_DIVERGENCE = "brownTriGramKLDivergence";
	
	public static final String PARAM_BROWN_FILE_PATH = "brownFilePath";
	
	@ConfigurationParameter(name = PARAM_BROWN_FILE_PATH, mandatory = true)
	private String brownFilePath;
	
	private Collection<Token> backGroundTokens;
	private Collection<Sentence> backGroundSentences;
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		backGroundTokens = getBrownTokens(brownFilePath);
		backGroundSentences = getBrownSentences(brownFilePath);
		return true;
	}
	
	private Collection<Token> getBrownTokens(String brownFilePath) {
		CollectionReader reader;
		JCas jcas = null;
		try {
			reader = createReader(
	                TeiReader.class,
	                TeiReader.PARAM_SOURCE_LOCATION, brownFilePath,
	                TeiReader.PARAM_PATTERNS, new String[] {
	                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"});
			jcas = JCasFactory.createJCas();
			reader.getNext(jcas.getCas());
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}

		return JCasUtil.select(jcas, Token.class);
	}
	
	private Collection<Sentence> getBrownSentences(String brownFilePath) {
		CollectionReader reader;
		JCas jcas = null;
		try {
			reader = createReader(
	                TeiReader.class,
	                TeiReader.PARAM_SOURCE_LOCATION, brownFilePath,
	                TeiReader.PARAM_PATTERNS, new String[] {
	                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"});
			jcas = JCasFactory.createJCas();
			reader.getNext(jcas.getCas());
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}

		return JCasUtil.select(jcas, Sentence.class);
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> result = new HashSet<Feature>();
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		result.add(new Feature(BROWN_TOKEN_KL_DIVERGENCE, getTokenDivergence(jcas,tokens,backGroundTokens)));
		result.add(new Feature(BROWN_POS_KL_DIVERGENCE,getPOSDivergence(tokens,backGroundTokens)));
		result.add(new Feature(BROWN_BIGRAM_KL_DIVERGENCE, getNgrammDivergence( sentences,backGroundSentences, 2)));
		result.add(new Feature(BROWN_TRIGRAM_KL_DIVERGENCE, getNgrammDivergence( sentences, backGroundSentences, 3)));

		return result;
	}

}
