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
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;
import de.unidue.ltl.escrito.io.util.Utils;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.agreement.QuadraticallyWeightedKappa;

public class Asap2Reader extends JCasCollectionReader_ImplBase {
	public static final Integer[] promptIds = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	protected static final String LANGUAGE = "en";

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = false, defaultValue = "ASAP")
	protected String corpusName;


	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	protected String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	public static final String PARAM_PROMPT_IDS = "EssaySetId";
	@ConfigurationParameter(name = PARAM_PROMPT_IDS, mandatory = false, defaultValue = "-1")
	protected Integer[] requestedPromptIds; 

	protected int currentIndex;

	protected Map<String, List<Asap2Item>> itemMap;

	protected Queue<Asap2Item> asap2Items;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		itemMap = new HashMap<String, List<Asap2Item>>();
		asap2Items = new LinkedList<Asap2Item>();

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
			EvaluationData<Integer> evalData = new EvaluationData<Integer>();

			while ((nextLine = reader.readLine()) != null) {
				// skip the header
				if (nextLine.startsWith("Id")) {
					nextLine = reader.readLine();
				}

				int answerId       = -1;
				int promptId   = -1;
				String goldClass = null;
				String valClass  = null;
				String text      = null;


				String[] nextItem = nextLine.split(separator);

				if (nextItem.length == 5) {
					answerId     = Integer.parseInt(nextItem[0]);
					promptId = Integer.parseInt(nextItem[1]);
					goldClass  = nextItem[2];
					valClass   = nextItem[3];
					text       = Utils.cleanString(nextItem[4]);
				}
				else if (nextItem.length == 3) {
					answerId     = Integer.parseInt(nextItem[0]);
					promptId = Integer.parseInt(nextItem[1]);
					text       = Utils.cleanString(nextItem[2]);
				}
				else {
					throw new IOException("Wrong file format. Found "+nextItem.length+" elements per line. Expected 5 elements. "+nextLine);
				} 

				Asap2Item newItem = new Asap2Item(answerId, promptId, goldClass, valClass, text);

				if (requestedPromptIds != null && (!Arrays.asList(requestedPromptIds).contains(promptId))) {
					continue;
				}
				evalData.register(Integer.parseInt(goldClass), Integer.parseInt(valClass));
				
				List<Asap2Item> itemList;
				if (itemMap.containsKey(goldClass)) {
					itemList = itemMap.get(goldClass);
				}
				else {
					itemList = new ArrayList<Asap2Item>();
				}
				itemList.add(newItem);
				itemMap.put(goldClass, itemList);

				asap2Items.add(newItem);
				//	System.out.println("Added item");
			}
			// compute IAA
			QuadraticallyWeightedKappa qwk = new QuadraticallyWeightedKappa(evalData);
			System.out.println("QWK: "+qwk.getResult());
			currentIndex = 0;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}


	public boolean hasNext()
			throws IOException 
	{
		return !asap2Items.isEmpty();
	}


	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{
		Asap2Item asap2Item = asap2Items.poll();
		getLogger().debug(asap2Item);
		String itemId = asap2Item.getPromptId()+"_"+asap2Item.getTextId(); 

		try
		{
			jcas.setDocumentLanguage(LANGUAGE);
			jcas.setDocumentText(asap2Item.getText());

			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			dmd.setDocumentId(itemId); // + "-" + asap2Item.getEssaySetId());
			dmd.setDocumentTitle(asap2Item.getText());
			dmd.setDocumentUri(inputFileURL.toURI().toString());
			dmd.setCollectionId(itemId);

		} 
		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}

		LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId(String.valueOf(asap2Item.getPromptId()));
		// TODO set TA ids
		learnerAnswer.addToIndexes();

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		unit.setSuffix(itemId);
		unit.addToIndexes();

		// The gold score is always assigned to the container CAS
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(asap2Item.getGoldClass());
		outcome.addToIndexes();

		currentIndex++;
	}
}



