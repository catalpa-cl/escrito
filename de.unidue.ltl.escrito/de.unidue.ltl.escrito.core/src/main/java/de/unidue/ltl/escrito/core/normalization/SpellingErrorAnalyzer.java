package de.unidue.ltl.escrito.core.normalization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.statistics.correlation.PearsonCorrelation;
import org.dkpro.statistics.correlation.SpearmansRankCorrelation;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * 
 * Annotator for gathering basic statistics about spelling errors in the data
 * 
 * 
 * @author andrea
 *
 */

public class SpellingErrorAnalyzer extends JCasAnnotator_ImplBase {
	public static final String PARAM_OUTPUT_TEXTS_LOCATION = "locationOfOutputs";
	@ConfigurationParameter(name = PARAM_OUTPUT_TEXTS_LOCATION, mandatory = true)
	private String locationOfOutputs;
	private String locationOfFrequencyListOutput;
	private String locationOfStatisticOutput;

	public static final String PARAM_DICTIONAY = "extention";
	@ConfigurationParameter(name = PARAM_DICTIONAY, mandatory = false)
	private String extention;

	public static final String PARAM_PROMPTID = "promptId";
	@ConfigurationParameter(name = PARAM_PROMPTID, mandatory = true)
	private String promptId;

	// all the spelling errors in this prompt
	protected List<String> errorList = new ArrayList<String>();
	// spelling errors with frequency
	protected Map<String, Integer> errorFrequency = new HashMap<String, Integer>();
	// double list of #errors and note of every answer in this prompt
	// Use: calculate the correlation
	protected ArrayList<Double> countErrors = new ArrayList<Double>();
	protected ArrayList<Double> errorRatio = new ArrayList<Double>();
	protected ArrayList<Double> goldClass = new ArrayList<Double>();
	// #errors and #types
	// Use: compare the token/type ratio before and after extension
	protected int countTotalErrorTokens = 0;
	protected int countTotalErrorTypes = 0;
	protected long countTotalToken = 0;
	protected double errorTokenRation=0d;
	// #answers
	protected int countAnswers = 0;
	
	
	private String lineSeparator = System.getProperty("line.separator");

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		promptId = (String) context.getConfigParameterValue(PARAM_PROMPTID);
		locationOfOutputs = System.getenv("DKPRO_HOME")+"/processedData/ASAP/"+(String) context.getConfigParameterValue(PARAM_OUTPUT_TEXTS_LOCATION)+"/";
		locationOfFrequencyListOutput = locationOfOutputs + promptId+"_"+(String) context.getConfigParameterValue(PARAM_DICTIONAY)+"_deteleDachP.txt";
		locationOfStatisticOutput = locationOfOutputs +	promptId+"_"+"statistic.txt";
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		countAnswers ++;
		TextClassificationOutcome outcome = JCasUtil.selectSingle(aJCas, TextClassificationOutcome.class);
		String score= outcome.getOutcome();
		goldClass.add(Double.parseDouble(score));
		Collection<SpellingAnomaly> spellingErrors = JCasUtil.select(aJCas,SpellingAnomaly.class);
		countErrors.add((double)spellingErrors.size());
		Collection<Token> tokens= JCasUtil.select(aJCas,Token.class);
		countTotalToken +=tokens.size();
		errorRatio.add((double)spellingErrors.size()/tokens.size());
		countTotalErrorTokens+=spellingErrors.size();
		for(SpellingAnomaly s:spellingErrors){
			String errorText = s.getCoveredText().toLowerCase();
			errorList.add(errorText);
		}
	}
	@Override
	public void destroy(){
		System.out.println("#Answer in Prompt "+promptId +": "+countAnswers);
		//calculate the error token ratio
		errorTokenRation= (double)countTotalErrorTokens/countTotalToken;
		//count the frequency of every spelling errors
		for(String error: errorList){
			if(!errorFrequency.containsKey(error)){
				errorFrequency.put(error, 1);
			}else{
				errorFrequency.put(error, errorFrequency.get(error)+1);
			}
		}
		
		//write out the frequency List
		try {
			writeSortedErrorFrequency(locationOfFrequencyListOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//calculate and write out the statistics
		double pearson = PearsonCorrelation.computeCorrelation(countErrors, goldClass);
		double pearsonWithNormalization = PearsonCorrelation.computeCorrelation(errorRatio, goldClass);
		double spearman = SpearmansRankCorrelation.computeCorrelation(countErrors, goldClass);
		double spearmanWithNormalization = SpearmansRankCorrelation.computeCorrelation(errorRatio, goldClass);
		try {
			BufferedWriter statisticWriter = new BufferedWriter(new FileWriter(new File(locationOfStatisticOutput)));
			statisticWriter.write("pearson"+"\t"+pearson+lineSeparator);
			statisticWriter.write("pearson after normalisation"+"\t"+pearsonWithNormalization+lineSeparator);
			statisticWriter.write("spearman"+"\t"+spearman+lineSeparator);
			statisticWriter.write("spearman after normalisation"+"\t"+spearmanWithNormalization+lineSeparator);
			statisticWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void writeSortedErrorFrequency(String locationOfOutputTexts) throws IOException{	
		BufferedWriter frequencyWriter = new BufferedWriter(new FileWriter(new File(locationOfOutputTexts)));
		frequencyWriter.write("#SpellingErrors: "+countTotalErrorTokens+lineSeparator);
		frequencyWriter.write("#Answers: "+countAnswers+lineSeparator);
		frequencyWriter.write("#SE/Answer: "+(double)countTotalErrorTokens/countAnswers+lineSeparator);
		frequencyWriter.write("#Tokens: "+countTotalToken+lineSeparator);
		frequencyWriter.write("#SE/Token: "+(double)countTotalErrorTokens/countTotalToken+lineSeparator);
		frequencyWriter.write("#types: "+errorFrequency.keySet().size()+lineSeparator);		
		frequencyWriter.write("Error Token Ration: "+errorTokenRation*100+"%"+lineSeparator+lineSeparator);
		Map <String,Integer> sortedErrorFrequency =sortByComparator(errorFrequency,false);	
		for(String s:sortedErrorFrequency.keySet()){
			frequencyWriter.write(s+"\t"+sortedErrorFrequency.get(s)+lineSeparator);
		}
		frequencyWriter.close();
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
}
