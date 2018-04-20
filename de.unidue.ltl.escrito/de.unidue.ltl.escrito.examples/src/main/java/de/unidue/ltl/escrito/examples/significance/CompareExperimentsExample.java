package de.unidue.ltl.escrito.examples.significance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.core.EvaluationEntry;
import de.unidue.ltl.evaluation.measures.significance.mcnemar.McNemar;
import de.unidue.ltl.evaluation.measures.significance.mcnemar.McNemarType;

/**
 * 
 * @author andrea
 *
 * In order to compare two or more experiments on the same data, 
 * the output of the experimental runs has to be placed in a common folder.
 * Make sure that the experiments have different names.
 * At the moment, we support evaluations of standard train-test setups
 *
 *
 */
public class CompareExperimentsExample {

	public static void main(String[] args) throws Exception{
		String experimentFolder = "/Users/andrea/dkpro/org.dkpro.lab/repository/DataForComparison";
		compareExperimets(experimentFolder);
	}

	private static void compareExperimets(String experimentFolder) throws Exception {
		Map<String, EvaluationData<String>> evaluations = new HashMap<String, EvaluationData<String>>();
		Map<String, Map<String, String>> resultsPerRunPerId = new HashMap<String, Map<String, String>>();
		// read all subfolders and identify WekaTestTasks
		File exps = new File(experimentFolder);
		File[] directories = exps.listFiles();
		for (File dir : directories){
			if (dir.isDirectory() && isRelevantDir(dir)){
				String expName = extractExperimentName(dir);
				System.out.println(expName);
				// read labeledItemsFiles and transfer to eval format
				EvaluationData<String> evaluationData = readLabeledItems(dir);
				evaluations.put(expName, evaluationData);
				resultsPerRunPerId.put(expName, new HashMap<String, String>());
				Iterator<EvaluationEntry<String>> iter = evaluationData.iterator();
				while (iter.hasNext()){
					EvaluationEntry<String> e = iter.next();
					resultsPerRunPerId.get(expName).put(e.getName(), e.getPredicted());
				}
			}
		}
		// compare and write overall output
		System.out.println("Identified "+evaluations.keySet().size()+" experiments for Comparison");
		if (evaluations.keySet().size() > 1){
			List<String> expNames = new ArrayList<String>();
			expNames.addAll(evaluations.keySet());
			Collections.sort(expNames);
			System.out.print("#Id\titem\tgold");
			for (String expName : expNames){
				System.out.print("\t"+expName);
			}
			System.out.println();

			EvaluationData<String> firstEval = evaluations.get(expNames.get(0));
			Iterator<EvaluationEntry<String>> iter = firstEval.iterator();
			while (iter.hasNext()){
				EvaluationEntry<String> e = iter.next();
				String id = e.getName();
				System.out.print(id+"\t"+e.getGold());
				for (String expName : expNames){
					System.out.print("\t"+resultsPerRunPerId.get(expName).get(id));
				}
				System.out.println();
			}
			
			System.out.println("\n\nPairwise Significance (McNemar)");
			for (int i = 0; i<expNames.size(); i++){
				for (int j = 1; j<expNames.size(); j++){
					if (i!=j){
						String exp1 = expNames.get(i);
						String exp2 = expNames.get(j);
						System.out.print("Comparing "+exp1+" and "+exp2+":\t");
						EvaluationData<String> eval1 = evaluations.get(exp1);
						EvaluationData<String> eval2 = evaluations.get(exp2);
						System.out.println(McNemar.computeSignificance(eval1, eval2, McNemarType.YATES));
					}
				}
			}
		}
	}

	private static String extractExperimentName(File dir) {
		String folderName = dir.getName();
		folderName = folderName.substring(13); //cut away WekaTestTask
		folderName = folderName.substring(0, folderName.lastIndexOf("-TrainTest"));
		return folderName;

	}

	private static boolean isRelevantDir(File dir) {
		return dir.getName().startsWith("WekaTestTask");
	}


	public static EvaluationData<String> readLabeledItems(File dir) {
		EvaluationData<String> evaluationString	= new EvaluationData<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(dir+"/labeledItems.txt"));
			String line;
			while ((line = br.readLine())!= null){
				if (line.startsWith("#")){
					// do not do anything
				} else {
					String[] parts = line.split("\t"); 
					String id = parts[0];
					String surface = parts[1];
					String gold = parts[2];
					String prediction = parts[3];	
					evaluationString.register(String.valueOf(gold), String.valueOf(prediction), id+"\t"+surface);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return evaluationString;
	}





}
