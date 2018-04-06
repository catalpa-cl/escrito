//package de.unidue.ltl.edu.scoring.features.essay.core;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//import org.apache.uima.cas.text.AnnotationIndex;
//import org.apache.uima.fit.descriptor.TypeCapability;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.jcas.tcas.Annotation;
//import org.dkpro.tc.api.exception.TextClassificationException;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.features.FeatureExtractor;
//import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
//import org.dkpro.tc.api.type.TextClassificationTarget;
//
//import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
//import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
//
///**
// * Counts grammar anomalies assigned by LanguageToolChecker except citing related anomalies
// */
//@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly" })
//public class GrammarMistakesDFE extends FeatureExtractorResource_ImplBase
//implements FeatureExtractor{
//
//	public static final String FN_NR_OF_GRAMMAR_MISTAKES = "NrOfGrammarMistakes";
//	//TODO Fehlererkennung erkennt Fehler nicht (Kasusfehler?)
//	@Override
//	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
//			throws TextClassificationException 
//	{		
//		double numberOfMistakes=0;
//		AnnotationIndex<Annotation> index=jcas.getAnnotationIndex(GrammarAnomaly.type);
//		Iterator<Annotation> iterator= index.iterator();
//		while(iterator.hasNext()){
//			GrammarAnomaly anno= (GrammarAnomaly) iterator.next();
//			String annoText= anno.getCoveredText();
//			//don't consider citing
//			if(!annoText.matches(".*\\d.*")&& !annoText.contains(".") && (annoText.length()>1)){
//				numberOfMistakes++;
//			}
//		}
//		//Normalization on total count of words
//		if(numberOfMistakes>0){
//			numberOfMistakes=numberOfMistakes/JCasUtil.select(jcas, Token.class).size();
//		}
//		Set<Feature> featList = new HashSet<Feature>();
//		//		System.out.println("Mistakes: "+i);
//		featList.add(new Feature(FN_NR_OF_GRAMMAR_MISTAKES, numberOfMistakes));
//		return featList;
//	}
//
//}
