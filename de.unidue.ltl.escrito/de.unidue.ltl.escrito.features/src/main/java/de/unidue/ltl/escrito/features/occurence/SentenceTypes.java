package de.unidue.ltl.escrito.features.occurence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

// TODO: percentage of sentences ending in a question mark, exclamation mark, full stop.

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})

public class SentenceTypes extends FeatureExtractorResource_ImplBase
implements FeatureExtractor
{

	public static final String QUESTION_RATIO = "ratioOfQuestionSentences";
	public static final String EXCLAMATION_RATIO = "ratioOfExclamationSentences";
	
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		int all = 0	;
		int questions = 0;
		int exclamations = 0;
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		for (Sentence sentence : sentences) {
			if (sentence.getCoveredText().trim().equals("?")){
				questions++;
			} else if (sentence.getCoveredText().trim().equals("!")){
				exclamations++;
			}
			all++;
		}
		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(QUESTION_RATIO, (double) questions/all, FeatureType.NUMERIC));
		features.add(new Feature(EXCLAMATION_RATIO, (double) exclamations/all, FeatureType.NUMERIC));
		return features;
	}
}
