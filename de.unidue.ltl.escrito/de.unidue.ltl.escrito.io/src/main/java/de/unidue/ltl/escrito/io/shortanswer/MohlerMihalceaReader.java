package de.unidue.ltl.escrito.io.shortanswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
import de.unidue.ltl.escrito.io.util.Utils;

public class MohlerMihalceaReader
extends JCasCollectionReader_ImplBase
{

	public static final Integer[] promptIds = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

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

	public static final String PARAM_PROMPT_IDS = "QuestionId";
	@ConfigurationParameter(name = PARAM_PROMPT_IDS, mandatory = false, defaultValue = "-1")
	protected Integer[] requestedPromptIds; 

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = true)
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = true)
	private String targetAnswerPrefix;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = true)
	protected String corpusName;

	protected int currentIndex;    

	protected Queue<MohlerMihalceaItem> items;
	protected Map<String, String> targetAnswers;
	protected Map<String, String> questions;

	protected static final double multiplicationFactor = 2;



	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<MohlerMihalceaItem>();
		targetAnswers = new HashMap<String, String>();
		questions = new HashMap<String, String>();

		boolean onlyValidIds = true;
		for (int requestedPromptId : requestedPromptIds){
			onlyValidIds = onlyValidIds && Arrays.asList(promptIds).contains(requestedPromptId);
		}
		if (requestedPromptIds != null && !onlyValidIds) {
			getLogger().warn("Invalid questionId(s) - using all documents");
			requestedPromptIds = null;
		}


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
				if (nextLine.startsWith("#")) {
					nextLine = reader.readLine();
				}
				String[] nextItem = nextLine.split(separator);
				int promptId = 0;
				int assignmentId = 0;
				String studentId = null;
				String text      = null;
				int score = -1;
				String question = null;
				String targetAnswer = null;

				if (nextItem.length == 7) {
					//System.out.println(nextItem[0]+"\t"+nextItem[1]+"\t"+nextItem[2]);
					promptId = Integer.parseInt(nextItem[0]);
					assignmentId = Integer.parseInt(nextItem[1]);
					// we need to convert the double to an int with a distance of 1 between two adjacent labels.
					score    = (int) (Double.parseDouble(nextItem[2]) * multiplicationFactor ); 
					studentId  = nextItem[3];
					text       = Utils.cleanString(nextItem[4]).trim();
					question = Utils.cleanString(nextItem[5]).trim();
					targetAnswer = Utils.cleanString(nextItem[6]).trim();
				}
				else {
					throw new IOException("Wrong file format. Problem with line "+nextLine);
				}

				// if validEssaySetId is set, then skip if not equal with current 
				if (requestedPromptIds != null && (!Arrays.asList(requestedPromptIds).contains(promptId))) {
					continue;
				}

				MohlerMihalceaItem newItem = new MohlerMihalceaItem(studentId, promptId, text, score, assignmentId);

				items.add(newItem);
				// only one target answer, so same Id as for prompt
				targetAnswers.put(String.valueOf(promptId), Utils.cleanString(targetAnswer));
				questions.put(String.valueOf(promptId), Utils.cleanString(question));

			}   
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		currentIndex = 0;
		Utils.preprocessConnectedTexts(targetAnswers, corpusName, targetAnswerPrefix, "en");
		Utils.preprocessConnectedTexts(questions, corpusName, questionPrefix, "en");
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
		MohlerMihalceaItem item = items.poll();
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
		LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId(String.valueOf(item.getPromptId()));
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
		outcome.setOutcome(Integer.toString(item.getScore()));
		outcome.addToIndexes();

		currentIndex++;
	}



	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}
}

