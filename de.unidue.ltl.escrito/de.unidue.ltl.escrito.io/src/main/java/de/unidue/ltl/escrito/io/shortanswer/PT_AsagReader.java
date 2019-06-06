package de.unidue.ltl.escrito.io.shortanswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.unidue.ltl.escrito.core.types.LearnerAnswer;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;
import de.unidue.ltl.escrito.io.generic.GenericDatasetItem;
import de.unidue.ltl.escrito.io.util.Utils;


public class PT_AsagReader  extends JCasCollectionReader_ImplBase{

	
	public static final String language = "pt";

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SCORE_FACTOR = "ScoreMultiplicationFactor";
	@ConfigurationParameter(name = PARAM_SCORE_FACTOR, mandatory = false, defaultValue = "1")
	private Integer scoreMultiplicationFactor;

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = true)
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = true)
	private String targetAnswerPrefix;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = false, defaultValue = "PT_ASAG")
	protected String corpusName;

	public static final String PARAM_PROMPT_SET_ID = "PromptSetId";
	@ConfigurationParameter(name = PARAM_PROMPT_SET_ID, mandatory = false)
	protected String requestedPromptSetId; 

	public static final int[] PromptSetIds = {36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50};

	protected int currentIndex;    

	protected Queue<GenericDatasetItem> items;

	private Map<String, String> questions;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<GenericDatasetItem>();
		questions = new HashMap<String, String>();
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							inputFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			//ignoreFirstLine
			nextLine = reader.readLine();

			String promptId = null;
			String text      = null;
			String score    = "-1";


			while ((nextLine = reader.readLine()) != null) {
			//	System.out.println("line: "+nextLine);
				String pattern = "^(\\d\\d),(.+?),(\\d)$";
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(nextLine);
				String pattern_begin = "^(\\d\\d),(.+?)$";
				Pattern r_begin = Pattern.compile(pattern_begin);
				Matcher m_begin = r_begin.matcher(nextLine);
				String pattern_end = "^(.+?),(\\d)$";
				Pattern r_end = Pattern.compile(pattern_end);
				Matcher m_end = r_end.matcher(nextLine);
				if (m.find()){
					promptId = m.group(1);
					text = m.group(2);
					score = m.group(3);
					GenericDatasetItem newItem = new GenericDatasetItem(promptId, String.valueOf(items.size()), text, score, promptId);
					if (requestedPromptSetId != null && !requestedPromptSetId.equals(promptId)) {

					} else {				
				//		System.out.println("1: "+text);
						items.add(newItem);   
					}
				} else {
					if (m_begin.find()){
						promptId = m_begin.group(1);
						text += m_begin.group(2);
					} else if (m_end.find()){
						text += m_end.group(1);
						score = m_end.group(2);
						GenericDatasetItem newItem = new GenericDatasetItem(promptId, String.valueOf(items.size()), text, score, promptId);
						if (requestedPromptSetId != null && !requestedPromptSetId.equals(promptId)) {

						} else {	
					//		System.out.println("2: "+text);
							items.add(newItem);   
						}
					} else {
						text += nextLine.trim();
					}	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e);
		}
		currentIndex = 0;
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
		GenericDatasetItem item = items.poll();
		getLogger().debug(item);
		String itemId = String.valueOf(item.getPromptId()+"_"+item.getAnswerId());	         
		try
		{
			jcas.setDocumentLanguage(language);
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

		LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId(item.getPromptId());
		StringArray ids = new StringArray(jcas, 1);
		// We only have one exactly target answer per learner, so we use the same id as for the prompt
		ids.set(0, String.valueOf(item.getPromptId()));
		learnerAnswer.setReferenceAnswerIds(ids);
		learnerAnswer.addToIndexes();

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		unit.setSuffix(itemId);
		unit.addToIndexes();      
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(item.getGrade());
		outcome.addToIndexes();
		currentIndex++;
	}



	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}


}
