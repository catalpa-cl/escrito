package de.unidue.ltl.escrito.examples.normalization;



import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.core.normalization.DocumentTextWriter;

public class ReadFromBinCasEample {

	
	public static void main(String[] args) throws UIMAException, IOException{
		String pathToBincases = "src/main/resources/spellingCorrection";
		readFromBinCas(pathToBincases);
	}

	private static void readFromBinCas(String pathToBincases) throws UIMAException, IOException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, pathToBincases,
				BinaryCasReader.PARAM_LANGUAGE, "en",
				BinaryCasReader.PARAM_PATTERNS, "*.bin");
		AnalysisEngineDescription seg = createEngineDescription(OpenNlpSegmenter.class,
				OpenNlpSegmenter.PARAM_LANGUAGE, "en");
		AnalysisEngineDescription textWriter = createEngineDescription(DocumentTextWriter.class);
		SimplePipeline.runPipeline(reader, seg, textWriter);
	}
	
	
}
