package de.unidue.ltl.escrito.features.complexity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.googlecode.jweb1t.JWeb1TSearcher;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WordWeb1TFrequency
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String AVG_TOKEN_WEB1TFREQUENCY = "avgTokenWeb1TFrequency";
	public static final String MEDIAN_TOKEN_WEB1TFREQUENCY = "medianTokenWeb1TFrequency";
	public static final String PARAM_WEB1T_FILE_PATH = "web1TFilePath";
	
	// TODO convert to resource version like used in ctest experiments
	@ConfigurationParameter(name = PARAM_WEB1T_FILE_PATH, mandatory = true)
	private String web1TFilePath;

	private JWeb1TSearcher searcher;
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			searcher = new JWeb1TSearcher(new File(web1TFilePath), 1, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		Set<Feature> featList = new HashSet<Feature>();
		
		double avgWordFrequency = 0;
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		double[] frequencies= new double[tokens.size()];
		int i=0;
		for (Token token : tokens) {
			double tokenFrequency= getTokenFreqency(token.getCoveredText());
			if(!Double.isNaN(tokenFrequency)){
				avgWordFrequency+=tokenFrequency;
				frequencies[i]=tokenFrequency;
				i++;
			}
		}
		//Normalization on total count of words
		avgWordFrequency= (double)avgWordFrequency/tokens.size();
		featList.add(new Feature(AVG_TOKEN_WEB1TFREQUENCY, avgWordFrequency, FeatureType.NUMERIC));
		featList.add(new Feature(MEDIAN_TOKEN_WEB1TFREQUENCY,calcMedian(frequencies), FeatureType.NUMERIC));
		return featList;
	}


	private double getTokenFreqency(String coveredText) {
		double frequency=0.0;
		try {
			frequency= searcher.getFrequency(coveredText);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(frequency==0.0)return 0.0;
		else return Math.log(frequency);
	}

	 public double calcMedian(double[] frequencies)
	  {
	    Arrays.sort(frequencies);
	    int medianPosition = frequencies.length/2;
	    if (frequencies.length%2==0){
	      return (frequencies[medianPosition - 1]+frequencies[medianPosition])/2;
	    }
	    else{
	      return frequencies[medianPosition];
	    }
	  }
	 //for testing only
	public void init(String filePath){
		try {
			searcher = new JWeb1TSearcher(new File(filePath), 1, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
