package de.unidue.ltl.escrito.core.learningcurve;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.weka.report.WekaBaselineMajorityClassIdReport;
import org.dkpro.tc.ml.weka.report.WekaBaselineRandomIdReport;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.task.serialization.WekaLoadModelConnector;
import org.dkpro.tc.ml.weka.task.serialization.WekaSerliazeModelConnector;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

public class LearningCurveAdapter implements TcShallowLearningAdapter


{


	public static TcShallowLearningAdapter getInstance() {
		return new LearningCurveAdapter();
	}

	@Override
	public ExecutableTaskBase getTestTask()
	{
	return new LearningCurveTask();
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass()
	{
		return DummyOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass()
	{
		return DummyOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getRandomBaselineIdReportClass()
	{
		return DummyOutcomeIDReport.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds)
	{
		return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}

	@Override
	public Class<? extends DataWriter> getDataWriterClass()
	{
		return WekaDataWriter.class;
	}

	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
	{
		return WekaLoadModelConnector.class;
	}

	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask()
	{
		return WekaSerliazeModelConnector.class;
	}

	@Override
	public boolean useSparseFeatures()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}





//
//{
//
//	public static TcShallowLearningAdapter getInstance() {
//		return new LearningCurveAdapter();
//	}
//	
//	@Override
//	public ExecutableTaskBase getTestTask() {
//		return new LearningCurveTask();
//	}
//
//	@Override
//	public Class<? extends ReportBase> getOutcomeIdReportClass() {
//		return DummyOutcomeIDReport.class;
//	}
//
////	@Override
////	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
////		return CvLearningCurveReport.class;
////	}
//
//	@SuppressWarnings("unchecked")
//    @Override
//	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
//			String[] files, int folds) {
//		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
//	}
//
////	@Override
////	public String getFrameworkFilename(AdapterNameEntries name) {
////
////        switch (name) {
////            case featureVectorsFile:  return "training-data.arff.gz";
////            case predictionsFile      :  return "predictions.arff";
////            case featureSelectionFile :  return "attributeEvaluationResults.txt";
////        }
////        
////        return null;
////	}
//	
//	@Override
//	public Class<? extends DataWriter> getDataWriterClass() {
//		return WekaDataWriter.class;
//	}
//	
//	@Override
//	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
//		return WekaLoadModelConnector.class;
//	}
//
//	@Override
//	public Class<? extends ModelSerializationTask> getSaveModelTask() {
//		return WekaSerliazeModelConnector.class;
//	}
//	
////	@Override
////    public String getFeatureStore()
////    {
////        return DenseFeatureStore.class.getName();
////    }
//
//	@Override
//	public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass() {
//		return WekaBaselineMajorityClassIdReport.class;
//	}
//
//	@Override
//	public Class<? extends ReportBase> getRandomBaselineIdReportClass() {
//		return WekaBaselineRandomIdReport.class;
//	}
//
//	@Override
//	public boolean useSparseFeatures() {
//		return false;
//	}
//}

