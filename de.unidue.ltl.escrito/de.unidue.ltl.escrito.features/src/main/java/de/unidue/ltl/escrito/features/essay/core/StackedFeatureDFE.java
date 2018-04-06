package de.unidue.ltl.edu.scoring.features.essay.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.feature.InstanceIdFeature;

public class StackedFeatureDFE
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String STACKED_NGRAM_VALUE = "stackedNGramValue";
	public static final String STACKED_POSNGRAM_VALUE = "stackedPOSNGRAMValue";
	public static final String STACKED_SKIPNGRAM_VALUE = "stackedSkipNGRAMValue";
	public static final String STACKED_POSITIONNGRAM_VALUE = "stackedPositionNGRAMValue";

	public static final String PARAM_ID2OUTCOME_FOLDER_PATH = "id2outcomeFilePath";
	@ConfigurationParameter(name = PARAM_ID2OUTCOME_FOLDER_PATH, mandatory = true)
	private String id2outcomeFilePath;

	private Map<String, Integer> id2Outcome_ngram;
	private Map<String, Integer> id2Outcome_POSngram;
	private Map<String, Integer> id2Outcome_Skipngram;
	private Map<String, Integer> id2Outcome_Positionngram;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		id2Outcome_ngram = getId2OutcomeMap(id2outcomeFilePath+"/id2outcome.txt");
		id2Outcome_POSngram=getId2OutcomeMap(id2outcomeFilePath+"/id2outcome_POSngram.txt");
		id2Outcome_Skipngram=getId2OutcomeMap(id2outcomeFilePath+"/id2outcome_skipngram.txt");
		id2Outcome_Positionngram=getId2OutcomeMap(id2outcomeFilePath+"/id2outcome_positionNgram.txt");
		return true;
	}

	@SuppressWarnings("resource")
	private Map<String, Integer> getId2OutcomeMap(String id2outcomeFilePath) {
		Map<String, Integer> map =  new HashMap<String, Integer>();
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(id2outcomeFilePath));
			while ((line = reader.readLine()) != null) {
				if(!line.startsWith("#")){
					String[] pair=line.split("=");
					map.put(pair[0], Integer.parseInt( pair[1].split(";")[0]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		String id= (String) InstanceIdFeature.retrieve(jcas).getValue();
		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(STACKED_NGRAM_VALUE,  id2Outcome_ngram.get(id)));
		featList.add(new Feature(STACKED_POSNGRAM_VALUE,  id2Outcome_POSngram.get(id)));
		featList.add(new Feature(STACKED_SKIPNGRAM_VALUE,  id2Outcome_Skipngram.get(id)));
		featList.add(new Feature(STACKED_POSITIONNGRAM_VALUE,  id2Outcome_Positionngram.get(id)));
		return featList;
	}

}
