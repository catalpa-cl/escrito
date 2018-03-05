package de.unidue.ltl.escrito.core.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class QuadraticWeightedKappa
{
    
    public static double getKappa(List<Integer> ratingsA, List<Integer> ratingsB, Integer... categories) {
        return getKappa(ratingsA.toArray(new Integer[ratingsA.size()]), ratingsB.toArray(new Integer[ratingsB.size()]), categories);
    }

    public static double getKappa(Integer[] ratingsA, Integer[] ratingsB, Integer... categories) {
        if (ratingsA.length != ratingsB.length) {
            throw new IllegalArgumentException("Rating vectors need to be of equal size.");
        }
        
        if (Arrays.equals(ratingsA,ratingsB)) {
        	return 1.0;
        }
    
//        int minRating = Collections.min(Arrays.asList(categories));
//        int maxRating = Collections.max(Arrays.asList(categories));
        ConfusionMatrix confMatrix = new ConfusionMatrix(ratingsA, ratingsB, categories);

        int nrofRatings = categories.length;
        int nrofScoredItems = ratingsA.length;

        FrequencyDistribution<Integer> freqDistA = new FrequencyDistribution<Integer>(Arrays.asList(ratingsA));
        FrequencyDistribution<Integer> freqDistB = new FrequencyDistribution<Integer>(Arrays.asList(ratingsB));
        
        double numerator = 0.0;
        double denominator = 0.0;

        for (int outerCategory : categories) {
            for (int innerCategory : categories) {
                int nMinusOne = nrofRatings - 1;
                int distance = outerCategory - innerCategory;
                double weight = (double) (distance*distance) / (nMinusOne*nMinusOne);
                
                double expectedCount = (double) (freqDistA.getCount(outerCategory) * freqDistB.getCount(innerCategory)) / nrofScoredItems;
                numerator += weight * confMatrix.getElement(outerCategory, innerCategory) / nrofScoredItems;
                denominator += weight * expectedCount / nrofScoredItems;
            }
        }

        return 1.0 - numerator / denominator;
    }
    
    public static double getMeanKappa(Double[] kappas) {
        return getMeanKappa(Arrays.asList(kappas));
    }

    public static double getMeanKappa(List<Double> kappas) {
        List<Double> weights = new ArrayList<Double>();
        for (int i=0; i<kappas.size(); i++) {
            weights.add(1.0);
        }
        return getMeanWeightedKappa(kappas, weights);
    }

    public static double getMeanWeightedKappa(Double[] kappas, Double[] weights) {
        return getMeanWeightedKappa(Arrays.asList(kappas), Arrays.asList(weights));
    }

    /*
     * Compute mean for Fisher-Z score transformed kappas and then transform back.
     * 
     * @return The mean kappa value.
     */
    public static double getMeanWeightedKappa(List<Double> kappas, List<Double> weights) {
        
        // ensure that kappas are in the range [-.999, .999]
        for (int i=0; i< kappas.size(); i++) {
            if (kappas.get(i) < -0.999) {
                kappas.set(i, -0.999);
            }
            else if (kappas.get(i) > 0.999) {
                kappas.set(i, 0.999);
            }
        }

        // normalize weights
        double meanWeight = StatUtils.mean( ArrayUtils.toPrimitive(weights.toArray(new Double[weights.size()]) ));
        for (int i=0; i<weights.size(); i++) {
            weights.set(i, weights.get(i) / meanWeight);
        }
        
        List<Double> zValues = new ArrayList<Double>();
        for (int i=0; i< kappas.size(); i++) {
            zValues.add(
                    0.5 * Math.log( (1+kappas.get(i))/(1-kappas.get(i))) * weights.get(i)
            );
        }
        double z = StatUtils.mean( ArrayUtils.toPrimitive(zValues.toArray(new Double[zValues.size()]) ));
        
        double kappa = (Math.exp(2*z)-1) / (Math.exp(2*z)+1);
        
        return kappa;
    }
}
