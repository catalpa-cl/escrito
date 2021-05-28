package de.unidue.ltl.escrito.features.length;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
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

import com.googlecode.jweb1t.JWeb1TSearcher;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.WordSyllableCounter;

//@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.token", 
//"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class NrOfSyllables 
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{

	//TODO: language as parameter


	public static final String FN_NUM_SYLLABLES = "AverageNumberOfSyllablesPerWord";
	public static final String FN_NUM_SIMPLE = "FrequencyOfSimpleWords";
	public static final String FN_NUM_COMPLEX = "FreuencyOfComplexWords";


	WordSyllableCounter wsc;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		wsc = new WordSyllableCounter("de");
		return true;
	}


	@Override
	public Set<Feature> extract(JCas aJCas, TextClassificationTarget aTarget) throws TextClassificationException {
		Set<Feature> features = new HashSet<Feature>();
		int numWords = 0;
		int numSyllables = 0;
		int numSimpleWords = 0;
		int numComplexWords = 0;
		for (Token token : JCasUtil.select(aJCas, Token.class)) {
			if (!(isPunctuation(token))){
				numWords++;
				int syllables = wsc.countSyllables(token.getCoveredText());
			//	System.out.println(token.getCoveredText()+" "+syllables);
				numSyllables += syllables;
				if (syllables > 2 ){
					numComplexWords++;
				} else {
					numSimpleWords++;
				}
			}
		}
		if(numWords > 0){
			features.add(new Feature(FN_NUM_SYLLABLES, (1.0*numSyllables)/numWords, FeatureType.NUMERIC));
			features.add(new Feature(FN_NUM_COMPLEX, (1.0*numComplexWords)/numWords, FeatureType.NUMERIC));
			features.add(new Feature(FN_NUM_SIMPLE, (1.0*numSimpleWords)/numWords, FeatureType.NUMERIC));
		} else {
			features.add(new Feature(FN_NUM_SYLLABLES, 0.0, FeatureType.NUMERIC));
			features.add(new Feature(FN_NUM_COMPLEX, 0.0, FeatureType.NUMERIC));
			features.add(new Feature(FN_NUM_SIMPLE, 0.0, FeatureType.NUMERIC));
		}
		return features;
	}


	private boolean isPunctuation(Token token) {
		// System.out.println(token.getPos().getCoarseValue());
		return token.getPos().getCoarseValue().startsWith("PUNC");
	}

	
	
}
