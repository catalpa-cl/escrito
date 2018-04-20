package de.unidue.ltl.escrito.io.shortanswer;

import java.io.File; 
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;



public class SRAReader extends JCasCollectionReader_ImplBase{

	protected static final String DEFAULT_LANGUAGE = "en";

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = DEFAULT_LANGUAGE)
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_CORPUSNAME = "corpusName";
	@ConfigurationParameter(name = PARAM_CORPUSNAME, mandatory = true)
	protected String corpusName;

	public static final String[] PromptSetIds = new String[] {"BULB_C_VOLTAGE_EXPLAIN_WHY1","BULB_C_VOLTAGE_EXPLAIN_WHY2","BULB_C_VOLTAGE_EXPLAIN_WHY6","BULB_ONLY_EXPLAIN_WHY2","BULB_ONLY_EXPLAIN_WHY4","BULB_ONLY_EXPLAIN_WHY6","BURNED_BULB_LOCATE_EXPLAIN_Q","OTHER_TERMINAL_STATE_EXPLAIN_Q","TERMINAL_STATE_EXPLAIN_Q","VOLTAGE_AND_GAP_DISCUSS_Q","VOLTAGE_DEFINE_Q","VOLTAGE_DIFF_DISCUSS_1_Q","VOLTAGE_DIFF_DISCUSS_2_Q","VOLTAGE_GAP_EXPLAIN_WHY1","VOLTAGE_GAP_EXPLAIN_WHY3","VOLTAGE_GAP_EXPLAIN_WHY4","VOLTAGE_GAP_EXPLAIN_WHY5","VOLTAGE_GAP_EXPLAIN_WHY6","VOLTAGE_INCOMPLETE_CIRCUIT_2_Q","BURNED_BULB_PARALLEL_EXPLAIN_Q1","BURNED_BULB_PARALLEL_EXPLAIN_Q2","BURNED_BULB_PARALLEL_EXPLAIN_Q3","BURNED_BULB_PARALLEL_WHY_Q","GIVE_CIRCUIT_TYPE_HYBRID_EXPLAIN_Q2","GIVE_CIRCUIT_TYPE_HYBRID_EXPLAIN_Q3","GIVE_CIRCUIT_TYPE_PARALLEL_EXPLAIN_Q2","HYBRID_BURNED_OUT_EXPLAIN_Q1","HYBRID_BURNED_OUT_EXPLAIN_Q3","HYBRID_BURNED_OUT_WHY_Q2","HYBRID_BURNED_OUT_WHY_Q3","OPT1_EXPLAIN_Q2","OPT2_EXPLAIN_Q","PARALLEL_SWITCH_EXPLAIN_Q1","PARALLEL_SWITCH_EXPLAIN_Q2","PARALLEL_SWITCH_EXPLAIN_Q3","SWITCH_TABLE_EXPLAIN_Q1","SWITCH_TABLE_EXPLAIN_Q2","SWITCH_TABLE_EXPLAIN_Q3","CONDITIONS_FOR_BULB_TO_LIGHT","DAMAGED_BUILD_EXPLAIN_Q","DAMAGED_BULB_EXPLAIN_2_Q","GIVE_CIRCUIT_TYPE_SERIES_EXPLAIN_Q","SHORT_CIRCUIT_EXPLAIN_Q_2","SHORT_CIRCUIT_EXPLAIN_Q_4","SHORT_CIRCUIT_EXPLAIN_Q_5","SHORT_CIRCUIT_X_Q","SWITCH_OPEN_EXPLAIN_Q","EM_45b","EM_45c","EM_16b","EM_21a","EM_21b","EM_43b","EM_46","EM_48b","EM_26","EM_27b","EM_35","EM_47","FN_20a","FN_20b","FN_19b","FN_24b","FN_24c","FN_17a","FN_17c","FN_27a","FN_27b","II_13a","II_26","II_13b","II_24b","II_20b","II_38","LF_39","LF_31b","LF_33b","LF_34b","LF_18a","LF_28a2","LF_26a2","LF_26b2","LF_13a","LF_27a","LF_6b","LP_15c","LP_16d","ME_27b","ME_28b","ME_30","ME_5b","ME_66a","ME_66b","ME_6b","ME_72","ME_7a","ME_7b","ME_17a","ME_17b","ME_17c","ME_17d","ME_17e","ME_38a","ME_10","ME_78b","ME_79","ME_65a","ME_65b","ME_69b","ME_73","MS_64a","MS_43a","MS_43b","MS_39","MS_50a","MS_14b","MS_30b","MX_1","MX_22a","MX_24","MX_52b","MX_42a","MX_16a","MX_46b","MX_47b","MX_10","MX_18","MX_19","MX_36a","MX_36b","MX_41","MX_11a","MX_11c","MX_11e","MX_11f","MX_49","MX_53","PS_2a","PS_45b","PS_44","PS_12","PS_51a","PS_51b","PS_15bp","PS_26p","PS_46b","PS_4ap","PS_4bp","SE_10","SE_24a","SE_3c","SE_4a","SE_16b2","SE_22a","SE_22b","SE_22c","SE_47b","SE_51b","SE_25a","SE_31b","SE_45","SE_46","SE_48","ST_54b2","ST_54b3","ST_58","ST_25b1","ST_25b2","ST_52a","ST_31b","ST_59","VB_1","VB_12d","VB_40a","VB_5a","VB_5b","VB_22c","VB_42","VB_15a","VB_15b","VB_15c","VB_29"};

	public static final String PARAM_PROMPT_SET_ID = "PromptSetId";
	@ConfigurationParameter(name = PARAM_PROMPT_SET_ID, mandatory = false)
	protected String requestedPromptSetId; 

	public static final String PARAM_QUESTION_PREFIX = "QuestionPrefix";
	@ConfigurationParameter(name = PARAM_QUESTION_PREFIX, mandatory = true)
	private String questionPrefix;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = true)
	private String targetAnswerPrefix;

	protected Queue<SRAItem> items;
	private List<String> answerIds;
	private List<String> questionIds;
	private List<String> modules;
	private Map<String, String> questions;
	private Map<String, String> targetAnswers;

	protected int currentIndex; 





	public void initialize(UimaContext aContext) throws ResourceInitializationException{
		answerIds= new ArrayList<String>();
		questionIds= new ArrayList<String>();
		modules=new ArrayList<String>();
		items = new LinkedList<SRAItem>();   	
		questions = new HashMap<String, String>();
		targetAnswers = new HashMap<String, String>();

		try{
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			File inputFile = new File(inputFileURL.getFile());
			//xml filter
			File[] fileArray = inputFile.listFiles(
					new FilenameFilter(){  
						public boolean accept(File dir, String name){  
							return name.indexOf(".xml")!=-1;
						}  
					});                                                              
			for(File f:fileArray){
				System.out.println(f.getName());
				SAXReader reader = new SAXReader();
				Document document = reader.read( f);

				Element root = document.getRootElement();


				//read question
				final XPath questionXPath = new Dom4jXPath("//question");
				for(Object questionElement:questionXPath.selectNodes(root)){

					String questionId="";
					String questionText="";
					String module="";
					if(questionElement instanceof Element){
						Element questionNode = (Element) questionElement;

						for(Object o:questionNode.attributes()){
							Attribute attribute=(Attribute) o;
							String name =attribute.getName();
							if(name.equals("id")){
								questionId=attribute.getValue();
								questionIds.add(questionId);
							}
							if(name.equals("module")){
								module =attribute.getName();
								modules.add(module);
							}
						}
						final XPath questionTextXPath = new Dom4jXPath("//questionText");
						for(Object questionTextElement:questionTextXPath.selectNodes(root)){
							Element questionTextNode = (Element) questionTextElement;
							questionText=questionTextNode.getText();
						} 
						//questions.put(String.valueOf(questionId), Utils.cleanString(questionText));
						questions.put(String.valueOf(questionId), questionText);
								} 

					ArrayList<String> targetAnswerIds = new ArrayList<String>();
					//read target answers
					final XPath targetAnswerXPath = new Dom4jXPath("//referenceAnswer");
					for (Object targetAnswerElement : targetAnswerXPath.selectNodes(root)) {
						if (targetAnswerElement instanceof Element) {
							Element targetAnswerNode = (Element) targetAnswerElement;
							for(Object obj:targetAnswerNode.attributes()){
								Attribute att =(Attribute) obj;
								String targetAnswerName =att.getName();
								if (targetAnswerName.equals("id")) {
									targetAnswerIds.add(att.getValue());
									targetAnswers.put(att.getValue(), targetAnswerNode.getText());
						//			System.out.println(att.getValue()+"\t"+targetAnswerNode.getText());
								}
							}
						}
					} 
//					System.out.println("TAs:");
//					for (String ta : targetAnswerIds){
//						System.out.println(ta);
//					}



					// if valid PromptSetId is set, then skip if not equal with current 
					if (requestedPromptSetId != null && !requestedPromptSetId.equals(questionId)) {
						break;
					}
					//read student answer
					final XPath answerXPath = new Dom4jXPath("//studentAnswer");		        
					for (Object answerElement : answerXPath.selectNodes(root)) {
						SRAItem answer = new SRAItem("","","","","","");
						answer.setQuestionId(questionId);
						answer.setModule(module);
						if (answerElement instanceof Element) {
							Element answerNode = (Element) answerElement;

							answer.setText(answerNode.getText());

							for (Object o : answerNode.attributes()) {
								Attribute attribute = (Attribute) o;  
								String name = attribute.getName();
								if (name.equals("id")) {
									answer.setAnswerId(attribute.getValue());
									answerIds.add(attribute.getValue());
								}
								else if (name.equals("accuracy")) {
									answer.setnWayGrade(attribute.getValue());
								}
								//read target answer of Beetle
								answer.setTargetAnswerIds(targetAnswerIds);
								//read target answer of sciEntsBank
								//								final XPath targetAnswerXPath = new Dom4jXPath("//referenceAnswer");
								//								for (Object targetAnswerElement : targetAnswerXPath.selectNodes(root)) {
								//									String targetAnswerId = "";
								//									String targetAnswer ="";
								//									if (targetAnswerElement instanceof Element) {
								//										Element targetAnswerNode = (Element) targetAnswerElement;
								//										for(Object obj:targetAnswerNode.attributes()){
								//											Attribute att =(Attribute) obj;
								//											if (att.getName().equals("id")&&att.getValue().substring(2,3).equals("_")) {    		             	                    		
								//												answer.setTargetAnswer(targetAnswerNode.getText()); 
								//												answer.setTargetAnswerId(targetAnswerId);
								//												//	System.out.println("ID: "+targetAnswerId);
								//												targetAnswerId=att.getValue();
								//												targetAnswer=targetAnswerNode.getText();     		             	                    		
								//											}		             	                    	
								//											System.out.println("TA: "+targetAnswerId+"\t"+targetAnswer);
								//											targetAnswers.put(String.valueOf(targetAnswerId), Utils.cleanString(targetAnswer));
								//										}
								//									}
								//								}     					                

							}               
						}  
						items.add(answer);
					}
				}     	                 	              	      	           
			}            
		}
		catch(RuntimeException e){
			throw e;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}		
		currentIndex = 0;
	//	Utils.preprocessConnectedTexts(targetAnswers, corpusName, targetAnswerPrefix, "en");
	//	Utils.preprocessConnectedTexts(questions, corpusName, questionPrefix, "en");
	}



	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, answerIds.size(), Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return !items.isEmpty();
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{
		SRAItem item = items.poll();
		getLogger().debug(item);

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
			dmd.setDocumentId(String.valueOf(item.getAnswerId())); 
			dmd.setDocumentTitle(item.getText());
			dmd.setDocumentUri(inputFileURL.toURI().toString());
			dmd.setCollectionId(item.getAnswerId());

		} 
		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}
		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		unit.setSuffix(item.getAnswerId());
		unit.addToIndexes();
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(item.getnWayGrade());
		outcome.addToIndexes();
//		LearnerAnswerWithReferenceAnswer learnerAnswer = new LearnerAnswerWithReferenceAnswer(jcas, 0, jcas.getDocumentText().length());
//		learnerAnswer.setPromptId(item.getQuestionId());
//		StringArray ids = new StringArray(jcas, item.getTargetAnswerIds().size());
//		for (int i = 0; i<item.getTargetAnswerIds().size(); i++){
//			ids.set(i, item.getTargetAnswerIds().get(i));
//		}
//		learnerAnswer.setReferenceAnswerIds(ids);
//		learnerAnswer.addToIndexes();

		currentIndex++;
	}
}
