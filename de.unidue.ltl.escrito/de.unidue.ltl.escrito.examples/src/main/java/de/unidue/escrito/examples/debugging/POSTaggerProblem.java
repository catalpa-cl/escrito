package de.unidue.escrito.examples.debugging;


import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.unidue.ltl.escrito.examples.basics.Experiments_ImplBase;

public class POSTaggerProblem {

	public static void main(String[] args) throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "de");

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(
				"In seinem Kommentar in der Süddeutschen Zeitung über die Studiengebühren wirft Roland Preuß 2014 die Frage auf,"
						+ " ob der Staat und somit die Gesellschaft jedem Studierenden das Studium finanzieren sollte. "
						+ "Zu dem Zeitpunkt der Verfassung haben alle Bundesländer die Studiengebühren abgeschafft und "
						+ "Preuß beschreibt die Situation an deutschen Unis mit: Arm lebt und lernt neben Reich. ");
		engine.process(jcas);
		System.out.println("fertig");

		AnalysisEngine engine2 = createEngine(Experiments_ImplBase.getPreprocessing("de"));		
		JCas jcas2 = engine2.newJCas();
		jcas2.setDocumentLanguage("de");
		jcas2.setDocumentText(
				"In seinem Kommentar in der Süddeutschen Zeitung über die Studiengebühren wirft Roland Preuß 2014 die Frage auf,"
						+ " ob der Staat und somit die Gesellschaft jedem Studierenden das Studium finanzieren sollte. "
						+ "Zu dem Zeitpunkt der Verfassung haben alle Bundesländer die Studiengebühren abgeschafft und "
						+ "Preuß beschreibt die Situation an deutschen Unis mit: Arm lebt und lernt neben Reich. ");
		engine.process(jcas2);
		System.out.println("fertig2");
	}


}
