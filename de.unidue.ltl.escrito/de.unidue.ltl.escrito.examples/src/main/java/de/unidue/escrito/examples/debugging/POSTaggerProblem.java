package de.unidue.escrito.examples.debugging;


import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.examples.basics.FeatureSettings;
import de.unidue.ltl.escrito.io.generic.GenericDatasetReader;

public class POSTaggerProblem extends Experiments_ImplBase implements Constants{

	public static void main(String[] args) throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "de");

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(
				"In seinem Kommentar in der Süddeutschen Zeitung über die Studiengebühren wirft Roland Preuß 2014 die Frage auf,"
						+ " ob der Staat und somit die Gesellschaft jedem Studierenden das Studium finanzieren sollte. "
						+ "Zu dem Zeitpunkt der Verfassung haben alle Bundesländer die Studiengebühren abgeschafft und "
						+ "Preuß beschreibt die Situation an deutschen Unis mit: Arm lebt und lernt neben Reich. ");
		engine.process(jcas);
		System.out.println("Schritt 1 - fertig");

		AnalysisEngine engine2 = createEngine(Experiments_ImplBase.getPreprocessing("de"));		
		JCas jcas2 = engine2.newJCas();
		jcas2.setDocumentLanguage("de");
		jcas2.setDocumentText(
				"In seinem Kommentar in der Süddeutschen Zeitung über die Studiengebühren wirft Roland Preuß 2014 die Frage auf,"
						+ " ob der Staat und somit die Gesellschaft jedem Studierenden das Studium finanzieren sollte. "
						+ "Zu dem Zeitpunkt der Verfassung haben alle Bundesländer die Studiengebühren abgeschafft und "
						+ "Preuß beschreibt die Situation an deutschen Unis mit: Arm lebt und lernt neben Reich. ");
		engine.process(jcas2);
		System.out.println("Schritt 2 - fertig");


		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				GenericDatasetReader.class,
				GenericDatasetReader.PARAM_INPUT_FILE, "src/main/resources/exampleTexts/smallExampleDataset.tsv",
				GenericDatasetReader.PARAM_LANGUAGE, "en",
				GenericDatasetReader.PARAM_CORPUSNAME, "debug");
		runBaselineExperiment("debug", readerTrain, readerTrain, "en");
		System.out.println("Schritt 3 - fertig");
	}

	private static void runBaselineExperiment(String experimentName, CollectionReaderDescription readerTrain,
			CollectionReaderDescription readerTest, String languageCode) throws Exception {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();

		ParameterSpace pSpace = null;
		pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT), FeatureSettings.getFeatureSetsDimBaseline(),
				learningsArgsDims);
		runTrainTest(pSpace, experimentName, getPreprocessing(languageCode));
	}
}
