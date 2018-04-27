package de.unidue.ltl.escrito.features.ngrams;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.meta.CharacterNGramMC;
import org.junit.Before;
import de.unidue.ltl.escrito.features.core.io.TestReaderSingleLabelDocumentReader;

public class CharNGramsNormalizedFeatureExtractorTest extends LuceneMetaCollectionBasedFeatureTestBase
{
	
	
	  static String FEATURE_NAME = "23423432434";

	    @Before
	    public void setup()
	    {
	        super.setup();
	        featureClass = CharNGramsNormalizedFeatureExtractor.class;
	        metaCollectorClass = CharacterNGramMC.class;
	    }

	    @Override
	    protected void evaluateMetaCollection(File luceneFolder) throws Exception
	    {
	        Set<String> entriesFromIndex = getEntriesFromIndex(luceneFolder);
	        System.out.println(entriesFromIndex);
	        assertEquals(41, entriesFromIndex.size());
	    }

	    @Override
	    protected void evaluateExtractedFeatures(File output) throws Exception
	    {
	        List<Instance> instances = readInstances(output);
	        assertEquals(1, instances.size());


	        List<Feature> features = new ArrayList<Feature>(instances.get(0).getFeatures());
	        System.out.println(features);
	        assertEquals(10, features.size());
	        assertEquals(features.get(0).getName(), ("CharNGramsNormalizedFeatureExtractor_a"));
	        assertEquals(features.get(1).getName(), ("CharNGramsNormalizedFeatureExtractor_at"));
	        assertEquals((double)features.get(0).getValue(), 0.12121212121212122, 0.0001);
	        assertEquals((double)features.get(1).getValue(), 0.09375, 0.0001);
		 }

	    @Override
	    protected CollectionReaderDescription getMetaReader() throws Exception
	    {
	        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
	                TestReaderSingleLabelDocumentReader.PARAM_LANGUAGE, "en",
	                TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION,
	                "src/test/resources/ngrams/text3.txt");
	    }

	    @Override
	    protected Object[] getMetaCollectorParameters(File luceneFolder)
	    {
	        return new Object[] { CharacterNGramMC.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
	                CharNGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
	                CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, 10, 
	                CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
	                CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_MAX_N, 2, CharacterNGramMC.PARAM_TARGET_LOCATION,
	                luceneFolder.toString() };
	    }

	    @Override
	    protected Object[] getFeatureExtractorParameters(File luceneFolder)
	    {
	        return new Object[] { CharNGramsNormalizedFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
	        		CharNGramsNormalizedFeatureExtractor.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
	        		CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_USE_TOP_K, "10", 
	        		CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_MIN_N, "1",
	        		CharNGramsNormalizedFeatureExtractor.PARAM_NGRAM_MAX_N, "2", CharacterNGramMC.PARAM_TARGET_LOCATION,
	                luceneFolder.toString() };
	    }

	    @Override
	    protected CollectionReaderDescription getFeatureReader() throws Exception
	    {
	        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
	                TestReaderSingleLabelDocumentReader.PARAM_LANGUAGE, "en",
	                TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION,
	                "src/test/resources/ngrams/text3.txt");
	    }



}

