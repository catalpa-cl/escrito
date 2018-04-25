package de.unidue.ltl.escrito.core.normalization;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import com.github.liblevenshtein.collection.dictionary.SortedDawg;
import com.github.liblevenshtein.serialization.PlainTextSerializer;
import com.github.liblevenshtein.serialization.Serializer;
import com.github.liblevenshtein.transducer.Algorithm;
import com.github.liblevenshtein.transducer.Candidate;
import com.github.liblevenshtein.transducer.ITransducer;
import com.github.liblevenshtein.transducer.factory.TransducerBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.AnnotationChecker;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;



/**
 * 
 * Spellcheckoption based on Levenshtein distance. Extracts all suggestions from the dictionary up to a certain Levenshtein distance.
 * Also contains methods for determining problems with merged words.
 * 
 * 
 * @author andrea
 *
 */



@TypeCapability(
		inputs={
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
		outputs={
				"de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly",
		"de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction"})

public class LevenshteinChecker
extends JCasAnnotator_ImplBase {
	/**
	 * Location from which the model is read. The model file is a simple word-list with one word
	 * per line.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
	private String dictPath;

	/**
	 * The character encoding used by the model.
	 */
	public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
	@ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String dictEncoding;

	/**
	 * Determines the maximum edit distance (as an int value) that a suggestion for a spelling error may have.
	 * E.g. if set to one suggestions are limited to words within edit distance 1 to the original word.
	 */
	public static final String PARAM_SCORE_THRESHOLD = "ScoreThreshold";
	@ConfigurationParameter(name = PARAM_SCORE_THRESHOLD, mandatory = true, defaultValue = "1")
	private int scoreThreshold;

	ITransducer<Candidate>[] transducers;


	SortedDawg dictionary = null;


	@Override
	public void initialize(final UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);
		InputStream is = null;
		transducers = new ITransducer[this.scoreThreshold];
		try {
			InputStream stream = Files.newInputStream(Paths.get(dictPath));
			// The PlainTextSerializer constructor accepts an optional boolean specifying
			// whether the dictionary is already sorted lexicographically, in ascending
			// order.  If it is sorted, then passing true will optimize the construction
			// of the dictionary; you may pass false whether the dictionary is sorted or
			// not (this is the default and safest behavior if you don't know whether the
			// dictionary is sorted).
			final Serializer serializer = new PlainTextSerializer(false);
			dictionary = serializer.deserialize(SortedDawg.class, stream);
			System.out.println("Levenshtein threshold: "+this.scoreThreshold);
			for (int i = 1; i<= this.scoreThreshold; i++){
				ITransducer<Candidate> transducer = new TransducerBuilder()
						.dictionary(dictionary)
						.algorithm(Algorithm.TRANSPOSITION)
						.defaultMaxDistance(i)
						.includeDistance(true)
						.build();
				transducers[i-1] = transducer;
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			closeQuietly(is);
		}
		//System.err.println(scoreThreshold);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(final JCas jcas)
			throws AnalysisEngineProcessException
	{

		AnnotationChecker.requireExists(this, jcas, this.getLogger(), Token.class);
		AnnotationChecker.requireNotExists(this, jcas, this.getLogger(),
				SpellingAnomaly.class, SuggestedAction.class);

		for (Token t : select(jcas, Token.class)) {
			String tokenText = t.getCoveredText().toLowerCase();

			if (SpellingUtils.doNotSpellcheck(tokenText)){
				continue;
			}

			if (!dictionary.contains(tokenText)) {

				SpellingAnomaly anomaly = new SpellingAnomaly(jcas, t.getBegin(), t.getEnd());

				SuggestionCostTuples tuples = new SuggestionCostTuples();
				
				// merged words: if we split the word, both supparts exist in the lexicon
				for (int i = 0; i<tokenText.length(); i++){
					String word1 = tokenText.substring(0, i);
					String word2 = tokenText.substring(i, tokenText.length());
					if (dictionary.contains(word1) && dictionary.contains(word2)){
		//				System.out.println("Found\t"+tokenText+"\t"+word1+"\t"+word2);
						tuples.addTuple(word1+" "+word2, 1);
						break;
					}
				}
				
				// TODO: wir müssen Fälle wie "1.you" anders aufsplitten
				if (tokenText.contains(".")){
					String[] parts = tokenText.split("\\.");
					boolean allFound = true;
					for (String part : parts){
						if (!dictionary.contains(part) && (!(part.matches("\\d")))){
							allFound = false;
							break;
						}
					}
			//		System.out.println(tokenText+"\t"+allFound);
					if (allFound){
						String replacement = String.join(" . ", parts);
						replacement = replacement.replaceAll("(\\d) ", "$1");
						tuples.addTuple(replacement, parts.length-1);
					}
				}
				
				if (tokenText.contains(":")){
					String[] parts = tokenText.split(":");
					boolean allFound = true;
					for (String part : parts){
						if (!dictionary.contains(part) && (!(part.matches("\\d")))){
							allFound = false;
							break;
						}
					}
			//		System.out.println(tokenText+"\t"+allFound);
					if (allFound){
						String replacement = String.join(" : ", parts);
						replacement = replacement.replaceAll("(\\d) ", "$1");
						tuples.addTuple(replacement, parts.length-1);
					}
				}
				
				
				for (ITransducer<Candidate> it : transducers){
					for (Candidate candidate : it.transduce(tokenText.toLowerCase())){
						String suggestionString = candidate.term();
						int cost = candidate.distance();
						tuples.addTuple(suggestionString, cost);
					}
				}
				if (tuples.size() > 0) {
					FSArray actions = new FSArray(jcas, tuples.size());
					int i=0;
				//	System.out.print(anomaly.getCoveredText()+"\t");
					for (SuggestionCostTuple tuple : tuples) {
						SuggestedAction action = new SuggestedAction(jcas);
						action.setReplacement(tuple.getSuggestion());
						action.setCertainty(tuple.getNormalizedCost(tuples.getMaxCost()));
						actions.set(i, action);
						i++;
				//		System.out.print(action.getReplacement()+"\t");
					}
				//	System.out.println();
					anomaly.setSuggestions(actions);
					anomaly.addToIndexes();
				}else{
					anomaly.addToIndexes(jcas);
				}
			}
		}
	}

	class SuggestionCostTuples implements Iterable<SuggestionCostTuple> {
		private final List<SuggestionCostTuple> tuples;
		private int maxCost;

		public SuggestionCostTuples()
		{
			tuples = new ArrayList<SuggestionCostTuple>();
			maxCost = 0;
		}

		public void addTuple(String suggestion, int cost) {
			tuples.add(new SuggestionCostTuple(suggestion, cost));

			if (cost > maxCost) {
				maxCost = cost;
			}
		}

		public int getMaxCost() {
			return maxCost;
		}

		public int size() {
			return tuples.size();
		}

		@Override
		public Iterator<SuggestionCostTuple> iterator()
		{
			return tuples.iterator();
		}
	}

	class SuggestionCostTuple {
		private final String suggestion;
		private final Integer cost;

		public SuggestionCostTuple(String suggestion, Integer cost)
		{
			this.suggestion = suggestion;
			this.cost = cost;
		}

		public String getSuggestion()
		{
			return suggestion;
		}

		public Integer getCost()
		{
			return cost;
		}

		public float getNormalizedCost(int maxCost)
		{
			if (maxCost > 0) {
				return (float) cost / maxCost;
			}
			else {
				return 0f;
			}
		}
	}
}