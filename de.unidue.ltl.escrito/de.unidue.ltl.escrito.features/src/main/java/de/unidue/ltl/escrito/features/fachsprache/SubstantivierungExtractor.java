package de.unidue.ltl.escrito.features.fachsprache;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
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
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Counts the appearance of the German noun-forming suffixes.
 * 
 * @author Yuning
 */

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class SubstantivierungExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	public static final String PARAM_SUFFIXES_FILE_PATH = "suffixesFilePath";
	@ConfigurationParameter(name = PARAM_SUFFIXES_FILE_PATH, mandatory = true)
	private String suffixesFilePath;

	private List<String> suffixes;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		suffixes = getSuffixes(suffixesFilePath);
		return true;
	}

	private List<String> getSuffixes(String suffixesFile) {
		List<String> list = new ArrayList<String>();
		Scanner s;
		try {
			s = new Scanner(new File(suffixesFile));
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();
			System.out.println("Read " + list.size() + " suffixes from " + suffixesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return list;
	}

	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		Set<Feature> featureList = new HashSet<Feature>();
		//sum of nouns
		int countNouns = 0;
		for (Token t : JCasUtil.select(view, Token.class)) {
			if(t.getPos().getPosValue().startsWith("N")){
				countNouns++;
			}
		}
		//System.out.println("Total Number of Nouns: "+countNouns);
		//frequency of nouns, which ends with suffixes
		FrequencyDistribution<String> suffixFD = new FrequencyDistribution<String>();
		for (String s : suffixes) {
			//System.out.print("["+s+"] : ");
			for (Token t : JCasUtil.select(view, Token.class)) {
				if(t.getPos().getPosValue().startsWith("N")&&t.getLemma().getValue().endsWith(s)){
					//System.out.println(t.getCoveredText());
					suffixFD.inc(s);
				}		
			}
			//System.out.println(suffixFD.getCount(s));
			featureList.add(new Feature("frequencyOf"+s.toUpperCase(),(double)suffixFD.getCount(s)/countNouns, FeatureType.NUMERIC));
		}
		featureList.add(new Feature("frequencyOfAllSuffixes",(double)suffixFD.getN()/countNouns, FeatureType.NUMERIC));
		return featureList;
	}
	
}
