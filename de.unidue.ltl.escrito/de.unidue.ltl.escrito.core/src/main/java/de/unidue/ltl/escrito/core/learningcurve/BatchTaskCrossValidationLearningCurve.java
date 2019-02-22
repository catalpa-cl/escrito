package de.unidue.ltl.escrito.core.learningcurve;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation;

public class BatchTaskCrossValidationLearningCurve extends ExperimentCrossValidation  implements Constants{

	
	  public BatchTaskCrossValidationLearningCurve(String aExperimentName, Class<? extends TcShallowLearningAdapter> mlAdapter, int numFolds)
	            throws TextClassificationException
	    {
		  super(aExperimentName, numFolds);
	    }
	

	@Override
  protected void init()
	{
		if (experimentName == null) {
            throw new IllegalStateException("You must set an experiment name");
        }

        if (aNumFolds < 2) {
            throw new IllegalStateException(
                    "Number of folds is not configured correctly. Number of folds needs to be at "
                            + "least 2 (but was " + aNumFolds + ")");
        }

        // initialize the setup
        initTask = new InitTask();
    //    initTask.setMlAdapter(mlAdapter);
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
                if (aNumFolds == LEAVE_ONE_OUT) {
                    aNumFolds = fileNames.length;
                }

                //is executed if we have less CAS than requested folds and manual mode is turned off
                if (!useCrossValidationManualFolds && fileNames.length < aNumFolds) {
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
                DimensionBundle<Collection<String>> foldDim = getFoldDim(fileNames);
                Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);
            }

            private File createRequestedNumberOfCas(File xmiPathRoot, int numAvailableJCas, String featureMode)
            {

                try {
                    File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(),
                            aNumFolds, numAvailableJCas, FM_SEQUENCE.equals(featureMode));

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

                if (numCas < aNumFolds) {
                    throw new IllegalStateException(
                            "Not enough TextClassificationUnits found to create at least ["
                                    + aNumFolds + "] folds");
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
   //     extractFeaturesTrainTask.setMlAdapter(mlAdapter);
        extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        extractFeaturesTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
   //     extractFeaturesTestTask.setMlAdapter(mlAdapter);
        extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        extractFeaturesTestTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        extractFeaturesTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

        // classification (numFolds times)
     // TODO kann das wirklich weg?
    //    testTask = mlAdapter.getTestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        testTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());

        if (innerReports != null) {
            for (Report report : innerReports) {
                testTask.addReport(report);
            }
        }

        // always add OutcomeIdReport
        // TODO kann das wirklich weg?
     //   testTask.addReport(mlAdapter.getOutcomeIdReportClass());
     //   testTask.addReport(BatchBasicResultReport.class);

        testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                TEST_TASK_INPUT_KEY_TEST_DATA);
        
        // this is the only line that changed TODO fix problematic import
        testTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);

        // ================== CONFIG OF THE INNER BATCH TASK =======================

        crossValidationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
        crossValidationTask.setType(crossValidationTask.getType().replaceAll("\\$[0-9]+", "-") + experimentName);
        crossValidationTask.addTask(metaTask);
        crossValidationTask.addTask(extractFeaturesTrainTask);
        crossValidationTask.addTask(extractFeaturesTestTask);
        crossValidationTask.addTask(testTask);
        crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
        // report of the inner batch task (sums up results for the folds)
        // we want to re-use the old CV report, we need to collect the evaluation.bin files from
        // the test task here (with another report)
        // TODO kann das wirklich weg?
        //   testTask.addReport(mlAdapter.getOutcomeIdReportClass());
        //crossValidationTask.addReport(mlAdapter.getBatchTrainTestReportClass());
        crossValidationTask.setAttribute(TC_TASK_TYPE, TcTaskType.CROSS_VALIDATION.toString());

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTask);
        addTask(crossValidationTask);
	}
	
	
	
}
