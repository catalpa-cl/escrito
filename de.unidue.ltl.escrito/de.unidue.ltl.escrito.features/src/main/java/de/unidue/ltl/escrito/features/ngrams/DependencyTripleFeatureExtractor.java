package de.unidue.ltl.edu.scoring.features.general;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class DependencyTripleFeatureExtractor extends LuceneFeatureExtractorBase
implements FeatureExtractor {

	
	
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
        FrequencyDistribution<String> documentDependencies = null;

        documentDependencies = DependencyUtils.getDocumentDependencies(jcas, target, ngramLowerCase);
        		
        System.out.println(topKSet.getKeys().size()+" triples found");
        for (String topNgram : topKSet.getKeys()) {
        	System.out.println("top dependency triple: "+topNgram);
            if (documentDependencies.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true));
            }
        }
        return features;
    }

	
	@Override
	protected void logSelectionProcess(long N) {
		getLogger().log(Level.INFO, "+++ SELECTING THE " + N + " MOST FREQUENT DEPENDENCY TRIPLES");		
	}


	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(
			Map<String, Object> parameterSettings)
					throws ResourceInitializationException
	{
		return Arrays.asList(
				new MetaCollectorConfiguration(DependencyMetaCollector.class, parameterSettings)
				.addStorageMapping(DependencyMetaCollector.PARAM_TARGET_LOCATION,
						DependencyTripleFeatureExtractor.PARAM_SOURCE_LOCATION,
						DependencyMetaCollector.LUCENE_DIR));
	}


	@Override
	protected String getFieldName() {
		return "dependency_" + featureExtractorName;
	}


	@Override
	protected int getTopN() {
		return ngramUseTopK;
	}


	@Override
	protected String getFeaturePrefix() {
		return "dependency";
	}

}
