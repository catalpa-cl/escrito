package de.unidue.ltl.edu.scoring.features.general;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyUtils {

	public static FrequencyDistribution<String> getDocumentDependencies(JCas jcas, TextClassificationTarget fullDoc, boolean lowercase) {
		FrequencyDistribution<String> documentDependencies = new FrequencyDistribution<String>();
		for (Sentence s : selectCovered(jcas, Sentence.class, fullDoc)) {
			for (Dependency dep : selectCovered(jcas, Dependency.class, s)){
				String dependencyString = dependencyToString(dep);
				if (lowercase) {
					dependencyString.toLowerCase();
				}
				documentDependencies.inc(dependencyString);
			}
		}
		return documentDependencies;
	}

	
	public static String dependencyToString(Dependency dep) {
		return dep.getGovernor().getCoveredText()+"_"+dep.getDependencyType()+"_"+dep.getDependent().getCoveredText();
	}
	
	
}
