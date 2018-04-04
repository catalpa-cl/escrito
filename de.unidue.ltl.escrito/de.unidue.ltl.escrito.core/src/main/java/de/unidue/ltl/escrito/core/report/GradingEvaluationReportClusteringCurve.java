package de.unidue.ltl.escrito.core.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.statistics.agreement.coding.BennettSAgreement;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;
import org.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import org.dkpro.statistics.agreement.coding.ScottPiAgreement;
import org.dkpro.statistics.agreement.distance.OrdinalDistanceFunction;
import org.dkpro.tc.core.Constants;
//import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import de.unidue.ltl.escrito.core.clustering.ClusterExemplarTask;
import weka.classifiers.Evaluation;
import weka.core.SerializationHelper;

/*
 * Whenever we have a learning curve iwth only one datapoint per number of training data
 * 
 */

public class GradingEvaluationReportClusteringCurve extends ReportBase {

	public static final String RESULTS_FILENAME = "classification_results.txt";

	public static final String PERCENTAGEAGREEMENT = "percentageAgreement";
	public static final String STATISTICS_FILE_NAME = "statistics.txt";
	private static final String WEIGHTEDFMESSURE = "weightedFmessure";
	private static final String WEIGHTEDPRECISION = "weightedPrecision";
	private static final String WEIGHTEDRECALL = "weightedRecall";
	private static final String FLEISSKAPPA = "fleissKappa";
	private static final String RANDOLPHSKAPPA = "randolphsKappa";
	private static final String COHENSKAPPA = "cohensKappa";
	private static final String KRIPPENDORFSALPHA = "krippendorfsAlpha";
	private static final String SCOTTSPI = "scottsPi";
	private static final String BENNETS = "bennetsS";
	private static final String ACCURACYOFMAJORCLASS = "accuracyOfMajorClass";
	private static final String MAJORCLASS = "majorClass";
	public static final String QUADRATIC_WEIGHTED_KAPPA = "quadraticWeightedKappa";

	Map<String, Double> results = new HashMap<String, Double>();


	// TODO wieso geht das nicht?
	//@Discriminator(name = "dimension_number_of_training_instances")
	//public static int[] NUMBER_OF_TRAINING_INSTANCES;


	@Override
	public void execute() throws Exception {

		// TODO: Das ist unpraktisch, wir würden diesen Report auch gerne fp
		for (int numberOfClusters : ClusterExemplarTask.NUMBER_OF_TRAINING_INSTANCES ){
			System.out.println(numberOfClusters);
			File evaluationFile = getContext().getFile(Constants.TEST_TASK_OUTPUT_KEY+"/"+Constants.EVAL_FILE_NAME+"_" + numberOfClusters, AccessMode.READONLY);
			
			Properties props = new Properties();

			weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
					.read(evaluationFile.getAbsolutePath());

			CodingAnnotationStudy study = getStudy(eval);
			PercentageAgreement pa = new PercentageAgreement(study);
			FleissKappaAgreement fleissKappa = new FleissKappaAgreement(study);
			RandolphKappaAgreement randolphKappa = new RandolphKappaAgreement(study);
			CohenKappaAgreement cohenKappa = new CohenKappaAgreement(study);
			KrippendorffAlphaAgreement krippendorfAlpha = new KrippendorffAlphaAgreement(
					study, new OrdinalDistanceFunction());
			ScottPiAgreement scottPi = new ScottPiAgreement(study);
			BennettSAgreement bennetS = new BennettSAgreement(study);

			int majorClass = getMajorClass(eval);
			// TP+TN / TP+TN+FP+FN
			double accuracyOfMajorClass = (eval.numTruePositives(majorClass) + eval
					.numTrueNegatives(majorClass))
					/ (eval.numTruePositives(majorClass)
							+ eval.numTrueNegatives(majorClass)
							+ eval.numFalsePositives(majorClass) + eval
							.numFalseNegatives(majorClass));

			results.put(QUADRATIC_WEIGHTED_KAPPA,getQuadraticWeightedKappa(eval));
			results.put(MAJORCLASS, (double) majorClass);
			results.put(ACCURACYOFMAJORCLASS, accuracyOfMajorClass);
			results.put(WEIGHTEDFMESSURE, eval.weightedFMeasure());
			results.put(WEIGHTEDPRECISION, eval.weightedPrecision());
			results.put(WEIGHTEDRECALL, eval.weightedRecall());
			results.put(PERCENTAGEAGREEMENT, pa.calculateAgreement());
			results.put(FLEISSKAPPA, fleissKappa.calculateAgreement());
			results.put(RANDOLPHSKAPPA, randolphKappa.calculateAgreement());
			results.put(COHENSKAPPA, cohenKappa.calculateAgreement());
			results.put(KRIPPENDORFSALPHA, krippendorfAlpha
					.calculateAgreement());
			results.put(SCOTTSPI, scottPi.calculateAgreement());
			results.put(BENNETS, bennetS.calculateAgreement());

			System.out.println(eval.toMatrixString());

			for (String s : results.keySet()) {
				if (s.equals("majorClass")) {
					// translate from index to classname
					String className = WekaUtils.getClassLabels(eval.getHeader(),
							false).get(results.get(s).intValue());
					System.out.println(s + ": " + className);
					props.setProperty(s, className);
				} else {
					System.out.printf(s+": %.2f"+System.getProperty("line.separator"), results.get(s));
					//				System.out.println(s + ": " + results.get(s));
					props.setProperty(s, results.get(s).toString());
				}
			}

			// Write out properties
			getContext().storeBinary(numberOfClusters+"_"+RESULTS_FILENAME,
					new PropertiesAdapter(props));


		}
	}

