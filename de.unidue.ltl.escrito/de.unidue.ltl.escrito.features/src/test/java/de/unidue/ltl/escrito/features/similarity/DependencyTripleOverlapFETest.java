package de.unidue.ltl.escrito.features.similarity;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase;
import de.unidue.ltl.escrito.features.core.EssayGradingTestBase.ParserType;
import de.unidue.ltl.escrito.features.core.io.TestReaderSingleLabel;
import de.unidue.ltl.escrito.features.ngrams.DependencyMetaCollector;

public class DependencyTripleOverlapFETest extends EssayGradingTestBase{
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	@Before
	public void setupLogging(){
		System.setProperty("org.apache.uima.logger.class",
				"org.apache.uima.util.impl.Log4jLogger_impl");
	}
	@Test
	public void dependencyTripleOverlapExtractorTest() throws Exception {
		File dependencyFolder = folder.newFolder();
		File outputPath = folder.newFolder();
		
		Object[] parameters = new Object[] {
				DependencyTripleOverlapFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,"dependencyTripleOverlapFeatureExtractor",
				DependencyTripleOverlapFeatureExtractor.PARAM_SOURCE_LOCATION,dependencyFolder.toString(),
				DependencyMetaCollector.PARAM_TARGET_LOCATION,dependencyFolder.toString()
		};
		ExternalResourceDescription featureExtractor = ExternalResourceFactory.createExternalResourceDescription(DependencyTripleOverlapFeatureExtractor.class,parameters);
		List<ExternalResourceDescription> fes = new ArrayList<>();
		fes.add(featureExtractor);
		List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
				"src/test/resources/dependencies/*.txt");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription posTagger = createEngineDescription(OpenNlpPosTagger.class,OpenNlpPosTagger.PARAM_LANGUAGE,"de");
		AnalysisEngineDescription mateParserDE = createEngineDescription(MateParser.class,MateParser.PARAM_LANGUAGE,"de", MateParser.PARAM_PRINT_TAGSET,true);
		AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(DependencyMetaCollector.class,parameterList.toArray());
		String[] outcomes = {"DependencyFullMatch1","DependencyMatchWithoutRelation1","DependencyFullMatch2","DependencyMatchWithoutRelation2"};
		AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
				outputPath.getAbsolutePath(),
				JsonDataWriter.class.getName(),
				Constants.LM_SINGLE_LABEL, 
				Constants.FM_DOCUMENT, 
				false,false,false,false,new ArrayList<>(),fes,outcomes);
		// run meta collector
		SimplePipeline.runPipeline(reader, segmenter, posTagger, mateParserDE,metaCollector);
		// run FE(s)
		SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);	
	}
}
