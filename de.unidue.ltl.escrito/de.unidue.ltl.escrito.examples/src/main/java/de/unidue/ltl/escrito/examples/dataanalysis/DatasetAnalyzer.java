package de.unidue.ltl.escrito.examples.dataanalysis;

import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


public class DatasetAnalyzer  extends JCasAnnotator_ImplBase {

	
	private int tokenCount = 0;
	private int answerCount = 0;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<Token> tokens = JCasUtil.select(aJCas,Token.class);
	//	System.out.println(tokens.size());		
		answerCount++;
		tokenCount+=tokens.size();
	}

	
	@Override
	public void destroy(){
//		System.out.println(answerCount+" answers");
	//	System.out.println("avg length:\t"+Math.round(100.0*tokenCount/answerCount)/100.0);
		System.out.println(Math.round(100.0*tokenCount/answerCount)/100.0);
	}
	
	
}
