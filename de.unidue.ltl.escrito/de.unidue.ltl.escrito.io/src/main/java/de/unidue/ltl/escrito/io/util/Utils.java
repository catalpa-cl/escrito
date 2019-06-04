package de.unidue.ltl.escrito.io.util;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.jfree.util.Log;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.escrito.core.types.LearnerAnswerToken;
import de.unidue.ltl.escrito.generic.GenericDatasetItem;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;



public class Utils {

	public static void preprocessConnectedTexts(Map<String, String> textsById, String folderName, String prefix, String lang) {
		AnalysisEngine engine = null;
		try {
			// TODO: How do we get the same preprocessing as the target answer in there? Or at least: any defined preprocessing?
			if (lang.equals("de")){
				AnalysisEngineDescription description = createEngineDescription(
						createEngineDescription(BreakIteratorSegmenter.class),
						// TODO: reintegrate that
						//	createEngineDescription(LanguageToolChecker.class,
						//			LanguageToolChecker.PARAM_LANGUAGE, lang),
						createEngineDescription(OpenNlpPosTagger.class,
								OpenNlpPosTagger.PARAM_LANGUAGE, "de",
								MateLemmatizer.PARAM_LANGUAGE, "de"),
						createEngineDescription(MateLemmatizer.class,
								MateLemmatizer.PARAM_LANGUAGE, "de"),
						createEngineDescription(
								BerkeleyParser.class,
								BerkeleyParser.PARAM_LANGUAGE,"de",
								//BerkeleyParser.PARAM_LANGUAGE,"en",
								BerkeleyParser.PARAM_WRITE_PENN_TREE,true
								)
						);

				engine = createEngine(description);
			} else if (lang.equals("en")){
				AnalysisEngineDescription description = createEngineDescription(
						createEngineDescription(BreakIteratorSegmenter.class),
						// TODO: reintegrate that
						//	createEngineDescription(LanguageToolChecker.class,
						//			LanguageToolChecker.PARAM_LANGUAGE, lang),
						createEngineDescription(OpenNlpPosTagger.class,
								OpenNlpPosTagger.PARAM_LANGUAGE, "en"),
						createEngineDescription(OpenNlpChunker.class,
								OpenNlpChunker.PARAM_LANGUAGE, "en"),
						createEngineDescription(MateLemmatizer.class,
								MateLemmatizer.PARAM_LANGUAGE, "en"),
						createEngineDescription(
								BerkeleyParser.class,
								BerkeleyParser.PARAM_LANGUAGE,"en",
								BerkeleyParser.PARAM_WRITE_PENN_TREE,true
								)
						);

				engine = createEngine(description);
			}
			if (lang.equals("de") || lang.equals("en")) {	
				// ok
			} else {
				System.err.println("Unknwown language "+lang);
				System.exit(-1);
			}
			for (String id : textsById.keySet()){
				String idOrig = id;
				id = prefix +"_"+id;
				System.out.println("Processing text with id "+id);
				String filePath = System.getenv("DKPRO_HOME")+"/processedData/"+folderName;
				File f = new File(filePath+"/"+id+".bin");
				//				if(f.exists()) { 
				//					//System.out.println("File "+f.toString()+" already exists. We do not process it again.");
				//					Log.info("File "+f.toString()+" already exists. We do not process it again.");
				//					continue;
				//				}
				String ta = textsById.get(idOrig);
				JCas jcas = engine.newJCas();
				jcas.setDocumentText(ta);
				engine.process(jcas);
				DocumentMetaData dmd = DocumentMetaData.create(jcas);
				dmd.setDocumentId(id); 
				dmd.setDocumentTitle(id);
				dmd.setDocumentUri(id);
				dmd.setCollectionId(id);
				AnalysisEngine writerEngine = AnalysisEngineFactory.createEngine(
						BinaryCasWriter.class,
						BinaryCasWriter.PARAM_OVERWRITE, true,
						BinaryCasWriter.PARAM_TARGET_LOCATION, filePath
						);
				writerEngine.process(jcas);
			}
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// HOTFIX for Issue 445 in DKPro Core
	public static String cleanString(String textForCas){
		textForCas = textForCas.replace("…", "...");
		textForCas = textForCas.replace("´", "'");
		return textForCas.replace("’", "'");
	}


	public static StringArray toStringArray(JCas jcas, String s){
		StringArray array = new StringArray(jcas, 1);
		array.set(0, s);
		return array;
	}

	//requires the file to be in format for the GenericReader
	public static Set<String> extractPromptIds(String inputFilePath, boolean ignoreFirstLine, String separator) throws IOException{
		Set<String> promptIds;

		promptIds = new HashSet<String>();
		BufferedReader reader = new BufferedReader(
				new FileReader(inputFilePath)
				);
		String nextLine;
		if (ignoreFirstLine) {
			nextLine = reader.readLine();
		}			
		while ((nextLine = reader.readLine()) != null) {
			//System.out.println("line: "+nextLine);
			String[] nextItem = nextLine.split(separator);
			String promptId = null;

			if (nextItem.length>=4) {
				promptId  = nextItem[0];
				promptIds.add(promptId);
			}
		}  
		reader.close();
	return promptIds;

	}

}
