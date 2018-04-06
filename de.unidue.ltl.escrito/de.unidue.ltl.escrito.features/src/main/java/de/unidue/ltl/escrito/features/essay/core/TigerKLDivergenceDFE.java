package de.unidue.ltl.edu.scoring.features.essay.core;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.TigerXmlReader;

// TODO rausfinden, warum das nicht mehr get


@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class TigerKLDivergenceDFE
	extends KLDivergenceFE_Base
{

	public static final String TIGER_TOKEN_KL_DIVERGENCE = "tigerTokenKLDivergence";
	public static final String TIGER_POS_KL_DIVERGENCE = "tigerPosKLDivergence";

	public static final String PARAM_TIGER_FILE_PATH = "tigerFilePath";
	public static final String TIGER_BIGRAM_KL_DIVERGENCE = "tigerBiGramKLDivergence";
	public static final String TIGER_TRIGRAM_KL_DIVERGENCE = "tigerTriGramKLDivergence";

	@ConfigurationParameter(name = PARAM_TIGER_FILE_PATH, mandatory = true)
	private String tigerFilePath;

	private Collection<Token> backGroundTokens;
	private Collection<Sentence> backGroundSentences;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		backGroundTokens = getTigerTokens(tigerFilePath);
		backGroundSentences = getTigerSentences(tigerFilePath);
		return true;
	}


	// TODO use lemmas/ lower case
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> result = new HashSet<Feature>();
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		result.add(new Feature(TIGER_TOKEN_KL_DIVERGENCE, getTokenDivergence(jcas,tokens,backGroundTokens)));
		result.add(new Feature(TIGER_POS_KL_DIVERGENCE,getPOSDivergence(tokens,backGroundTokens)));
		result.add(new Feature(TIGER_BIGRAM_KL_DIVERGENCE, getNgrammDivergence(sentences, backGroundSentences, 2)));
		result.add(new Feature(TIGER_TRIGRAM_KL_DIVERGENCE, getNgrammDivergence(sentences, backGroundSentences, 3)));


		return result;
	}

	
	/**
	 * Read the tiger corpus and return a collection of tokens
	 * 
	 * @param tigerFilePath
	 * @return
	 */
	private Collection<Token> getTigerTokens(String tigerFilePath) {
		CollectionReader reader;
		JCas jcas = null;
		try {
			reader = createReader(TigerXmlReader.class,
					TigerXmlReader.PARAM_SOURCE_LOCATION, tigerFilePath,
					TigerXmlReader.PARAM_PATTERNS, "*.xml",
					TigerXmlReader.PARAM_LANGUAGE, "de");
			jcas = JCasFactory.createJCas();
			reader.getNext(jcas.getCas());
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}

		return JCasUtil.select(jcas, Token.class);
	}
	
	private Collection<Sentence> getTigerSentences(String tigerFilePath) {
		CollectionReader reader;
		JCas jcas = null;
		try {
			reader = createReader(TigerXmlReader.class,
					TigerXmlReader.PARAM_SOURCE_LOCATION, tigerFilePath,
					TigerXmlReader.PARAM_PATTERNS, "*.xml",
					TigerXmlReader.PARAM_LANGUAGE, "de");
			jcas = JCasFactory.createJCas();
			reader.getNext(jcas.getCas());
		} catch (UIMAException | IOException e) {
			e.printStackTrace();
		}

		return JCasUtil.select(jcas, Sentence.class);
	}

	public void init(String tigerPath) {
		backGroundTokens = getTigerTokens(tigerPath);
	}

}
