package de.unidue.ltl.escrito.features.fachsprache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Counts the appearance of the German noun-forming suffixes.
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

	private List<String> getSuffixes(String suffixesFile)
			throws ResourceInitializationException
	{
		List<String> list = new ArrayList<String>();
		
		try {
			for (String suffix : FileUtils.readLines(new File(suffixesFile))) {
				list.add(suffix);
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		return list;
	}

	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		Set<Feature> featureList = new HashSet<Feature>();

		int countNouns = JCasUtil.select(view, NN.class).size();
		
		FrequencyDistribution<String> suffixFD = new FrequencyDistribution<String>();

		for (String s : suffixes) {
			for (Token t : JCasUtil.select(view, Token.class)) {
				POS pos = t.getPos();
				if (pos instanceof NN) {
					if (t.getLemma().getValue().endsWith(s)){
						suffixFD.inc(s);
					}	
				}
			}
			double suffixRatio = (double) suffixFD.getCount(s) / countNouns;
			featureList.add(new Feature("frequencyOf" + s.toUpperCase(), suffixRatio, FeatureType.NUMERIC));
		}
		
		double nominalizations = (double)suffixFD.getN() / countNouns;
		featureList.add(new Feature("frequencyOfAllSuffixes", nominalizations, FeatureType.NUMERIC));
		return featureList;
	}
	
}
