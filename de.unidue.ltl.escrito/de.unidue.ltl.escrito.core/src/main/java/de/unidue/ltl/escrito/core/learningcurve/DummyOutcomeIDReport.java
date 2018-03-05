package de.unidue.ltl.escrito.core.learningcurve;

import java.io.File;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.Properties;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.util.MultilabelResult;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.core.Instances;

public class DummyOutcomeIDReport extends ReportBase{

	@Override
	public void execute() throws Exception {

		InputStream is = new StringBufferInputStream("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD\n"
				+"#labels 0=dummy1 1=dummy2\n"
				+"0_0_dummy=0;1;-1\n"
				+"1_0_dummy=1;1;-1\n");
		getContext().storeBinary(Constants.ID_OUTCOME_KEY, is);

	}

}
