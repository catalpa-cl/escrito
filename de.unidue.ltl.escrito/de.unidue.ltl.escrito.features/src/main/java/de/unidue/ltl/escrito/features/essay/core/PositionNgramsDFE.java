//package de.unidue.ltl.edu.scoring.features.essay.core;
//
//import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
//import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
//import static org.apache.uima.fit.util.JCasUtil.toText;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.uima.analysis_engine.AnalysisEngine;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.dkpro.tc.api.exception.TextClassificationException;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.features.FeatureExtractor;
//import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
//import org.dkpro.tc.api.features.util.FeatureUtil;
//import org.dkpro.tc.api.type.TextClassificationTarget;
////import org.dkpro.tc.features.ngram.base.LuceneNgramFeatureExtractorBase;
//import org.dkpro.tc.features.ngram.LuceneNGramPFE;
//import org.dkpro.tc.features.ngram.util.NGramUtils;
//
//import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
//import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
//import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
//
//public class PositionNgramsDFE
//	extends LuceneNGramFeatureExtractor
//	implements FeatureExtractor{
//
//	protected int folds=5;
//	
//	@Override
//	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
//			throws TextClassificationException
//	{
//		Set<Feature> features = new HashSet<Feature>();
//		List<Token> tokens =  new ArrayList<Token>(JCasUtil.select(jcas, Token.class));
//		int split=tokens.size()/folds;
//		for(int i=0;i<folds;i++){
//			JCas view=null;
//			if(i==0){
//				view=getSubCas(jcas,tokens.subList(0, split),i);
//			}else if(i==folds-1){
//				view=getSubCas(jcas,tokens.subList(split*i, tokens.size()),i);
//			}else{
//				view=getSubCas(jcas,tokens.subList(split*i, split*(i+1)),i);
//			}
//			FrequencyDistribution<String> documentNgrams = null;
//	        documentNgrams = NGramUtils.getDocumentNgrams(view, target, ngramLowerCase,
//	                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
//	        features.addAll(getFeaturesForFold(i,documentNgrams));
//		}
//		return features;
//	}
//
//	private Collection<? extends Feature> getFeaturesForFold(int i,
//			FrequencyDistribution<String> documentNgrams) {
//		List<Feature> features = new ArrayList<Feature>();
//		for (String topNgram : topKSet.getKeys()) {
//            if (documentNgrams.getKeys().contains(topNgram)) {
//                features.add(new Feature(getFeaturePrefix() +"_" +"FOLD"+i+ "_" + topNgram, 1));
//            }
//            else {
//                features.add(new Feature(getFeaturePrefix()+"_" +"FOLD"+i+ "_" + topNgram, 0));
//            }
//        }
//		return features;
//	}
//
//	private JCas getSubCas(JCas jcas, List<Token> subList, int i) {
//		JCas view = null;
//		try {
//	        AnalysisEngine engine=createEngine(createEngineDescription(BreakIteratorSegmenter.class));
////			view=jcas.createView(String.valueOf(i));
//	        view=engine.newJCas();
//			view.setDocumentText(StringUtils.join(toText(subList), " "));
//			engine.process(view);
//		} catch (AnalysisEngineProcessException|ResourceInitializationException e) {
//			e.printStackTrace();
//		} 
//		return view;
//	}
//
//	public void init(int nGramMin, int nGramMax, int topK) {
//		this.ngramMinN=nGramMin;
//		this.ngramMaxN=nGramMax;
//		this.ngramUseTopK=topK;
//		try {
//            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//		System.out.println(stopwords);
//		
//        try {
//			topKSet = getTopNgrams();
//		} catch (ResourceInitializationException e) {
//			e.printStackTrace();
//		}
//
//        prefix = getFeaturePrefix();
//
//	}
//
//	@Override
//	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
//			throws ResourceInitializationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
