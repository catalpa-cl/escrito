package de.unidue.ltl.escrito.io.util;

import java.util.HashMap;
import java.util.Map;

public class ExperimentalResultsCollectionUtils {

	/**
	 * 
	 * 
	 * @param experimentalFolderPath a folder containing the output folders for all relevant experiments
	 * @param evalMeasure the measure to be considered
	 * @return a Map from ExperimentIds to the value for the specific measure
	 */
	public static Map<String, Double> getEvalValueForAllExperiments(String experimentalFolderPath, String evalMeasure){
		return getEvalValueForAllExperiments(experimentalFolderPath, evalMeasure, ".*");
	}
	
	
	
	/**
	 * 
	 * 
	 * @param experimentalFolderPath a folder containing the output folders for all relevant experiments
	 * @param evalMeasure the measure to be considered
	 * @param filterPattern pattern to be matched against the experiment name
	 * @return a Map from ExperimentIds to the value for the specific measure
	 */
	public static Map<String, Double> getEvalValueForAllExperiments(String experimentalFolderPath, String evalMeasure, String filterPattern){
		Map<String, Double> results = new HashMap<String, Double>();
		// TODO
		
		return results;
	}
	
	/**
	 * 
	 * @param experimentalFolderPath
	 * @param filterPattern
	 * @return a map from prompt id to answerid to classification value
	 */
	public static Map<String, Map<String, String>> getClassificationResultsForAllExperiments(String experimentalFolderPath, String filterPattern){
		Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();
		// TODO
		
		return results;
	}
}
