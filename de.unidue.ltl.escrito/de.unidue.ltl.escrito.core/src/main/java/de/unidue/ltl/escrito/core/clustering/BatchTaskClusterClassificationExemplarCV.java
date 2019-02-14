/**
 * Copyright 2014
 * Language Technology Lab
 * University of Duisburg-Essen
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
package de.unidue.ltl.escrito.core.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.weka.task.WekaTestTask;

import de.unidue.ltl.escrito.core.learningcurve.TrainingDataSelectionTestTask;

/**
 * Clustering setup
 * 
 */
public class BatchTaskClusterClassificationExemplarCV
extends Experiment_ImplBase implements Constants
{
	protected Comparator<String> comparator;
	protected int numFolds = 10;

	protected InitTask initTask;
	protected MetaInfoTask metaTask;
	protected ExtractFeaturesTask extractFeaturesTrainTask;
	protected ExtractFeaturesTask extractFeaturesTestTask;
	protected TaskBase testTask;

	private String experimentName;
	private AnalysisEngineDescription preprocessingPipeline;
	private List<String> operativeViews;
	private List<Class<? extends Report>> innerReports;
	protected ClusterExemplarTask clusteringTask;


	public BatchTaskClusterClassificationExemplarCV()
	{/* needed for Groovy */
	}

	/*
	 * Preconfigured train-test setup.
	 * 
	 * @param aExperimentName
	 *            name of the experiment
	 * @param class1 
	 * @param preprocessingPipeline
	 *            preprocessing analysis engine aggregate
	 */
	public BatchTaskClusterClassificationExemplarCV(String aExperimentName,
		//	Class<? extends TcShallowLearningAdapter> mlAdapter, 
			AnalysisEngineDescription preprocessingPipeline)
	{
		setExperimentName(aExperimentName);
		setPreprocessingPipeline(preprocessingPipeline);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
	//	setMachineLearningAdapter(mlAdapter);
	}

	/*
	 * Initializes the experiment. This is called automatically before execution. It's not done
	 * directly in the constructor, because we want to be able to use setters instead of the
	 * three-argument constructor.
	 * 
	 * @throws IllegalStateException
	 *             if not all necessary arguments have been set.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Override
	protected void init()
			throws IllegalStateException
	{

		if (experimentName == null) {
			throw new IllegalStateException("You must set an experiment name");
		}

		if (numFolds < 2) {
			throw new IllegalStateException(
					"Number of folds is not configured correctly. Number of folds needs to be at "
							+ "least 2 (but was " + numFolds + ")");
		}

		// initialize the setup
		initTask = new InitTask();
	//	initTask.setMlAdapter(mlAdapter);
		initTask.setPreprocessing(getPreprocessing());
		initTask.setOperativeViews(operativeViews);
		initTask.setType(initTask.getType() + "-" + experimentName);
		initTask.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

		

		// inner batch task (carried out numFolds times)
		DefaultBatchTask crossValidationTask = new DefaultBatchTask()
		{
			@Discriminator(name=DIM_FEATURE_MODE)
			private String featureMode;

			@Discriminator(name=DIM_CROSS_VALIDATION_MANUAL_FOLDS)
			private boolean useCrossValidationManualFolds;

			@Override
			public void initialize(TaskContext aContext)
			{
				super.initialize(aContext);

				File xmiPathRoot = aContext.getFolder(InitTask.OUTPUT_KEY_TRAIN,
						AccessMode.READONLY);
				Collection<File> files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" },
						true);
				String[] fileNames = new String[files.size()];
				int i = 0;
				for (File f : files) {
					// adding file paths, not names
					fileNames[i] = f.getAbsolutePath();
					i++;
				}
				Arrays.sort(fileNames);
				if (numFolds == LEAVE_ONE_OUT) {
					numFolds = fileNames.length;
				}

				//is executed if we have less CAS than requested folds and manual mode is turned off
				if (!useCrossValidationManualFolds && fileNames.length < numFolds) {
					xmiPathRoot = createRequestedNumberOfCas(xmiPathRoot, fileNames.length, featureMode);
					files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" }, true);
					fileNames = new String[files.size()];
					i = 0;
					for (File f : files) {
						// adding file paths, not names
						fileNames[i] = f.getAbsolutePath();
						i++;
					}
				}
				// don't change any names!!
				FoldDimensionBundle<String> foldDim = getFoldDim(fileNames);
				Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

				ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
				setParameterSpace(pSpace);
			}

			private File createRequestedNumberOfCas(File xmiPathRoot, int numAvailableJCas, String featureMode)
			{

				try {
					File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(),
							numFolds, numAvailableJCas, FM_SEQUENCE.equals(featureMode));

					verfiyThatNeededNumberOfCasWasCreated(outputFolder);

					return outputFolder;
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

			private void verfiyThatNeededNumberOfCasWasCreated(File outputFolder)
			{
				int numCas = 0;
				for (File f : outputFolder.listFiles()) {
					if (f.getName().contains(".bin")) {
						numCas++;
					}
				}

				if (numCas < numFolds) {
					throw new IllegalStateException(
							"Not enough TextClassificationUnits found to create at least ["
									+ numFolds + "] folds");
				}
			}
		};

		// ================== SUBTASKS OF THE INNER BATCH TASK =======================

		// collecting meta features only on the training data (numFolds times)
		metaTask = new MetaInfoTask();
		metaTask.setOperativeViews(operativeViews);
		metaTask.setType(metaTask.getType() + "-" + experimentName);
		metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

		// extracting features from training data (numFolds times)
		extractFeaturesTrainTask = new ExtractFeaturesTask();
		extractFeaturesTrainTask.setTesting(false);
		extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
				+ experimentName);
	//	extractFeaturesTrainTask.setMlAdapter(mlAdapter);
		extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
		extractFeaturesTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

		// extracting features from test data (numFolds times)
		extractFeaturesTestTask = new ExtractFeaturesTask();
		extractFeaturesTestTask.setTesting(true);
		extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
				+ experimentName);
	//	extractFeaturesTestTask.setMlAdapter(mlAdapter);
		extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
		extractFeaturesTestTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
		extractFeaturesTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

		// test task operating on the models of the feature extraction trSydneyain and test tasks
		clusteringTask = new ClusterExemplarTask();
		clusteringTask.setType(clusteringTask.getType() + "-" + experimentName);
	//	clusteringTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
		clusteringTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
				WekaTestTask.TEST_TASK_INPUT_KEY_TRAINING_DATA);


		// classification (numFolds times)
		// test task operating on the models of the feature extraction train and test tasks
		testTask = new TrainingDataSelectionTestTask();
		testTask.setType(testTask.getType() + "-" + experimentName);

		if (innerReports != null) {
			for (Class<? extends Report> report : innerReports) {
				testTask.addReport(report);
				System.out.println("XXX: "+report.getName());
			}
		} 

		// always add OutcomeIdReport
		//testTask.addReport(mlAdapter.getOutcomeIdReportClass());
		//testTask.addReport(BatchBasicResultReport.class);

		testTask.addImport(clusteringTask, ClusterTrainTask.ADAPTED_TRAINING_DATA,
				WekaTestTask.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
				WekaTestTask.TEST_TASK_INPUT_KEY_TEST_DATA);


		// ================== CONFIG OF THE INNER BATCH TASK =======================

		crossValidationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
		crossValidationTask.setType(crossValidationTask.getType().replaceAll("\\$[0-9]+", "-") + experimentName);
		crossValidationTask.addTask(metaTask);
		crossValidationTask.addTask(extractFeaturesTrainTask);
		crossValidationTask.addTask(extractFeaturesTestTask);
		crossValidationTask.addTask(clusteringTask);
		crossValidationTask.addTask(testTask);
		crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
		// report of the inner batch task (sums up results for the folds)
		// we want to re-use the old CV report, we need to collect the evaluation.bin files from
		// the test task here (with another report)
		//crossValidationTask.addReport(mlAdapter.getBatchTrainTestReportClass());
		crossValidationTask.setAttribute(TC_TASK_TYPE, TcTaskType.CROSS_VALIDATION.toString());

		// DKPro Lab issue 38: must be added as *first* task
		addTask(initTask);
		addTask(crossValidationTask);
	}




	public void setExperimentName(String experimentName)
	{
		this.experimentName = experimentName;
	}

	public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline)
	{
		this.preprocessingPipeline = preprocessingPipeline;
	}

	public void setOperativeViews(List<String> operativeViews)
	{
		this.operativeViews = operativeViews;
	}

	/**
	 * Sets the report for the test task
	 * 
	 * @param innerReport
	 *            classification report or regression report
	 */
	public void addInnerReport(Class<? extends Report> innerReport)
	{
		if (innerReports == null) {
			innerReports = new ArrayList<Class<? extends Report>>();
		}
		this.innerReports.add(innerReport);
	}
	
	 protected FoldDimensionBundle<String> getFoldDim(String[] fileNames)
	    {
	        if (comparator != null) {
	            return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames),
	                    numFolds, comparator);
	        }
	        return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), numFolds);
	    }

	    public void setNumFolds(int numFolds)
	    {
	        this.numFolds = numFolds;
	    }

	    public void setComparator(Comparator<String> aComparator)
	    {
	        comparator = aComparator;
	    }

	
}
