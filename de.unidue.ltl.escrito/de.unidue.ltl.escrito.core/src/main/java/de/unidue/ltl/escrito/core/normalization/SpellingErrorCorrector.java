package de.unidue.ltl.escrito.core.normalization;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.unidue.ltl.escrito.core.types.LearnerAnswer;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;
import edu.gatech.gtri.stringmetric.DamerauLevenshteinDistance;

public class SpellingErrorCorrector extends JCasAnnotator_ImplBase {
	public static final String PARAM_OUTPUT_TEXTS_LOCATION = "locationOfOutputs";
	@ConfigurationParameter(name = PARAM_OUTPUT_TEXTS_LOCATION, mandatory = true)
	private String locationOfOutputs;
	private String locationOfErrorModel;
	private String locationOfTextWithoutErrors;

	public static final String PARAM_DATASET = "dataset";
	@ConfigurationParameter(name = PARAM_DATASET, mandatory = false, defaultValue = "all")
	private String dataset;

	public static final String PARAM_PROMPTID = "promptId";
	@ConfigurationParameter(name = PARAM_PROMPTID, mandatory = true)
	private String promptId;


	// spelling errors and their frequency
	protected  List<String> errorList = new ArrayList<String>();
	protected  Map<String, Integer> errorFrequency = new HashMap<String, Integer>();
	// spelling error - suggestion one to one map
	protected  Map<String, String> errorToOneSuggestion = new HashMap<String, String>();
	private  StringBuffer sbTextWithoutErrors = new StringBuffer();
	// spelling error model
	protected  Map<String, Map<String, Integer>> errorFormFrequency = new HashMap<String, Map<String, Integer>>();
	protected  Map<String, HashSet<String>> suggestionOfError = new HashMap<String, HashSet<String>>();
	protected  Map<String, Integer> easilyMissspelledWordFrequency = new HashMap<String, Integer>();
	protected  Map<String, Map<String, Double>> errorModel = new HashMap<String, Map<String, Double>>();
	// top n unigram in this prompt
	//protected  HashMap<String, Integer> unigrams = new HashMap<String, Integer>();
	protected  HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
	// collect spelling error with no suggestions and no suited unigram
	protected  Set<String> errorTokensWithNoSuggestionSet = new HashSet<String>();
	protected  Set<String> errorTokensWithNoUnigramSet = new HashSet<String>();
	protected  Map<String, ArrayList<String>> errorSuggestionsWithNoUnigram = new HashMap<String, ArrayList<String>>();

