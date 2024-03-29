/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.unidue.ltl.escrito.core.learningcurve;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcAbstractReport;
import org.dkpro.tc.ml.weka.core._eka;

import de.unidue.ltl.escrito.core.report.ReportUtils;
import de.unidue.ltl.escrito.core.visualization.XYChartPlotter;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import de.unidue.ltl.evaluation.measures.agreement.QuadraticallyWeightedKappa;
import weka.core.SerializationHelper;


/**
 * Report that computes evaluation results given the classification results.
 * Collects results for each of the learning curve runs.
 */
public class LearningCurveReport
extends TcAbstractReport
implements Constants
{

	public static final String RESULTS_FILENAME = "learningCurveResults";
	public static final String QUADRATIC_WEIGHTED_KAPPA_MEAN = "quadraticWeightedKappa_mean";
	public static final String QUADRATIC_WEIGHTED_KAPPA_MIN = "quadraticWeightedKappa_min";
	public static final String QUADRATIC_WEIGHTED_KAPPA_MAX = "quadraticWeightedKappa_max";
	public static final String ACCURACY_MEAN = "accuracy_mean";
	public static final String ACCURACY_MIN = "accuracy_min";
	public static final String ACCURACY_MAX = "accuracy_max";


	List<String> actualLabelsList = new ArrayList<String>();
	List<String> predictedLabelsList = new ArrayList<String>();
	// in ML mode, holds a map for building the Label Power Set over all label actuals/predictions
	HashMap<String, Map<String, Integer>> tempM = new HashMap<String, Map<String, Integer>>();
	// holds overall CV results
	Map<String, Double> results = new HashMap<String, Double>();
	// holds PR curve data
	List<double[][]> prcData = new ArrayList<double[][]>();
	public static Double [][] accumulatedArrayResults = new Double[LearningCurveTask.NUMBER_OF_TRAINING_INSTANCES.length][10];

	@Discriminator(name = "dimension_iterations")
	public Integer ITERATIONS;

	@Override
	public void execute()
			throws Exception
	{
		System.out.println("Execute Learning Curve Report");
		String LearningCurveTaskId = null;
		String report = "";
		List<Double> numInstances = new ArrayList<Double>();
		List<Double> minValues = new ArrayList<Double>();
		List<Double> maxValues = new ArrayList<Double>();
		List<Double> avgValues = new ArrayList<Double>();
		List<Double> minValues_acc = new ArrayList<Double>();
		List<Double> maxValues_acc = new ArrayList<Double>();
		List<Double> avgValues_acc = new ArrayList<Double>();
		for (Integer numberOfInstances : LearningCurveTask.NUMBER_OF_TRAINING_INSTANCES) {
			numInstances.add(numberOfInstances*1.0);
			List<Configuration> selectedItemsOverall = new ArrayList<Configuration>();
			Properties props = new Properties();
			List<Double> kappas = new ArrayList<Double>();
			List<Double> accs = new ArrayList<Double>();
			for (int iteration=0; iteration<LearningCurveTask.ITERATIONS; iteration++) {
				//	System.out.println("Iteration: "+iteration);
				File evaluationFile = null;
				File selectedItemFile = null;
				StorageService storageService = getContext().getStorageService();
				Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());
				List<String> allIds = new ArrayList<String>();
				allIds.addAll(collectTasks(taskIds));
				for (String id : taskIds) {
					if (!TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, id)) {
						continue;
					}
					LearningCurveTaskId = id;
					evaluationFile = storageService.locateKey(id, Constants.TEST_TASK_OUTPUT_KEY+"/"+Constants.EVAL_FILE_NAME+"_" + numberOfInstances + "_" + iteration);
					selectedItemFile = storageService.locateKey(id, Constants.TEST_TASK_OUTPUT_KEY+"/"+Constants.EVAL_FILE_NAME+"_" + numberOfInstances + "_" + iteration+"_itemIds.txt");
				}

				// we need to check non-existing files as we might skip some training sizes
				if (!evaluationFile.exists()) {
					continue;
				}

				// read selected Items
				BufferedReader br = new BufferedReader(new FileReader(selectedItemFile));
				Set<String> selectedItems = new HashSet<String>();
				String line = br.readLine();
				while (line != null){
					line = line.trim();
					selectedItems.add(line);
					line = br.readLine();
				}
				br.close();

				weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
						.read(evaluationFile.getAbsolutePath());

				//System.out.println(eval.getHeader());
				List<String> classLabels = _eka.getClassLabels(eval.getHeader(), false);
				//System.out.println("Classlabels: "+classLabels);
				List<Integer> classLabelsInteger = new ArrayList<Integer>();
				for (String classLabel : classLabels) {
					classLabelsInteger.add((int) (Double.parseDouble(classLabel)));
					//classLabelsInteger.add(Integer.parseInt(classLabel));
				}

				double[][] confusionMatrix = eval.confusionMatrix();

				List<Integer> goldLabelsList = new ArrayList<Integer>();
				List<Integer> predictedLabelsList = new ArrayList<Integer>();

				// fill rating lists from weka confusion matrix
				for (int c = 0; c < confusionMatrix.length; c++) {
					for (int r = 0; r < confusionMatrix.length; r++) {
						for (int i=0; i < (int) confusionMatrix[c][r]; i++) {
							goldLabelsList.add(classLabelsInteger.get(c));
							predictedLabelsList.add(classLabelsInteger.get(r));
						}
					}
				}

				EvaluationData<Integer> evalData = new EvaluationData<Integer>();
				for (int i = 0; i<goldLabelsList.size(); i++){
					evalData.register(goldLabelsList.get(i), predictedLabelsList.get(i));
				}
				QuadraticallyWeightedKappa<Integer> qwk = new QuadraticallyWeightedKappa<Integer>(evalData);
				double kappa = qwk.getResult();

				kappas.add(kappa);
				selectedItemsOverall.add(new Configuration(kappa, selectedItems));
				
				Accuracy<Integer> accuracy = new Accuracy<Integer>(evalData);
				double acc = accuracy.getResult();
				accs.add(acc);
			}

			double min_qwk = -1.0;
			double max_qwk = -1.0;
			double min_acc = -1.0;
			double max_acc = -1.0;
			if (kappas.size() > 0) {
				min_qwk = Collections.min(kappas);
				max_qwk = Collections.max(kappas);
			}
			if (accs.size() > 0) {
				min_acc = Collections.min(accs);
				max_acc = Collections.max(accs);
			}
			double meanKappa = ReportUtils.getMeanKappa(kappas);
			double meanAcc = ReportUtils.getMean(accs);
			results.put(QUADRATIC_WEIGHTED_KAPPA_MEAN, meanKappa);
			results.put(QUADRATIC_WEIGHTED_KAPPA_MIN, min_qwk);
			results.put(QUADRATIC_WEIGHTED_KAPPA_MAX, max_qwk);
			results.put(ACCURACY_MEAN, meanAcc);
			results.put(ACCURACY_MIN, min_acc);
			results.put(ACCURACY_MAX, max_acc);
			System.out.println(numberOfInstances + "\t" + meanKappa + "\t" + min_qwk + "\t" + max_qwk + "\t" + meanAcc + "\t" + min_acc + "\t" + max_acc);
			report += (numberOfInstances + "\t" + meanKappa + "\t" + min_qwk + "\t" + max_qwk + "\n");
			minValues.add(min_qwk);
			maxValues.add(max_qwk);
			avgValues.add(meanKappa);
			minValues_acc.add(min_acc);
			maxValues_acc.add(max_acc);
			avgValues_acc.add(meanAcc);
			
			for (String s : results.keySet()) {
				props.setProperty(s, results.get(s).toString());
			}

			// Write out properties
			getContext().getStorageService().storeBinary(LearningCurveTaskId, RESULTS_FILENAME + "_" + numberOfInstances+".txt", new PropertiesAdapter(props));
			//	getContext().storeBinary(RESULTS_FILENAME + "_" + numberOfInstances+".txt", new PropertiesAdapter(props));


			selectedItemsOverall.sort(null);

			BufferedWriter bw = new BufferedWriter(new FileWriter(getContext().getStorageService().locateKey(LearningCurveTaskId, "selectedItemIds_"+numberOfInstances+".txt")));
			//	BufferedWriter bw = new BufferedWriter(new FileWriter(getContext().getFile("selectedItemIds_"+numberOfInstances+".txt", AccessMode.READWRITE)));

			for (Configuration c : selectedItemsOverall){
				bw.write(c.toString()+"\n");
			}
			bw.close();
		}
		BufferedWriter bw_gesamt = new BufferedWriter(new FileWriter(getContext().getStorageService().locateKey(LearningCurveTaskId, "learningCurveResult.txt")));
		bw_gesamt.write(report);
		bw_gesamt.close();
		XYChartPlotter plotter = new XYChartPlotter("# training data", "QWK", "LearningCurve");
		plotter.addSeries(numInstances, minValues, "min", Color.red);
		plotter.addSeries(numInstances, maxValues, "max", Color.GREEN);
		plotter.addSeries(numInstances, avgValues, "mean", Color.BLACK);
		plotter.plot(getContext().getStorageService().locateKey(LearningCurveTaskId, "learningCurve_QWK.jpeg"));
		plotter = new XYChartPlotter("# training data", "Accuracy", "LearningCurve");
		plotter.addSeries(numInstances, minValues_acc, "min", Color.red);
		plotter.addSeries(numInstances, maxValues_acc, "max", Color.GREEN);
		plotter.addSeries(numInstances, avgValues_acc, "mean", Color.BLACK);
		plotter.plot(getContext().getStorageService().locateKey(LearningCurveTaskId, "learningCurve_accuracy.jpeg"));
		
		LeaningCurveReportUtils.writeLatex(numInstances, minValues, maxValues, avgValues, LearningCurveTaskId, "QWK", getContext().getStorageService().locateKey(LearningCurveTaskId, "learningCurveResult_QWK.tex"));
		LeaningCurveReportUtils.writeLatex(numInstances, minValues_acc, maxValues_acc, avgValues_acc, LearningCurveTaskId, "accuracy", getContext().getStorageService().locateKey(LearningCurveTaskId, "learningCurveResult_accuracy.tex"));
	}

	
}


class Configuration implements Comparable<Configuration>{

	double kappa;
	Set<String> itemIds;

	public Configuration(double kappa, Set<String> itemIds){
		this.kappa = kappa;
		this.itemIds = itemIds;
	}

	public String toString(){
		return this.kappa+"\t"+this.itemIds.toString();
	}

	@Override
	public int compareTo(Configuration c) {
		return Double.compare(this.kappa,c.kappa); 
	}

}
