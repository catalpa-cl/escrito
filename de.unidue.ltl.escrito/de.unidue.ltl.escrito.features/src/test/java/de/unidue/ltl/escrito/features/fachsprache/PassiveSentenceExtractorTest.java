package de.unidue.ltl.escrito.features.fachsprache;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Assert;
import org.junit.Test;

import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.fachsprache.PassiveSentenceExtractor;


public class PassiveSentenceExtractorTest extends EssayGradingTestBase {
	
	@Test
	public void PassiveFeatureExtractorTest() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",false);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText("Ich schließe die Tür. Die Tür wird geschlossen. "
				+ "Die Suppe wird gekocht. Jemand kocht die Suppe. "
				+ "Das Tor kann nicht geöffnet werden. Das Tor lässt sich nicht öffnen. "
				+ "Die Aufgabe kann nicht gelöst werden. Die Aufgabe ist nicht lösbar. "
				+ "Sie müssen die Rechnung sofort bezahlen. Die Rechnung ist sofort zu bezahlen. Es ist anzunehmen, dass es bald passiert. "
				+ "Dem Kind werden 100 Euro geschenkt. Das Kind kriegt 100 Euro geschenkt. ");
		engine.process(jcas);
		
		PassiveSentenceExtractor extractor = FeatureUtil.createResource(
				PassiveSentenceExtractor.class,
				PassiveSentenceExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,"PassiveSentenceExtractor"
				);
		
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
		Assert.assertEquals(7, features.size());
		
		assertFeatures("FrequencyOfTypicalPassive",0.38461538, features,0.0001);//5/13
		//1/13
		assertFeatures("FrequencyOfPassiveWithImpersonalPronoun",0.07692308, features,0.0001);
		assertFeatures("FrequencyOfPassiveWithSichLassen",0.07692308, features,0.0001);
		assertFeatures("FrequencyOfPassiveWithAdjective",0.07692308, features,0.0001);
		assertFeatures("FrequencyOfBekommenGruppe", 0.07692308,features,0.0001);
		
		assertFeatures("FrequencyOfPassiveWithZu",0.15384615, features,0.0001);//2/13
		
		assertFeatures("FrequencyOfPassiveSentences",0.8461538461538461,features,0.0001);//11/13
	}
	
	@Test
	public void PassiveFeatureInEssayTest() throws Exception {
		AnalysisEngine engine = getPreprocessingEngine("de",false);

		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(
				"In seinem Kommentar in der Süddeutschen Zeitung über die Studiengebühren wirft Roland Preuß 2014 die Frage auf, ob der Staat und somit die Gesellschaft jedem Studierenden das Studium finanzieren sollte. Zu dem Zeitpunkt der Verfassung haben alle Bundesländer die Studiengebühren abgeschafft und Preuß beschreibt die Situation an deutschen Unis mit: Arm lebt und lernt neben Reich. Auch wenn der Autor äußert, dass die Studiengebühren auch die armen Menschen nicht von einem Studium abgehalten haben, wirft der Autor die zentrale These auf, dass Studiengebühren für Akademiker eingeführt werden sollten. Preuß fordert, dass die viel verdienenden Akademiker mittels zusätzlicher Steuern etwas zurückgeben sollten. Um seine These zu untermauen, vergleicht er das Leben eines Akademikers mit einem Nicht-Akademiker: Der Akademiker verdient auf der einen Seite mehr Geld und muss zum anderen dieselben allgemeinen Steuern zahlen. Zusätzlich bezahlen laut dem Autor beispielsweise die Menschen ihre Meisterausbildungen selbst, wohingegen ein Studium vom Staat finanziert wird. Deshalb schlägt der Autor ein Modell vor, wie es das beispielsweise bereits in Australien gibt: Wer später überdurchschnittlich verdient, zahlt einen kleinen Anteil seines Einkommens, bis die Summe [des Studiums] abgestottert ist. Das so zusätzlich eingenommene Geld könne somit laut Preuß in die Bildung investiert werden. Zusätzlich wirft der Autor in seinem Kommentar eine Aussage von Gegnern der Studiengebühren auf, die lautet: Die Gesellschaft profitiert von akademisch Gebildeten. Auf den ersten Blick könnte man durchaus verstehen, wenn Gegner dieser These erläutern, dass beispielsweise Menschen, die Philosophie oder Kunst studiert haben keinen Profit für die Gesellschaft darstellen. Für die These spricht hingegen, dass beispielsweise der Abschluss eines Medizinstudiums als ein absoluter Profit für die Gesellschaft angesehen wird. Doch auch akademisch Gebildete, die Naturwissenschaften studiert haben können als hilfreich für die Menschen angesehen werden. Diese Akademiker haben beispielsweise durch ihre Erfindungen und mathematisch gelegten Grundlagen die westliche Zivilisation geprägt. Auch Akademiker wie Lehrer und Psychologen stellen einen Profit für die Gesellschaft dar, da sie die Menschen und wie sie lernen erforschen. Diese Kette akademisch Gebildeter, die einen Mehrwert für die Gesellschaft darstellen, kann selbstverständlich noch viel weitergeführt werden. Meiner Meinung nach können aber auch Akademiker, die beispielsweise Philosophie oder Kunst studiert haben genau wie Mediziner, Naturwissenschaftler, Lehrer und Psychologen als Mehrwert für die Gesellschaft angesehen werden. Denn wenn jemand die Leistung dieses Akademikers in Anspruch nehmen möchte und dafür bezahlt, ist in einer liberalen Gesellschaft nichts gegen seinen akademischen Grad einzuwenden. Solange Menschen die Leistungen der Akademiker nutzen, muss meiner Meinung nach nicht nach Profit für die Allgemeinheit gefragt werden.");
		engine.process(jcas);
		
		PassiveSentenceExtractor extractor = FeatureUtil.createResource(
				PassiveSentenceExtractor.class,
				PassiveSentenceExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,"PassiveSentenceExtractor"
		);
		
		Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));
		
		Assert.assertEquals(7, features.size());
	}

}
