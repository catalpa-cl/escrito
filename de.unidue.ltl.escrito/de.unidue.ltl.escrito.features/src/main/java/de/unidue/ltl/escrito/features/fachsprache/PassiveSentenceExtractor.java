package de.unidue.ltl.escrito.features.fachsprache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Counts the occurrence of the passive sentences.
 * 
 * @author Yuning
 */

public class PassiveSentenceExtractor  extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		Set<Feature> passiveFeatures = new HashSet<Feature>();
		Collection<Sentence> sentences = JCasUtil.select(view, Sentence.class);
		//System.out.println("Sentence Sum: "+sentences.size());
		//Counter of different type of passive sentences
		int countTypicalPassive = 0;
		int countPassiveWithImpersonalPronoun  = 0;
		int countPassiveWithSichLassen = 0 ;
		int countPassiveWithAdjective = 0;
		int countPassiveWithZu = 0;
		int countPassiveWithBekommen = 0;
		
		for(Sentence s:sentences){
			Collection<Token> tokenInSentence = JCasUtil.selectCovered(Token.class, s);

			boolean impersonalPronoun =false;
			boolean werden = false;
			boolean sein = false;
			boolean partizipII = false;			
			boolean adjective = false;
			boolean zu = false;
			boolean lassen = false; 
			boolean sich = false;
			boolean bekommen = false;
			
			for(Token t :tokenInSentence){
				String text = t.getCoveredText().toLowerCase();
				String posValue = t.getPos().getPosValue();
				String lemmaValue = t.getLemma().getValue();
				//System.out.print(lemmaValue+" ");
				//System.out.print("["+posValue+"] ");
							
				if(text.equals("man")||text.equals("jemand")) //search "man/jemand"
					impersonalPronoun = true;			
				if ((posValue.equals("VAFIN")||posValue.equals("VAINF"))&&lemmaValue.equals("werden")) //search "werden"
					werden = true;	
				if ((posValue.equals("VAFIN")||posValue.equals("VAINF"))&&lemmaValue.equals("sein")) //search "werden"
					sein = true;	
				if (posValue.equals("VVPP")) //search "Partizip II"
					partizipII = true;
				if((posValue.equals("ADJD")&&t.getCoveredText().endsWith("bar"))|| (posValue.equals("ADJD")&&t.getCoveredText().endsWith("lich")))  //search adjective end with "bar" or "lich"
					adjective = true;			
				if(posValue.equals("PTKZU")||posValue.equals("VVIZU")) //search "zu"
					zu = true;
				if (lemmaValue.equals("lassen")||lemmaValue.equals("lässen")) //search "lassen", "lässen" is only for MateLemmatizer
					lassen = true;			
				if (lemmaValue.equals("sich")) //search "sich"
					sich = true;				
				if(lemmaValue.equals("bekommen")||lemmaValue.equals("kriegen")) //search "bekommen/kriegen"
					bekommen = true;
			}
			//System.out.println();
			//System.out.println(s.getCoveredText());
			if((werden||sein)&&partizipII){
				countTypicalPassive++; //typical passive form with "werden + Partizip Perfekt"
				//System.out.println("[typical passive]");
			}
			if(lassen&&sich){
				countPassiveWithSichLassen++; //passive form with "sich lassen"
				//System.out.println("[passive with sich lassen]");
			}
			if(sein&&adjective){
				countPassiveWithAdjective++; //passive form with adjective suffix "bar"
				//System.out.println("[passive with adjektive]");
			}
			if(sein&&zu){
				countPassiveWithZu++; //passive form with "sein + zu + Infinitiv"
				//System.out.println("[passive with zu]");
			}
			if(bekommen){
				countPassiveWithBekommen++; //passive form with "bekommen/kriegen"
				//System.out.println("[passive with bekommen]");
			}
			if(impersonalPronoun){
				countPassiveWithImpersonalPronoun++; //passive form with "man/jemand"
				//System.out.println("[passive with Impersonal Pronoun]");
			}
		}
		passiveFeatures.add(new Feature("FrequencyOfTypicalPassive", (double)countTypicalPassive/sentences.size(), FeatureType.NUMERIC));
		passiveFeatures.add(new Feature("FrequencyOfPassiveWithImpersonalPronoun", (double)countPassiveWithImpersonalPronoun/sentences.size(), FeatureType.NUMERIC));
		passiveFeatures.add(new Feature("FrequencyOfPassiveWithSichLassen", (double)countPassiveWithSichLassen/sentences.size(), FeatureType.NUMERIC));
		passiveFeatures.add(new Feature("FrequencyOfPassiveWithAdjective", (double)countPassiveWithAdjective/sentences.size(), FeatureType.NUMERIC));
		passiveFeatures.add(new Feature("FrequencyOfPassiveWithZu",(double)countPassiveWithZu/sentences.size(), FeatureType.NUMERIC));
		passiveFeatures.add(new Feature("FrequencyOfBekommenGruppe",(double)countPassiveWithBekommen/sentences.size(), FeatureType.NUMERIC));
		int sum = countTypicalPassive + countPassiveWithImpersonalPronoun  + countPassiveWithSichLassen + countPassiveWithAdjective + countPassiveWithZu + countPassiveWithBekommen;
		passiveFeatures.add(new Feature("FrequencyOfPassiveSentences",(double)sum/sentences.size(), FeatureType.NUMERIC));
		return passiveFeatures;
	}
}
