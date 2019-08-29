package de.unidue.ltl.escrito.features.ngrams;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.tc.core.Constants.NGRAM_GLUE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class NgramUtils {

	 public static FrequencyDistribution<String> getDocumentMixedNgrams(JCas jcas,
				Annotation focusAnnotation, int minN, int maxN, boolean useCanonical)
		{
			FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();

			if (JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
				for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
					List<String> labelStrings = new ArrayList<String>();
					for (POS p : JCasUtil.selectCovered(jcas, POS.class, s)) {
						if (isContentWord(p)){
							if (useCanonical) {
								labelStrings.add(p.getClass().getSimpleName());
							}
							else {
								labelStrings.add(p.getPosValue());
							}
						} else {
							labelStrings.add(p.getCoveredText());
						}
					}
					String[] posarray = labelStrings.toArray(new String[labelStrings.size()]);
					for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
						posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
					}
				}
			}
			else {
				List<String> labelStrings = new ArrayList<String>();
				for (POS p : selectCovered(POS.class, focusAnnotation)) {
					if (isContentWord(p)){
						if (useCanonical) {
							labelStrings.add(p.getClass().getSimpleName());
						}
						else {
							labelStrings.add(p.getPosValue());
						}
					} else {
						labelStrings.add(p.getCoveredText());
					}
				}
				String[] posarray = labelStrings.toArray(new String[labelStrings.size()]);
				for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
					posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
				}
			}
			return posNgrams;
		}
	    

	  public static boolean isContentWord(POS p) {
			String simpleName = p.getClass().getSimpleName();
		//	System.out.println(simpleName + " "+p.getCoveredText());
			if (simpleName.equals("ADJ") || simpleName.equals("POS_ADJ")
					|| simpleName.startsWith("N") || simpleName.startsWith("POS_N")
					|| simpleName.startsWith("V") || simpleName.startsWith("POS_V")
					|| simpleName.equals("O") || simpleName.equals("POS_O")
					|| simpleName.equals("CARD") || simpleName.equals("POS_CARD") ){
				return true;
			} else {
				return false;
			}
		}
	    
	
	
	
}
