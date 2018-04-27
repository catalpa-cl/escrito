package de.unidue.ltl.escrito.features.ngrams;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.meta.PosNGramMC;
import org.junit.Before;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.features.core.io.TestReaderSingleLabelDocumentReader;

public class PosNGramsNormalizedFeatureExtractorTest
extends LuceneMetaCollectionBasedFeatureTestBase
{

   @Before
   public void setupLogging()
   {
       super.setup();
       featureClass = PosNGramsNormalizedFeatureExtractor.class;
       metaCollectorClass = PosNGramMC.class;
   }

   private Collection<? extends String> getUniqueFeatureNames(List<Instance> instances)
   {
       Set<String> s = new HashSet<>();

       for (Instance i : instances) {
           for (Feature f : i.getFeatures()) {
               s.add(f.getName());
           }
       }

       return s;
   }

   private int getUniqueOutcomes(List<Instance> instances)
   {
       Set<String> outcomes = new HashSet<String>();
       instances.forEach(x -> outcomes.addAll(x.getOutcomes()));
       return outcomes.size();
   }

   @Override
   protected void evaluateMetaCollection(File luceneFolder) throws Exception
   {
       Set<String> entries = getEntriesFromIndex(luceneFolder);
       assertEquals(40, entries.size());
   }

   @Override
   protected void evaluateExtractedFeatures(File output) throws Exception
   {
       List<Instance> instances = readInstances(output);
       assertEquals(4, instances.size());
       assertEquals(1, getUniqueOutcomes(instances));

       Set<String> featureNames = new HashSet<String>(getUniqueFeatureNames(instances));
       assertEquals(5, featureNames.size());
       System.out.println(featureNames);
       assertTrue(featureNames.contains("PosNGramsNormalizedFeatureExtractor_CARD"));
       assertTrue(featureNames.contains("PosNGramsNormalizedFeatureExtractor_NN"));
       assertTrue(featureNames.contains("PosNGramsNormalizedFeatureExtractor_CARD_CARD"));
   }

   @Override
   protected void runMetaCollection(File luceneFolder, AnalysisEngineDescription metaCollector)
       throws Exception
   {

       CollectionReaderDescription reader = getMetaReader();

       AnalysisEngineDescription segmenter = AnalysisEngineFactory
               .createEngineDescription(BreakIteratorSegmenter.class);

       AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
               OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en");

       SimplePipeline.runPipeline(reader, segmenter, posTagger, metaCollector);
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
       return new Object[] { PosNGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
    		   PosNGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "5", PosNGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION,
               luceneFolder.toString(), PosNGramMC.PARAM_TARGET_LOCATION,
               luceneFolder.toString() };
   }

   @Override
   protected Object[] getFeatureExtractorParameters(File luceneFolder)
   {
       return getMetaCollectorParameters(luceneFolder);
   }

   protected void runFeatureExtractor(File luceneFolder,
           AnalysisEngineDescription featureExtractor)
       throws Exception
   {

       CollectionReaderDescription reader = getFeatureReader();

       AnalysisEngineDescription segmenter = AnalysisEngineFactory
               .createEngineDescription(BreakIteratorSegmenter.class);

       AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
               OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en");

       SimplePipeline.runPipeline(reader, segmenter, posTagger, featureExtractor);
   }
}




//
//
//extends EssayGradingTestBase
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
//	public void lucenePosNGramFeatureExtractorTest()
//			throws Exception
//	{
//
//		File luceneFolder = folder.newFolder();
//		File outputPath = folder.newFolder();
//
//		Object[] parameters = new Object[] {
//				PosNGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
//				PosNGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "10",
//				PosNGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
//				PosNGramMC.PARAM_TARGET_LOCATION, luceneFolder.toString()};
//
//		ExternalResourceDescription featureExtractor = ExternalResourceFactory.createExternalResourceDescription(PosNGramsNormalizedFeatureExtractor.class, parameters);
//		List<ExternalResourceDescription> fes = new ArrayList<>();
//		fes.add(featureExtractor);
//
//		List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
//
//		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
//				TestReaderSingleLabel.class, 
//				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/*.txt",
//				TestReaderSingleLabel.PARAM_LANGUAGE, "en");
//
//		AnalysisEngineDescription preprocessing = createEngineDescription(
//				createEngineDescription(BreakIteratorSegmenter.class),
//				createEngineDescription(OpenNlpPosTagger.class)
//				);
//
//		AnalysisEngineDescription metaCollector = AnalysisEngineFactory
//				.createEngineDescription(LucenePOSNGramMetaCollector.class, parameterList.toArray());
//
//		AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
//				outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
//				Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, DenseFeatureStore.class.getName(), false,
//				false, false, new ArrayList<>(), false, fes);
//
//		// run meta collector
//		SimplePipeline.runPipeline(reader, preprocessing, metaCollector);
//
//		// run FE(s)
//		SimplePipeline.runPipeline(reader, preprocessing, featExtractorConnector);
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
//			if (id == 3){
//				Collection<Feature> features = inst.getFeatures();
//				for (Feature feature: features){
//					if (feature.getName().equals("ngram_NN_PUNC")){
//						System.out.println(feature.toString());
//						// 4 out of 18
//						assertEquals(0.222, (double)feature.getValue(), 0.01);
//					}
//				}
//			}
//			if (id == 2){
//				Collection<Feature> features = inst.getFeatures();
//				for (Feature feature: features){
//					if (feature.getName().equals("posngram_V")){
//						System.out.println(feature.toString());
//						// 1 out of 8
//						assertEquals(0.25, (double)feature.getValue(), 0.01);
//					}
//				}
//			}
//		}
//
//		Set<String> featureNames = new HashSet<String>(fs.getFeatureNames());
//		System.out.println(featureNames);
//		assertEquals(10, featureNames.size());
//		assertTrue(featureNames.contains("posngram_CARD"));
//		assertTrue(featureNames.contains("posngram_CARD_CARD"));
//
//	}
//
//
//}
//
