package de.unidue.ltl.escrito.core.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	
	
	
}
