package de.unidue.ltl.edu.scoring.features.general;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class IoUtils {

	public static JCas loadJCasFromFile(String fileId, String folder, String prefix) {
		CollectionReader reader;
		JCas comparisonView = null;

		try {

			System.out.println("Reading text with id "+fileId+".bin and prefix "+prefix+" from "+folder);
			reader = CollectionReaderFactory.createReader(
					BinaryCasReader.class,
					BinaryCasReader.PARAM_SOURCE_LOCATION, folder,
					BinaryCasReader.PARAM_PATTERNS, prefix+"_"+fileId+".bin",
					BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, "typesystem.bin");
			comparisonView = JCasFactory.createJCas();
			reader.getNext(comparisonView.getCas());
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CollectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comparisonView;
	}
	
	
	
	
}
