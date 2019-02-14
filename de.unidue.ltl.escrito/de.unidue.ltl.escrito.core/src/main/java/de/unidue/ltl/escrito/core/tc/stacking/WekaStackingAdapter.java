package de.unidue.ltl.escrito.core.tc.stacking;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;

public class WekaStackingAdapter extends WekaAdapter{
	
	
	@Override
	public ExecutableTaskBase getTestTask() {
		System.out.println("Create stacking task.");
		return new WekaStackingTask();
		}
		
	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return WekaStackingOutcomeIDReport.class;
	}

}
