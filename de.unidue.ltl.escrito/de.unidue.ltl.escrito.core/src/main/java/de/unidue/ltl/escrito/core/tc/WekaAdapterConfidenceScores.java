package de.unidue.ltl.escrito.core.tc;

import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.ml.weka.WekaAdapter;

public class WekaAdapterConfidenceScores extends WekaAdapter {

	
	 @Override
	    public ExecutableTaskBase getTestTask()
	    {
	        return new WekaTestTaskConfidenceScores();
	    }
	
	
}
