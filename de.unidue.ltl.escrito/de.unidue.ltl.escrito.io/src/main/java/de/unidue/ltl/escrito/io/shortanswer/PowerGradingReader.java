package de.unidue.ltl.escrito.io.shortanswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
import de.unidue.ltl.escrito.core.types.LearnerAnswer;
import de.unidue.ltl.escrito.io.util.Utils;

public class PowerGradingReader
extends JCasCollectionReader_ImplBase
{

	public static final Integer[] promptIds = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 13, 20 };

	protected static final String LANGUAGE = "en";

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	protected String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	protected String separator;

	public static final String PARAM_PROMPT_IDS = "PromptId";
	@ConfigurationParameter(name = PARAM_PROMPT_IDS, mandatory = false, defaultValue = "-1")
	protected Integer[] requestedPromptIds; 
	
	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = false, defaultValue = "PG")
	protected String corpusName;

	protected int currentIndex;    

	protected Queue<PowerGradingItem> items;

	private List<Integer> grades1;
	private List<Integer> grades2;
	private List<Integer> grades3;


	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<PowerGradingItem>();

		boolean onlyValidIds = true;
		for (int requestedPromptId : requestedPromptIds){
			onlyValidIds = onlyValidIds && Arrays.asList(promptIds).contains(requestedPromptId);
		}
		if (requestedPromptIds != null && !onlyValidIds) {
			getLogger().warn("Invalid questionId(s) - using all documents");
			requestedPromptIds = null;
		}

		grades1 = new ArrayList<Integer>();
		grades2 = new ArrayList<Integer>();
		grades3 = new ArrayList<Integer>();

		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							inputFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				// skip the header
				if (nextLine.startsWith("student\t")) {
					nextLine = reader.readLine();
				}

				String[] nextItem = nextLine.split(separator);

				String studentId = null;
				int promptId   = -1;
				String text      = null;
				int grader1      = -1;
				int grader2      = -1;
				int grader3      = -1;

				if (nextItem.length == 6) {
					studentId  = nextItem[0];
					promptId = Integer.parseInt(nextItem[1]);
					text       = nextItem[2];
					grader1    = Integer.parseInt(nextItem[3]);
					grader2    = Integer.parseInt(nextItem[4]);
					grader3    = Integer.parseInt(nextItem[5]);
				}
				else {
					throw new IOException("Wrong file format.");
				}

				// HOTFIX for Issue 445 in DKPro Core
				text = text.replace("’", "'");

				if (requestedPromptIds != null && (!Arrays.asList(requestedPromptIds).contains(promptId))) {
					continue;
				}

				PowerGradingItem newItem = new PowerGradingItem(studentId, promptId, text, grader1, grader2, grader3);

				items.add(newItem);

				if (newItem.getGrader1() != -1) {
					grades1.add(newItem.getGrader1());
					grades2.add(newItem.getGrader2());
					grades3.add(newItem.getGrader3());           	
				}
			}   
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		currentIndex = 0;
//		Utils.preprocessConnectedTexts(targetAnswers, corpusName, targetAnswerPrefix, "en");
//		Utils.preprocessConnectedTexts(questions, corpusName, questionPrefix, "en");
	}

	@Override
	public boolean hasNext()
			throws IOException 
	{
		return !items.isEmpty();
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{
		PowerGradingItem item = items.poll();
		getLogger().debug(item);
		String itemId = item.getPromptId()+"_"+item.getStudentId(); 

		try
		{
			jcas.setDocumentLanguage(LANGUAGE);
			jcas.setDocumentText(item.getText());

			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			dmd.setDocumentId(itemId); 
			dmd.setDocumentTitle(item.getText());
			dmd.setDocumentUri(inputFileURL.toURI().toString());
			dmd.setCollectionId(itemId);

		} 

		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}

		LearnerAnswer learnerAnswer = new LearnerAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId(String.valueOf(item.getPromptId()));
		learnerAnswer.addToIndexes();

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		unit.setSuffix(itemId);
		unit.addToIndexes();

		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(Integer.toString(item.getGrader1()));
		outcome.addToIndexes();

		currentIndex++;
	}



	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}


}


