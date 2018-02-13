package de.unidue.ltl.escrito.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
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


/*
 * We expect a generic data format with one item per line, and at least 4 columns: promptid (can be the same for all items), answerid, answer text and score.
 * The score has to be either numeric or categorial.
 * If it is numeric, then is should be mappable to an integer, to ensure that the values are equally spaced. 
 * (a suitable factor is needed in that case, e.g. 2, if the scores in the dataset are 0.0, 0.5, 1.0 etc)
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

	protected int currentIndex;    

	protected Queue<GenericDatasetItem> items;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<GenericDatasetItem>();
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
				int score    = -1;

				if (nextItem.length>=4) {
					promptId  = nextItem[0];
					answerId = nextItem[1];
					text       = nextItem[2];
					double rawScore    = scoreMultiplicationFactor*Double.parseDouble(nextItem[3]);
					if (rawScore % 1 == 0)	{
						score = (int) rawScore;
					} else {
						System.err.println("Problem processing score "+rawScore+" with multiplication factor " + scoreMultiplicationFactor
					+ "Your scores are not integers and you did not provide a suitable multiplication factor.");
						System.exit(-1);
					}
					text = Utils.cleanString(text);
					GenericDatasetItem newItem = new GenericDatasetItem(promptId, answerId, text, score);
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
			dmd.setDocumentTitle(itemId);
			dmd.setDocumentUri(inputFileURL.toURI().toString());
			dmd.setCollectionId(itemId);

		} 

		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}

		LearnerAnswer learnerAnswer = new LearnerAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId("-1");
		learnerAnswer.addToIndexes();

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		unit.setSuffix(itemId);
		unit.addToIndexes();      
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(Integer.toString(item.getGrade()));
		outcome.addToIndexes();
		currentIndex++;
	}



	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}


}
