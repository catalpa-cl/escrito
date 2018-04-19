package de.unidue.ltl.escrito.features.similarity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.features.ngram.util.TermFreqTuple;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.unidue.ltl.escrito.features.ngrams.DependencyMetaCollector;
import de.unidue.ltl.escrito.features.ngrams.DependencyTripleFeatureExtractor;
import de.unidue.ltl.escrito.features.similarity.meta.DependencyTripleIdfCollector;

@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class DependencyTripleOverlapFeatureExtractor extends FeatureExtractorResource_ImplBase
implements PairFeatureExtractor, MetaDependent{

	public static final String FEAT_DEP_FULL_MATCH_1 = "DependencyFullMatch1";	
	public static final String FEAT_DEP_MATCH_WO_REL_1 = "DependencyMatchWithoutRelation1";	

	public static final String FEAT_DEP_FULL_MATCH_2 = "DependencyFullMatch2";	
	public static final String FEAT_DEP_MATCH_WO_REL_2 = "DependencyMatchWithoutRelation2";	


	public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
	private String idfFile;

	public static final String PARAM_USE_IDF = "useIdf";
	@ConfigurationParameter(name = PARAM_USE_IDF, mandatory = false, defaultValue = "false")
	private boolean useIdf;

	private Map<String, Double> idfs;
	private double maxIdfValue;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException
	{
		System.out.println("INITIALIZE");
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		} 
		try {
			FileInputStream fis = new FileInputStream(new File(idfFile));
			ObjectInputStream ois = new ObjectInputStream(fis);
			idfs = (Map<String, Double>) ois.readObject();
			System.out.println("Read "+idfs.keySet().size()+ " dependency triple idf entries from "+idfFile+" "+idfs.keySet().toString());
			ois.close();
			fis.close();
			maxIdfValue  = 0.0;
			for (String idf : idfs.keySet()){
				if (idfs.get(idf) > maxIdfValue){
					maxIdfValue = idfs.get(idf);
				}
			}
			System.out.println("maxIdfValue: "+maxIdfValue);
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.exit(-1);
		} catch(ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			System.exit(-1);
		}
		return true;
	}

	@Override
	public Set<Feature> extract(JCas view1, JCas view2)
			throws TextClassificationException {

		Set<Feature> features = new HashSet<Feature>();

		Collection<Dependency> dependencyTriples1 = JCasUtil.select(view1, Dependency.class);
		Collection<Dependency> dependencyTriples2 = JCasUtil.select(view2, Dependency.class);
		int numTriples1 = dependencyTriples1.size();
		int numTriples2 = dependencyTriples2.size();
		// exact matches
		int matchingTriplesView1_withRel = 0;
		int matchingTriplesView1_withoutRel = 0;
		for (Dependency triple1 : dependencyTriples1){
			String gov1 = triple1.getGovernor().getLemma().getValue();
			String dep1 = triple1.getDependent().getLemma().getValue();
			String rel1 = triple1.getDependencyType();
			String dependencyTriple = gov1+"_"+dep1+"_"+rel1;
			String dependencyTripleWoRel = gov1+"_"+dep1;
			for (Dependency triple2 : dependencyTriples2){
				String gov2 = triple2.getGovernor().getLemma().getValue();
				String dep2 = triple2.getDependent().getLemma().getValue();
				String rel2 = triple2.getDependencyType();	
				if (gov1.equals(gov2) && dep1.equals(dep2)){
					if (useIdf){
						if (this.idfs.containsKey(dependencyTripleWoRel)){
							matchingTriplesView1_withoutRel+= this.idfs.get(dependencyTripleWoRel);
						} else {
							matchingTriplesView1_withoutRel+= maxIdfValue;
						}
					} else {
						matchingTriplesView1_withoutRel++;
					}
					if (rel1.equals(rel2)){
						if (useIdf){
							if (this.idfs.containsKey(dependencyTriple)){
								matchingTriplesView1_withoutRel+= this.idfs.get(dependencyTriple);
							} else {
								matchingTriplesView1_withoutRel+= maxIdfValue;
							}
						} else {
							matchingTriplesView1_withRel++;
						}
					}
				}
			}
		}
		features.add(new Feature(FEAT_DEP_FULL_MATCH_1, 1.0*matchingTriplesView1_withRel/numTriples1, FeatureType.NUMERIC));
		features.add(new Feature(FEAT_DEP_MATCH_WO_REL_1, 1.0*matchingTriplesView1_withoutRel/numTriples1, FeatureType.NUMERIC));
		features.add(new Feature(FEAT_DEP_FULL_MATCH_2, 1.0*matchingTriplesView1_withRel/numTriples2, FeatureType.NUMERIC));
		features.add(new Feature(FEAT_DEP_MATCH_WO_REL_2, 1.0*matchingTriplesView1_withoutRel/numTriples2, FeatureType.NUMERIC));
		return features;		
	}



	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(
			Map<String, Object> parameterSettings)
					throws ResourceInitializationException
					{
		return Arrays.asList(
				new MetaCollectorConfiguration(DependencyTripleIdfCollector.class, parameterSettings)
				.addStorageMapping(DependencyTripleIdfCollector.PARAM_TARGET_LOCATION,
						DependencyTripleOverlapFeatureExtractor.PARAM_SOURCE_LOCATION,
						DependencyTripleIdfCollector.IDF_DEPENDENCIES_DIR)
				);
					}
}
