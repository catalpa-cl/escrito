package de.unidue.ltl.escrito.core.preprocessing;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.escrito.core.IoUtils;
import de.unidue.ltl.escrito.core.LRUCache;
import de.unidue.ltl.escrito.core.Utils;
import de.unidue.ltl.escrito.core.types.LearnerAnswerToken;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;

public class LearnerTokenAnnotator extends JCasAnnotator_ImplBase{

	/*
	 * This annotator annotates every Token in a LearnerAnswer as LearnerAnswerToken with the additional information whether it
	 * occurs in the question in order to be able to ignore prompt material.
	 * We also annotate punctuation and material from a stopword list.
	 * 
	 * These information can be used optioannly in feature extractors, currently in those which are based on comparison with a target answer.
	 * 
	 * 
	 */
	
	

	public static final String PARAM_ADDITIONAL_TEXTS_LOCATION = "locationOfAdditionalTexts";
	@ConfigurationParameter(name = PARAM_ADDITIONAL_TEXTS_LOCATION, mandatory = true)
	private String locationOfAdditionalTexts;

	private LRUCache<String, JCas> questionViewCache;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		locationOfAdditionalTexts = System.getenv("DKPRO_HOME")+"/processedData/"+(String) context.getConfigParameterValue(PARAM_ADDITIONAL_TEXTS_LOCATION);
		questionViewCache = new LRUCache<String, JCas>(50);
		System.out.println("Initialize LearnerTokenAnnotator");
		System.out.println("location of additional texts: "+locationOfAdditionalTexts);
	}


	@Override
	public void process(JCas jcas) 
			throws AnalysisEngineProcessException
	{
		LearnerAnswerWithReferenceAnswer learnerAnswer = JCasUtil.selectSingle(jcas, LearnerAnswerWithReferenceAnswer.class);
		List<Token> tokens =  JCasUtil.selectCovered(Token.class, learnerAnswer);
		
		String questionId = learnerAnswer.getPromptId();
		JCas questionView = null;
		if (questionViewCache.containsKey(questionId)){
			questionView = questionViewCache.get(questionId); 
		} else {
			try {
				questionView = IoUtils.loadJCasFromFile(questionId, locationOfAdditionalTexts, "Q");
			} catch (UIMAException | IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
			questionViewCache.put(questionId, questionView);
		}
		// get the contained tokens
		List<String> lemmasInQuestion = Utils.extractAllLemmasFromView(questionView, false, false, false);

		//create one LearnerAnswerToken per token
		for (Token t : tokens){
			LearnerAnswerToken lat = new LearnerAnswerToken(jcas);
			lat.setToken(t);
			lat.setBegin(t.getBegin());
			lat.setEnd(t.getEnd());
			String lemma = t.getLemma().getValue();
			if (lemmasInQuestion.contains(lemma)){
				lat.setIsQuestionMaterial(true);
			} else {
				lat.setIsQuestionMaterial(false);
			}
			if (isPunctuation(t)){
				lat.setIsPunctuation(true);
			} else {
				lat.setIsPunctuation(false);
			}
			if (isStopWord(t)){
				lat.setIsStopWord(true);
			} else {
				lat.setIsStopWord(false);
			}
		//	System.out.println(lemma+" "+lat.getIsQuestionMaterial());
			lat.addToIndexes();
		}
	}


	private boolean isStopWord(Token t) {
		// TODO Auto-generated method stub
		return false;
	}

// TODO: only works for STTS
	private boolean isPunctuation(Token t) {
		String pos = t.getPos().getPosValue();
	//	System.out.println(pos+"\t"+pos.contains("$"));
		return (pos.contains("$"));
	}

}
