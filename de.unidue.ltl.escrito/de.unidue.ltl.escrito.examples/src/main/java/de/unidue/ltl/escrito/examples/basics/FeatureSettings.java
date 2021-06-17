package de.unidue.ltl.escrito.examples.basics;

import org.dkpro.lab.task.Dimension;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.maxnormalization.TokenLengthRatio;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.features.ngram.PosNGram;
import org.dkpro.tc.features.ngram.SkipWordNGram;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.style.AdjectiveEndingFeatureExtractor;
import org.dkpro.tc.features.syntax.POSRatioFeatureExtractor;
import org.dkpro.tc.features.syntax.PronounRatioFeatureExtractor;
import org.dkpro.tc.features.syntax.QuestionsRatioFeatureExtractor;

import de.unidue.ltl.escrito.features.coherencecohesion.NrOfConnectives;
import de.unidue.ltl.escrito.features.coherencecohesion.PairwiseSentenceSimilarity;
import de.unidue.ltl.escrito.features.complexity.SyntacticVariability;
import de.unidue.ltl.escrito.features.complexity.SyntaxTreeDepth;
import de.unidue.ltl.escrito.features.complexity.TraditionalReadabilityMeasures;
import de.unidue.ltl.escrito.features.complexity.TypeTokenRatioFeatureExtractor;
import de.unidue.ltl.escrito.features.errors.NumberOfGrammarMistakes;
import de.unidue.ltl.escrito.features.length.AvgNrOfCharsPerSentence;
import de.unidue.ltl.escrito.features.length.AvgNrOfCharsPerToken;
import de.unidue.ltl.escrito.features.length.AvgNrOfTokensPerSentence;
import de.unidue.ltl.escrito.features.length.NrOfChars;
import de.unidue.ltl.escrito.features.length.NrOfSentences;
import de.unidue.ltl.escrito.features.length.NrOfTokens;
import de.unidue.ltl.escrito.features.occurence.NrOfCommas;
import de.unidue.ltl.escrito.features.occurence.NumberOfQuotations;
import de.unidue.ltl.escrito.features.occurence.NumberOfSubordinateClauses;
import de.unidue.ltl.escrito.features.occurence.SpeechThoughtWritingRepresentation;
import de.unidue.ltl.escrito.features.similarity.EmbeddingSimilarityFeatureExtractor;
import de.unidue.ltl.escrito.features.similarity.PairwiseFeatureWrapper;
import de.unidue.ltl.escrito.features.similarity.StringSimilarityFeatureExtractor;
import de.unidue.ltl.escrito.features.similarity.WordOverlapFeatureExtractor;

