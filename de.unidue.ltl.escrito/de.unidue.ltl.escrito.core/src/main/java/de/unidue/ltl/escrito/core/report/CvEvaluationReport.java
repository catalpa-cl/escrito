package de.unidue.ltl.escrito.core.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;

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

public class CvEvaluationReport extends BatchReportBase
implements Constants{

	public static final String RESULTS_FILENAME = "combined_classification_results.txt";
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

	public void execute() throws Exception {
		StorageService store = getContext().getStorageService();


		for (TaskContextMetadata subcontext : getSubtasks()) {
			System.out.println(subcontext.toString());

			Map<String, String> instanceId2TextMap = new HashMap<String, String>();

			if (!TcTaskTypeUtil.isCrossValidationTask(store, subcontext.getId())) {
				continue;
			}
			
			if (TcTaskTypeUtil.isFeatureExtractionTestTask(store, subcontext.getId())){
				// TODO: Das geht nicht, da komme ich nicht in den richtigen Task rein!
				System.out.println("Found FETestTak");
				Utils.extendInstanceId2TextMapCV(instanceId2TextMap, store, subcontext.getId());
			}
			
			
			
			Properties props = new Properties();

			File id2oFile = store.locateKey(subcontext.getId(),
					Constants.FILE_COMBINED_ID_OUTCOME_KEY);
			File id2oFileMaj = store.locateKey(subcontext.getId(),
					Constants.FILE_COMBINED_BASELINE_MAJORITY_OUTCOME_KEY);

			System.out.println(id2oFile);

			EvaluationData<Double> evaluationDouble = ReportUtils.readId2OutcomeAsDouble(id2oFile);
			EvaluationData<String> evaluationString = ReportUtils.readId2OutcomeAsString(id2oFile);
			EvaluationData<String> evaluationStringMajority = ReportUtils.readId2OutcomeAsString(id2oFileMaj);

			System.out.println("Read map with "+instanceId2TextMap.size()+" entries");


			Map<String, Double> results = new HashMap<String, Double>();

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
				props.setProperty(s, results.get(s).toString());
			}

			File itemsFile = new File(id2oFile.getParentFile(), LABELED_ITEMS_FILENAME);
			ReportUtils.writeLabeledOutput(instanceId2TextMap, evaluationString, itemsFile);


			// Write out properties
			//getContext().storeBinary(RESULTS_FILENAME, new PropertiesAdapter(props));
			// Write results
			File outfile = new File(id2oFile.getParentFile(), RESULTS_FILENAME);
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










}
