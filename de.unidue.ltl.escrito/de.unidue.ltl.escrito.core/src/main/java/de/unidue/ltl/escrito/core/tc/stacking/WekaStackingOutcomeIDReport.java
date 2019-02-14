package de.unidue.ltl.escrito.core.tc.stacking;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.ml.weka.util.MultilabelResult;

import weka.core.Instances;

public class WekaStackingOutcomeIDReport extends WekaOutcomeIDReport{

	
	 private File mlResults;
	
	  @Override
	    public void execute()
	        throws Exception
	    {
	        File arff = WekaUtils.getFile(getContext(), "",
	                AdapterNameEntries.predictionsFile, AccessMode.READONLY);
	        mlResults = WekaUtils.getFile(getContext(), "",
	                WekaStackingTask.evaluationBin, AccessMode.READONLY);

	        boolean multiLabel = getDiscriminators()
	                .get(WekaStackingTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
	                .equals(Constants.LM_MULTI_LABEL);
	        boolean regression = getDiscriminators()
	                .get(WekaStackingTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
	                .equals(Constants.LM_REGRESSION);

	        Instances predictions = WekaUtils.getInstances(arff, multiLabel);

	        List<String> labels = getLabels(predictions, multiLabel, regression);
	        
	        Properties props;
	        
	        if(multiLabel){
	        	MultilabelResult r = WekaUtils.readMlResultFromFile(mlResults);
	        	props = generateMlProperties(predictions, labels, r);
	        }
	        else{
	        	props = generateSlProperties(predictions, regression, labels);
	        }
	        

	        
	        getContext().storeBinary(Constants.ID_OUTCOME_KEY,
	                new PropertiesAdapter(props, generateHeader(labels)));
	    }
	  
	
	  private List<String> getLabels(Instances predictions, boolean multiLabel, boolean regression)
	    {
	        if (regression) {
	            return Collections.emptyList();
	        }

	        return WekaUtils.getClassLabels(predictions, multiLabel);
	    }
	
}
