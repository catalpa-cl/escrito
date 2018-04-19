package de.unidue.ltl.escrito.features.similarity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.escrito.features.similarity.meta.WordIdfCollector;


public class WordOverlapFeatureExtractor 
extends FeatureExtractorResource_ImplBase
implements PairFeatureExtractor, MetaDependent{


	public static final String FEAT_OVERLAP_TA = "TokenOverlapTargetAnswer";
	public static final String FEAT_OVERLAP_LA = "TokenOverlapReferenceAnswer";

	public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
	private String idfFile;

	public static final String PARAM_IGNORE_QUESTION_MATERIAL = "ignoreQuestionMaterial";
	@ConfigurationParameter(name = PARAM_IGNORE_QUESTION_MATERIAL, mandatory = false, defaultValue = "false")
	private boolean ignoreQuestionMaterial;

	public static final String PARAM_IGNORE_STOPWORDS = "ignoreStopwords";
	@ConfigurationParameter(name = PARAM_IGNORE_STOPWORDS, mandatory = false, defaultValue = "false")
	private boolean ignoreStopwords;

	
	public static final String PARAM_IGNORE_PUNCTUATION = "ignorePunctuation";
	@ConfigurationParameter(name = PARAM_IGNORE_PUNCTUATION, mandatory = false, defaultValue = "false")
	private boolean ignorePunctuation;

	
	
	public static final String PARAM_USE_IDF = "useIdf";
	@ConfigurationParameter(name = PARAM_USE_IDF, mandatory = false, defaultValue = "false")
	private boolean useIdf;


	private Map<String, Double> idfs;
	private double maxIdfValue;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			FileInputStream fis = new FileInputStream(new File(idfFile));
			ObjectInputStream ois = new ObjectInputStream(fis);
			idfs = (Map<String, Double>) ois.readObject();
			ois.close();
			fis.close();
			//System.out.println("Read "+idfs.keySet().size()+ " word idf entries from "+idfFile+" "+idfs.keySet().toString());
			maxIdfValue  = 0.0;
			for (String idf : idfs.keySet()){
				if (idfs.get(idf) > maxIdfValue){
					maxIdfValue = idfs.get(idf);
				}
			}
			System.out.println("maxIdfValue: "+maxIdfValue);
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		} catch(ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			System.exit(-1);
		}
		return true;
	}




	@Override
	public Set<Feature> extract(JCas view1, JCas view2)
			throws TextClassificationException {

		Set<Feature> features = new HashSet<Feature>();
		List<String> wordsLA = Utils.extractAllLemmasFromView(view1, ignoreQuestionMaterial, ignoreStopwords, ignorePunctuation);
		List<String> wordsTA = Utils.extractAllLemmasFromView(view2, ignoreQuestionMaterial, ignoreStopwords, ignorePunctuation);
		int containedWords = 0;
		for (String w1 : wordsLA){
			if (wordsTA.contains(w1)){
				if (useIdf){
					// if we have an idf value for a word, we use it
					if (this.idfs.containsKey(w1)){
						containedWords+=this.idfs.get(w1);
					} else {
						// otherwise take the maximal idf value that occured in the training data (most likely corresponding to a word seen once)	
						containedWords += maxIdfValue;
					}
				} else {
					containedWords++;
				}
			}
		}
		features.add(new Feature(FEAT_OVERLAP_TA, 1.0*containedWords/wordsTA.size(), FeatureType.NUMERIC));
		features.add(new Feature(FEAT_OVERLAP_LA, 1.0*containedWords/wordsLA.size(), FeatureType.NUMERIC));
		return features;
	}


	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(
			Map<String, Object> parameterSettings)
					throws ResourceInitializationException
					{
		System.out.println("I NEED A METACOLLECTOR!");
		return Arrays.asList(new MetaCollectorConfiguration(WordIdfCollector.class,
				parameterSettings).addStorageMapping(
						WordIdfCollector.PARAM_TARGET_LOCATION,
						WordOverlapFeatureExtractor.PARAM_SOURCE_LOCATION,
						WordIdfCollector.IDF_WORDS_DIR)
				);
					}

}

