package de.unidue.ltl.escrito.languagetool;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 *  //TODO add documentation + test case
 */
public class GrammarMistakesAnalyzer 
	extends JCasAnnotator_ImplBase
{


	public static final String PARAM_OUTPUT_PATH = "outputPath";
	@ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
	private String outputPath;

	private StringBuilder sb = new StringBuilder();


	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		super.initialize(context);
		outputPath = (String) context.getConfigParameterValue(PARAM_OUTPUT_PATH);
		System.out.println("GrammarMistakesAnalyzer running");	// TODO use logging instead
		sb.append("ID");
		sb.append("\t");
		sb.append("numberOfMistakes");
		sb.append("\n");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
		String id = meta.getDocumentId();

		double numberOfMistakes = 0;
		for (GrammarAnomaly anomaly : JCasUtil.select(jcas, GrammarAnomaly.class)) {
			
			String anomalyText = anomaly.getCoveredText();
			//don't consider citing
			// TODO example what this should match?
			if(!anomalyText.matches(".*\\d.*") && !anomalyText.contains(".")){
				numberOfMistakes++;
			}
		}
		
		//Normalization on total count of words
		System.out.println("Mistakes: " + numberOfMistakes);
		if (numberOfMistakes>0){
			numberOfMistakes = numberOfMistakes / JCasUtil.select(jcas, Token.class).size();
		}
		System.out.println("Mistakes: " + numberOfMistakes);
		sb.append(id);
		sb.append("\t");
		sb.append(numberOfMistakes);
		sb.append("\n");
	}


	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException 
	{
		super.collectionProcessComplete();
		
		try {
			FileUtils.writeStringToFile(new File(outputPath+"grammarMistakes.txt"), sb.toString(), "UTF-8");
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}

