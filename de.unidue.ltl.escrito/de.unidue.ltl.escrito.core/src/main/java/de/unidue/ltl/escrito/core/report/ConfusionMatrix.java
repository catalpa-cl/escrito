package de.unidue.ltl.escrito.core.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

public class ConfusionMatrix
{

    private List<Integer> ratingsA;
    private List<Integer> ratingsB;
    
    private Integer[] categories;
    
    private Map<Integer, Map<Integer, Integer>> confusionMatrix;
    
    public ConfusionMatrix(Integer[] ratingsA, Integer[] ratingsB, Integer... categories) {
        this(Arrays.asList(ratingsA), Arrays.asList(ratingsB), categories);
    }

    public ConfusionMatrix(List<Integer> ratingsA, List<Integer> ratingsB, Integer... categories)
    {
        if (ratingsA.size() != ratingsB.size()) {
            throw new IllegalArgumentException("Rating vectors need to be of equal size.");
        }
        
        this.ratingsA = ratingsA;
        this.ratingsB = ratingsB;
        
        this.categories = categories;
        
        confusionMatrix = new TreeMap<Integer, Map<Integer, Integer>>();
        
        for (Integer category : categories) {
            confusionMatrix.put(category, new TreeMap<Integer,Integer>());
            for (Integer innerCategory : categories) {
                confusionMatrix.get(category).put(innerCategory, 0);
            }
        }

        for (int i=0; i<ratingsA.size(); i++) {
            int ratingA = ratingsA.get(i);
            int ratingB = ratingsB.get(i);
            
            if (!confusionMatrix.get(ratingA).containsKey(ratingB)) {
                confusionMatrix.get(ratingA).put(ratingB, 1);
            }
            else {
                confusionMatrix.get(ratingA).put(ratingB, confusionMatrix.get(ratingA).get(ratingB) + 1);
            }
        }
    }

    public String getConfusionMatrixSerialization() {

        List<Integer> values = new ArrayList<Integer>();
        for (int outerCategory : categories) {
            for (int innerCategory : categories) {
                values.add(confusionMatrix.get(outerCategory).get(innerCategory));
            }
        }
        
        return StringUtils.join(values, ",");
    }

    public void printConfusionMatrix() {
        System.out.print("\t");
        for (int category : categories) {
            System.out.print(category + "\t");
        }
        System.out.println();

        for (int outerCategory : categories) {
            System.out.print(outerCategory + "\t");
            for (int innerCategory : categories) {
                System.out.print(confusionMatrix.get(outerCategory).get(innerCategory) + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public Integer getElement(int categoryA, int categoryB) {
        return confusionMatrix.get(categoryA).get(categoryB);
    }
    
    public List<Integer> getRatingsA()
    {
        return ratingsA;
    }

    public List<Integer> getRatingsB()
    {
        return ratingsB;
    }
}
