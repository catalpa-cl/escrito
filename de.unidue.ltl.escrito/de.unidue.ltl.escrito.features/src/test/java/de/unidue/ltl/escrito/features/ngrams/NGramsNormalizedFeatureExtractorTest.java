package de.unidue.ltl.escrito.features.ngrams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.junit.Before;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.io.TestReaderSingleLabelDocumentReader;

public class NGramsNormalizedFeatureExtractorTest
extends LuceneMetaCollectionBasedFeatureTestBase
{
   @Before
   public void setup()
   {
       super.setup();
       featureClass = NGramsNormalizedFeatureExtractor.class;
       metaCollectorClass = WordNGramMC.class;
   }

   @Override
   protected void evaluateMetaCollection(File luceneFolder) throws Exception
   {
       Set<String> entriesFromIndex = getEntriesFromIndex(luceneFolder);
       assertEquals(86, entriesFromIndex.size());
   }

   @Override
   protected void evaluateExtractedFeatures(File output) throws Exception
   {
       List<Instance> instances = readInstances(output);
       assertEquals(4, instances.size());
       assertEquals(1, getUniqueOutcomes(instances));

       Set<String> featureNames = new HashSet<String>();
       for (Instance i : instances) {
           for (Feature f : i.getFeatures()) {
               featureNames.add(f.getName());
           }
       }
       assertEquals(10, featureNames.size());
       assertTrue(featureNames.contains("ngram_4"));
       assertTrue(featureNames.contains("ngram_5"));
       assertTrue(featureNames.contains("ngram_5_5"));
       
       Iterator<Instance> instancesIter = instances.iterator();
		while (instancesIter.hasNext()){
			Instance inst = instancesIter.next();
			int id = inst.getJcasId();
			System.out.println("id: "+id);
			if (id == 3){
				Collection<Feature> features = inst.getFeatures();
				System.out.println(features.size());
				for (Feature feature: features){
					System.out.println(feature.toString());
					if (feature.getName().equals("ngram_trees")){
						System.out.println(feature.toString());
						// 3 out of 19
						assertEquals(0.1570, (double)feature.getValue(), 0.01);
					}
				}
			}
			if (id == 0){
				Collection<Feature> features = inst.getFeatures();
				System.out.println(features.size());
				for (Feature feature: features){
					System.out.println(feature.toString());
					if (feature.getName().equals("ngram_5_5")){
						System.out.println(feature.toString());
						// 4 out of 14
						assertEquals(0.2857, (double)feature.getValue(), 0.01);
					}
				}
			}
		}
   }

   @Override
   protected CollectionReaderDescription getMetaReader() throws Exception
   {
       return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
               TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/*.txt");
   }

   @Override
   protected CollectionReaderDescription getFeatureReader() throws Exception
   {
       return getMetaReader();
   }

   @Override
   protected Object[] getMetaCollectorParameters(File luceneFolder)
   {
       return new Object[] { NGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
    		   NGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "10", 
    		   NGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION,
               luceneFolder.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
               luceneFolder.toString() };
   }

   @Override
   protected Object[] getFeatureExtractorParameters(File luceneFolder)
   {
       return getMetaCollectorParameters(luceneFolder);
   }

   @Test
   public void luceneNGramFeatureExtractorNonDefaultFrequencyThresholdTest() throws Exception
   {

       File luceneFolder = folder.newFolder();
       File outputPath = folder.newFolder();

       Object[] parameters = new Object[] { 
    		   NGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "10",
    		   NGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123", 
    		   NGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION, luceneFolder.toString(), 
    		   NGramsNormalizedFeatureExtractor.PARAM_NGRAM_FREQ_THRESHOLD, "0.1f",
               WordNGramMC.PARAM_TARGET_LOCATION, luceneFolder.toString() };

       List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

       CollectionReaderDescription reader = getMetaReader();

       AnalysisEngineDescription segmenter = AnalysisEngineFactory
               .createEngineDescription(BreakIteratorSegmenter.class);

       AnalysisEngineDescription metaCollector = AnalysisEngineFactory
               .createEngineDescription(WordNGramMC.class, parameterList.toArray());

       ExternalResourceDescription featureExtractor = ExternalResourceFactory
               .createExternalResourceDescription(NGramsNormalizedFeatureExtractor.class, parameters);
       List<ExternalResourceDescription> fes = new ArrayList<>();
       fes.add(featureExtractor);

       AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
               outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
               Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, false, false, false, false,
               Collections.emptyList(), fes, new String[] {});

       // run meta collector
       SimplePipeline.runPipeline(reader, segmenter, metaCollector);

       // run FE(s)
       SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);

       List<Instance> instances = readInstances(outputPath);

       assertEquals(4, instances.size());
       assertEquals(1, getUniqueOutcomes(instances));
       for (Instance i : instances) {
           assertTrue(i.getFeatures().isEmpty());
       }
       
       
       
       
   }

   private int getUniqueOutcomes(List<Instance> instances)
   {
       Set<String> outcomes = new HashSet<String>();
       instances.forEach(x -> outcomes.addAll(x.getOutcomes()));
       return outcomes.size();
   }

}





//
//
//{
//	@Rule
//	public TemporaryFolder folder = new TemporaryFolder();
//	
//	@Before
//	public void setupLogging()
//	{
//		System.setProperty("org.apache.uima.logger.class",
//				"org.apache.uima.util.impl.Log4jLogger_impl");
//	}
//
//	@Test
//	public void luceneNGramFeatureExtractorTest()
//			throws Exception
//	{
//
//		File luceneFolder = folder.newFolder();
//		File outputPath = folder.newFolder();
//
//		Object[] parameters = new Object[] {
//				NGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
//				NGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "10",
//				NGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
//				WordNGramMC.PARAM_TARGET_LOCATION, luceneFolder.toString()};
//
//		ExternalResourceDescription featureExtractor = ExternalResourceFactory.createExternalResourceDescription(NGramsNormalizedFeatureExtractor.class, parameters);
//		List<ExternalResourceDescription> fes = new ArrayList<>();
//		fes.add(featureExtractor);
//
//		List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
//
//		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
//				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
//				"src/test/resources/ngrams/*.txt");
//
//		AnalysisEngineDescription segmenter = AnalysisEngineFactory
//				.createEngineDescription(BreakIteratorSegmenter.class);
//
//
//		AnalysisEngineDescription metaCollector = AnalysisEngineFactory
//				.createEngineDescription(WordNGramMC.class, parameterList.toArray());
//
//		AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
//				outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
//				Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, DenseFeatureStore.class.getName(), false,
//				false, false, new ArrayList<>(), false, fes);
//
//		// run meta collector
//		SimplePipeline.runPipeline(reader, segmenter, metaCollector);
//
//		// run FE(s)
//		SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);
//
//		Gson gson = new Gson();
//		FeatureStore fs = gson.fromJson(
//				FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
//				DenseFeatureStore.class);
//		assertEquals(4, fs.getNumberOfInstances());
//		assertEquals(1, fs.getUniqueOutcomes().size());
//		
//		
//		Iterator<Instance> instances = fs.getInstances().iterator();
//		while (instances.hasNext()){
//			Instance inst = instances.next();
//			int id = inst.getJcasId();
//			System.out.println(id);
//			if (id == 3){
//				Collection<Feature> features = inst.getFeatures();
//				for (Feature feature: features){
//					if (feature.getName().equals("ngram_trees")){
//						System.out.println(feature.toString());
//						// 3 out of 19
//						assertEquals(0.1570, (double)feature.getValue(), 0.01);
//					}
//				}
//			}
//			if (id == 0){
//				Collection<Feature> features = inst.getFeatures();
//				for (Feature feature: features){
//					if (feature.getName().equals("ngram_5_5")){
//						System.out.println(feature.toString());
//						// 4 out of 14
//						assertEquals(0.2857, (double)feature.getValue(), 0.01);
//					}
//				}
//			}
//		}
//		
//		Set<String> featureNames = new HashSet<String>(fs.getFeatureNames());
//		System.out.println(featureNames);
//		assertEquals(10, featureNames.size());
//		assertTrue(featureNames.contains("ngram_4"));
//		assertTrue(featureNames.contains("ngram_5"));
//		assertTrue(featureNames.contains("ngram_5_5"));
//
//	}
//
//
//}
//
