package de.unidue.ltl.escrito.features.fachsprache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

/**
 * Counts the occurrence of the prepositional phrases.
 * 
 * @author Yuning
 */
@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
		"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent"})
public class PrepositionalPhraseExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		int countPP = 0;
		int constituentsSize = 0;

			Collection<Constituent> constituents = JCasUtil.select(view,Constituent.class);
			constituentsSize+=constituents.size();
			for (Constituent constituent : constituents) {
				if(constituent.getConstituentType().equals("PP")){			
					Collection<POS> pos =JCasUtil.selectCovered(POS.class, constituent);
					ArrayList<String> posList = new ArrayList<String>();
					for(POS p:pos){
						posList.add(p.getPosValue());
					}
					if(!((posList.contains("NN")||posList.contains("NE"))&&(posList.contains("APPR")||posList.contains("APPR"))))
						continue;
					if(!constituent.getParent().getType().getName().endsWith("NP")){
						countPP++;
						//System.out.println(constituent.getCoveredText());
					}
				}
			}

		Set<Feature> ppFeatures = new HashSet<Feature>();
		ppFeatures.add(new Feature("frequencyOfPrepositionalPhrase", (double)countPP/constituentsSize, FeatureType.NUMERIC));
		//System.out.println((double)countPP/constituentsSize);
		return ppFeatures;
	}
}
