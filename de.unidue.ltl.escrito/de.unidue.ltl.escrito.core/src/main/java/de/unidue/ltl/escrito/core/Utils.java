package de.unidue.ltl.escrito.core;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.task.InitTask;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class Utils {

	public static Map<String, String> getInstanceId2TextMap(TaskContext aContext)
			throws ResourceInitializationException
	{	
		Map<String, String> instanceId2TextMap = new HashMap<String,String>();

		// TrainTest setup: input files are set as imports
		File root = aContext.getStorageLocation(InitTask.OUTPUT_KEY_TRAIN, AccessMode.READONLY);
		Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
		CollectionReaderDescription reader = createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
				files);
		for (JCas jcas : new JCasIterable(reader)) {
			DocumentMetaData dmd = DocumentMetaData.get(jcas);
			instanceId2TextMap.put(dmd.getDocumentId(), jcas.getDocumentText());
	//		System.out.println(dmd.getDocumentId()+"\t"+jcas.getDocumentText());
		}
		//	System.out.println("Map with "+instanceId2TextMap.size()+" entries");
		return instanceId2TextMap;
	}
	
	
}
