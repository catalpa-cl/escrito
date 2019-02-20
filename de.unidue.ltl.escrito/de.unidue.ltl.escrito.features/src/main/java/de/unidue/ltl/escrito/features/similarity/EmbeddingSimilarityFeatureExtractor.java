package de.unidue.ltl.escrito.features.similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;



public class EmbeddingSimilarityFeatureExtractor 
extends FeatureExtractorResource_ImplBase
implements PairFeatureExtractor{


	public static final String FEAT_EMBEDDING_SIM = "embeddingSimilarity";
	public static final String FEAT_EMBEDDING_SIM_PAIRWISE = "pairwiseEmbeddingSimilarity";

	public static final String PARAM_IGNORE_UNKNOWN = "ignoreUnknownWords";
	@ConfigurationParameter(name = PARAM_IGNORE_UNKNOWN, mandatory = false, defaultValue = "false")
	public boolean ignoreUnknownWords;

	public static final String PARAM_ONLY_CONTENT_WORDS = "onlyContentWords";
	@ConfigurationParameter(name = PARAM_ONLY_CONTENT_WORDS, mandatory = false, defaultValue = "false")
	public boolean onlyContentWords;


	public static final String PARAM_RESOURCE_LOCATION = "resourceLocation";
	@ConfigurationParameter(name = PARAM_RESOURCE_LOCATION, mandatory = true)
	public String resourceLocation;


	// TODO what one could parametrize: aggregation method (sum up, pairwise maximum etc, for now we just sum vectors)

	// TODO at some point we might need a different way of representing vectors
	Map<String, double[]> embeddingsMap = new HashMap<String, double[]>();


	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		embeddingsMap = readEmbeddings(resourceLocation);
		// what one could also do: read them in map only if they contain relevant words, extract those words in metacollector
		return true;
	}

	public static Map<String, double[]> readEmbeddings(String embeddingsSourceLocation) {
		Map<String, double[]> map = new HashMap<String, double[]>();
		//	System.out.println("Start reading embeddings from "+embeddingsSourceLocation);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(embeddingsSourceLocation));
			String line = br.readLine();
			while (line != null){
				String[] parts = line.split(" ");
				if (parts.length != 65){
					System.out.println("Strange line "+parts.length);
				}
				double[] vector = new double[64];
				for (int i = 1; i<65; i++){
					vector[i-1] = Double.parseDouble(parts[i]);
				}
				map.put(parts[0], vector);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		//	System.out.println("Read "+map.size()+" embeddings");
		return map;
	}

	@Override
	public Set<Feature> extract(JCas view1, JCas view2)
			throws TextClassificationException {
		Set<Feature> features = new HashSet<Feature>();
		List<String> wordsLA = null;
		List<String> wordsTA = null;
		if (onlyContentWords){
			wordsLA = Utils.extractAllContentWordsFromView(view1);
			wordsTA = Utils.extractAllContentWordsFromView(view2);
		} else {
			wordsLA = Utils.extractAllWordsFromView(view1);
			wordsTA = Utils.extractAllWordsFromView(view2);
		}

		// sum up feature vectors LA, TA
		// compute cosine similarity
		double[] vectorLA = produceAveragedVector(wordsLA);
		double[] vectorTA = produceAveragedVector(wordsTA);
//		System.out.println("LA: "+Arrays.toString(vectorLA));
//		System.out.println("TA: "+Arrays.toString(vectorTA));

		features.add(new Feature(FEAT_EMBEDDING_SIM, Utils.computeCosineSimilarity(vectorLA, vectorTA), FeatureType.NUMERIC));



		//find for each word in LA, the best match in TA
		double summedSimilarities = 0.0;
		for (String wordLA : wordsLA){
			double max = 0.0;
			for (String wordTA : wordsTA){
				double sim = 0.0;
				if (embeddingsMap.get(wordLA) != null && embeddingsMap.get(wordTA) != null){
					sim = Utils.computeCosineSimilarity(embeddingsMap.get(wordLA), embeddingsMap.get(wordTA));
				}
				if (sim > max){
					max = sim;
				}
			}
			summedSimilarities+= max;
		}
		features.add(new Feature(FEAT_EMBEDDING_SIM_PAIRWISE, summedSimilarities/wordsLA.size(), FeatureType.NUMERIC));

		return features;
	}

	private double[] produceAveragedVector(List<String> words) {
		double[] resultVector = new double[64];
		for (String word : words){
			/*	if (word.equals("to") ||
					word.equals("a") ||
					word.equals("from") ||
					word.equals("the") ||
					word.equals("our") ||
					word.equals("is") ||
					word.equals(".") ||
					word.equals("that") 
					){
				continue;
			}*/
			if (embeddingsMap.containsKey(word)){
				double[] wordVector = embeddingsMap.get(word);
		//		System.out.println(word+": "+Arrays.toString(wordVector));
				resultVector = Utils.addVectors(resultVector, wordVector);
			} else {
//				System.out.println("No embedding for "+word);
				if (ignoreUnknownWords){
					// don't do anything
				} else {
					double[] wordVector = embeddingsMap.get("_UNK");
//					System.out.println(word+": "+Arrays.toString(wordVector));
					resultVector = Utils.addVectors(resultVector, wordVector);
				}
			}
		}
		return resultVector;
	}





}

