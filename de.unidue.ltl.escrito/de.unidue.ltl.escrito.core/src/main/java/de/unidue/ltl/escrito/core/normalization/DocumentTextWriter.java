package de.unidue.ltl.escrito.core.normalization;


import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * Write the text and infor for a certain jcas. Used only for quic sanity checking.
 * 
 * @author andrea
 *
 */


public class DocumentTextWriter extends JCasAnnotator_ImplBase{
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		System.out.println(aJCas.getDocumentText());
		System.out.println(JCasUtil.selectAll(aJCas));
		}

}
