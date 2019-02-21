package de.unidue.ltl.escrito.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
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


/**
 * We expect a generic data format with one item per line, and at least 4 columns: 
 *  1: promptid (can be the same for all items),
 *  2: answerid, 
 *  3: answer text and 
 *  4: score.
 * The score has to be either numeric or categorial.
 * If it is numeric, then is should be mappable to an integer, to ensure that the values are equally spaced. 
 * (a suitable factor is needed in that case, e.g. 2, if the scores in the dataset are 0.0, 0.5, 1.0 etc)
 * Optional, a reference answer can be provided in column 5, and the question string in column 6 in which case you also have to set the parameters
 * corpusName, TargetAnswerPrefix and questionPrefix.
 * These parameters have also be handed over to any similarity-based feature extractor.
 * We expect that there is only one reference answer and one question for all answers that belong to the same prompt.
 *
 *
 */


// TODO: accept also categorical scores


public class GenericDatasetReader  extends JCasCollectionReader_ImplBase{


	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true  )
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	public static final String PARAM_SCORE_FACTOR = "ScoreMultiplicationFactor";
	@ConfigurationParameter(name = PARAM_SCORE_FACTOR, mandatory = false, defaultValue = "1")
	private Integer scoreMultiplicationFactor;

	public static final String PARAM_IGNORE_FIRST_LINE = "ignoreFirstLine";
	@ConfigurationParameter(name = PARAM_IGNORE_FIRST_LINE, mandatory = false, defaultValue = "false")
	private Boolean ignoreFirstLine;

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = false, defaultValue = "Q")
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = false, defaultValue = "TA")
	private String targetAnswerPrefix;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = true)
	protected String corpusName;

	protected int currentIndex;    

	protected Queue<GenericDatasetItem> items;

	private Map<String, String> questions;
	private Map<String, String> targetAnswers;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<GenericDatasetItem>();
		questions = new HashMap<String, String>();
		targetAnswers = new HashMap<String, String>();
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							inputFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			if (ignoreFirstLine) {
				nextLine = reader.readLine();
			}
			while ((nextLine = reader.readLine()) != null) {
				System.out.println("line: "+nextLine);
				String[] nextItem = nextLine.split(separator);
				String promptId = null;
				String answerId = null;
				String text      = null;
				String score    = "-1";

				if (nextItem.length>=4) {
					GenericDatasetItem newItem = null ;
					promptId  = nextItem[0];
					answerId = nextItem[1];
					text       = nextItem[2];
					if (scoreMultiplicationFactor != 1){
						double scoreNumeric = 0.0;
						try {
							scoreNumeric = Double.parseDouble(nextItem[3]);
						}						
						catch (NumberFormatException e){
							System.err.println("We cannot handle categorical values and scoremultiplication at the same time. please remove the score multiplication factor.");
							System.exit(-1);
						}
						double rawScore    = scoreMultiplicationFactor*scoreNumeric;
						if (rawScore % 1 == 0)	{
							score = String.valueOf((int) rawScore);
						} else {
							System.err.println("Problem processing score "+rawScore+" with multiplication factor " + scoreMultiplicationFactor
									+ "Your scores are not integers and you did not provide a suitable multiplication factor.");
							System.exit(-1);
						}
					} else {
						score = nextItem[3];
					}
					text = Utils.cleanString(text);
					if (nextItem.length >=5){
						targetAnswers.put(promptId, Utils.cleanString(nextItem[4]));
					}
					if (nextItem.length >=6){
						questions.put(promptId, Utils.cleanString(nextItem[5]));
					} 
					newItem = new GenericDatasetItem(promptId, answerId, text, score, promptId);
					//		System.out.println(newItem.toString());
					items.add(newItem);   
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
