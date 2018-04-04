package de.unidue.ltl.escrito.core.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcBatchReportBase;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.agreement.CohenKappa;
import de.unidue.ltl.evaluation.measures.agreement.LinearlyWeightedKappa;
import de.unidue.ltl.evaluation.measures.agreement.QuadraticallyWeightedKappa;
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.categorial.Precision;
import de.unidue.ltl.evaluation.measures.categorial.Recall;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;
import de.unidue.ltl.evaluation.measures.Accuracy;

public class GradingEvaluationReport extends TcBatchReportBase {

	public static final String RESULTS_FILENAME = "classification_results.txt";

	public static final String STATISTICS_FILE_NAME = "statistics.txt";
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
		File evaluationFile = null;
		File evaluationFileMajority = null;
		StorageService storageService = getContext().getStorageService();
		Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());
		List<String> allIds = new ArrayList<String>();
		allIds.addAll(collectTasks(taskIds));
		for (String id : taskIds) {
			if (!TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, id)) {
				continue;
			}
			evaluationFile = storageService.locateKey(id, Constants.ID_OUTCOME_KEY);
			evaluationFileMajority = storageService.locateKey(id, Constants.BASELINE_MAJORITIY_ID_OUTCOME_KEY);
		}

		Properties props = new Properties();

		EvaluationData<Double> evaluationDouble = ReportUtils.readId2OutcomeAsDouble(evaluationFile);
		EvaluationData<String> evaluationString = ReportUtils.readId2OutcomeAsString(evaluationFile);
		EvaluationData<String> evaluationStringMajority = ReportUtils.readId2OutcomeAsString(evaluationFileMajority);

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
