package de.unidue.ltl.escrito.io.shortanswer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import de.unidue.ltl.escrito.io.util.Utils;

public class CreeReader extends JCasCollectionReader_ImplBase {

	protected static final String DEFAULT_LANGUAGE = "en";

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL  inputURL;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = false, defaultValue = "CREE")
	protected String corpusName;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = DEFAULT_LANGUAGE)
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_PROMPT_SET_ID = "PromptSetId";
	@ConfigurationParameter(name = PARAM_PROMPT_SET_ID, mandatory = false)
	protected String requestedPromptSetId; 

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = true)
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = true)
	private String targetAnswerPrefix;

	public static final String PARAM_PREPROCESSING_OF_CONNECTED_TEXTS = "preproTexts";
	@ConfigurationParameter(name = PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, mandatory = false, defaultValue="true")
	protected boolean preproTexts;

	protected int currentIndex;    

	protected Queue<CreeItem> items;
	private Map<String, String> targetAnswers;
	private Map<String, String> questions;

	private List<Integer> grades;
	
	
	public static final String[] PromptIds_dev = new String[] {"DU3C6R41", "DU3C6R42", "DU3C6R45", "DU3C5R33", "DU3C6R23", "DU3C6R46", 
			"DU3C5R34", "DU3C6R24", "DU3C6R43", "DU3C5R31", "DU3C6R21", "DU3C6R44", "DU3C5R32", "DU3C6R22", "DU3C5R48", "DU3C6R16",
			"DU3C5R49", "DU3C5R46", "DU3C6R36", "DU3C6R14", "DU3C5R47", "DU3C6R37", "DU3C6R15", "DU3C5R310", "DU3C5R311", "DU3C5R41",
			"DU3C6R31", "DU3C5R44", "DU3C6R34", "DU3C6R12", "DU3C5R45", "DU3C6R35", "DU3C6R13", "DU3C5R42", "DU3C6R32", "DU3C5R43", 
			"DU3C6R33", "DU3C6R11", "DU3C6R49", "DU3C6R27", "DU3C5R37", "DU3C5R38", "DU3C6R47", "DU3C5R35", "DU3C6R26", "DU3C5R36", 
			"DU3C5R39"};
	public static final String[] PromptIds_test = new String[] {"TU3CH6R33", "TU3CH6R32", "TU3CH6R31", "TU3CH6R37", "TU3CH6R36", 
			"TU3CH6R35", "TU3CH6R34", "TU1CH1R42", "TU1CH1R43", "TU1CH1R41", "TU3CH6R27", "TU3CH6R22", "TU3CH5R31", "TU3CH6R21",
			"TU3CH5R32", "TU3CH6R26", "TU3CH5R35", "TU3CH5R36", "TU3CH6R24", "TU3CH5R33", "TU3CH6R23", "TU3CH5R34", "TU1CH1R36", 
			"TU1CH1R37", "TU1CH1R31", "TU1CH1R32", "TU1CH1R33", "TU1CH1R34"};
	
	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<CreeItem>();

		grades = new ArrayList<Integer>();
		targetAnswers = new HashMap<String, String>();
		questions = new HashMap<String, String>();

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
			Utils.preprocessConnectedTexts(targetAnswers, corpusName, targetAnswerPrefix, "en");
			Utils.preprocessConnectedTexts(questions, corpusName, questionPrefix, "en");
		}
	}

	private void extractLearnerAnswersFromFile(URL fileURL) throws JDOMException, IOException {
		Document doc = new SAXBuilder().build(fileURL);
		Element corpus = doc.getRootElement();
		Element qNode = corpus.getChild("Questions");
		List<Element> questionNodes = qNode.getChildren("Question");
		for (Element question: questionNodes){
			String qid = question.getAttributeValue("id");
			String questionText = question.getChild("questionString").getText().trim();
			questions.put(qid, Utils.cleanString(questionText));
	//		System.out.println("Question "+qid+": "+questionText);
			Element taNode = question.getChild("TargetAnswers");
			List<Element> targetAnswerNodes = taNode.getChildren("TargetAnswer");
			String firstTargetAnswer = null;
			String firstTargetAnswerId = null;
			boolean foundFirst = false;
			if (targetAnswerNodes.size() > 1){
				System.err.println("More than one target answer found.");
			}
			for (Element targetAnswer : targetAnswerNodes){
				String taid = targetAnswer.getAttributeValue("question_id");
				//Some target answers have no text
				if (!(targetAnswer.getChildren().isEmpty())){
					String taText = targetAnswer.getChild("answerText").getText().trim();
					targetAnswers.put(taid, Utils.cleanString(taText));
			//		System.out.println("TA "+taid+": "+taText);
					if (!(foundFirst)){
						firstTargetAnswer = Utils.cleanString(taText);
						firstTargetAnswerId = taid;
						foundFirst = true;
					}
				}
			}
			Element laNode = question.getChild("StudentAnswers");
			List<Element> studentAnswers = laNode.getChildren("StudentAnswer");
			for (Element studentAnswer : studentAnswers){
				String id = studentAnswer.getAttributeValue("id");
				String studentId = studentAnswer.getAttributeValue("student_id");
				List<Element> diagnoses = studentAnswer.getChildren("diagnosis");
				if (diagnoses.size() != 2){
					System.err.println("Found "+diagnoses.size()+" diagnoses");
				}
				String answertext1 = diagnoses.get(0).getChild("answerText").getText().trim();
				String binary1 = diagnoses.get(0).getAttributeValue("binary");
				String detailed1 = diagnoses.get(0).getAttributeValue("detailed");
				String teacherId1 = diagnoses.get(0).getAttributeValue("teacher_id");
				String diagnosisId1 = diagnoses.get(0).getAttributeValue("id");
				String answertext2 = diagnoses.get(1).getChild("answerText").getText().trim();
				String binary2 = diagnoses.get(1).getAttributeValue("binary");
				String detailed2 = diagnoses.get(1).getAttributeValue("detailed");
				String teacherId2 = diagnoses.get(1).getAttributeValue("teacher_id");
				String diagnosisId2 = diagnoses.get(1).getAttributeValue("id");
			//	System.out.println("StudentAnswer "+id+" "+studentId+" "+binary1+"/"+binary2+" "+detailed1+"/"+detailed2+" "+answertext1);
				int score = -1;
				if (binary1.equals("N")){
					score = 0;
				} else if (binary1.equals("Y")){
					score = 1;
				} else {
					System.err.println("Illegal binary value "+binary1);
				}
				// TODO, do this in a more relaxed way
				answertext1 = Utils.cleanString(answertext1);
				CreeItem item = new CreeItem(studentId, qid, answertext1, score, firstTargetAnswer);
				item.setTargetAnswerId(firstTargetAnswerId);
				//		System.out.println("TA id: "+closestTaId);
				item.setBinary1(binary1);
				item.setBinary2(binary2);
				item.setDetailed1(detailed1);
				item.setDetailed2(detailed2);
				item.setTeacherId1(teacherId1);
				item.setTeacherId2(teacherId2);
				item.setAnswerText1(answertext1);
				item.setAnswerText2(answertext2);
				item.setDiagnosisId1(diagnosisId1);
				item.setDiagnosisId2(diagnosisId2);
				if (requestedPromptSetId != null && !requestedPromptSetId.equals(qid)) {
					break;
				} else {
				items.add(item);
				}
			}   
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
		CreeItem item = items.poll();
		getLogger().debug(item);
		String itemId = item.getQuestionId()+"_"+item.getStudentId(); 

		try
		{
			if (language != null) {
				jcas.setDocumentLanguage(language);
			}
			else {
				jcas.setDocumentLanguage(DEFAULT_LANGUAGE);
			}

			jcas.setDocumentText(item.getText());

			// TODO: student ID is not unique, we should use a combination of studentId and questionId
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
		StringArray ids = new StringArray(jcas, 1);
		ids.set(0, item.getClosestTaId1());
		learnerAnswer.setReferenceAnswerIds(ids);
		learnerAnswer.addToIndexes();
		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit 
		//	System.out.println("ItemId: "+itemId);
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

class CreeItem extends ItemWithTargetAnswer {


	public String getAnswerText1() {
		return answerText1;
	}

	public void setAnswerText1(String answerText1) {
		this.answerText1 = answerText1;
	}

	public String getAnswerText2() {
		return answerText2;
	}

	public void setAnswerText2(String answerText2) {
		this.answerText2 = answerText2;
	}

	public String getTeacherId1() {
		return teacherId1;
	}

	public void setTeacherId1(String teacherId1) {
		this.teacherId1 = teacherId1;
	}

	public String getTeacherId2() {
		return teacherId2;
	}

	public void setTeacherId2(String teacherId2) {
		this.teacherId2 = teacherId2;
	}

	public String getBinary1() {
		return binary1;
	}

	public void setBinary1(String binary1) {
		this.binary1 = binary1;
	}

	public String getBinary2() {
		return binary2;
	}

	public void setBinary2(String binary2) {
		this.binary2 = binary2;
	}

	public String getDetailed1() {
		return detailed1;
	}

	public void setDetailed1(String detailed1) {
		this.detailed1 = detailed1;
	}

	public String getDetailed2() {
		return detailed2;
	}

	public void setDetailed2(String detailed2) {
		this.detailed2 = detailed2;
	}

	public String getDiagnosisId1() {
		return diagnosisId1;
	}

	public void setDiagnosisId1(String diagnosisId1) {
		this.diagnosisId1 = diagnosisId1;
	}

	public String getDiagnosisId2() {
		return diagnosisId2;
	}

	public void setDiagnosisId2(String diagnosisId2) {
		this.diagnosisId2 = diagnosisId2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getClosestTaId1() {
		return closestTaId1;
	}

	public void setClosestTaId1(String closestTaId1) {
		this.closestTaId1 = closestTaId1;
	}

	public String getClosestTaId2() {
		return closestTaId2;
	}

	public void setClosestTaId2(String closestTaId2) {
		this.closestTaId2 = closestTaId2;
	}

	public String getTargetAnswerId() {
		return targetAnswerId;
	}

	public void setTargetAnswerId(String targetAnswerId) {
		this.targetAnswerId = targetAnswerId;
	}

	String answerText1;
	String answerText2;
	String teacherId1;
	String teacherId2;
	String binary1;
	String binary2;
	String detailed1;
	String detailed2;
	String diagnosisId1;
	String diagnosisId2;
	String question; 
	String closestTaId1;
	String closestTaId2;
	String targetAnswerId;

	public CreeItem(String studentId, String questionId, String text, int grade, String targetAnwer) {
		super(studentId, questionId, text, grade, targetAnwer);
	}


}