public class FeatureSettings
implements Constants
{


	/*
	 * Standard Baseline feature set for prompt-specific scoring 
	 */
	public static Dimension<TcFeatureSet> getFeatureSetsDimBaseline()
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						//			TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(
								WordNGram.class,
								WordNGram.PARAM_NGRAM_MIN_N, 1,
								WordNGram.PARAM_NGRAM_MAX_N, 3,
								WordNGram.PARAM_NGRAM_USE_TOP_K, 10000
								),
						TcFeatureFactory.create(
								CharacterNGram.class,
								CharacterNGram.PARAM_NGRAM_MIN_N, 2,
								CharacterNGram.PARAM_NGRAM_MAX_N, 5,
								CharacterNGram.PARAM_NGRAM_USE_TOP_K, 10000
								)
						)
				);
		return dimFeatureSets;
	}


	public static Dimension<TcFeatureSet> getFeatureSetNGrams()
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						//			TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(
								WordNGram.class,
								WordNGram.PARAM_NGRAM_MIN_N, 1,
								WordNGram.PARAM_NGRAM_MAX_N, 3,
								WordNGram.PARAM_NGRAM_USE_TOP_K, 10000
								)
						)
				);
		return dimFeatureSets;
	}		


	/*
	 * Standard Baseline feature set for prompt-specific scoring 
	 */
	public static Dimension<TcFeatureSet> getFeatureSetsSimilarity(String corpusName)
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						TcFeatureFactory.create(
								PairwiseFeatureWrapper.class,
								PairwiseFeatureWrapper.PARAM_PAIRWISE_FEATURE_EXTRACTOR, WordOverlapFeatureExtractor.class.getName(),
								PairwiseFeatureWrapper.PARAM_TARGET_ANSWER_PREFIX, "TA",
								PairwiseFeatureWrapper.PARAM_AGGREGATION_METHOD, PairwiseFeatureWrapper.AggregationMethod.MAXIMUM,
								PairwiseFeatureWrapper.PARAM_ADDITIONAL_TEXTS_LOCATION, System.getenv("DKPRO_HOME")+"/processedData/"+corpusName
								),
						TcFeatureFactory.create(
								PairwiseFeatureWrapper.class,
								PairwiseFeatureWrapper.PARAM_PAIRWISE_FEATURE_EXTRACTOR, EmbeddingSimilarityFeatureExtractor.class.getName(),
								EmbeddingSimilarityFeatureExtractor.PARAM_RESOURCE_LOCATION, System.getenv("DKPRO_HOME")+"/embed/"+"en.polyglot.txt",
								PairwiseFeatureWrapper.PARAM_TARGET_ANSWER_PREFIX, "TA",
								PairwiseFeatureWrapper.PARAM_AGGREGATION_METHOD, PairwiseFeatureWrapper.AggregationMethod.MAXIMUM,
								PairwiseFeatureWrapper.PARAM_ADDITIONAL_TEXTS_LOCATION, System.getenv("DKPRO_HOME")+"/processedData/"+corpusName
								),
						TcFeatureFactory.create(
								de.unidue.ltl.escrito.features.similarity.PairwiseFeatureWrapper.class,
								PairwiseFeatureWrapper.PARAM_PAIRWISE_FEATURE_EXTRACTOR, StringSimilarityFeatureExtractor.class.getName(),
								StringSimilarityFeatureExtractor.PARAM_STRING_TILING_MIN, "2",
								StringSimilarityFeatureExtractor.PARAM_STRING_TILING_MAX, "5",
								PairwiseFeatureWrapper.PARAM_TARGET_ANSWER_PREFIX, "TA",
								PairwiseFeatureWrapper.PARAM_AGGREGATION_METHOD, PairwiseFeatureWrapper.AggregationMethod.MAXIMUM,
								PairwiseFeatureWrapper.PARAM_ADDITIONAL_TEXTS_LOCATION, System.getenv("DKPRO_HOME")+"/processedData/"+corpusName
								)
						)
				);
		return dimFeatureSets;
	}


	/*
	 * Standard Baseline feature set for prompt-specific scoring 
	 */
	public static Dimension<TcFeatureSet> getFeatureSetsSimilarityDummy(String corpusName)
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						TcFeatureFactory.create(
								StringSimilarityFeatureExtractor.class
								),
						TcFeatureFactory.create(
								WordOverlapFeatureExtractor.class
								)
						)
				);
		return dimFeatureSets;
	}



	public static Dimension<TcFeatureSet> getFeatureSetsEssayFull()
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						TcFeatureFactory.create(
								WordNGram.class,
								WordNGram.PARAM_NGRAM_MIN_N, 1,
								WordNGram.PARAM_NGRAM_MAX_N, 3,
								WordNGram.PARAM_NGRAM_USE_TOP_K, 10000
								),
						TcFeatureFactory.create(
								PosNGram.class,
								PosNGram.PARAM_NGRAM_MIN_N, 1,
								PosNGram.PARAM_NGRAM_MAX_N, 3,
								PosNGram.PARAM_NGRAM_USE_TOP_K, 1000
								),
						TcFeatureFactory.create(
								SkipWordNGram.class,
								SkipWordNGram.PARAM_SKIP_SIZE, 2,
								SkipWordNGram.PARAM_NGRAM_MIN_N, 2,
								SkipWordNGram.PARAM_NGRAM_MAX_N, 5,
								SkipWordNGram.PARAM_NGRAM_USE_TOP_K, 10000
								),					
						TcFeatureFactory.create(
								CharacterNGram.class,
								CharacterNGram.PARAM_NGRAM_MIN_N, 2,
								CharacterNGram.PARAM_NGRAM_MAX_N, 5,
								CharacterNGram.PARAM_NGRAM_USE_TOP_K, 10000
								),
						// Length
						TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(NrOfChars.class),
						TcFeatureFactory.create(NrOfSentences.class),
						TcFeatureFactory.create(AvgNrOfTokensPerSentence.class),
						TcFeatureFactory.create(AvgNrOfCharsPerSentence.class),
						TcFeatureFactory.create(AvgNrOfCharsPerToken.class),
						TcFeatureFactory.create(TokenLengthRatio.class),

						TcFeatureFactory.create(POSRatioFeatureExtractor.class),
						TcFeatureFactory.create(PronounRatioFeatureExtractor.class),
						TcFeatureFactory.create(QuestionsRatioFeatureExtractor.class),
						TcFeatureFactory.create(AdjectiveEndingFeatureExtractor.class),

						TcFeatureFactory.create(NrOfCommas.class),
						TcFeatureFactory.create(
								NrOfConnectives.class,
								NrOfConnectives.PARAM_CONNECTIVES_FILE_PATH, "src/main/resources/lists/en/connectives_en.txt"
								),
						TcFeatureFactory.create(NumberOfQuotations.class),
						TcFeatureFactory.create(
								SpeechThoughtWritingRepresentation.class,
								SpeechThoughtWritingRepresentation.PARAM_LANGUAGE, "en",
								//SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH, "src/main/resources/lists/de/reporting_verbs_krestel_de.txt"
								SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH, "src/main/resources/lists/en/reporting_verbs_krestel_en.txt"
								),
						TcFeatureFactory.create(TypeTokenRatioFeatureExtractor.class),
						TcFeatureFactory.create(
								PairwiseSentenceSimilarity.class,
								PairwiseSentenceSimilarity.PARAM_NR_OF_SAMPLES, 5
								),
						TcFeatureFactory.create(SyntaxTreeDepth.class, SyntaxTreeDepth.LANGUAGE, "de"),
						TcFeatureFactory.create(
								NumberOfSubordinateClauses.class,
								NumberOfSubordinateClauses.PARAM_LANGUAGE, "de",
								//								NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/causalIndicators_de.txt",
								//								NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/temporalIndicators_de.txt"
								NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/causalIndicators_en.txt",
								NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/temporalIndicators_en.txt"
								),
						TcFeatureFactory.create(SyntacticVariability.class),
						TcFeatureFactory.create(TraditionalReadabilityMeasures.class)
						//						TcFeatureFactory.create(NumberOfGrammarMistakes.class,
						//								NumberOfGrammarMistakes.PARAM_INPUT_LOCATION, "src/main/resources/lists/de/grammarMistakes.txt")
						)
				);
		return dimFeatureSets;
	}


	public static Dimension<TcFeatureSet> getFeatureSetsEssayNoNgrams()
	{
		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				new TcFeatureSet(
						TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(NrOfChars.class),
						TcFeatureFactory.create(NrOfSentences.class),
						TcFeatureFactory.create(AvgNrOfTokensPerSentence.class),
						TcFeatureFactory.create(AvgNrOfCharsPerSentence.class),
						TcFeatureFactory.create(AvgNrOfCharsPerToken.class),
						TcFeatureFactory.create(TokenLengthRatio.class),

						TcFeatureFactory.create(POSRatioFeatureExtractor.class),
						TcFeatureFactory.create(PronounRatioFeatureExtractor.class),
						TcFeatureFactory.create(QuestionsRatioFeatureExtractor.class),
						TcFeatureFactory.create(AdjectiveEndingFeatureExtractor.class),

						TcFeatureFactory.create(NrOfCommas.class),
						TcFeatureFactory.create(
								NrOfConnectives.class,
								NrOfConnectives.PARAM_CONNECTIVES_FILE_PATH, "src/main/resources/lists/en/connectives_en.txt"
								),
						TcFeatureFactory.create(NumberOfQuotations.class),
						TcFeatureFactory.create(
								SpeechThoughtWritingRepresentation.class,
								SpeechThoughtWritingRepresentation.PARAM_LANGUAGE, "en",
								//SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH, "src/main/resources/lists/de/reporting_verbs_krestel_de.txt"
								SpeechThoughtWritingRepresentation.PARAM_REPORTING_VERBS_FILE_PATH, "src/main/resources/lists/en/reporting_verbs_krestel_en.txt"
								),
						TcFeatureFactory.create(TypeTokenRatioFeatureExtractor.class),
						TcFeatureFactory.create(
								PairwiseSentenceSimilarity.class,
								PairwiseSentenceSimilarity.PARAM_NR_OF_SAMPLES, 5
								),
						TcFeatureFactory.create(SyntaxTreeDepth.class, SyntaxTreeDepth.LANGUAGE, "de"),
						TcFeatureFactory.create(
								NumberOfSubordinateClauses.class,
								NumberOfSubordinateClauses.PARAM_LANGUAGE, "de",
								//								NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/causalIndicators_de.txt",
								//								NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/de/temporalIndicators_de.txt"
								NumberOfSubordinateClauses.PARAM_CAUSAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/causalIndicators_en.txt",
								NumberOfSubordinateClauses.PARAM_TEMPORAL_INDICATORS_FILE_PATH, "src/main/resources/lists/en/temporalIndicators_en.txt"
								),
						TcFeatureFactory.create(SyntacticVariability.class),
						TcFeatureFactory.create(TraditionalReadabilityMeasures.class)
						//						TcFeatureFactory.create(NumberOfGrammarMistakes.class,
						//								NumberOfGrammarMistakes.PARAM_INPUT_LOCATION, "src/main/resources/lists/de/grammarMistakes.txt")
						)
				);
		return dimFeatureSets;
	}



}
