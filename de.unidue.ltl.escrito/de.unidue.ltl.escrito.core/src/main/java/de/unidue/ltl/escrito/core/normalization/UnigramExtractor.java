package de.unidue.ltl.escrito.core.normalization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * 
 * Extracts all lexical material not occuring in a certain spelling dictionary.
 * Used to process reading texts, which we assume to be error free but to contain unusual words not present in a spelling dictionary.
 * If these words are used by a learner, they should not be marked as spelling mistakes.
 * 
 * @author andrea
 *
 */

public class UnigramExtractor extends JCasAnnotator_ImplBase{
	public static final String PARAM_OUTPUT_LOCATION = "locationOfOutputTexts";
	@ConfigurationParameter(name = PARAM_OUTPUT_LOCATION, mandatory = true)
	private String locationOfUnigram;
	
	public static final String PARAM_NAME = "name";
	@ConfigurationParameter(name = PARAM_NAME, mandatory = true)
	private String name;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		name =(String) context.getConfigParameterValue(PARAM_NAME);
		locationOfUnigram = (String) context.getConfigParameterValue(PARAM_OUTPUT_LOCATION)+name;
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
		Collection<Token> tokens= JCasUtil.select(aJCas,Token.class);
		Collection<SpellingAnomaly> spellingErrors = JCasUtil.select(aJCas,SpellingAnomaly.class);
		Set<String> spellingErrorSet = new HashSet<String>();
		for(SpellingAnomaly s:spellingErrors){
			spellingErrorSet.add(s.getCoveredText().toLowerCase());
		}
		for(Token t: tokens){
			String tokenText = t.getCoveredText().toLowerCase();
			System.out.println(tokenText);
			if(spellingErrorSet.contains(tokenText)){
				if(tokenText.matches("^[a-zA-Z].*$"))
					fd.inc(tokenText);
			}		
		}
		System.out.println(fd.getKeys().size());
		List<String> unigram=fd.getMostFrequentSamples(fd.getKeys().size());
		Collections.sort(unigram, new Comparator<String>() {
		    @Override
		    public int compare(String s1, String s2) {
		        return s1.compareToIgnoreCase(s2);
		    }
		});

		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(locationOfUnigram)));
			for (String token : unigram) {
				writer.write(token+System.getProperty("line.separator"));
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
