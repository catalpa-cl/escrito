package de.unidue.ltl.escrito.features.essay;

import static org.apache.uima.fit.util.JCasUtil.toText;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public abstract class KLDivergence_Base 
extends FeatureExtractorResource_ImplBase 
implements FeatureExtractor 
{

	/*
	 * computes the average Kullback-Leibler-Divergence for n-grams of the
	 * length n
	 * 
	 */
	protected double getNgrammDivergence(Collection<Sentence> sentences,
			Collection<Sentence> backgroundSentences, int n){
		FrequencyDistribution<String> fdFG = getNgramFD(sentences, n);
		FrequencyDistribution<String> fdBG = getNgramFD(backgroundSentences, n);
		double ngramDivergence = 0d;
		for(String ngram:fdFG.getKeys()){
			ngramDivergence += getDivergence(fdFG, fdBG, ngram, fdFG.getKeys().size(), fdBG.getKeys().size());
			//System.out.println(ngramDivergence);
		}
		return ngramDivergence;
	}


	/*
	 * inc all n-grams in a Collection of sentences to a frequency distribution
	 */
	private FrequencyDistribution<String> getNgramFD(Collection<Sentence> sentences, int n) {
		FrequencyDistribution<String> fd = new FrequencyDistribution<>();
		ArrayList<String> ngrams = new ArrayList<String>();
		for(Sentence s : sentences){
			ngrams.addAll(getNewNgramInSentence(s,n));
		}
		fd.incAll(ngrams);
		return fd;
	}
	/*
	 * get n-grams in a sentence
	 */
	private ArrayList<String> getNewNgramInSentence(Sentence sentence, int n){
		ArrayList<String> ngrams = new ArrayList<String>();
		Collection<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);
		ArrayList<String> tokenListInSentence = new ArrayList<String>();
			for (Token t : tokens) {
				if(!t.getPos().getPosValue().startsWith("$")) {
					tokenListInSentence.add(t.getCoveredText());
				}
			}
			for (int i = 0; i < tokenListInSentence.size() - n; i++) {
				String[] ngram = new String[n];
				for (int j = 0; j < n; j++) {
					ngram[j] = tokenListInSentence.get(i + j);
				}
				ngrams.add(StringUtils.join(ngram, " "));
			}
			return ngrams;
	}

	/*
	 * computes the average Kullback-Leibler-Divergence for POS
	 * 
	 */
	protected double getPOSDivergence(Collection<Token> tokens,
			Collection<Token> backGroundTokens) {
		FrequencyDistribution<String> fdFG = getPosFD(tokens);
		FrequencyDistribution<String> fdBG = getPosFD(backGroundTokens);

		double posDivergence = 0;
		for (Token token : tokens) {
			if (token.getPos() == null){
				System.err.println("No POS for token "+token.getCoveredText()+" in essay ");
			} else {
				posDivergence += getDivergence(fdFG, fdBG, token.getPos()
						.getPosValue(), tokens.size(), backGroundTokens.size());
			}
		}
		return posDivergence = posDivergence / tokens.size();
	}

	/*
	 * computes the average Kullback-Leibler-Divergence for tokens Ignores
	 * punctuation
	 * 
	 */
	protected double getTokenDivergence(JCas jcas, Collection<Token> tokens,
			Collection<Token> backGroundTokens) {
		// get the fds
		FrequencyDistribution<String> fdFG = new FrequencyDistribution<String>(
				toText(tokens));
		FrequencyDistribution<String> fdBG = new FrequencyDistribution<String>(
				toText(backGroundTokens));

		double tokenDivergence = 0;
		int n = 0;
		for (Token token : tokens) {
			// ignore punctuation
			if (token.getPos() == null){
				System.err.println("No POS for token "+token.getCoveredText()+" in essay "+jcas.getDocumentText().substring(0, 100));
			} else {
				if(!token.getPos().getPosValue().startsWith("$")){
					n++;
					tokenDivergence += getDivergence(fdFG, fdBG,
							token.getCoveredText(), tokens.size(),
							backGroundTokens.size());
				}
			}
		}
		return tokenDivergence = tokenDivergence / n;
	}

	/*
	 * incs all POS-Values of a set of tokens to fd
	 * 
	 */
	private FrequencyDistribution<String> getPosFD(Collection<Token> tokens) {
		FrequencyDistribution<String> fd = new FrequencyDistribution<>();
		for (Token t : tokens) {
			if (t.getPos() == null){
				System.err.println("No POS for token "+t.getCoveredText()+" in essay ");
			} else {		
				if (t.getPos() != null && t.getPos().getPosValue() != null){
					fd.inc(t.getPos().getPosValue());
				}
			}
		}
		return fd;
	}

	// TODO is KL computed right? correct Smoothing?
	/*
	 * Calculates the Kullback-Leibler-Divergence between two frequency
	 * distributions on a given annotation
	 * 
	 * @param fdFG
	 *            frequency distribution of foreground corpus
	 * @param fdBG
	 *            frequency distribution of backgroundground corpus
	 */
	private double getDivergence(FrequencyDistribution<String> fdFG,
			FrequencyDistribution<String> fdBG, String currentAnno,
			int lengthOfFG, int lengthOfBG) {
		double frequency1 = (double) fdFG.getCount(currentAnno) / lengthOfFG;
		double frequency2 = 0;
		if (fdBG.contains(currentAnno)) {
			frequency2 = (double) fdBG.getCount(currentAnno) / lengthOfBG;
			return frequency1
					* (Math.log(frequency1 / frequency2) / Math.log(2));
		} else {
			return 0;
		}
	}

}