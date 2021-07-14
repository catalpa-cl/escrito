package de.unidue.ltl.escrito.features.general;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;


/*
 * Extracts the surface string of the input as feature. 
 * This can be helpful, if we want to do e.g. knn with customized distance metrics operating on similarities between items
 * 
 * 
 */

public class SurfaceStringFeatureExtractor extends FeatureExtractorResource_ImplBase 
implements FeatureExtractor {

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget tct) throws TextClassificationException {
		Set<Feature> features = new HashSet<Feature>();
		Feature f = new Feature("surfaceString", jcas.getDocumentText(), FeatureType.STRING);
		features.add(f);
		return features;
	}

}
