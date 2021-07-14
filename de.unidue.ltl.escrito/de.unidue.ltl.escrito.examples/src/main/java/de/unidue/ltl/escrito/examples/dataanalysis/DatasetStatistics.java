package de.unidue.ltl.escrito.examples.dataanalysis;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.io.shortanswer.Asap2Reader;
import de.unidue.ltl.escrito.io.shortanswer.CreeReader;
import de.unidue.ltl.escrito.io.shortanswer.CregReader;
import de.unidue.ltl.escrito.io.shortanswer.CssagReader;
import de.unidue.ltl.escrito.io.shortanswer.MohlerMihalceaReader;
import de.unidue.ltl.escrito.io.shortanswer.PT_AsagReader;
import de.unidue.ltl.escrito.io.shortanswer.PowerGradingReader;
import de.unidue.ltl.escrito.io.shortanswer.SRAReader;


public class DatasetStatistics {

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException{
		//		analyzeASAP_DE();
//		analyzeSRA_beetle();
		analyzeSRA_sciEntsBank();
		//		analyzeCREG();
		//		analyzeCREE();
		//		analyzeCSSAG();
		//		analyzePT_ASAG();

		//		analyzePowergrading();
		//		analyzeASAP();
		//		analyzeMohlerMihalcea();
	}


	private static void analyzeMohlerMihalcea() throws ResourceInitializationException, UIMAException, IOException {
		for (int id : MohlerMihalceaReader.promptIds){
			//	System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					MohlerMihalceaReader.class,
					MohlerMihalceaReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/mohler_and_mihalcea/basicDataset/assign.txt",
					MohlerMihalceaReader.PARAM_PROMPT_IDS, id,
					MohlerMihalceaReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					MohlerMihalceaReader.PARAM_QUESTION_PREFIX, "Q",
					MohlerMihalceaReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
	}


	private static void analyzeCREG() throws ResourceInitializationException, UIMAException, IOException {
		for (String id : CregReader.PromptIds_KU){
			//	System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					CregReader.class,
					CregReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/CREG/CREG-1032/KU-data.xml",
					CregReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					CregReader.PARAM_PROMPT_SET_ID, id,
					CregReader.PARAM_QUESTION_PREFIX, "Q",
					CregReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "de"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
		for (String id : CregReader.PromptIds_OSU){
			//	System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					CregReader.class,
					CregReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/CREG/CREG-1032/OSU-data.xml",
					CregReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					CregReader.PARAM_PROMPT_SET_ID, id,
					CregReader.PARAM_QUESTION_PREFIX, "Q",
					CregReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "de"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
	}

	private static void analyzeCREE() throws ResourceInitializationException, UIMAException, IOException {
		for (String id : CreeReader.PromptIds_dev){
			//		System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					CreeReader.class,
					CreeReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/CREE/corpus-development.xml",
					CreeReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					CreeReader.PARAM_PROMPT_SET_ID, id,
					CreeReader.PARAM_QUESTION_PREFIX, "Q",
					CreeReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
		for (String id : CreeReader.PromptIds_test){
			//		System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					CreeReader.class,
					CreeReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/CREE/corpus-test.xml",
					CreeReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					CreeReader.PARAM_PROMPT_SET_ID, id,
					CreeReader.PARAM_QUESTION_PREFIX, "Q",
					CreeReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
	}


	private static void analyzeCSSAG() throws ResourceInitializationException, UIMAException, IOException {
		for (int id = 1; id <=31; id++){
			//	System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					CssagReader.class,
					CssagReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/CSSAG/Questions/Question"+id+".xml",
					CssagReader.PARAM_PROMPT_SET_ID, String.valueOf(id),
					CssagReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					CssagReader.PARAM_QUESTION_PREFIX, "Q",
					CssagReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "de"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
	}

	private static void analyzeSRA_beetle() throws ResourceInitializationException, UIMAException, IOException {
		for (String id : SRAReader.PromptSetIds_beetle){
			//	System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					SRAReader.class,
					SRAReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/beetle/",
					SRAReader.PARAM_PROMPT_SET_ID, id,
					SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
					SRAReader.PARAM_QUESTION_PREFIX, "Q",
					SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
			AnalysisEngineDescription segmenter =createEngineDescription(
					BreakIteratorSegmenter.class,
					BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
					);
			AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
			SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
		}
	}



		private static void analyzeSRA_sciEntsBank() throws ResourceInitializationException, UIMAException, IOException {
			for (String id : SRAReader.PromptSetIds_SciEntsBank){
				//		System.out.println(id);
				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						SRAReader.class,
						SRAReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/SRA/training/2way/SciEntsBank/",
						SRAReader.PARAM_PROMPT_SET_ID, id,
						SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
						SRAReader.PARAM_QUESTION_PREFIX, "Q",
						SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
				AnalysisEngineDescription segmenter =createEngineDescription(
						BreakIteratorSegmenter.class,
						BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
						);
				AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
				SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
			}
		}


		private static void analyzePT_ASAG() throws ResourceInitializationException, UIMAException, IOException {
			for (int id : PT_AsagReader.PromptSetIds){
				//	System.out.println(id);
				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						PT_AsagReader.class,
						PT_AsagReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/PT_ASAG/PT_ASAG_2018_V1.0/student_answers_and_grades.csv",
						PT_AsagReader.PARAM_PROMPT_SET_ID, String.valueOf(id),
						PT_AsagReader.PARAM_QUESTION_PREFIX, "Q",
						PT_AsagReader.PARAM_TARGET_ANSWER_PREFIX, "TA");
				AnalysisEngineDescription segmenter =createEngineDescription(
						BreakIteratorSegmenter.class,
						BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
						);
				AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
				SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
			}
		}


		private static void analyzeASAP_DE() throws ResourceInitializationException, UIMAException, IOException {
			for (int id : new int[]{1,2,10}){
				System.out.println(id);
				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						Asap2Reader.class,
						Asap2Reader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/asap/crosslingual/asap_de/germanAsap_clean.txt",
						Asap2Reader.PARAM_PROMPT_IDS, id);
				AnalysisEngineDescription segmenter =createEngineDescription(
						BreakIteratorSegmenter.class,
						BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
						);
				AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
				SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
			}
		}

		private static void analyzePowergrading() throws UIMAException, IOException {
			for (int id : PowerGradingReader.promptIds){
				//	System.out.println("\n"+id);
				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						PowerGradingReader.class,
						PowerGradingReader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/powergrading//studentanswers_grades_698.tsv",
						PowerGradingReader.PARAM_PROMPT_IDS, id);
				AnalysisEngineDescription segmenter =createEngineDescription(
						BreakIteratorSegmenter.class,
						BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
						);
				AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
				SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
			}
		}

		private static void analyzeASAP() throws ResourceInitializationException, UIMAException, IOException {
			for (int id : Asap2Reader.promptIds){
				//	System.out.println(id);
				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						Asap2Reader.class,
						Asap2Reader.PARAM_INPUT_FILE, System.getenv("DKPRO_HOME")+"/datasets/asap/originalData/train_plus_test_repaired.txt",
						Asap2Reader.PARAM_PROMPT_IDS, id);
				AnalysisEngineDescription segmenter =createEngineDescription(
						BreakIteratorSegmenter.class,
						BreakIteratorSegmenter.PARAM_LANGUAGE, "en"
						);
				AnalysisEngineDescription datasetAnalyzer =createEngineDescription(DatasetAnalyzer.class);
				SimplePipeline.runPipeline(reader, segmenter, datasetAnalyzer);
			}
		}

	}
