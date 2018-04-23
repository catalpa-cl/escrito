package de.unidue.ltl.escrito.core.normalization;

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
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
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

	public static final String PARAM_SETTING = "setting";
	@ConfigurationParameter(name = PARAM_SETTING, mandatory = true)
	private String setting;

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
	Map<String, String> suggestionMap = new HashMap<String, String>();
	Map<String, SpellingAnomaly> anomalyMap = new HashMap<String, SpellingAnomaly>();


	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		promptId = (String) context.getConfigParameterValue(PARAM_PROMPTID);
		dataset = (String) context.getConfigParameterValue(PARAM_DATASET);
		setting = (String) context.getConfigParameterValue(PARAM_SETTING);
		locationOfOutputs = System.getenv("DKPRO_HOME")+"/datasets/asap/dataAfterCorrection/";
		locationOfErrorModel = "src/main/resources/unigram/" + promptId + "_" + "errorModelWithFrequency.txt";
		locationOfTextWithoutErrors = locationOfOutputs + promptId + "_" + dataset + ".txt";


		// initialize the Unigram.
		String locationOfUnigram = "src/main/resources/unigram/" + promptId + "_unigram.txt";
		String locationOfNgram = "src/main/resources/ngrams/" + promptId + "_ngram.txt";
		try {
//			BufferedReader unigramReader = new BufferedReader(new FileReader(new File(locationOfUnigram)));
//			String nextLine = unigramReader.readLine();
//			while (nextLine != null) {
//				String[] nextItem = nextLine.split("\t");
//				unigrams.put(nextItem[0], Integer.parseInt(nextItem[1]));
//				nextLine = unigramReader.readLine();
//			}
//			unigramReader.close();
			BufferedReader ngramReader = new BufferedReader(new FileReader(new File(locationOfNgram)));
			String nextLine = ngramReader.readLine();
			while (nextLine != null) {
				String[] nextItem = nextLine.split("\t");
				ngrams.put(nextItem[0], Integer.parseInt(nextItem[1]));
				nextLine = ngramReader.readLine();
			}
			ngramReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// initialize the error frequency list
		String locationOfErrorFrequencylist = "src/main/resources/frequencyList/" + promptId + "_frequencyList.txt";
		try {
			BufferedReader frequencyReader = new BufferedReader(new FileReader(new File(locationOfErrorFrequencylist)));
			String nextLine = frequencyReader.readLine();
			while (nextLine != null) {
				String[] nextItem = nextLine.split("\t");
				String error = nextItem[0].toLowerCase();
				int frequency = Integer.parseInt(nextItem[1]);
				if(!errorFrequency.keySet().contains(error)){
					errorFrequency.put(error, frequency);
				}else{
					errorFrequency.put(error, errorFrequency.get(error)+frequency);
				}
				nextLine = frequencyReader.readLine();
			}
			frequencyReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sbAnnotation.append(" # webanno.custom.Error | Correction"+lineSeparator);
	}

	// TODO: options for selecting a suggestion: closest one, one with highest frequency in prompt, one with highest frequency in other training data

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData dmd = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String ID = dmd.getDocumentId();
		String preText = "";
		String postText = "";
		Collection<SpellingAnomaly> spellingErrors = JCasUtil.select(aJCas, SpellingAnomaly.class);
		String jcasText = aJCas.getDocumentText();
		for (SpellingAnomaly s : spellingErrors) {
			int errorBegin = s.getBegin();
			if (errorBegin < 20) {
				preText ="["+ID+"_"+errorBegin+ "] "+ jcasText.substring(0, errorBegin);
			} else {
				preText ="["+ID+"_"+errorBegin+ "] "+ jcasText.substring(errorBegin - 20, errorBegin);
			}
			int errorEnd = s.getEnd();
			if (errorEnd + 20 > jcasText.length() - 1) {
				postText = jcasText.substring(errorEnd, jcasText.length())+" ";
			} else {
				postText = jcasText.substring(errorEnd, errorEnd + 20)+" ";
			}
			String errorText = s.getCoveredText().toLowerCase();
			if (errorTextMap.containsKey(s)){
				System.err.println("already contained");
			}
			errorTextMap.put(ID+"_"+errorBegin, errorText);
			preTextMap.put(ID+"_"+errorBegin, preText);
			postTextMap.put(ID+"_"+errorBegin, postText);
			String bestSuggestion = getBestSuggestion(s, errorText);
			if (bestSuggestion != null) {
				suggestionMap.put(ID+"_"+errorBegin, bestSuggestion);
			} else {
				suggestionMap.put(ID+"_"+errorBegin, "***");
			}
			anomalyMap.put(ID+"_"+errorBegin, s);
		}


		//		//if(annotatedItemId<=20){
		//		sbAnnotation.append("#id="+annotatedItemId+lineSeparator);
		//		sbAnnotation.append("#text="+aJCas.getDocumentText()+lineSeparator);
		//		int annotatedTokenId = 1;
		//
		//		Collection<SpellingAnomaly> spellingErrors = JCasUtil.select(aJCas, SpellingAnomaly.class);
		//		for (SpellingAnomaly s : spellingErrors) {
		//			String errorText = s.getCoveredText().toLowerCase();
		//			errorList.add(errorText);
		//			String bestSuggestion = getBestSuggestion(s);
		//			if (bestSuggestion != null) {
		//				if (!suggestionOfError.keySet().contains(bestSuggestion)) {
		//					suggestionOfError.put(bestSuggestion, new HashSet<String>());
		//				}
		//				suggestionOfError.get(bestSuggestion).add(errorText);
		//				errorToOneSuggestion.put(errorText, bestSuggestion);
		//			}
		//			String suggestions = "";
		//			if (s.getSuggestions() != null){
		//				for (int i = 0; i<s.getSuggestions().size(); i++){
		//					suggestions += " "+s.getSuggestions(i).getReplacement();
		//				}
		//			}
		//			suggestions = suggestions.trim();
		//			//		System.out.println("Error:\t"+errorText+"\t"+suggestions);
		//		}
				//	if(!dataset.equals("all")){
					writeTextWithoutErrors(aJCas);
				//}
		//
		//		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		//		for(Token t: tokens){
		//			sbAnnotation.append(annotatedItemId+"-"+annotatedTokenId+"\t");
		//			sbAnnotation.append(t.getCoveredText()+"\t");
		//			String tokenText = t.getCoveredText().toLowerCase();
		//			if (errorToOneSuggestion.keySet().contains(tokenText)){
		//				sbAnnotation.append("B-"+errorToOneSuggestion.get(tokenText)+lineSeparator);
		//			} else if(errorList.contains(tokenText)){
		//				sbAnnotation.append("B-webanno.custom.Error_"+lineSeparator);
		//			} else {
		//				sbAnnotation.append("O"+lineSeparator);
		//			}
		//			annotatedTokenId++;
		//		}
		//
		//		annotatedItemId++;
		//		sbAnnotation.append(lineSeparator);
		//		//	}
	}


	@Override
	public void destroy() {
		HashMap<String, String> sortedMap = (HashMap<String, String>) sortByValue(errorTextMap);
		for(String id : sortedMap.keySet()){
			SpellingAnomaly s = anomalyMap.get(id);
			sbAnnotation.append("#id="+countError+lineSeparator);
			sbAnnotation.append("#text="+preTextMap.get(id)+sortedMap.get(id)+postTextMap.get(id)+lineSeparator);
			sbAnnotation.append(countError+"-1"+"\t"+preTextMap.get(id)+"\t"+"O"+lineSeparator);
			if(!suggestionMap.get(id).equals("***")){
				sbAnnotation.append(countError + "-2" + "\t" + sortedMap.get(id) + "\t" + "B-" + suggestionMap.get(id) + lineSeparator);
			}else{
				sbAnnotation.append(countError + "-2" + "\t" + sortedMap.get(id) + "\t" + "B-webanno.custom.Error_" + lineSeparator);
			}
			sbAnnotation.append(countError + "-3" + "\t" + postTextMap.get(id) + "\t" + "O" + lineSeparator);
			sbAnnotation.append(lineSeparator);
			countError++;
		}
		String locationOfAnnotationsDir = System.getenv("DKPRO_HOME") + "/datasets/asap/annotation_" + setting;
		String locationOfAnnotationsFile = System.getenv("DKPRO_HOME") + "/datasets/asap/annotation_" + setting + "/"
				+ promptId + ".tsv";
		File file = new File(locationOfAnnotationsDir);
		if (!file.exists()) {
			file.mkdir();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(locationOfAnnotationsFile)));
			bw.write(sbAnnotation.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//		String locationOfAnnotationsFile=System.getenv("DKPRO_HOME")+"/datasets/asap/annotation/"+promptId+".tsv";
		//		try {
		//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(locationOfAnnotationsFile)));
		//			bw.write(sbAnnotation.toString());
		//			bw.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		// print out all the error tokens without best suggestion
		System.out.println("Error Tokens With No Suggestions: " + errorTokensWithNoSuggestionSet.size());
		System.out
		.println("Error Tokens With Suggestions, but no suited unigram: " + errorTokensWithNoUnigramSet.size());
		for (String error : errorTokensWithNoSuggestionSet) {
			System.out.println("This error has no suggestion: " + error + "\t" + errorFrequency.get(error));
		}

		for (String error : errorSuggestionsWithNoUnigram.keySet()) {
			if (errorFrequency.containsKey(error) && errorFrequency.get(error) > 1) {
				System.out.print(errorFrequency.get(error) + "\t" + error + ":");
				for (String suggestion : errorSuggestionsWithNoUnigram.get(error)) {
					System.out.print(suggestion + ", ");
				}
				System.out.println();
			}
		}
		// write out the text without spelling errors
		try {
			BufferedWriter textWriter = new BufferedWriter(new FileWriter(new File(locationOfTextWithoutErrors)));
			textWriter.write(sbTextWithoutErrors.toString());
			textWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// iterate all the suggestions as easily miss-spelled words
		// count the frequency of every miss-spelled word and the frequency of
		// every miss-spelled form
		for (String easilyMissspelledWord : suggestionOfError.keySet()) {
			Integer countEasilyMissspelledWord = 0;
			HashMap<String, Integer> errorDistributionForCurrentWord = new HashMap<String, Integer>();
			for (String error : suggestionOfError.get(easilyMissspelledWord)) {
				errorDistributionForCurrentWord.put(error, errorFrequency.get(error));
				if (errorFrequency.containsKey(error)){
					countEasilyMissspelledWord += errorFrequency.get(error);
				}
			}
			easilyMissspelledWordFrequency.put(easilyMissspelledWord, countEasilyMissspelledWord);
			errorFormFrequency.put(easilyMissspelledWord, errorDistributionForCurrentWord);
		}

		// sorted easily miss-spelled words by frequency
		// calculate the probability of every miss-spelled form
		Map<String, Integer> sortedEasilyMissspelledWordFrequency = sortByComparator(easilyMissspelledWordFrequency,
				false);
		for (String easilySpelledWord : sortedEasilyMissspelledWordFrequency.keySet()) {
			HashMap<String, Double> errorModelForCurrentWord = new HashMap<String, Double>();
			for (String error : suggestionOfError.get(easilySpelledWord)) {
				double ef = 0.0;
				if (errorFrequency.containsKey(error)){
					ef = errorFrequency.get(error);
				}
				if (easilyMissspelledWordFrequency.containsKey(easilySpelledWord)){
					errorModelForCurrentWord.put(error,
							ef / easilyMissspelledWordFrequency.get(easilySpelledWord));
				}
			}
			errorModel.put(easilySpelledWord, errorModelForCurrentWord);
		}
		System.out.println(errorModel.get("balance"));
		//print out the error model
		if(dataset.equals("all")){
			writeErrorModel(locationOfErrorModel);
		}
	}

	private String lineSeparator = System.getProperty("line.separator");

	private void writeErrorModel(String locationOfErrorModel) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(locationOfErrorModel)));
			Map<String, Integer> sortedEasilyMissspelledWordFrequency = sortByComparator(easilyMissspelledWordFrequency,
					false);
			for (String s : sortedEasilyMissspelledWordFrequency.keySet()) {
				writer.write("------" + lineSeparator);
				writer.write(s+ "\t" +easilyMissspelledWordFrequency.get(s)+ lineSeparator);
				for (String e : errorModel.get(s).keySet()) {
					writer.write(e + "\t" +errorFrequency.get(e) + lineSeparator);
				}
			}
			writer.close();
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
			for (int i = 0; i < s.getSuggestions().size(); i++) {
				// 1) check whether it is a splitting mistake with punctuation involved
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

			for (int i = 0; i < s.getSuggestions().size(); i++) {
				// 1) check whether it is a splitting mistake without punctuation involved
				if (foundBestSuggestion == false) {
					if (s.getSuggestions(i).getReplacement().contains(" ")) {
						String replacement = s.getSuggestions(i).getReplacement();
						String bigram = replacement.replace(" ", "_");
						if (ngrams.containsKey(bigram)){
							foundBestSuggestion = true;
							bestSuggestion = s.getSuggestions(i).getReplacement();	
							System.out.println("2: "+tokenText+"\t"+bestSuggestion);
						}
						//						String[] parts = replacement.split(" ");
						//						boolean foundAll = false;
						//						for (String part : parts){
						//							if (unigrams.keySet().contains(part)){
						//								foundAll = true;
						//							} else {
						//								foundAll = false;
						//								break;
						//							}
						//						}
						//						if (foundAll){
						//							foundBestSuggestion = true;
						//							bestSuggestion = s.getSuggestions(i).getReplacement();
						//							System.out.println("2: "+tokenText+"\t"+bestSuggestion);
						//						}
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
		}
		return bestSuggestion;
	}

	private void writeTextWithoutErrors(JCas aJCas) {
		DocumentMetaData dmd = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		sbTextWithoutErrors.append(
				dmd.getDocumentId().substring(dmd.getDocumentId().lastIndexOf("_") + 1, dmd.getDocumentId().length())
				+ "\t");
		sbTextWithoutErrors.append(promptId + "\t");
		TextClassificationOutcome outcome = JCasUtil.selectSingle(aJCas, TextClassificationOutcome.class);
		sbTextWithoutErrors.append(outcome.getOutcome() + "\t");
		sbTextWithoutErrors.append("-1" + "\t");
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t : tokens) {
			String tokenText = t.getCoveredText().toLowerCase();
			if (errorToOneSuggestion.keySet().contains(tokenText)) {
				tokenText = errorToOneSuggestion.get(tokenText);
			}
			sbTextWithoutErrors.append(tokenText + " ");	
		}
		sbTextWithoutErrors.append(lineSeparator);
	}

	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, boolean order) {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());
				}
			}
		});
		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
