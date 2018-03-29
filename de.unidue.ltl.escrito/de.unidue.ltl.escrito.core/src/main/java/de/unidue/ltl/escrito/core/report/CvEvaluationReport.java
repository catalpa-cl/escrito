package de.unidue.ltl.escrito.core.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import de.unidue.ltl.evaluation.ConfusionMatrix;
import de.unidue.ltl.evaluation.EvaluationData;
import de.unidue.ltl.evaluation.measure.agreement.CohenKappa;
import de.unidue.ltl.evaluation.measure.agreement.LinearlyWeightedKappa;
import de.unidue.ltl.evaluation.measure.agreement.QuadraticallyWeightedKappa;
import de.unidue.ltl.evaluation.measure.categorial.Accuracy;
import weka.core.Instance;

public class CvEvaluationReport extends BatchReportBase
implements Constants{

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
	private static final String ACCURACY = "accuracy";
	public static final String QUADRATIC_WEIGHTED_KAPPA = "quadraticWeightedKappa";
	public static final String LINEAR_KAPPA = "linearly weighted kappa";
	private static final String CONFUSION_MATRIX = "confusion matrix";

	public void execute() throws Exception {
		StorageService store = getContext().getStorageService();


		for (TaskContextMetadata subcontext : getSubtasks()) {
			System.out.println(subcontext.toString());
			if (!TcTaskTypeUtil.isCrossValidationTask(store, subcontext.getId())) {
				continue;
			}
			Properties props = new Properties();

			File id2oFile = store.locateKey(subcontext.getId(),
					 Constants.FILE_COMBINED_ID_OUTCOME_KEY);
			// the serialized output seem to to be not working correctly.
			//Constants.SERIALIZED_ID_OUTCOME_KEY);


			//			ObjectInputStream inputStream = new ObjectInputStream(
			//					new FileInputStream(id2o));
			//			Id2Outcome id2Outcome = (Id2Outcome) inputStream.readObject();
			//			inputStream.close();
			EvaluationData<Double> evaluationDouble 
			= new EvaluationData<Double>();
			EvaluationData<String> evaluationString 
			= new EvaluationData<String>();


			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(id2oFile));
				String line;
				Map<String, String> labelMappings = new HashMap<>();
				while ((line = br.readLine())!= null){
					if (line.startsWith("#ID")){
						// do not do anything
					} else if (line.startsWith("#labels")) {
						// example #labels 0=10 1=11 2=12 3=2 4=3 5=4 6=5 7=6 8=7 9=8 10=9
						String[] labelMappingStrings = line.split(" ");
						for (int i = 1; i<labelMappingStrings.length; i++){
							String part = labelMappingStrings[i];
							if (part.contains("=")){
								labelMappings.put(part.split("=")[0], part.split("=")[1]);
							}
						}
					} else {
						// example line: 623_0=0,0,0,0,0,0,0,0,0,1,0;0,0,0,0,0,0,0,0,0,0,1;-1.0
						String id = line.split("=")[0];
						String everythingElse = line.split("=")[1];
						String prediction = everythingElse.split(";")[0];
						String gold = everythingElse.split(";")[1];
						String[] predictions = prediction.split(",");
						String[] golds = gold.split(",");
						int goldIndex = -1;
						int predictionIndex = -1;
						for (int i = 0; i<predictions.length; i++){
							if (predictions[i].equals("1")){
								predictionIndex = i;
								break;
							}
						}
						for (int i = 0; i<golds.length; i++){
							if (golds[i].equals("1")){
								goldIndex = i;
								break;
							}
						}
						evaluationString.register(labelMappings.get(String.valueOf(goldIndex)), labelMappings.get(String.valueOf(predictionIndex)));
						evaluationDouble.register(Double.parseDouble(labelMappings.get(String.valueOf(goldIndex))), Double.parseDouble(labelMappings.get(String.valueOf(predictionIndex))));
				//		System.out.println(line+"\t"+predictionIndex+"\t"+goldIndex);
					}

				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			double[] goldLabels = new double[(int) evaluationDouble.size()];
			double[] predictions =  new double[(int) evaluationDouble.size()];
			
			for (int i = 0; i<evaluationDouble.size(); i++){
				goldLabels[i] = evaluationDouble.get(i).getGold();
				predictions[i] = evaluationDouble.get(i).getPredicted();
			}
			
			Map<String, Double> results = new HashMap<String, Double>();

			Set<Double> allGoldLabels = new HashSet<Double>();
			for (double gold : goldLabels){
				allGoldLabels.add(gold);
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
			for (int i = 0; i<goldLabels.length; i++){
				double gold = goldLabels[i];
				// round the predictions to full integers as we have as allowed classes
				double predicted = Math.round(predictions[i]);
				if (predicted < min){
					predicted = min;
				}
				if (predicted > max){
					predicted = max;
				}
			//	evaluationDouble.register(gold, predicted);
			//	evaluationString.register(String.valueOf(gold), String.valueOf(predicted));
			}
			Accuracy acc = new Accuracy(evaluationString);
			results.put(ACCURACY, acc.getAccuracy());
			CohenKappa<String> kappa = new CohenKappa<String>(evaluationString);
			results.put(COHENSKAPPA, kappa.getAgreement());

			LinearlyWeightedKappa lwKappa = new LinearlyWeightedKappa(evaluationDouble);
			QuadraticallyWeightedKappa qwKappa = new QuadraticallyWeightedKappa(evaluationDouble);
			results.put(LINEAR_KAPPA, lwKappa.getAgreement());
			results.put(QUADRATIC_WEIGHTED_KAPPA, qwKappa.getAgreement());

			ConfusionMatrix<String> matrix = new ConfusionMatrix<String>(evaluationString);
			String confusionMatrix = matrix.toString();
			System.out.println(matrix);

			for (String s : results.keySet()) {
				if (s.equals("majorClass")) {
					// translate from index to classname
					//					String className = WekaUtils.getClassLabels(eval.getHeader(),
					//							false).get(results.get(s).intValue());
					//					System.out.println(s + ": " + className);
					//					props.setProperty(s, className);
				} else {
					System.out.printf(s+": %.2f"+System.getProperty("line.separator"), results.get(s));
					props.setProperty(s, results.get(s).toString());
				}
			}
			// TODO: this needs to be printed in a different way
			props.setProperty(CONFUSION_MATRIX, confusionMatrix);


			// Write out properties
			getContext().storeBinary(RESULTS_FILENAME, new PropertiesAdapter(props));


		}


	}










}
