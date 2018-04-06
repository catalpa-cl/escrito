package de.unidue.ltl.edu.scoring.features.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.escrito.core.types.LearnerAnswerToken;

public class Utils {


	public static List<String> extractAllLemmasFromView(JCas view, 
			boolean ignoreQuestionMaterial,
			boolean ignoreStopwords,
			boolean ignorePunctuation) {
		if (JCasUtil.exists(view, LearnerAnswerToken.class)){
			Collection<LearnerAnswerToken> tokens = JCasUtil.select(view, LearnerAnswerToken.class);
			List<String> words = new ArrayList<String>();
			Iterator<LearnerAnswerToken> iter = tokens.iterator();
			while (iter.hasNext()){
				LearnerAnswerToken t = iter.next();
				if (ignoreToken(t, ignoreQuestionMaterial, ignoreStopwords, ignorePunctuation)){
					System.out.println("Ignore token "+t.getCoveredText());
				} else {
					words.add(t.getToken().getLemma().getValue()); 
				}
			}
			return words;
		} else {
			//if we do not have learner answer tokens annotated, take all tokens
			Collection<Token> tokens = JCasUtil.select(view, Token.class);
			List<String> words = new ArrayList<String>();
			Iterator<Token> iter = tokens.iterator();
			while (iter.hasNext()){
				words.add(iter.next().getLemma().getValue()); 
			}
			return words;
		}
	}


	private static boolean ignoreToken(LearnerAnswerToken t, boolean ignoreQuestionMaterial, boolean ignoreStopwords,
			boolean ignorePunctuation) {
		return ((t.getIsPunctuation() && ignorePunctuation)
				|| (t.getIsQuestionMaterial() && ignoreQuestionMaterial)
				|| (t.getIsStopWord() && ignoreStopwords));
	}


	public static List<String> extractAllContentWordLemmasFromView(JCas view) {
		Collection<Token> tokens = JCasUtil.select(view, Token.class);
		List<String> words = new ArrayList<String>();
		Iterator<Token> iter = tokens.iterator();
		while (iter.hasNext()){
			Token t = iter.next();
			String pos = t.getPos().getPosValue();
			if (pos.startsWith("NN") || pos.equals("MD") || pos.startsWith("VB") || pos.startsWith("JJ")){
				words.add(t.getLemma().getValue().toLowerCase()); 
			}
		}
		return words;
	}


	public static List<String> extractAllWordsFromView(JCas view) {
		Collection<Token> tokens = JCasUtil.select(view, Token.class);
		List<String> words = new ArrayList<String>();
		Iterator<Token> iter = tokens.iterator();
		while (iter.hasNext()){
			Token t = iter.next();
			words.add(t.getCoveredText()); 
		}
		return words;
	}




	public static double computeCosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}   
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}




	public static double[] addVectors(double[] resultVector, double[] wordVector) {
		if (wordVector.length != resultVector.length){
			System.err.println("Vectors do not have the same length");
			System.exit(-1);
		} 
		for (int i = 0; i<resultVector.length; i++){
			resultVector[i] = resultVector[i]+wordVector[i];
		}
		return resultVector;
	}

}
