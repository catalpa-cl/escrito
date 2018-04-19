package de.unidue.ltl.escrito.features.similarity.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyTripleIdfCollector extends MetaCollector{


	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private String idfFile;

	public final static String IDF_DEPENDENCIES_DIR = "idf_dependencies";
	

	private FrequencyDistribution<String> dfs;
	private int n = 0;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException  {
		//System.out.println("INITIALIZE METACOLLECTOR DEPENDENCIES");
		super.initialize(context);
		this.dfs = new FrequencyDistribution<String>();	
	}

	/*
	 * 
	 * Collect all dependencies in one doocument (count them only once), then add them to the overall list
	 * Consider both dependencies with and without the relation
	 * 
	 */

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		n++;
		Collection<Dependency> triples = JCasUtil.select(jcas, Dependency.class);
		Set<String> dependencies = new HashSet<String>();
		for (Dependency triple : triples){
			String gov = triple.getGovernor().getLemma().getValue();
			String dep = triple.getDependent().getLemma().getValue();
			String rel = triple.getDependencyType();
			dependencies.add(gov+"_"+dep+"_"+rel);
			dependencies.add(gov+"_"+dep);
			}
		for (String dependency : dependencies){
			this.dfs.inc(dependency);
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
