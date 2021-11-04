//package de.unidue.ltl.escrito.features.similarity;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.uima.fit.descriptor.ConfigurationParameter;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.apache.uima.resource.ResourceSpecifier;
//import org.dkpro.tc.api.exception.TextClassificationException;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
//import org.dkpro.tc.api.features.FeatureType;
//import org.dkpro.tc.api.features.PairFeatureExtractor;
//
//import edu.cmu.lti.jawjaw.pobj.POS;
//import edu.cmu.lti.lexical_db.ILexicalDatabase;
//import edu.cmu.lti.lexical_db.NictWordNet;
//import edu.cmu.lti.lexical_db.data.Concept;
//import edu.cmu.lti.ws4j.Relatedness;
//import edu.cmu.lti.ws4j.RelatednessCalculator;
//import edu.cmu.lti.ws4j.impl.HirstStOnge;
//import edu.cmu.lti.ws4j.impl.JiangConrath;
//import edu.cmu.lti.ws4j.impl.LeacockChodorow;
//import edu.cmu.lti.ws4j.impl.Lesk;
//import edu.cmu.lti.ws4j.impl.Lin;
//import edu.cmu.lti.ws4j.impl.Path;
//import edu.cmu.lti.ws4j.impl.Resnik;
//import edu.cmu.lti.ws4j.impl.WuPalmer;
//
//
///**
// * Feature Extractor for wordnet based sentence similarity features
// * as described in Mihalcea et. al 2006
// * 
// * TODO: do we care about POS? We probably should.
// * 
// */
//
//
//public class WordNetSimilarityFeatureExtractor 
//extends FeatureExtractorResource_ImplBase
//implements PairFeatureExtractor{
//
//
//	public static final String FEAT_MCS = "AggregatedWordNetSimilarity";
//	public static final String FEAT_MCS_REL_TA = "AggregatedWordNetSimilarityBasisTA";
//	public static final String FEAT_MCS_REL_LA = "AggregatedWordNetSimilarityBasisLA";
//
//
//	public static final String PARAM_CORPUS_NAME = "corpusName";
//	@ConfigurationParameter(name = PARAM_CORPUS_NAME, mandatory = true)
//	private String corpusName;
//
//	
//	public static final String PARAM_PROMPT_ID = "promptId";
//	@ConfigurationParameter(name = PARAM_PROMPT_ID, mandatory = false)
//	private String promptId;
//
//	
//	private static ILexicalDatabase db = new NictWordNet();
//	private static RelatednessCalculator[] rcs = {
//		//new BaselineRelatedness(db),
//		//new HirstStOnge(db), // very slow
//		//new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
//		//new Resnik(db), 
//		new JiangConrath(db)
//		//, new Lin(db), new Path(db)
//	};
//
//
//
//	// Map from Metric to set of words to value
//	private Map<String, Map<Set<String>,Double>> cache = new HashMap();
//	private Map<String, Double> idfValues;
//
//	@Override
//	public boolean initialize(ResourceSpecifier aSpecifier,
//			Map aAdditionalParams) throws ResourceInitializationException {
//		if (!super.initialize(aSpecifier, aAdditionalParams)) {
//			return false;
//		}
//		File f = new File("src/main/resources/preparedWordNetSimilarities/"+corpusName+"_"+promptId+"_wordnet.ser");
//		if (f.exists()){
//			FileInputStream fis;
//			try {
//				fis = new FileInputStream("src/main/resources/preparedWordNetSimilarities/"+corpusName+"_"+promptId+"_wordnet.ser");
//				ObjectInputStream ois = new ObjectInputStream(fis);
//				this.cache = (HashMap<String, Map <Set<String>,Double>>) ois.readObject();
//				ois.close();
//				fis.close();
//			} catch (IOException | ClassNotFoundException e) {
//				e.printStackTrace();
//				System.exit(-1);
//			}
//			System.out.println("READ CACHED SIMILARITIES!");
//		//	System.exit(-1);
//		} else {
//			System.out.println("NO CACHED SIMILARITIES FOUND!");
//			System.exit(-1);
//		//	this.cache = new HashMap<String, Map <Set<String>,Double>>();	
//		}
//		for ( RelatednessCalculator rc : rcs ) {
//			if (!(this.cache.containsKey(rc.getClass().getName()))){
//				this.cache.put(rc.getClass().getName(), new HashMap<Set<String>, Double>());
//			}
//		}
//		return true;
//	}
//
//
//
//
//	@Override
//	public Set<Feature> extract(JCas view1, JCas view2)
//			throws TextClassificationException {
//
//	/*	for ( RelatednessCalculator rc : rcs ) {
//			if (!(this.cache.containsKey(rc.getClass().getName()))){
//				this.cache.put(rc.getClass().getName(), new HashMap<Set<String>, Double>());
//			}
//		}*/
//		
//		Set<Feature> features = new HashSet<Feature>();
//
//		List<String> words1 = Utils.extractAllContentWordLemmasFromView(view1);
//		List<String> words2 = Utils.extractAllContentWordLemmasFromView(view2);
//		idfValues = new HashMap<String, Double>();
//		for (String word: words1){
//			idfValues.put(word, 1.0);
//		}
//		for (String word: words2){
//			idfValues.put(word, 1.0);
//		}
//		for ( RelatednessCalculator rc : rcs ) {
//			//	System.out.println("Using relatedness measure "+rc.getClass().getName());
//			double relatedness1 = getDirectionalRelatedness(words1, words2, rc);
//			if (Double.isNaN(relatedness1)){
//				relatedness1 = 0.0;
//			}
//			double relatedness2 = getDirectionalRelatedness(words2, words1, rc);
//			if (Double.isNaN(relatedness2)){
//				relatedness2 = 0.0;
//			}
//			//	System.out.println(rc.getClass().getName()+"\t"+relatedness1+"\t"+relatedness2);
//			features.add(new Feature(FEAT_MCS_REL_LA+"_"+rc.toString(), relatedness1, FeatureType.NUMERIC));
//			features.add(new Feature(FEAT_MCS_REL_TA+"_"+rc.toString(), relatedness2, FeatureType.NUMERIC));
//			features.add(new Feature(FEAT_MCS+"_"+rc.toString(), 0.5 * ( relatedness1 + relatedness2), FeatureType.NUMERIC));
//		}
//		return features;
//	}
//
//
//	// Using code from dkpro similarity MCS06AggregateComparator
//
//	private double getDirectionalRelatedness(Collection<String> stringList1,
//			Collection<String> stringList2, RelatednessCalculator rc) {
//		double weightedSum = 0.0;
//		double idfSum = 0.0; 
//
//		for (String w1 : stringList1) {
//			w1 = w1.toLowerCase();
//			Set<Double> subscores = new HashSet<Double>();	
//			//		System.out.println("word1: "+w1);
//			for (String w2 : stringList2) {		
//				w2 = w2.toLowerCase();
//				//			System.out.println("word2: "+w2);
//				Set<String> wordset = new HashSet<String>();
//				wordset.add(w1);
//				wordset.add(w2);
//				double score;
//				if (cache.get(rc.getClass().getName()).containsKey(wordset)) {
//					score = cache.get(rc.getClass().getName()).get(wordset);
//				//	System.out.println("old");
//				} else { 
//					score = getWordSimilarity(w1, w2, rc);
//					cache.get(rc.getClass().getName()).put(wordset, score);
//				//	System.out.println("new");
//				}
//			//	System.out.println("Score: "+score);
//				subscores.add(score);
//			}
//
//			// Get best score for the pair (w1, w2)
//			double bestSubscore = 0.0;
//			if (stringList2.size() > 0) {
//				// we have a similarity measure - higher is better
//				bestSubscore = Collections.max(subscores);
//				// Handle error cases such as "not found"
//				if (bestSubscore < 0.0) {
//					bestSubscore = 0.0;
//				}
//			}
//		//	System.out.println("Bestscore: "+bestSubscore);	
//			// Weight
//			double weightedScore;
//			if (idfValues.containsKey(w1)) {
//				weightedScore = bestSubscore * idfValues.get(w1);
//
//				weightedSum += weightedScore;
//				//			System.out.println("weightedSum: "+weightedSum);
//				idfSum += idfValues.get(w1);
//			} else {
//				// Well, ignore this token.
//				//System.out.println("Ignoring token: \"" + w1 + "\"");
//			}
//		}
//	//	System.out.println(weightedSum+"\t"+idfSum);
//		return weightedSum / idfSum;
//	}
//
//	public double getWordSimilarity(String w1, String w2, RelatednessCalculator rc) {
//		if (rc instanceof BaselineRelatedness){
//			return rc.calcRelatednessOfWords(w1, w2);
//		} else {
//			if (w1.equals(w2)){
//				return 1.0;
//			}
//			List<POS[]> posPairs = rc.getPOSPairs();
//			double maxScore = -1D;
//			for(POS[] posPair: posPairs) {
//				List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(w1, posPair[0].toString());
//				List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(w2, posPair[1].toString());
//				for(Concept synset1: synsets1) {
//					for (Concept synset2: synsets2) {
//						Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
//						double score = relatedness.getScore();
//						// bug in the wordnet library, words from the same synset can get very large relatedness scores
//						if (score > 1.0){
//							score = 1.0;
//						}
//						if (score > maxScore) { 
//							maxScore = score;
//						}
//					}
//				}
//			}
//			if (maxScore == -1D) {
//				maxScore = 0.0;
//			}
//			return maxScore;
//		}
//	}
//
//
//}
//
//
///* 
// * A basic variant of lexical relatedness that is returning 1 for string-identical words and 0 otherwise
// * 
// */
//
//class BaselineRelatedness extends RelatednessCalculator{
//
//	public BaselineRelatedness(ILexicalDatabase db) {
//		super(db);
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	protected Relatedness calcRelatedness(Concept arg0, Concept arg1) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public double calcRelatednessOfWords(String word0, String word1) {
//		if (word0.equals(word1)){
//			return 1.0;
//		} else {
//			return 0.0;
//		}
//	}
//
//	@Override
//	public List<POS[]> getPOSPairs() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
