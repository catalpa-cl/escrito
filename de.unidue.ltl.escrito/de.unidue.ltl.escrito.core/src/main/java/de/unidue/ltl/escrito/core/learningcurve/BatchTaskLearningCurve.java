package de.unidue.ltl.escrito.core.learningcurve;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.ml.ExperimentTrainTest;

public class BatchTaskLearningCurve
extends ExperimentTrainTest {

	
	  public BatchTaskLearningCurve(String aExperimentName, Class<? extends TcShallowLearningAdapter> mlAdapter)
	            throws TextClassificationException
	    {
		  super(aExperimentName);
	    }
	

	@Override
    protected void init()
	{
		super.init();
		super.testTask.addImport(super.initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
	}

}
