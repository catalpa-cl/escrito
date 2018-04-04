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

public class GradingEvaluationReportClusteringCurveCV extends ReportBase {

	public static final String RESULTS_FILENAME = "classification_results.txt";



	@Override
	public void execute() throws Exception {

		// TODO: Das ist unpraktisch, wir würden diesen Report auch gerne fp
		for (int numberOfClusters : ClusterExemplarTask.NUMBER_OF_TRAINING_INSTANCES ){
			System.out.println(numberOfClusters);
			
	}
	}

}
