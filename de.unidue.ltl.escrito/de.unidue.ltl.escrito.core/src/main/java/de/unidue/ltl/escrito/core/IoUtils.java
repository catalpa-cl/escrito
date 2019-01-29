package de.unidue.ltl.escrito.core;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class IoUtils {

	public static JCas loadJCasFromFile(String fileId, String folder, String prefix) 
			throws UIMAException, IOException {

		// TODO use logging
		System.out.println("Reading text with id "+fileId+".bin and prefix "+prefix+" from "+folder);
		CollectionReader reader = CollectionReaderFactory.createReader(
				BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, folder,
				BinaryCasReader.PARAM_PATTERNS, prefix+"_"+fileId+".bin",
				BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, "typesystem.bin");
		JCas comparisonView = JCasFactory.createJCas();
		
		reader.getNext(comparisonView.getCas());
		return comparisonView;
	}
	
	
	
	
}
