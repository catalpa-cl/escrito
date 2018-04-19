package de.unidue.ltl.escrito.features.coherencecohesion;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PairwiseSentenceSimilarityTest {
	@Test
    public void pairwiseSentenceSimilarityFeatureExtractorTest()
        throws Exception
    {
		AnalysisEngineDescription description= createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(MateLemmatizer.class));
        AnalysisEngine engine=createEngine(description);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("de");
//        jcas.setDocumentText("Schafe essen häufig große Blumen. Trotzdem vertilgen Schafe manchmal auch kleine Blumen.");
        jcas.setDocumentText("Der vorliegende Text \"Die Suche nach verborgenen Goldadern\" von Lothar Müller, welcher am 22.09.2008 in der Süddeutschen Zeitung veröffentlicht wurde, beschäftigt sich mit dem Thema \"Unbildung\" die in der heutigen Zeit bei der Bevölkerung anzutreffen ist." +
        		"Zunächst führt Lothar Müller in seiner Einleitung die bekannte und zugleich älteste Vergleichsfigur \"der modernen Pädagogen\" (Z. 4) ein, nämlich die des Gärtners.Seine Aufgabe besteht darin, eine Pflanzenzucht zu betreiben mit möglichst adäquaten Ergebnissen. Dieses Bild vom Gärtner" +
        		" setzt der Autor zu Anfang in Bezug zu dem heutigen Pädagogen beziehungsweise Lehrer. Genau wie der Gärtner versucht der Lehrer, in diesem Fall im Hinblick auf " +
        		"die Erziehung und Bildung von Kindern, seine „ungebärdig-wilden Zöglinge“ zu einem Wesen heranzuziehen, welches, wie auch die Pflanzen des Gärtners, gute Eigenschaften hat." +
        		"Damit wird die Aufgabe des Lehrers laut Lothar klar definiert: Das Ziel eines jeden Lehrers soll darin bestehen, diverse Bildungsziele zur Entfaltung zu bringen (Z. 14)." +
        		"Daraus leitet Lothar schließlich ab, dass genau durch diese Entfaltung die herrschende Unbildung, die der Lehrer versucht zu „bekämpfen“, definiert werden kann." +
        		"Schließlich gelangt der Leser zu der zentralen Frage des Textes von Lothar Müller. \"Was ist Unbildung noch, außer dass sie ein Mangel an Bildung ist?\"" +
        		"Um diese Frage zu klären führt er Erkenntnisse bezüglich \"ausbildungsmüden Jugendlichen\" ein, die der Soziologe Heinz Bude in seinem Buch \"Die Ausgeschlossenen\" (2008) dargelegt werden." +
        		"In ihm werden die heutigen Jugendlichen wie folgt charakterisiert: Sie gelten im Hinblick auf Bildung oder Bildungsinstitutionen (Z. 24) als „müde“, wohingegen sie zur gleichen Zeit als schnell," +
        		" wendig und mit einer raschen Auffassungsgabe usw. und mit einer Art von „funktionalem Analphabetismus (Z. 25-27) beglückt scheinen." +
        		"In dem darauffolgenden Abschnitt „Kontinuität statt harter Brüche“ führt der Autor Budes Aufforderung auf." +
        		"Die Gesellschaft solle \"Figuren der Bildungsferne nicht ausschließlich als Mängelwesen\" wahrnehmen, denn eben diese als \"komplexe Mischwesen\" sehen, die aus „Nicht-Können“ und „Können“ bestehen (Z. 32)." +
        		"Um die Aussagen Budes näher auszuführen führt er das zentrale Thema \"hidden intellectualism\" von dem Literaturwissenschaftler Gerald Graff aus der Zeitschrift Pedagogy an." +
        		"Hier werden zwei unterschiedliche Charaktere beschrieben: Zum einen Gerald Graff selbst, der aus einer jüdischen Mittelschichtsfamilie stammt" +
        		" und jegliche Art von Bildung in seiner Jugend ablehnte und schließlich Hochschullehrer an einer Universität wurde." +
        		"Zum anderen Michael Warner, welcher in einer jüdischen Mittelschichtfamilie in Chicago groß wurde, die vom christlichen Fundamentalismus und vom Erweckungsprediger " +
        		"Pat Robertson beeinflusst wurde. Dieser entwickelte sich später als \"atheistischer, den Lehren Michel Foucults anhängenden Intellektuellen\", und stellt sich schließlich selbst als \"Bote Satans\" dar (Z. 48)." +
        		"Die beiden Beispiele, inwiefern das Milieu, in dem man lebt und die jeweilige Erziehung prägend sind für einen Menschen wird mit der Definition \"hidden intelectualism\" bezeichnet." +
        		"Der Autor sieht es dabei als interessant an, dass dieser „Pädagogikbegriff“ zu einer „starken Tradition des Fahndens nach heimlichen Lehrplänen“ herausfordert (Z. 77)." +
        		"Er kritisiert dabei das System des \"hidden curriculum\", da nach seiner Meinung die Jugendlichen als eine Art von Untertanen erzogen werden");
        engine.process(jcas);

        PairwiseSentenceSimilarity extractor = new PairwiseSentenceSimilarity();
        extractor.init(3);
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(5, features.size());

        assertFeatures("GreedyStringTilingSampleNr_1", 0.32939424508814913, features, 0.0001);
        assertFeatures("GreedyStringTilingSampleNr_2", 0.17462156493029532, features, 0.0001);
        assertFeatures("GreedyStringTilingSampleNr_3", 0.1823855755894591, features, 0.0001);
        assertFeatures(PairwiseSentenceSimilarity.PAIRWISE_SENTENCE_SIMILARITY_NOUNS, 0.38625596248686234, features, 0.0001);
        assertFeatures(PairwiseSentenceSimilarity.PAIRWISE_SENTENCE_SIMILARITY_GREEDY_TILE,  0.23868365846026235, features, 0.0001);
//        assertFeature(PairwiseSentenceSimilarityDFE.PAIRWISE_SENTENCE_SIMILARITY_NOUNS, 1.0, iter.next());
//        assertFeature(PairwiseSentenceSimilarityDFE.PAIRWISE_SENTENCE_SIMILARITY_GREEDY_TILE,  0.48484848484848486, iter.next());
   
    }
	
	@Test
    public void pairwiseSentenceSimilarityFeatureExtractorTest_en()
        throws Exception
    {
		AnalysisEngineDescription description= createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class), createEngineDescription(OpenNlpPosTagger.class),createEngineDescription(MateLemmatizer.class));
        AnalysisEngine engine=createEngine(description);
        		
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Sheeps eat frequently big flowers. Nevertheless sheeps consume little flowers sometimes, too.");
        engine.process(jcas);

        PairwiseSentenceSimilarity extractor = new PairwiseSentenceSimilarity();
        Set<Feature> features = extractor.extract(jcas, TextClassificationTarget.get(jcas));

        Assert.assertEquals(2, features.size());

        assertFeatures(PairwiseSentenceSimilarity.PAIRWISE_SENTENCE_SIMILARITY_NOUNS, 1.0, features, 0.0001);
        assertFeatures(PairwiseSentenceSimilarity.PAIRWISE_SENTENCE_SIMILARITY_GREEDY_TILE,  0.4411764705882353, features, 0.0001);
    }
	
}
