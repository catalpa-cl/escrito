package de.unidue.ltl.escrito.core.learningcurve;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.ml.ExperimentTrainTest;

public class BatchTaskLearningCurve
extends ExperimentTrainTest {

	
	  public BatchTaskLearningCurve(String aExperimentName, Class<? extends TCMachineLearningAdapter> mlAdapter)
	            throws TextClassificationException
	    {
		  super(aExperimentName, mlAdapter);
	    }
	

	@Override
    protected void init()
	{
		super.init();
		super.testTask.addImport(super.initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
	}

}
