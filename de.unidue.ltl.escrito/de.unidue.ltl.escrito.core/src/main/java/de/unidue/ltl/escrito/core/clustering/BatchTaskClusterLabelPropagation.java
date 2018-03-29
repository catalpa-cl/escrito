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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.core.Constants;

import de.unidue.ltl.escrito.core.report.GradingEvaluationReport;

/**
 * Clustering setup
 * 
 */
public class BatchTaskClusterLabelPropagation
extends ExperimentTrainTest implements Constants
{

    private String experimentName;
    private AnalysisEngineDescription preprocessingPipeline;
    private List<String> operativeViews;
    private List<Class<? extends Report>> innerReports;
    private ClusterLabelPropagationTask clusteringTask;


    public BatchTaskClusterLabelPropagation()
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
    public BatchTaskClusterLabelPropagation(String aExperimentName,
            //Class<? extends TCMachineLearningAdapter> mlAdapter, 
    		AnalysisEngineDescription preprocessingPipeline)
    {
        setExperimentName(aExperimentName);
        setPreprocessingPipeline(preprocessingPipeline);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
     //   setMachineLearningAdapter(mlAdapter);
    }



    /*
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected void init()
    {
        if (experimentName == null || preprocessingPipeline == null)

        {
            throw new IllegalStateException(
                    "You must set Experiment Name, DataWriter and Aggregate.");
        }

        // init the train part of the experiment
        initTaskTrain = new InitTask();
   //     initTaskTrain.setMlAdapter(mlAdapter);
        initTaskTrain.setPreprocessing(getPreprocessing());
        initTaskTrain.setOperativeViews(operativeViews);
        initTaskTrain.setTesting(false);
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
        initTaskTrain.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

        // init the test part of the experiment
        initTaskTest = new InitTask();
        initTaskTest.setTesting(true);
     //   initTaskTest.setMlAdapter(mlAdapter);
        initTaskTest.setPreprocessing(getPreprocessing());
        initTaskTest.setOperativeViews(operativeViews);
        initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);
        initTaskTest.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TEST.toString());
       
        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);
        metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
   //     featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
   //     featuresTestTask.setMlAdapter(mlAdapter);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTestTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        featuresTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

        // test task operating on the models of the feature extraction train and test tasks
        clusteringTask = new ClusterLabelPropagationTask();
        clusteringTask.setType(clusteringTask.getType() + "-" + experimentName);
        clusteringTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
        clusteringTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
        		WekaTestTask.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        
        addTask(initTaskTrain);
        addTask(initTaskTest);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(featuresTestTask);
        addTask(clusteringTask);
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
}
