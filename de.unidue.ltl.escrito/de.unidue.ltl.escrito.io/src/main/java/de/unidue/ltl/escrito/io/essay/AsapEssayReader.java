package de.unidue.ltl.escrito.io.essay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

// TODO ASAP and UDE reader seem to be very similar (use base class or even same reader?)
public class AsapEssayReader
extends JCasResourceCollectionReader_ImplBase
{

	public enum RatingBias {
		high,
		low
	}

	/**
	 * Language
	 */
	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true,defaultValue = "en")
	private String language;

	/**
	 * Target criterium that the learner should make use of
	 */
	public static final String PARAM_TARGET_LABEL = "TargetLabel";
	@ConfigurationParameter(name = PARAM_TARGET_LABEL, mandatory = true)
	protected String targetLabel;

	/**
	 * In case of two coders with different ratings, use lower rating/higher rating bias.
	 */
	public static final String PARAM_RATING_BIAS = "RatingBias";
	@ConfigurationParameter(name = PARAM_RATING_BIAS, mandatory = true)
	protected RatingBias ratingBias;

	public static final String PARAM_DO_SPARSECLASSMERGING = "DoSparseClassMerging";
	@ConfigurationParameter(name = PARAM_DO_SPARSECLASSMERGING, mandatory = true)
	private boolean doSparseClassMerging;


	public static final String PARAM_DO_NORMALIZATION = "doNormalization";
	@ConfigurationParameter(name = PARAM_DO_NORMALIZATION, mandatory = true)
	private boolean doNormalization;

	public static final String PARAM_ASAP_NUMBER = "asapNumber";
	@ConfigurationParameter(name = PARAM_ASAP_NUMBER, mandatory = false,defaultValue="0")
	private int asapNumber;

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	public static final String PARAM_QUESTION_ID = "QuestionId";
	@ConfigurationParameter(name = PARAM_QUESTION_ID, mandatory = false, defaultValue = "-1")
	protected Integer requestedQuestionId; 

	protected Queue<AsapItem> items;
	protected URL inputFileURL;
	int currentIndex;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<AsapItem>();
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							inputFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			System.out.println("Initialize");
			while ((nextLine = reader.readLine()) != null) {
				if (nextLine.startsWith("essay_id")){
					continue;
				}
				//System.out.println(nextLine);
				//String[] nextItem = nextLine.split(separator);
				// TODO: Das muss auch sinnvoller gehen.
				nextLine = nextLine.replaceAll("\t", " XXX ");
				String[] nextItem = nextLine.split("XXX");
				//System.out.println(nextItem.length);
				int essayId = -1;
				String text      = null;
				int score = -1;
				int essaySetId = -1;
				// Train Daten
				if (nextItem.length == 28) {
					//System.out.println(nextItem[0]+"\t"+nextItem[1]+"\t"+nextItem[2]);
					essayId = Integer.parseInt(nextItem[0].trim());
					essaySetId  = Integer.parseInt(nextItem[1].trim());
					score = Integer.parseInt(nextItem[6].trim());
					text = nextItem[2].trim();
					//	System.out.println(essayId+"\t"+essaySetId+"\t"+score+"\t"+text);
					// cut off first and last char (")
					if (text.startsWith("\"")){
						text = text.substring(1);
					}
					if (text.endsWith("\"")){
						text = text.substring(0, text.length()-1);
					}
					if (text.equals("")){
						text = "_";
					}
				}
				else {
					throw new IOException("Wrong file format ( "+nextItem.length+" parts). Problem with line "+nextLine);
				}

				// HOTFIX for Issue 445 in DKPro Core
				text = text.replace("â€™", "'");
				// TODO: There seems to be more going wrong, this is a drastic fix for now
				//	text = text.replace("[^a-zA-Z0-9Ã¤Ã¼Ã¶ÃŸÃ„ÃœÃ–\\(\\)-_ ,.;:\\.\\?]", "");
				//text = text.replaceAll("[^a-zA-Z0-9 ]", "");
				//	System.out.println(text);
				AsapItem newItem = new AsapItem(essayId, essaySetId, score, text);
				if (requestedQuestionId != null && requestedQuestionId != essaySetId) {
					continue;
				}
				items.add(newItem);
			}   
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		currentIndex = 0;
		System.out.println("Read "+items.size()+" items.");
	}



	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return currentIndex < items.size();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, items.size(), Progress.ENTITIES) };
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{    
		//System.out.println("GETNEXT");
		AsapItem item = items.poll();
		getLogger().debug(item);
		String itemId = item.getEssayId()+"_"+item.getEssaySetId(); 
		jcas.setDocumentText(item.getText());
		jcas.setDocumentLanguage(language);

		DocumentMetaData dmd = DocumentMetaData.create(jcas);
		dmd.setDocumentId(itemId); 
		dmd.setDocumentTitle(item.getText());
		dmd.setDocumentUri(inputFileString);
		dmd.setCollectionId(inputFileString);

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		//will add the token content as a suffix to the ID of this unit 



		//	System.out.println("ItemId: "+item.getId());
		unit.setSuffix(itemId);
		unit.addToIndexes();		 
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(String.valueOf(item.getScore()));
		outcome.addToIndexes();
	}

	static class AsapNormalization {

		private static Map<Integer,Integer> MAX_VALUE;
		static
		{
			MAX_VALUE =  new HashMap<Integer,Integer>();
			MAX_VALUE.put(1, 12);
			MAX_VALUE.put(2, 6);
			MAX_VALUE.put(3, 3);
			MAX_VALUE.put(4, 3);
			MAX_VALUE.put(5, 4);
			MAX_VALUE.put(6, 4);
			MAX_VALUE.put(7, 30);
			MAX_VALUE.put(8, 60);
		}

		private static Map<Integer,Integer> MIN_VALUE;
		static
		{
			MIN_VALUE =  new HashMap<Integer,Integer>();
			MIN_VALUE.put(1, 2);
			MIN_VALUE.put(2, 1);
			MIN_VALUE.put(3, 0);
			MIN_VALUE.put(4, 0);
			MIN_VALUE.put(5, 0);
			MIN_VALUE.put(6, 0);
			MIN_VALUE.put(7, 0);
			MIN_VALUE.put(8, 0);
		}

		public static Integer normalizeAsap(int asapNumber, int value)
		{
			if (MAX_VALUE.containsKey(asapNumber))
			{
				// difference (+1 for counting from zero)
				double numberOfClasses = MAX_VALUE.get(asapNumber) - MIN_VALUE.get(asapNumber) + 1;
				// nth position in the scale (+1 for zero counting)
				double numerator = value-MIN_VALUE.get(asapNumber) + 1;
				double newValue = (numerator/numberOfClasses) * 9.0;

				return (int) Math.round(newValue);
			}
			else {
				return value;
			}		
		}

	}
}

class AsapItem{

	protected int essayId;
	protected String text;
	protected int questionId;
	protected int score;


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(essayId);
		sb.append("-");
		sb.append(questionId);
		sb.append(" ");
		sb.append(score);
		sb.append(" ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");
		return sb.toString();        
	}

	public AsapItem(int essayId, int essaySetId, int goldClass, String text)
	{
		this.questionId = essaySetId;
		this.essayId = essayId;
		this.text = text;
		this.score = goldClass;

	}

	public int getEssayId()
	{
		return essayId;
	}

	public void setEssayId(int essayId)
	{
		this.essayId = essayId;
	}

	public int getEssaySetId()
	{
		return questionId;
	}

	public void setEssaySetId(int essaySetId)
	{
		this.questionId = essaySetId;
	}

	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
