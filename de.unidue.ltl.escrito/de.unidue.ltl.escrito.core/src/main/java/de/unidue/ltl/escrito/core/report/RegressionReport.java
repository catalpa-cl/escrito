package de.unidue.ltl.escrito.core.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.StreamWriter;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.ml.weka.task.WekaTestTask;

import de.unidue.ltl.evaluation.ConfusionMatrix;
import de.unidue.ltl.evaluation.EvaluationData;
import de.unidue.ltl.evaluation.measure.agreement.CohenKappa;
import de.unidue.ltl.evaluation.measure.agreement.LinearlyWeightedKappa;
import de.unidue.ltl.evaluation.measure.agreement.QuadraticallyWeightedKappa;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/*
 * Report for regression experiments 
 * 
 */

public class RegressionReport
extends ReportBase
{

	public static final String RESULTS_FILE_NAME = "regression_results.txt";
	public static final String CORRELATION_COEFFICIENT = "correlation coefficient";
	public static final String COHENS_KAPPA = "Cohen's Kappa";
	public static final String LINEAR_KAPPA = "linearly weighted kappa";
	public static final String QUADRATIC_KAPPA = "quadratically weighted kappa";
	private static final String CONFUSION_MATRIX = "confusion matrix";

	// holds overall evaluation results
	Map<String, Double> results = new HashMap<String, Double>();

	@Override
	public void execute()
			throws Exception
	{
		// we have to read the individual results to recompute the evaluation measures
		File evaluationFile = getContext().getFile("evaluation.bin", AccessMode.READONLY);
		File predictionsFile = getContext().getFile("predictions.arff", AccessMode.READONLY);
		Properties props = new Properties();

		// re-read the serialized weka-evaluation report
		weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
				.read(evaluationFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(
				new FileReader(predictionsFile));
		Instances instances = new Instances(reader);
		reader.close(); 

		System.out.println(eval.toSummaryString());
		results.put(CORRELATION_COEFFICIENT, eval.correlationCoefficient());

		EvaluationData<Double> evaluationDouble = new EvaluationData<Double>();
		EvaluationData<String> evaluationString = new EvaluationData<String>();
		Set<Double> allGoldLabels = new HashSet<Double>();
		for (Instance inst : instances){
			allGoldLabels.add(inst.value(instances.attribute("outcome")));
		}
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (Double gold : allGoldLabels){
			if (gold<min){
				min = gold;
			}
			if (gold>max){
				max = gold;
			}
		}
		for (Instance inst : instances){
			double gold = inst.value(instances.attribute("outcome"));
			// round the predictions to full integers as we have as allowed classes
			double predicted = Math.round(inst.value(instances.attribute("prediction")));
			if (predicted < min){
				predicted = min;
			}
			if (predicted > max){
				predicted = max;
			}
			evaluationDouble.register(gold, predicted);
			System.out.println(gold+"\t"+predicted);
			evaluationString.register(String.valueOf((int) gold), String.valueOf((int) predicted));
		}
		CohenKappa<String> kappa = new CohenKappa<String>(evaluationString);
		results.put(COHENS_KAPPA, kappa.getAgreement());

		LinearlyWeightedKappa<Double> lwKappa = new LinearlyWeightedKappa<Double>(evaluationDouble);
		QuadraticallyWeightedKappa<Double> qwKappa = new QuadraticallyWeightedKappa<Double>(evaluationDouble);
		results.put(LINEAR_KAPPA, lwKappa.getAgreement());
		results.put(QUADRATIC_KAPPA, qwKappa.getAgreement());

		ConfusionMatrix<String> matrix = new ConfusionMatrix<>(evaluationString);
		
		String confusionMatrix = matrix.toString();
		for (String s : results.keySet()) {
			System.out.println(s + ": " + results.get(s));
			props.setProperty(s, results.get(s).toString());
		}
		System.out.println(confusionMatrix);

		// todo: this needs to be printed in a different way
		props.setProperty(CONFUSION_MATRIX, confusionMatrix);
		// Write out properties
		getContext().storeBinary(RESULTS_FILE_NAME, new PropertiesAdapter(props));
	}




}
