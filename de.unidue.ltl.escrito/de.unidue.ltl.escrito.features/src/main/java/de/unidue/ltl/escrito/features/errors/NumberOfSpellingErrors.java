package de.unidue.ltl.escrito.features.errors;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.JWordSplitterAlgorithm;

public class NumberOfSpellingErrors
		extends FeatureExtractorResource_ImplBase
		implements FeatureExtractor
{

	public static final String NR_OF_SPELLINGMISTAKES = "nrOfSpellingMistakes";

	public static final String PARAM_DICT_PATH = "dictPath";
	@ConfigurationParameter(name = PARAM_DICT_PATH, mandatory = true)
	private String dictPath;

	private CaseSensitiveSpellDictionary dict;
	private JWordSplitterAlgorithm splitter;
	private SpellCheckingFilter filter;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			initDictionary(dictPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		filter= new SpellCheckingFilter();
		return true;
	}

	private void initDictionary(String dictPath) 
		throws IOException
	{
		File f = new File(dictPath);
		this.dict = new CaseSensitiveSpellDictionary(f);
		
		splitter = new JWordSplitterAlgorithm();
		Dictionary dict = new SimpleDictionary(f);
		splitter.setDictionary(dict);
	}

	// TODO: No Headline Detection!
	// TODO: If the Lemma is N (therefore valid uppercase) itself the approach fails
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{

		Set<String> set = new HashSet<String>();
		double ratio = 0;
		for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
			boolean sentenceBegin = true;
			boolean ignoreCase = true;
			for (Token t : JCasUtil.selectCovered(Token.class, s)) {
				// if we are at the start of the sentence the case will be
				// ignored
				if (sentenceBegin)
					ignoreCase = true;

				String tokenText = t.getCoveredText();
				String lemmaText = t.getLemma().getValue();

				if(isNe(t)){
					// change the flag
					sentenceBegin= changeFlag(sentenceBegin);
					continue;
				}
				
				// if there is a quotation capitalization will be ignored for the next word
				if (tokenText.matches("[\\„\"]")) {
					ignoreCase = true;
				}
				
				if(!filter.passFilter(tokenText)){
					// change the flag
					sentenceBegin= changeFlag(sentenceBegin);
					continue;
				}
				
				// first char of lemma gets the the case of the origin
				if (Character.isUpperCase(tokenText.charAt(0)))
					lemmaText = StringUtils.capitalize(lemmaText);
				// if the first token in a sentence or a noun is written with lower case
				if (Character.isLowerCase(tokenText.charAt(0))
						&& (sentenceBegin 
//								||isN(t)
								)) {
					set.add(tokenText);
					ratio++;
					// change the flag
					sentenceBegin= changeFlag(sentenceBegin);
					continue;
				}
				// check if the dictionary contains the word or its lemma.
				// Capitalization is ignored if the word is the first Word in the text or if it occurs after quotation marks
				if (!dict.isCorrect(lemmaText, ignoreCase)
						&& !dict.isCorrect(tokenText, ignoreCase)) {
					if (decompoundsAreNotInDict(tokenText)&&hyphenSepratedNotInDict(tokenText)) {
						System.out.println(tokenText);
						ratio++;
						set.add(tokenText);
					}
				}
				// change the flag
				sentenceBegin= changeFlag(sentenceBegin);
			}
		}

		//Normalization on total count of words
		if (ratio != 0) {
			ratio = (double) ratio / JCasUtil.select(jcas, Token.class).size();
		}
		System.out.println(set);
		System.out.println(ratio);
		return new Feature(NR_OF_SPELLINGMISTAKES, ratio, FeatureType.NUMERIC).asSet();
	}
private boolean isNe(Token t) {
	if ( t.getPos().getPosValue().equals("NE")) {
		return true;
	}
	return false;
	}

/**
 * checks if hyphen
 * @param tokenText
 * @return
 */
	private boolean hyphenSepratedNotInDict(String tokenText) {
		for(String word: tokenText.split("-")){
			if (!dict.isCorrect(StringUtils.capitalize(word),
					true)) {
				return true;
			}
		}
		return false;
	}

	private boolean changeFlag(boolean sentenceBegin) {
		if (sentenceBegin)sentenceBegin = false;
		return sentenceBegin;
	}

	private boolean isN(Token t) {
		if (t.getPos().getPosValue().equals("NN")) {
			return true;
		}
		return false;
	}

	/**
	 * checks if decompounds are NOT in the dict
	 * 
	 * @param tokenText
	 * @return
	 */
	private boolean decompoundsAreNotInDict(String tokenText) {
		List<DecompoundedWord> decompunds = splitter.split(tokenText)
				.getAllSplits();
		// if there is no splits return true
		if (decompunds.size() < 2)
			return true;
		boolean wholeDecompund = true;
		for (DecompoundedWord dW : decompunds) {
			// the first split contains the whole word --> shouldn't be
			// considered
			if (wholeDecompund) {
				wholeDecompund = false;
			} else {
				for (Fragment f : dW.getSplits()) {
					// check if a fragment is in dict ignore any
					// capitalization/non-capitalization
					if (!dict.isCorrect(StringUtils.capitalize(f.getWord()),
							true)) {
						return true;
					}
				}
			}
		}
		// if every split is contained in the dict --> return false
		return false;
	}
}
