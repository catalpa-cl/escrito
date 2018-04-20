package de.unidue.ltl.escrito.io.essay;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.unidue.ltl.escrito.io.util.XmlUtils;

// TODO ASAP and UDE reader seem to be very similar (use base class or even same reader?)
public class AsapEssayReader2
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


	public static final String PARAM_DO_NORMALIZATION = "false";
	@ConfigurationParameter(name = PARAM_DO_NORMALIZATION, mandatory = true)
	private boolean doNormalization;

	public static final String PARAM_ASAP_NUMBER = "asapNumber";
	@ConfigurationParameter(name = PARAM_ASAP_NUMBER, mandatory = false,defaultValue="0")
	private int asapNumber;

	protected Resource res;

	/**
	 * returns the asapNumber of a file based on the folder the file is contained in
	 * @param res
	 * @return asapNumber
	 */
	private int getNumberFromPath(Resource res) {
		// TODO use paths and return parent folder
		String[] pathParts=res.getPath().split("/");
		String asapFolder=pathParts[pathParts.length-2];
		//		System.out.println(res.getLocation()+ " "+Integer.parseInt(asapFolder.substring(asapFolder.length() - 1)));
		return Integer.parseInt(asapFolder.substring(asapFolder.length() - 1));
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{    
		res = nextFile();
		initCas(jcas, res);

		Element root;
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new BufferedInputStream(res.getInputStream()));
			root = document.getRootElement();
		}
		catch (DocumentException e) {
			throw new CollectionException(e);
		}
		catch (IOException e) {
			throw new CollectionException(e);
		}

		String essayText = "";
		// TODO: read id
		String id = "";
		try {  
			final XPath essayXP = new Dom4jXPath("//gradedEssay");

			Object essayElement = essayXP.selectSingleNode(root);
			Element essayNode = (Element) essayElement;
			essayText = XmlUtils.getText(essayNode, "descendant::essay");

			final XPath criteriumXP = new Dom4jXPath("//criterium");
			for (Object criteriumElement : criteriumXP.selectNodes(root)) {
				Element criteriumNode = (Element) criteriumElement;

				String name = XmlUtils.getAttributeValue(criteriumNode, "name");

				if (name.equals(targetLabel))
				{        
					List<Integer> codes = new ArrayList<Integer>();
					final XPath codeXP = new Dom4jXPath("descendant::coding");
					for (Object codeElement : codeXP.selectNodes(criteriumNode)) {
						Element codeNode = (Element) codeElement;
						codes.add(Integer.parseInt(codeNode.getText()));
					}

					Integer code1 = codes.get(0);
					Integer code2 = null;
					if (codes.size() > 1) {
						code2 = codes.get(1);
					}
					// a temporary number is used to prevent interference effects
					int tempAsapNumber = 0;
					if (asapNumber==0) {
						// not asap number configured?
						tempAsapNumber = getNumberFromPath(res);
					}
					else {
						//else set the number for the current normalization from path
						tempAsapNumber = asapNumber;
					}

					Integer resolvedCode = code1;
					if (code1 != null && code2 != null) {
						if (ratingBias.equals(RatingBias.low)) {
							if(doNormalization) {
								resolvedCode = AsapNormalization.normalizeAsap(tempAsapNumber, Math.min(code1, code2));
							}
							else {
								resolvedCode = Math.min(code1, code2);
							}
						}
						else if (ratingBias.equals(RatingBias.high)) {
							if(doNormalization) {
								resolvedCode = AsapNormalization.normalizeAsap(tempAsapNumber, Math.max(code1, code2));
							}
							else {
								resolvedCode = Math.max(code1, code2);
							}
						}
					}
					jcas.setDocumentText(essayText);
					jcas.setDocumentLanguage(language);
					TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
					//will add the token content as a suffix to the ID of this unit 

					//	System.out.println("ItemId: "+item.getId());
					unit.setSuffix(id);
					unit.addToIndexes();		 
					TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
					outcome.setOutcome(resolvedCode.toString());
					outcome.addToIndexes();
					
					DocumentMetaData dmd = DocumentMetaData.create(jcas);
					dmd.setDocumentId(id); 
					dmd.setDocumentTitle(essayText);
				}
			}
		}
		catch (JaxenException e) {
			throw new CollectionException(e);
		}
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
