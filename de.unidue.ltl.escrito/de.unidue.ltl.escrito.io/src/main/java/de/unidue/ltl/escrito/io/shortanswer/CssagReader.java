package de.unidue.ltl.escrito.io.shortanswer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;
import de.unidue.ltl.escrito.io.shortanswer.ItemWithTargetAnswer;
import de.unidue.ltl.escrito.io.util.Utils;

public class CssagReader extends JCasCollectionReader_ImplBase {

	protected static final String DEFAULT_LANGUAGE = "de";

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL  inputURL;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = false, defaultValue = "CSSAG")
	protected String corpusName;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = DEFAULT_LANGUAGE)
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	public static final String PARAM_PROMPT_SET_ID = "PromptSetId";
	@ConfigurationParameter(name = PARAM_PROMPT_SET_ID, mandatory = false)
	protected String requestedPromptSetId; 

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = false)
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = false)
	private String targetAnswerPrefix;


	public static final String PARAM_PREPROCESSING_OF_CONNECTED_TEXTS = "preproTexts";
	@ConfigurationParameter(name = PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, mandatory = false, defaultValue="true")
	protected boolean preproTexts;

	protected int currentIndex;    

	protected Queue<CssagItem> items;
	private Map<String, String> targetAnswers;
	private Map<String, String> questions;

	private List<Integer> grades;

	private Integer refAId;  // target answers don't have IDs in CSSAG; this is a counter to create unique IDs

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<CssagItem>();

		grades = new ArrayList<Integer>();
		targetAnswers = new HashMap<String, String>();
		questions = new HashMap<String, String>();

		refAId = 0;

		try {
			inputURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			// if the input is a directory, read all xml files form the directory
			if (new File ( inputURL.getFile()).isDirectory()){
				File[] fileArray = new File( inputURL.getFile()).listFiles(
						new FilenameFilter(){  
							public boolean accept(File dir, String name){  
								return name.indexOf(".xml")!=-1;
							}  
						}
						);
				//	System.out.println(Arrays.toString(fileArray));
				for (File file : fileArray){
					//	System.out.println(file.getPath());
					URL fileURL = ResourceUtils.resolveLocation(file.getPath(), this, aContext);
					extractLearnerAnswersFromFile(fileURL);
				}
			} else {
				extractLearnerAnswersFromFile(inputURL);
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Integer>>() {}.getType();

		try {
			FileUtils.writeStringToFile(new File("target/scores.txt"), gson.toJson(grades, listType));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		currentIndex = 0;	
		if (preproTexts){
			Utils.preprocessConnectedTexts(targetAnswers, corpusName, targetAnswerPrefix, "de");
			Utils.preprocessConnectedTexts(questions, corpusName, questionPrefix, "de");
		}
	}


	private void extractLearnerAnswersFromFile(URL fileURL) throws JDOMException, IOException {

		/*
	 items = new LinkedList<CssagItem>();

		grades = new ArrayList<Integer>();
		targetAnswers = new HashMap<String, String>();
		questions = new HashMap<String, String>();

		refAId = 0;
		 */
		Document doc = new SAXBuilder().build(fileURL);

		Element qNode = doc.getRootElement();

		String qid = qNode.getChild("questionID").getText().trim();
		String questionText = qNode.getChild("questionText").getText().trim();
		questions.put(qid, Utils.cleanString(questionText));
		//System.out.println("Question "+qid+": "+questionText);


		// CSSAG specific fields
		float maxPoints = Float.parseFloat(qNode.getChildText("maxPoints").trim());
		float irtDifficulty = Float.parseFloat(qNode.getChildText("IRTdifficulty").trim());
		String bcpNoContext = qNode.getChildText("BCPNoContext").trim();
		String bcpContext = qNode.getChildText("BCPContext").trim();

		String firstAnswerID = "";

		List<Element> targetAnswerNodes = qNode.getChildren("referenceAnswer");

		for (Element targetAnswer : targetAnswerNodes) {
			String taid = refAId.toString();
			if (firstAnswerID == "") {
				firstAnswerID = taid;
			}
			refAId++;

			String taText = targetAnswer.getText().trim();

			// BerkeleyParser can't process directions to human graders like "1 P" 

			taText = taText.replaceAll("[\\d\\.,]+ P( |\\n|\\.|,|\\)|$)","");

			targetAnswers.put(taid, Utils.cleanString(taText));
		}

		Element answerNode = qNode.getChild("studentAnswers");
		List<Element> answerNodes = answerNode.getChildren("answer");
		for (Element sAnswer : answerNodes) {

			String answerID = sAnswer.getChildText("answerID").trim();
			String studentId = sAnswer.getChildText("studentID").trim();

			String answertext = sAnswer.getChild("studentAnswer").getText().trim();

			// scores in CSSAG are floats; normalise to int
			int score = (int) (Float.parseFloat(sAnswer.getChild("score").getText().trim()) * 100);

			CssagItem item = new CssagItem(studentId, qid, answertext, score, targetAnswers.get(firstAnswerID));

			// add CSSAG specific fields
			item.setQuestion(questionText);
			item.setMaxPoints(maxPoints);
			item.setAnswerID(answerID);
			item.setIrtDifficulty(irtDifficulty);
			item.setBcpNoContext(bcpNoContext);
			item.setBcpContext(bcpContext);


			// add any additional target answers
			for (String key : targetAnswers.keySet()) {
				if (!key.equals(firstAnswerID))
					item.setTargetAnswer(targetAnswers.get(key));
			}

			items.add(item);

		}


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
		CssagItem item = items.poll();
		getLogger().debug(item);
		String itemId = item.getQuestionId()+"_"+item.getStudentId(); 
		//System.out.println("itemId: "+itemId);
		try
		{
			if (language != null) {
				jcas.setDocumentLanguage(language);
			}
			else {
				jcas.setDocumentLanguage(DEFAULT_LANGUAGE);
			}

			jcas.setDocumentText(item.getText());


			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			dmd.setDocumentId(itemId); 
			dmd.setDocumentTitle(itemId);
			dmd.setDocumentUri( inputURL.toURI().toString());
			dmd.setCollectionId(itemId);

		} 
		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}
		LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(jcas, 0, jcas.getDocumentText().length());
		learnerAnswer.setPromptId(item.getQuestionId());

		// add all possible target answers
		StringArray ids = new StringArray(jcas, targetAnswers.size());
		for (int i=0; i<targetAnswers.size();i++) {
			ids.set(i, targetAnswers.get(""+i));
		}

		learnerAnswer.setReferenceAnswerIds(ids);
		learnerAnswer.addToIndexes();
		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 

		unit.setSuffix(itemId);
		unit.addToIndexes();

		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(String.valueOf(item.getGrade()));	
		outcome.addToIndexes();

		currentIndex++;
	}



	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentIndex, grades.size(), Progress.ENTITIES) };
	}

}

class CssagItem extends ItemWithTargetAnswer {


	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public float getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(float maxPoints) {
		this.maxPoints = maxPoints;
	}

	public String getAnswerID() {
		return answerId;
	}

	public void setAnswerID(String answerID) {
		this.answerId = answerID;
	}


	public float getIrtDifficulty() {
		return irtDifficulty;
	}

	public void setIrtDifficulty(float irtDifficulty) {
		this.irtDifficulty = irtDifficulty;
	}

	public String getBcpNoContext() {
		return bcpNoContext;
	}

	public void setBcpNoContext(String bcpNoContext) {
		this.bcpNoContext = bcpNoContext;
	}

	public String getBcpContext() {
		return bcpContext;
	}

	public void setBcpContext(String bcpContext) {
		this.bcpContext = bcpContext;
	}




	String question; 
	// use list of target answers from the mother class to hold target answers
	float maxPoints;
	String answerId;
	// use mother's grade; grade is a float in the original, we record score*100 for compatibility with the parent classes
	float irtDifficulty;
	String bcpNoContext;
	String bcpContext;


	public CssagItem(String studentId, String questionId, String answerText, int grade, String targetAnswer) {
		super(studentId, questionId, answerText, grade, targetAnswer);
	}


}

