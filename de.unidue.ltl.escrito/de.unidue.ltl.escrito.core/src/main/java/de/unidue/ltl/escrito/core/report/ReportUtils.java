package de.unidue.ltl.escrito.core.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import de.unidue.ltl.evaluation.core.EvaluationData;

public class ReportUtils {

	public static EvaluationData<Double> readId2OutcomeAsDouble(File id2outcomeFile) {
		EvaluationData<Double> evaluationDouble = new EvaluationData<Double>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(id2outcomeFile));
			String line;
			Map<String, String> labelMappings = null;
			while ((line = br.readLine())!= null){
				 if (line.startsWith("#labels")) {
					labelMappings = computeLabelMapping(line);	
				} else if (line.startsWith("#")){
					// do not do anything
				} else {
					// example line: 623_0=1;1;-1.0
					String id = line.split("=")[0];
					String everythingElse = line.split("=")[1];
					String prediction = everythingElse.split(";")[0];
					String gold = everythingElse.split(";")[1];
					evaluationDouble.register(Double.parseDouble(labelMappings.get(String.valueOf(gold))), Double.parseDouble(labelMappings.get(String.valueOf(prediction))));
		//			System.out.println(line+"\t"+prediction+"\t"+gold);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return evaluationDouble;
	}



	public static EvaluationData<String> readId2OutcomeAsString(File id2outcomeFile) {
		EvaluationData<String> evaluationString	= new EvaluationData<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(id2outcomeFile));
			String line;
			Map<String, String> labelMappings = null;
			while ((line = br.readLine())!= null){
				if (line.startsWith("#labels")) {
					labelMappings = computeLabelMapping(line);	
				} else if (line.startsWith("#")){
					// do not do anything
				} else {
					// example line: 623_0=1;1;-1.0
					String id = line.split("=")[0];
					String everythingElse = line.split("=")[1];
					String prediction = everythingElse.split(";")[0];
					String gold = everythingElse.split(";")[1];
					evaluationString.register(labelMappings.get(String.valueOf(gold)), labelMappings.get(String.valueOf(prediction)));
		//			System.out.println(line+"\t"+prediction+"\t"+gold);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return evaluationString;
	}
	
	private static Map<String, String> computeLabelMapping(String line) {
		Map<String, String> labelMappings = new HashMap<>();
		// example #labels 0=10 1=11 2=12 3=2 4=3 5=4 6=5 7=6 8=7 9=8 10=9
		String[] labelMappingStrings = line.split(" ");
		for (int i = 1; i<labelMappingStrings.length; i++){
			String part = labelMappingStrings[i];
			if (part.contains("=")){
				labelMappings.put(part.split("=")[0], part.split("=")[1]);
			}
		}
		return labelMappings;
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
	
	    /**
	     * Compute mean for Fisher-Z score transformed kappas and then transform back.
	     * 
	     * @param kappas kappa values
	     * @param weights 
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
