package de.unidue.ltl.escrito.features.occurence;

/*******************************************************************************
Finds the PronounRatio for German text
*/

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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;

/**
* Extracts the ratio of the 8 major German pronouns to the total pronouns
* 
* German only.
*/

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class PronounRatio
   extends FeatureExtractorResource_ImplBase
   implements FeatureExtractor
{
   public static final String FN_ICH_RATIO = "PronounRatioIch";
   public static final String FN_ER_RATIO = "PronounRatioEr";
   public static final String FN_SIE_RATIO = "PronounRatioSie";
   public static final String FN_WIR_RATIO = "PronounRatioWir";
   public static final String FN_UNS_RATIO = "PronounRatioUns";
   public static final String FN_ES_RATIO = "PronounRatioEs";
   public static final String FN_DU_RATIO = "PronounRatioDu";
   public static final String FN_IHR_RATIO = "PronounRatioIhr";

   @Override
   public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
       throws TextClassificationException
   {

       int erCount = 0;
       int sieCount = 0;
       int ichCount = 0;
       int wirCount = 0;
       int ihrCount = 0;
       int unsCount = 0;
       int duCount = 0;
       int esCount = 0;

       int n = 0;
       for (POS_PRON pronoun : JCasUtil.selectCovered(jcas, POS_PRON.class, aTarget)) {
           n++;

           String text = pronoun.getCoveredText().toLowerCase();
           if (text.equals("er")) {
           	erCount++;
           }
           else if (text.equals("sie")) {
           	sieCount++;
           }
           else if (text.equals("ich")) {
           	ichCount++;
           }
           else if (text.equals("ihr")) {
           	ihrCount++;
           }
           else if (text.equals("wir")) {
           	wirCount++;
           }
           else if (text.equals("uns")) {
           	unsCount++;
           }
           else if (text.equals("du")) {
           	duCount++;
           }else if (text.equals("es")) {
               esCount++;
           }
       }

       Set<Feature> features = new HashSet<Feature>();
       features.add(new Feature(FN_ICH_RATIO, (double) ichCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_ER_RATIO, (double) erCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_SIE_RATIO, (double) sieCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_WIR_RATIO, (double) wirCount / n, n == 0, FeatureType.NUMERIC));
       features.add(
               new Feature(FN_UNS_RATIO, (double) unsCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_ES_RATIO, (double) esCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_DU_RATIO, (double) duCount / n, n == 0, FeatureType.NUMERIC));
       features.add(new Feature(FN_IHR_RATIO, (double) ihrCount / n, n == 0, FeatureType.NUMERIC));

       return features;
   }
}