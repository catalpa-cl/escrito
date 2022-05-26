package de.unidue.ltl.escrito.examples.models;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.unidue.ltl.escrito.core.IoUtils;
import de.unidue.ltl.escrito.core.types.LearnerAnswer;
import de.unidue.ltl.escrito.io.shortanswer.GenericItem;


/*
 * read essays (raw text) from a folder
 * reads additionally scores from a tsv file
 * 
 * 4 prompts, 2 Länder, 2 Zeitpunkte
 * 
 */




public class ReaderNewAnswers extends JCasCollectionReader_ImplBase {


	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;


	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = "en")
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	protected int currentIndex;    

	protected Queue<GenericItem> items;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException 
	{
		items = new LinkedList<GenericItem>();
		try{
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			File inputFile = new File(inputFileURL.getFile());
			File[] fileArray = inputFile.listFiles(
					new FilenameFilter(){  
						public boolean accept(File dir, String name){  
							return name.indexOf(".txt")!=-1;
						}  
					});                                                              
			for(File f:fileArray){
				String id = "-1";
				String text =cleanString(String.join(" ", FileUtils.readLines(f)));
				if (text.startsWith("missing data") || text.equals("")){
					continue;
				}
				int score = 1;
				GenericItem item = new GenericItem(id, id, text, score);
				items.add(item);
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}	
		currentIndex = 0;
	}


	// HOTFIX for Issue 445 in DKPro Core
	private static String cleanString(String textForCas){
		textForCas = textForCas.replaceAll("[^a-zA-Z0-9\\-\\.,:;\\(\\)\\? ]", "");	
		textForCas = textForCas.replace("…", "...");
		textForCas = textForCas.replace("´", "'");
		return textForCas.replace("’", "'").trim();
	}


	public boolean hasNext() throws IOException, CollectionException {
		return !items.isEmpty();
	}

	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		GenericItem item = items.poll();
		getLogger().debug(item);
		try
		{
			jcas.setDocumentLanguage(language);
			jcas.setDocumentText(item.getText());        	        	
			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			dmd.setDocumentId(item.getStudentId()); 
			dmd.setDocumentTitle(item.getText());
			dmd.setDocumentUri(inputFileURL.toURI().toString());
			dmd.setCollectionId(item.getStudentId());
		} 

		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}

		LearnerAnswer learnerAnswer = new LearnerAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId("-1");
		learnerAnswer.addToIndexes();

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		unit.setSuffix(item.getStudentId());
		unit.addToIndexes();      
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		// TODO
		outcome.setOutcome(String.valueOf(item.getGrade()));
		outcome.addToIndexes();
		currentIndex++;	
	}



}
