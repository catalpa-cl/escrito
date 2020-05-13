package de.unidue.ltl.escrito.features.errors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Counts grammar anomalies assigned by LanguageToolChecker except citing related anomalies
 * 
 * original code moved to languagetool module because of dependency conflicts
 * 
 * 
 * 
 */


public class NumberOfGrammarMistakes extends FeatureExtractorResource_ImplBase implements FeatureExtractor{
	public static final String PARAM_INPUT_LOCATION = "locationOfInput";
	@ConfigurationParameter(name = PARAM_INPUT_LOCATION, mandatory = true)
	private String locationOfInput;
	
	public static final String FN_NR_OF_GRAMMAR_MISTAKES = "NrOfGrammarMistakes";
	
	private HashMap<String, double[]> errorMap= new HashMap<String, double[]>(); 

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		//read out the number of errors from source
		try {
			BufferedReader br = new BufferedReader(
			           new InputStreamReader(new FileInputStream(new File(locationOfInput)), "UTF-8"));
			String nextLine;
			while((nextLine = br.readLine()) != null){
				//skip the first line
				if(nextLine.startsWith("ID")){
					nextLine = br.readLine();
				}
				String[] nextItem = nextLine.split("\t");
				String id =nextItem[0];
				double numErrors = Double.parseDouble(nextItem[1]);
				double[] numbers = {numErrors};
				errorMap.put(id, numbers);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public Set<Feature> extract(JCas aJCas, TextClassificationTarget target) throws TextClassificationException {
		DocumentMetaData dmd = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String id = dmd.getDocumentId();
		Set<Feature> features = new HashSet<Feature>();
		//adjustment so that "dummy key" will not lead to NullPointerException
		if (errorMap.containsKey(id)) {
			features.add(new Feature("NrOfGrammarMistakes", errorMap.get(id)[0], FeatureType.NUMERIC));
//		features.add(new Feature("grammarError", errorMap.get(id)[1]));
//		features.add(new Feature("spellingError", errorMap.get(id)[2]));
		} else {
			System.out.println("The key " + id + " is not included in the map!");
		}
		return features;
	}
}



//@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly" })
//public class NumberOfGrammarMistakes extends FeatureExtractorResource_ImplBase
//implements FeatureExtractor{
//
//	public static final String FN_NR_OF_GRAMMAR_MISTAKES = "NrOfGrammarMistakes";
//	//TODO Fehlererkennung erkennt Fehler nicht (Kasusfehler?)
//	@Override
//	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
//			throws TextClassificationException 
//	{		
//		double numberOfMistakes=0;
//		AnnotationIndex<Annotation> index=jcas.getAnnotationIndex(GrammarAnomaly.type);
//		Iterator<Annotation> iterator= index.iterator();
//		while(iterator.hasNext()){
//			GrammarAnomaly anno= (GrammarAnomaly) iterator.next();
//			String annoText= anno.getCoveredText();
//			System.out.println(annoText+"\t"+anno.toString());
//			//don't consider citing
//			if(!annoText.matches(".*\\d.*")&& !annoText.contains(".")){
//				numberOfMistakes++;
//			}
//		}
//		//Normalization on total count of words
//		System.out.println("Mistakes: "+numberOfMistakes);
//		if(numberOfMistakes>0){
//			numberOfMistakes=numberOfMistakes/JCasUtil.select(jcas, Token.class).size();
//		}
//		Set<Feature> featList = new HashSet<Feature>();
//		System.out.println("Mistakes: "+numberOfMistakes);
//		featList.add(new Feature(FN_NR_OF_GRAMMAR_MISTAKES, numberOfMistakes, FeatureType.NUMERIC));
//		return featList;
//	}
//
//}
