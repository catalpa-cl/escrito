package de.unidue.ltl.edu.scoring.features.essay.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AggregateBuilder;
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
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.TigerXmlReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class TaskKLDivergenceDFE
extends  KLDivergenceFE_Base
{
	public static final String TASK_TOKEN_KL_DIVERGENCE = "taskTokenKLDivergence";
	public static final String TASK_POS_KL_DIVERGENCE = "taskPosKLDivergence";

	public static final String PARAM_TASK_FILE_PATH = "taskFilePath";
	public static final String TASK_BIGRAM_KL_DIVERGENCE = "taskBiGramKLDivergence";
	public static final String TASK_TRIGRAM_KL_DIVERGENCE = "taskTriGramKLDivergence";

	@ConfigurationParameter(name = PARAM_TASK_FILE_PATH, mandatory = true)
	private String taskFilePath;

	private Collection<Token> backGroundTokens;
	private Collection<Sentence> backGroundSentences;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams) 
			throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			getTaskTokensAndSentences(taskFilePath);
		} catch (UIMAException | IOException e) {
			throw new ResourceInitializationException(e);
		}
		return true;
	}

	// TODO use lemmas/ lower case
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> result = new HashSet<Feature>();
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		result.add(new Feature(TASK_TOKEN_KL_DIVERGENCE, getTokenDivergence(jcas,tokens,backGroundTokens)));
		// TODO, aktuell haben wir keine POS tags!
		result.add(new Feature(TASK_POS_KL_DIVERGENCE, getPOSDivergence(tokens,backGroundTokens)));
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		result.add(new Feature(TASK_BIGRAM_KL_DIVERGENCE, getNgrammDivergence(sentences, backGroundSentences, 2)));
		result.add(new Feature(TASK_TRIGRAM_KL_DIVERGENCE, getNgrammDivergence(sentences, backGroundSentences, 3)));
		return result;
	}

	/**
	 * Read the tiger corpus and return a collection of tokens
	 * 
	 * @param taskFilePath
	 * @return
	 * @throws UIMAException 
	 * @throws IOException 
	 */
	private void getTaskTokensAndSentences(String taskFilePath) 
			throws UIMAException, IOException
	{
		CollectionReader reader = createReader(TextReader.class,
				TigerXmlReader.PARAM_SOURCE_LOCATION, taskFilePath,
				TigerXmlReader.PARAM_PATTERNS, "*.txt",
				TigerXmlReader.PARAM_LANGUAGE, "de");
		JCas jcas = JCasFactory.createJCas();
		reader.getNext(jcas.getCas());
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createEngineDescription(
					createEngineDescription(BreakIteratorSegmenter.class),
					createEngineDescription(OpenNlpPosTagger.class,
						OpenNlpPosTagger.PARAM_LANGUAGE, "de"))
				);
		AnalysisEngine engine = builder.createAggregate();
		engine.process(jcas);

		backGroundTokens = JCasUtil.select(jcas, Token.class);
		backGroundSentences = JCasUtil.select(jcas, Sentence.class);
	}


/*	public void init(String taskPath) 
			throws UIMAException, IOException
	{
		backGroundTokens = getTaskTokens(taskPath);
	}*/

}
