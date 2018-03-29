package de.unidue.ltl.escrito.core.learningcurve;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.tc.core.Constants;

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
