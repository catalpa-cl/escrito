package de.unidue.ltl.escrito.core.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcAbstractReport;

import de.unidue.ltl.escrito.core.Utils;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import de.unidue.ltl.evaluation.measures.agreement.CohenKappa;
import de.unidue.ltl.evaluation.measures.agreement.LinearlyWeightedKappa;
import de.unidue.ltl.evaluation.measures.agreement.QuadraticallyWeightedKappa;
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.categorial.Precision;
import de.unidue.ltl.evaluation.measures.categorial.Recall;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;

public class GradingEvaluationReport extends TcAbstractReport {

	public static final String RESULTS_FILENAME = "classification_results.txt";
	public static final String LABELED_ITEMS_FILENAME = "labeledItems.txt";

	private static final String WEIGHTEDFMEASURE = "weightedFmeasure";
	private static final String MICROFMEASURE = "microAveragedFmessure";
	private static final String MICROPRECISION = "microAveragedPrecision";
	private static final String MICRORECALL = "macroAveragedRecall";
	private static final String MACROFMEASURE = "macroAveragedFmessure";
	private static final String MACROPRECISION = "macroAveragedPrecision";
	private static final String MACRORECALL = "microAveragedRecall";
	private static final String COHENSKAPPA = "cohensKappa";
	private static final String ACCURACYOFMAJORCLASS = "accuracyOfMajorClass";
	private static final String MAJORCLASS = "majorClass";
	private static final String ACCURACY = "accuracy";
	private static final String LINEAR_KAPPA = "linearly weighted kappa";
	private static final String QUADRATIC_WEIGHTED_KAPPA = "quadratically weighted kappa";
	private static final String PEARSON = "pearson correlation";
	private static final String SPEARMAN = "spearman correlation";

	Map<String, Double> results = new HashMap<String, Double>();

	@Override
	public void execute() throws Exception {
		System.out.println("Grading Evaluation Report:");
		File evaluationFile = null;
		File evaluationFileMajority = null;
		File id2ConfidenceScoreFile = null;
		StorageService storageService = getContext().getStorageService();
		Map<String, String> instanceId2TextMap = new HashMap<String, String>();

		Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());
		List<String> allIds = new ArrayList<String>();
		allIds.addAll(collectTasks(taskIds));

		for (String id : taskIds) {

			if (TcTaskTypeUtil.isFeatureExtractionTestTask(storageService, id)) {
				String path = storageService.locateKey(id, "output/documentMetaData.txt").getAbsolutePath();
			//	System.out.println(path);
				instanceId2TextMap = Utils.getInstanceId2TextMap(path);
				System.out.println("Read map with "+instanceId2TextMap.size()+" entries");
			}
		//	System.out.println(id);
			if (!TcTaskTypeUtil.isFacadeTask(storageService, id)) {
				continue;
			}
			Set<String> wrapped = new HashSet<>();
			wrapped.add(id);
			Set<String> subTaskId = collectTasks(wrapped);
			for (String subId : subTaskId) {
				if (!TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, subId)) {
					continue;
				}
				evaluationFile = storageService.locateKey(subId, Constants.ID_OUTCOME_KEY);
				evaluationFileMajority = storageService.locateKey(subId, Constants.BASELINE_MAJORITIY_ID_OUTCOME_KEY);
				id2ConfidenceScoreFile = storageService.locateKey(subId, "id2ConfScore.txt");
				System.out.println(evaluationFile);
				System.out.println(evaluationFileMajority);
			}
		}

		Properties props = new Properties();

		EvaluationData<Double> evaluationDouble = ReportUtils.readId2OutcomeAsDouble(evaluationFile);
		EvaluationData<String> evaluationString = ReportUtils.readId2OutcomeAsString(evaluationFile);
		EvaluationData<String> evaluationStringMajority = ReportUtils.readId2OutcomeAsString(evaluationFileMajority);

		Map<Integer, Double> confScoreMap = ReportUtils.readId2ConfidenceScore(id2ConfidenceScoreFile);



		Accuracy<String> acc = new Accuracy<String>(evaluationString);
		results.put(ACCURACY, acc.getResult());

		Accuracy<String> accMaj = new Accuracy<String>(evaluationStringMajority);
		results.put(ACCURACYOFMAJORCLASS, accMaj.getResult());

		CohenKappa<String> kappa = new CohenKappa<String>(evaluationString);
		results.put(COHENSKAPPA, kappa.getResult());

		LinearlyWeightedKappa<Double> lwKappa = new LinearlyWeightedKappa<Double>(evaluationDouble);
		results.put(LINEAR_KAPPA, lwKappa.getResult());

		QuadraticallyWeightedKappa<Double> qwKappa = new QuadraticallyWeightedKappa<Double>(evaluationDouble);
		results.put(QUADRATIC_WEIGHTED_KAPPA, qwKappa.getResult());

		Precision<String> prec = new Precision<String>(evaluationString);
		results.put(MACROPRECISION, prec.getMacroPrecision());
		results.put(MICROPRECISION, prec.getMicroPrecision());

		Recall<String> rec = new Recall<String>(evaluationString);
		results.put(MACRORECALL, rec.getMacroRecall());
		results.put(MICRORECALL, rec.getMicroRecall());

		Fscore<String> f = new Fscore<String>(evaluationString);
		results.put(MACROFMEASURE, f.getMacroFscore());
		results.put(MICROFMEASURE, f.getMicroFscore());
		results.put(WEIGHTEDFMEASURE, f.getWeightedFscore());

		PearsonCorrelation pearson = new PearsonCorrelation(evaluationDouble);
		results.put(PEARSON, pearson.getResult());

		SpearmanCorrelation spearman = new SpearmanCorrelation(evaluationDouble);
		results.put(SPEARMAN, spearman.getResult());

		for (String s : results.keySet()) {
			System.out.printf(s+": %.2f"+System.getProperty("line.separator"), results.get(s));
			//				System.out.println(s + ": " + results.get(s));
			props.setProperty(s, results.get(s).toString());
		}

		File itemsFile = new File(evaluationFile.getParentFile(), LABELED_ITEMS_FILENAME);
		ReportUtils.writeLabeledOutput(instanceId2TextMap, evaluationString, itemsFile, confScoreMap);

		// Write results
		File outfile = new File(evaluationFile.getParentFile(), RESULTS_FILENAME);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outfile);
			props.store(fos, "Results");
		}
		finally {
			IOUtils.closeQuietly(fos);
		}   

	}


}