	private Double getQuadraticWeightedKappa(Evaluation eval) {
		List<String> classLabels = WekaUtils.getClassLabels(eval.getHeader(), false);

		List<Integer> classLabelsInteger = new ArrayList<Integer>();
		for (String classLabel : classLabels) {
			classLabelsInteger.add(Integer.parseInt(classLabel));
		}

		double[][] confusionMatrix = eval.confusionMatrix();

		List<Integer> goldLabelsList = new ArrayList<Integer>();
		List<Integer> predictedLabelsList = new ArrayList<Integer>();


		//        EvaluationData<Double> evaluationDouble 
		//		= new EvaluationData<Double>();
		//		EvaluationData<String> evaluationString 
		//		= new EvaluationData<String>();

		// fill rating lists from weka confusion matrix
		for (int c = 0; c < confusionMatrix.length; c++) {
			for (int r = 0; r < confusionMatrix.length; r++) {
				for (int i=0; i < (int) confusionMatrix[c][r]; i++) {
					goldLabelsList.add(classLabelsInteger.get(c));
					predictedLabelsList.add(classLabelsInteger.get(r));
					//     System.out.println(1.0*classLabelsInteger.get(c)+" "+1.0*classLabelsInteger.get(r));
					//                    evaluationDouble.register(1.0*classLabelsInteger.get(c), 1.0*classLabelsInteger.get(r));
					//                    evaluationString.register(String.valueOf(classLabelsInteger.get(c)), String.valueOf(classLabelsInteger.get(r)));
				}
			}
		}
		//        Accuracy<String> acc = new Accuracy<String>(evaluationString);
		//		results.put("Acc2", acc.getAccuracy());
		//		CohenKappa<String> kappa = new CohenKappa<String>(evaluationString);
		//		results.put("CohensKappa2", kappa.getAgreement());
		//
		//		LinearlyWeightedKappa<Double> lwKappa = new LinearlyWeightedKappa<Double>(evaluationDouble);
		//		QuadraticallyWeightedKappa<Double> qwKappa = new QuadraticallyWeightedKappa<Double>(evaluationDouble);
		//		results.put("LinearKappa2", lwKappa.getAgreement());
		//		results.put("QuadraticKappa2", qwKappa.getAgreement());
		//
		//		ConfusionMatrix<String> matrix = new ConfusionMatrix<String>(evaluationString);
		//		String confusionMatrix2 = matrix.toString();
		//		System.out.println(confusionMatrix2);
		// TODO: ist das das richtige Kappa?
		return QuadraticWeightedKappa.getKappa(goldLabelsList, predictedLabelsList, classLabelsInteger.toArray(new Integer[0]));
	}

	// find the major class
	private int getMajorClass(Evaluation eval) {
		double[][] confusionMatrix = eval.confusionMatrix();
		int tempResult = 0;
		int tempSum = 0;
		for (int i = 0; i < confusionMatrix[0].length; i++) {
			int sum = getSum(confusionMatrix[i]);
			if (sum > tempSum) {
				tempResult = i;
				tempSum = sum;
			}
		}
		return tempResult;
	}

	private int getSum(double[] ds) {
		int result = 0;
		for (double d : ds) {
			result += (int) d;
		}
		return result;
	}

	/**
	 * get a CodingAnnotationStudy from the weka evaluation
	 * 
	 * @param eval
	 * @return study
	 */
	private CodingAnnotationStudy getStudy(Evaluation eval) {
		List<String> classLabels = WekaUtils.getClassLabels(eval.getHeader(),
				false);
		CodingAnnotationStudy study = new CodingAnnotationStudy(2);
		double[][] confusionMatrix = eval.confusionMatrix();

		for (int i = 0; i < confusionMatrix[0].length; i++) {
			for (int j = 0; j < confusionMatrix[0].length; j++) {
				study.addMultipleItems((int) confusionMatrix[i][j],
						classLabels.get(i), classLabels.get(j));
			}
		}
		return study;
	}

}
