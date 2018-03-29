package de.unidue.ltl.escrito.core.learningcurve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;



public class CvLearningCurveReport extends BatchReportBase
implements Constants{

	public static final String RESULTS_FILENAME = "classification_results.txt";

	public void execute() throws Exception {
		StorageService store = getContext().getStorageService();
		Properties props = new Properties();
		for (int numTrain : LearningCurveTask.NUMBER_OF_TRAINING_INSTANCES) {
			System.out.println("#Training Instances: "+numTrain);
			ArrayList<Double> kappas_avg = new ArrayList<Double>();
			ArrayList<Double> kappas_min = new ArrayList<Double>();
			ArrayList<Double> kappas_max = new ArrayList<Double>();
			for (TaskContextMetadata subcontext : getSubtasks()) {
				//System.out.println(subcontext.toString());
				if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, subcontext.getId())){
				//	System.out.println("LearningCurveTask: "+subcontext.toString());
					File results = store.locateKey(subcontext.getId(),
							"learningCurveResults_"+numTrain+".txt");
					BufferedReader br = new BufferedReader(new FileReader(results));
					String line;
					while ((line = br.readLine()) != null){
						String[] parts = line.split("=");
						if (parts[0].equals("quadraticWeightedKappa_mean")){
							kappas_avg.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("quadraticWeightedKappa_min")){
							kappas_min.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("quadraticWeightedKappa_max")){
							kappas_max.add(Double.parseDouble(parts[1]));
						}
						//		System.out.println(line);
					}
					br.close();
				}
			}
			Double avg_avg = computeAverage(kappas_avg);
			Double avg_min = computeAverage(kappas_min);
			Double avg_max = computeAverage(kappas_max);
			System.out.println("Avg: "+avg_avg);
			System.out.println("Min: "+avg_min);
			System.out.println("Max: "+avg_max);
			props.setProperty(String.valueOf(numTrain), avg_min+" "+avg_avg+" "+avg_max);
		}
		getContext().storeBinary(RESULTS_FILENAME, new PropertiesAdapter(props));
	}

	private Double computeAverage(ArrayList<Double> values) {
		double sum = 0.0;
		for (double value : values){
			sum+=value;
		}
		return sum/values.size();
	}

}
