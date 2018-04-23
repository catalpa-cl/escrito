package de.unidue.ltl.escrito.languagetool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class GrammarMistakesAnalyzer extends JCasAnnotator_ImplBase {


	public static final String PARAM_OUTPUT_PATH = "outputPath";
	@ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
	private String outputPath;

	private static StringBuffer sbOutput =new StringBuffer();


	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		outputPath = (String) context.getConfigParameterValue(PARAM_OUTPUT_PATH);
		System.out.println("GrammarMistakesAnalyzer running");
		sbOutput.append("ID"+"\tnumberOfMistakes\n");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String id = meta.getDocumentId();

		double numberOfMistakes=0;
		AnnotationIndex<Annotation> index=aJCas.getAnnotationIndex(GrammarAnomaly.type);
		Iterator<Annotation> iterator= index.iterator();
		while(iterator.hasNext()){
			GrammarAnomaly anno= (GrammarAnomaly) iterator.next();
			String annoText= anno.getCoveredText();
	//		System.out.println(annoText+"\t"+anno.toString());
			//don't consider citing
			if(!annoText.matches(".*\\d.*")&& !annoText.contains(".")){
				numberOfMistakes++;
			}
		}
		//Normalization on total count of words
		System.out.println("Mistakes: "+numberOfMistakes);
		if(numberOfMistakes>0){
			numberOfMistakes=numberOfMistakes/JCasUtil.select(aJCas, Token.class).size();
		}
		System.out.println("Mistakes: "+numberOfMistakes);
		sbOutput.append(id+"\t"+numberOfMistakes+"\n");
	}


	@Override
	public void destroy(){
		try {		
			BufferedWriter numberWriter = new BufferedWriter(new FileWriter(new File(outputPath+"grammarMistakes.txt")));
			numberWriter.write(sbOutput.toString());
			numberWriter.flush();
			numberWriter.close();			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

