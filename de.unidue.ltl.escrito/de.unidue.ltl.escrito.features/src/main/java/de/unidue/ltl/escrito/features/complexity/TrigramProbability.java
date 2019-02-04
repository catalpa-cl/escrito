package de.unidue.ltl.escrito.features.complexity;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import com.googlecode.jweb1t.JWeb1TIterator;
import com.googlecode.jweb1t.JWeb1TSearcher;
import com.googlecode.jweb1t.util.NGramIterator;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/* requires Web1T trigram files + corresponding .idx File
 */

public class TrigramProbability extends FeatureExtractorResource_ImplBase implements FeatureExtractor{

	public static final String MINIMAL_TRIGRAM_PROBABILITY = "minTrigramProbability";
	public static final String AVERAGE_TRIGRAM_PROBABILITY = "averageTrigramProbability";
	
	File web1tFile = new File(System.getenv("DKPRO_HOME")+"/web1t/en/data");
	JWeb1TSearcher searcher = null;
	
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		
		try {
			searcher = new JWeb1TSearcher(web1tFile,3,3);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return true;
	}
	
	@Override
	public Set<Feature> extract(JCas arg0, TextClassificationTarget arg1) throws TextClassificationException {

		double min = Integer.MAX_VALUE;
		double average = 0;
		double freq;
		
		FrequencyDistribution<String> fd = NGramUtils.getDocumentNgrams(arg0, arg1, false, false, 3, 3, new HashSet<String>(), Token.class);
		
		for(String s : fd.getKeys()) {
			s = s.replaceAll("_", " ");
			//System.out.println(s);
			try {
				freq = searcher.getFrequency(s);

				if(freq == 0) {
					System.out.println("not found in web1t:\t"+s);
				}
				
			} catch (IOException e) {
				freq = 0;
				e.printStackTrace();
			}
			
				if(min > freq) {
					min = freq;
				}
				average += freq;
		}
		
		average = average/fd.getKeys().size();
		
		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(MINIMAL_TRIGRAM_PROBABILITY,min,FeatureType.NUMERIC));
		features.add(new Feature(AVERAGE_TRIGRAM_PROBABILITY,average, FeatureType.NUMERIC));

		return features;
	}
	
	

}
