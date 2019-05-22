package de.unidue.ltl.escrito.features.coherencecohesion;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PosTagTest {
	public static void main(String[] args) throws Exception {
		
		AnalysisEngine segmenter = AnalysisEngineFactory.createEngine(BreakIteratorSegmenter.class);
		AnalysisEngine posTagger = AnalysisEngineFactory.createEngine(OpenNlpPosTagger.class);
		JCas jcas = JCasFactory.createJCas();
		
		String text = "Lorem Ipsum ist ein einfacher Demo-Text für die Print- und Schriftindustrie. Lorem Ipsum ist in der Industrie bereits der Standard Demo-Text seit 1500, als ein unbekannter Schriftsteller eine Hand voll Wörter nahm und diese durcheinander warf um ein Musterbuch zu erstellen. Es hat nicht nur 5 Jahrhunderte überlebt, sondern auch in Spruch in die elektronische Schriftbearbeitung geschafft (bemerke, nahezu unverändert). Bekannt wurde es 1960, mit dem erscheinen von \"Letraset\", welches Passagen von Lorem Ipsum enhielt, so wie Desktop Software wie \"Aldus PageMaker\" - ebenfalls mit Lorem Ipsum.";
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(text);
		
		segmenter.process(jcas);
		posTagger.process(jcas);
	}
}
