package de.unidue.ltl.escrito.core.learningcurve;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcAbstractReport;

import de.unidue.ltl.escrito.core.visualization.XYChartPlotter;



public class CvLearningCurveReport extends TcAbstractReport
implements Constants{

	public static final String RESULTS_FILENAME_ACC = "classification_results_accuracy.txt";
	public static final String RESULTS_FILENAME_QWK = "classification_results_qwk.txt";

	public void execute() throws Exception {
		//StorageService store = getContext().getStorageService();
		Properties props_acc = new Properties();
		Properties props_kappa = new Properties();
		System.out.println("Executing Learning Curve CV report.");

		Set<String> idPool = collectTasks(getTaskIdsFromMetaData(getSubtasks()));
		
		String cvTaskId = null;
		StorageService storageService = getContext().getStorageService();
		for (String id : getTaskIdsFromMetaData(getSubtasks())){
			if (TcTaskTypeUtil.isCrossValidationTask(storageService, id)) {
				cvTaskId = id;;
			}
		}
		System.out.println(cvTaskId);
		
		StorageService store = getContext().getStorageService();
		List<Double> numInstances = new ArrayList<Double>();
		List<Double> minValues = new ArrayList<Double>();
		List<Double> maxValues = new ArrayList<Double>();
		List<Double> avgValues = new ArrayList<Double>();
		List<Double> minValues_acc = new ArrayList<Double>();
		List<Double> maxValues_acc = new ArrayList<Double>();
		List<Double> avgValues_acc = new ArrayList<Double>();
		
		for (int numTrain : LearningCurveTask.NUMBER_OF_TRAINING_INSTANCES) {
			numInstances.add(numTrain*1.0);
			System.out.println("#Training Instances: "+numTrain);
			ArrayList<Double> kappas_avg = new ArrayList<Double>();
			ArrayList<Double> kappas_min = new ArrayList<Double>();
			ArrayList<Double> kappas_max = new ArrayList<Double>();
			ArrayList<Double> accs_avg = new ArrayList<Double>();
			ArrayList<Double> accs_min = new ArrayList<Double>();
			ArrayList<Double> accs_max = new ArrayList<Double>();
			for (String id : idPool) {
//				System.out.println("id: "+id);
				if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)){
				//	System.out.println("LearningCurveTask: "+id);
					File results = store.locateKey(id,
							"learningCurveResults_"+numTrain+".txt");
					System.out.println(results.getAbsolutePath());
					BufferedReader br = new BufferedReader(new FileReader(results));
					String line;
					while ((line = br.readLine()) != null){
//						System.out.println(line);
						String[] parts = line.split("=");
						if (parts[0].equals("quadraticWeightedKappa_mean")){
							kappas_avg.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("quadraticWeightedKappa_min")){
							kappas_min.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("quadraticWeightedKappa_max")){
							kappas_max.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("accuracy_mean")){
							accs_avg.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("accuracy_min")){
							accs_min.add(Double.parseDouble(parts[1]));
						} else if (parts[0].equals("accuracy_max")){
							accs_max.add(Double.parseDouble(parts[1]));
						}
						//		System.out.println(line);
					}
					br.close();
				}
			}
			Double kappa_avg = computeAverage(kappas_avg);
			Double kappa_min = computeAverage(kappas_min);
			Double kappa_max = computeAverage(kappas_max);
			
			Double acc_avg = computeAverage(accs_avg);
			Double acc_min = computeAverage(accs_min);
			Double acc_max = computeAverage(accs_max);
			
			minValues.add(kappa_min);
			maxValues.add(kappa_max);
			avgValues.add(kappa_avg);
			minValues_acc.add(acc_min);
			maxValues_acc.add(acc_max);
			avgValues_acc.add(acc_avg);
			System.out.println("Avg: "+kappa_avg);
			System.out.println("Min: "+kappa_min);
			System.out.println("Max: "+kappa_max);
			props_kappa.setProperty(String.valueOf(numTrain)+"_kappa", kappa_min+" "+kappa_avg+" "+kappa_max);
			props_acc.setProperty(String.valueOf(numTrain)+"_acc", acc_min+" "+acc_avg+" "+acc_max);
		}
		getContext().storeBinary(RESULTS_FILENAME_QWK, new PropertiesAdapter(props_kappa));
		getContext().storeBinary(RESULTS_FILENAME_ACC, new PropertiesAdapter(props_acc));
		
		XYChartPlotter plotter = new XYChartPlotter("# training data", "QWK", "LearningCurve");
		plotter.addSeries(numInstances, minValues, "min", Color.red);
		plotter.addSeries(numInstances, maxValues, "max", Color.GREEN);
		plotter.addSeries(numInstances, avgValues, "mean", Color.BLACK);
		plotter.plot(getContext().getStorageService().locateKey(cvTaskId, "learningCurve_QWK.jpeg"));
		plotter = new XYChartPlotter("# training data", "Accuracy", "LearningCurve");
		plotter.addSeries(numInstances, minValues_acc, "min", Color.red);
		plotter.addSeries(numInstances, maxValues_acc, "max", Color.GREEN);
		plotter.addSeries(numInstances, avgValues_acc, "mean", Color.BLACK);
		plotter.plot(getContext().getStorageService().locateKey(cvTaskId, "learningCurve_accuracy.jpeg"));
		
		LeaningCurveReportUtils.writeLatex(numInstances, minValues, maxValues, avgValues, cvTaskId, "QWK", getContext().getStorageService().locateKey(cvTaskId, "learningCurveResult_QWK.tex"));
		LeaningCurveReportUtils.writeLatex(numInstances, minValues_acc, maxValues_acc, avgValues_acc, cvTaskId, "accuracy", getContext().getStorageService().locateKey(cvTaskId, "learningCurveResult_accuracy.tex"));
	
	
	}

	private Double computeAverage(ArrayList<Double> values) {
		double sum = 0.0;
		for (double value : values){
			sum+=value;
		}
		return sum/values.size();
	}

}