	protected StringBuilder sbAnnotation = new StringBuilder();
	int annotatedItemId =1;
	int countError = 1;
	Map<String, String> errorTextMap= new HashMap<String, String>();
	Map<String, String> preTextMap = new HashMap<String, String>();
	Map<String, String> postTextMap = new HashMap<String, String>();
	Map<String, String> bestSuggestionMap = new HashMap<String, String>();
	Map<String, SpellingAnomaly> anomalyMap = new HashMap<String, SpellingAnomaly>();


	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		promptId = (String) context.getConfigParameterValue(PARAM_PROMPTID);
		dataset = (String) context.getConfigParameterValue(PARAM_DATASET);
		locationOfOutputs = System.getenv("DKPRO_HOME")+"/datasets/asap/dataAfterCorrection/";
		locationOfErrorModel = "src/main/resources/unigram/" + promptId + "_" + "errorModelWithFrequency.txt";
		locationOfTextWithoutErrors = locationOfOutputs + promptId + "_" + dataset + ".txt";
	}

	// TODO: options for selecting a suggestion: closest one, one with highest frequency in prompt, one with highest frequency in other training data

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData dmd = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String ID = dmd.getDocumentId();
		Collection<SpellingAnomaly> spellingErrors = JCasUtil.select(aJCas, SpellingAnomaly.class);
		for (SpellingAnomaly s : spellingErrors) {
			System.out.println(s.getCoveredText());
			int errorBegin = s.getBegin();
			String errorText = s.getCoveredText().toLowerCase();
			String bestSuggestion = getBestSuggestion(s, errorText);
			System.out.println(s.getCoveredText()+"\t"+bestSuggestion);
			if (bestSuggestion != null) {
				//	System.out.println(s.getCoveredText()+"_"+errorBegin);
				bestSuggestionMap.put(s.getCoveredText()+"_"+errorBegin, bestSuggestion);
			} 
		}
		String correctedText = getTextWithoutErrors(aJCas);
		System.out.println("original: "+aJCas.getDocumentText());
		System.out.println("corrected: "+correctedText);
		try {

			AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BinaryCasWriter.class,
					BinaryCasWriter.PARAM_TARGET_LOCATION, "src/main/resources/spellingCorrection/",
					BinaryCasWriter.PARAM_OVERWRITE, true));
			AnalysisEngine engine=createEngine(description);

			JCas corrected = engine.newJCas();
			corrected.setDocumentText(correctedText);
			corrected.setDocumentLanguage(aJCas.getDocumentLanguage());

			DocumentMetaData dmd2 = DocumentMetaData.create(corrected);
			dmd2.setDocumentId(dmd.getDocumentId()); 
			dmd2.setDocumentTitle(correctedText);
			dmd2.setCollectionId(dmd.getDocumentId());

			if (JCasUtil.exists(aJCas, LearnerAnswerWithReferenceAnswer.class)){
				LearnerAnswerWithReferenceAnswer learnerAnswerOld = JCasUtil.selectSingle(aJCas, LearnerAnswerWithReferenceAnswer.class);
				LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(corrected, 0, corrected.getDocumentText().length());
				learnerAnswer.setPromptId(learnerAnswerOld.getPromptId());
				StringArray ids = new StringArray(corrected, learnerAnswerOld.getReferenceAnswerIds().size());
				for (int i = 0; i<learnerAnswerOld.getReferenceAnswerIds().size(); i++){
					ids.set(i, learnerAnswerOld.getReferenceAnswerIds().get(i));
				}
				learnerAnswer.setReferenceAnswerIds(ids);
				learnerAnswer.addToIndexes();
			} else {
				LearnerAnswer learnerAnswerOld = JCasUtil.selectSingle(aJCas, LearnerAnswer.class);
				LearnerAnswer learnerAnswer = new LearnerAnswer(corrected, 0, corrected.getDocumentText().length());
				learnerAnswer.setPromptId(learnerAnswerOld.getPromptId());
				learnerAnswer.addToIndexes();
			}

			TextClassificationTarget unit = new TextClassificationTarget(corrected, 0, corrected.getDocumentText().length());
			// will add the token content as a suffix to the ID of this unit 
			unit.setSuffix(dmd.getDocumentId());
			unit.addToIndexes();

			TextClassificationOutcome outcome_old  = JCasUtil.selectSingle(aJCas, TextClassificationOutcome.class);
			TextClassificationOutcome outcome = new TextClassificationOutcome(corrected, 0, corrected.getDocumentText().length());
			outcome.setOutcome(outcome_old.getOutcome());
			outcome.addToIndexes();
			engine.process(corrected);
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Override
	public void destroy() {
		// write out the text without spelling errors
		try {
			BufferedWriter textWriter = new BufferedWriter(new FileWriter(new File(locationOfTextWithoutErrors)));
			textWriter.write(sbTextWithoutErrors.toString());
			textWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private  String getBestSuggestion(SpellingAnomaly s, String tokenText) {
		String bestSuggestion = null;
		// if there are no suggestions, we can't correct
		if (s.getSuggestions()==null) {
			errorTokensWithNoSuggestionSet.add(s.getCoveredText().toLowerCase());
		} else if (s.getSuggestions().size() == 0) {
			errorTokensWithNoSuggestionSet.add(s.getCoveredText().toLowerCase());
			// if there is only one suggestion, take it	
		} else if (s.getSuggestions().size() == 1) {
			bestSuggestion = s.getSuggestions(0).getReplacement();
			// if there are several suggestions, then...
		} else if (s.getSuggestions().size() > 1) {
			boolean foundBestSuggestion = false;
			// 1) check whether it is a splitting mistake with punctuation involved
			for (int i = 0; i < s.getSuggestions().size(); i++) {
				if (foundBestSuggestion == false) {
					if (s.getSuggestions(i).getReplacement().replaceAll(" ", "").equals(tokenText) 
							&& (tokenText.contains(".") || tokenText.contains(":"))) {
						foundBestSuggestion = true;
						bestSuggestion = s.getSuggestions(i).getReplacement();
						System.out.println("1: "+tokenText+"\t"+bestSuggestion);
					} else {
						continue;
					}
				} else {
					break;
				}
			}

			// 2) check whether it is a splitting mistake without punctuation involved
			for (int i = 0; i < s.getSuggestions().size(); i++) {
				if (foundBestSuggestion == false) {
					if (s.getSuggestions(i).getReplacement().contains(" ")) {
						String replacement = s.getSuggestions(i).getReplacement();
						String bigram = replacement.replace(" ", "_");
						if (ngrams.containsKey(bigram)){
							foundBestSuggestion = true;
							bestSuggestion = s.getSuggestions(i).getReplacement();	
							System.out.println("2: "+tokenText+"\t"+bestSuggestion);
						}
					} else {
						continue;
					}
				} else {
					break;
				}
			}
			// TODO gestaffelt nach levenshtein distance
			// check whether we have a unigram from the other answers in the suggestions
			if (foundBestSuggestion == false) {
				int freq = -1;
				int dist = 100;
				for (int i = 0; i < s.getSuggestions().size(); i++) {
					String replacement = s.getSuggestions(i).getReplacement();
					DamerauLevenshteinDistance dld = new DamerauLevenshteinDistance();
					int distance = dld.distance(tokenText, replacement);
					if (distance > dist){
						continue;
					} else if (distance < dist){
						if (ngrams.containsKey(replacement)) {
							dist = distance;
							freq = ngrams.get(replacement);
							foundBestSuggestion = true;
							bestSuggestion = s.getSuggestions(i).getReplacement();
							System.out.println("3: "+tokenText+"\t"+bestSuggestion+"\t"+freq);
						} 
					} else {
						if (ngrams.containsKey(replacement) && ngrams.get(replacement)>freq) {
							freq = ngrams.get(replacement);
							foundBestSuggestion = true;
							bestSuggestion = s.getSuggestions(i).getReplacement();
							System.out.println("3: "+tokenText+"\t"+bestSuggestion+"\t"+freq);
						} 
					}
				} 
				if (foundBestSuggestion){
					if (tokenText.endsWith("s") && (!bestSuggestion.endsWith("s"))
							&& ngrams.containsKey(bestSuggestion+"s")){
						bestSuggestion = bestSuggestion+"s";
						System.out.println("3b: "+tokenText+"\t"+bestSuggestion+"\t"+freq);
					}
				}
			}
			if (foundBestSuggestion == false) {
				errorTokensWithNoUnigramSet.add(s.getCoveredText().toLowerCase());
				ArrayList<String> suggestions = new ArrayList<String>();
				for (int i = 0; i < s.getSuggestions().size(); i++) {
					suggestions.add(s.getSuggestions(i).getReplacement());
				}
				errorSuggestionsWithNoUnigram.put(s.getCoveredText().toLowerCase(), suggestions);
			}

			//Fallback: take the one with the lowest costs
			double minCost = Double.MAX_VALUE;
			String replacement = tokenText;
			if (foundBestSuggestion == false) {
				for (int i = 0; i < s.getSuggestions().size(); i++) {
					SuggestedAction sa = s.getSuggestions(i);
					double cost = sa.getCertainty();
					if (cost < minCost){
					//	System.out.println(sa.getReplacement()+"\t"+sa.getCertainty());
						minCost = cost;
						replacement = sa.getReplacement();
					}
				}
			}
			bestSuggestion = replacement;
		}
		return bestSuggestion;
	}

	private String getTextWithoutErrors(JCas aJCas) {
		String result = "";
		TextClassificationOutcome outcome = JCasUtil.selectSingle(aJCas, TextClassificationOutcome.class);
		result +=outcome.getOutcome() + "\t";
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t : tokens) {
			String tokenText = t.getCoveredText();
			String key = tokenText.toLowerCase()+"_"+t.getStart();
			if (bestSuggestionMap.keySet().contains(key)) {
				tokenText = bestSuggestionMap.get(key);
			}
			result +=tokenText + " ";	
		}
		return result;
	}

}
