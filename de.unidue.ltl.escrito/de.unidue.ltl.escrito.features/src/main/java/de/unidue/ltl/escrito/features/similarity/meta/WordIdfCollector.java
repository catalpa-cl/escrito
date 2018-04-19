package de.unidue.ltl.escrito.features.similarity.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.meta.MetaCollector;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WordIdfCollector extends MetaCollector{

	
	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private String idfFile;


	public final static String IDF_WORDS_DIR = "idf_words";

	private FrequencyDistribution<String> dfs;
	private int n = 0;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		//System.out.println("INITIALIZE METACOLLECTOR WORDS");
		super.initialize(context);
		this.dfs = new FrequencyDistribution<String>();	
	}
	
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		n++;
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		Set<String> lemmasWithPos = new HashSet<String>();
		for (Token t : tokens){
			lemmasWithPos.add(t.getLemma().getValue().toLowerCase()+"_"+t.getPos().getPosValue());
		}
		for (String lemma : lemmasWithPos){
			this.dfs.inc(lemma);
		}
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException
	{
		Map idfValues = new HashMap<String, Double>();
		for (String entry : this.dfs.getKeys()){
			idfValues.put(entry, Math.log(n/this.dfs.getCount(entry)));
		}
		super.collectionProcessComplete();
		try {
			FileOutputStream fos =
					new FileOutputStream(new File(idfFile));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(idfValues);
			oos.close();
			fos.close();
			} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
}
