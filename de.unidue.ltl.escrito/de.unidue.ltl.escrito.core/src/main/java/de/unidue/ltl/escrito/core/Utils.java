package de.unidue.ltl.escrito.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;

public class Utils {

	public static Map<String, String> getInstanceId2TextMap(TaskContext aContext)
			throws ResourceInitializationException
	{	
		Map<String, String> instanceId2TextMap = new HashMap<String,String>();
		String path = aContext.getFolder(Constants.TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY).getPath()
				+"/documentMetaData.txt";
	//	System.out.println(path);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			while (line != null){
				if (line.startsWith("#")){
					// skip
				} else {
					String[] parts = line.split("\t");
					instanceId2TextMap.put(parts[0], parts[1]);
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return instanceId2TextMap;
	}




}
