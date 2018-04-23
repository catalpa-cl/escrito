package de.unidue.ltl.escrito.features.occurance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
/**
 * counts the appearance of direct and indirect report representation. 
 * 
 */
@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
		"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class SpeechThoughtWritingRepresentation 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor{

	public static final String NR_OF_DIRECT_REPRESENTATION = "nrOfDirectRepresentation";
	
	public static final String PARAM_REPORTING_VERBS_FILE_PATH = "reportingVerbsFilePath";

	public static final String NR_OF_INDIRECT_REPRESENTATION = "nrOfIndirectRepresentation";

	public static final String NR_OF_REPORTED_REPRESENTATION = "nrOfReportedRepresentation";
	
    @ConfigurationParameter(name = PARAM_REPORTING_VERBS_FILE_PATH, mandatory = true)
    private String reportingVerbsFilePath;
    
	public static final String LANGUAGE = "language";
	@ConfigurationParameter(name = LANGUAGE, mandatory = true)
	private String language;

	private List<String> reportingVerbs;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String,Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		reportingVerbs = getReportingVerbs(reportingVerbsFilePath);
		return true;
	}
	private List<String> getReportingVerbs(String reportingVerbsFilePath) {
		List<String> list = new ArrayList<String>();
		Scanner s;
		try {
			s = new Scanner(new File(reportingVerbsFilePath));
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		double ratioDirectRepresentation = 0;
		double ratioIndirectRepresentation = 0;
		//reported representation, which can be a mere mentioning of a speech, thought, or writing act
		double ratioReportedRepresentation = 0;
		
		Collection<Lemma> lemmas= JCasUtil.select(jcas, Lemma.class);
		for(Lemma lemma : lemmas){
			if(reportingVerbs.contains(lemma.getValue())){
				for(Sentence s: JCasUtil.selectCovering(Sentence.class, lemma)){
					boolean direct = false;
					boolean indirect = false;				
					if(containsDirectSpeech(s,lemma)){
						ratioDirectRepresentation++;
						direct=true;
					}
					if(language.equals("de")){
						if (containsIndirectSpeechDe(s, lemma)) {
							ratioIndirectRepresentation++;
							indirect = true;
						}
					}else if(language.equals("en")){
						if (containsIndirectSpeechEn(s, lemma)) {
							ratioIndirectRepresentation++;
							indirect = true;
						}
					}else{
						System.err.println("Illegal Language");
					}
					//if the sentence is neither direct nor inderect we assume reported speech
					if(!direct&&!indirect){
						ratioReportedRepresentation++;
					}
				}
			}
		}
		double numOfSentences=JCasUtil.select(jcas, Sentence.class).size();
		//Normalization on total count of sentences
		ratioDirectRepresentation=ratioDirectRepresentation/numOfSentences;
		ratioIndirectRepresentation=ratioIndirectRepresentation/numOfSentences;
		ratioReportedRepresentation=ratioReportedRepresentation/numOfSentences;
		
		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(NR_OF_DIRECT_REPRESENTATION, ratioDirectRepresentation, FeatureType.NUMERIC));
		featList.add(new Feature(NR_OF_INDIRECT_REPRESENTATION, ratioIndirectRepresentation, FeatureType.NUMERIC));
		featList.add(new Feature(NR_OF_REPORTED_REPRESENTATION, ratioReportedRepresentation, FeatureType.NUMERIC));
		return featList;
	}
	/**
	 * checks POS-tags that follow the signal word. If the structure meets one
	 * of the specific pattern 1.0 is returned
	 * 
	 * @param sentence
	 * @param lemma
	 * @return
	 */
	private boolean containsIndirectSpeechEn(Sentence sentence, Lemma lemma) {
		List<Token> allPos = JCasUtil.selectCovered(Token.class, sentence);
		List<Token> pos = JCasUtil.selectBetween(Token.class, lemma,
				allPos.get(allPos.size() - 1));
		// subordinate clause with asking word(how, when, ...) : first token== WRB, last token verb
		if (pos.size() > 2 && pos.get(0).getPos().getPosValue().equals("WRB")
				&& pos.get(pos.size() - 1).getPos().getPosValue().equals("VBZ")) {
			return true;
		}
		// infinitive clause: last token is inifitive verb, the token before is TO
		if (pos.size() > 2
				&& pos.get(pos.size() - 1).getPos().getPosValue().equals("VB")
				&& pos.get(pos.size() - 2).getPos().getPosValue().equals("TO")) {
			return true;
		}
		// subordinate clause in subjunctive mode: second or third token is MD (would, should....)
		if (pos.size() > 2
				&& (pos.get(1).getPos().getPosValue().equals("MD") || pos
						.get(2).getPos().getPosValue().equals("MD"))) {
			return true;
		}
		return false;
	}

	private boolean containsIndirectSpeechDe(Sentence sentence, Lemma lemma) {
		List<Token> allPos= JCasUtil.selectCovered(Token.class, sentence);
		List<Token> pos=JCasUtil.selectBetween(Token.class, lemma, allPos.get(allPos.size()-1));
		if(pos.size()>0){
		//TODO: how to deal with phrases like "er erklärt ihr, dass" the PRF "ihr" will fool the indices
		//subordinate clause with conjuction: first token== comma , second token== conjunction, last token == verb (VAFIN||VVFIN)
		if(pos.get(0).getCoveredText().equals(",")&&
				pos.get(1).getPos().getPosValue().equals("KOUS")&&
				(pos.get(pos.size()-1).getPos().getPosValue().equals("VVFIN")||
						pos.get(pos.size()-1).getPos().getPosValue().equals("VAFIN"))){
			return true;
		}
		//infinitive clause: last token is infinitive verb (no comma)
		if(pos.get(pos.size()-1).getPos().getPosValue().equals("VVINF")){
			return true;
		}
		//subordinate clause in subjunctive mode: third token is verb in subjunctive mode
				if(pos.size()>2&&(pos.get(2).getPos().getPosValue().equals("VAFIN")||pos.get(2).getPos().getPosValue().equals("VVFIN"))){
					return true;
				}
		}
		return false;
	}
	/**
	 * returns 1.0 if the pattern ".*?" is found before or after the given signalword
	 * @param sentence s
	 * @param lemma
	 * @return
	 */
	private boolean containsDirectSpeech(Sentence s, Lemma lemma) {
		Pattern quoteRegexPattern = Pattern.compile("\".*?\"");
		Matcher quoteMatcherBefore = quoteRegexPattern.matcher(s
				.getCoveredText().substring(0,lemma.getBegin()-s.getBegin()));
		Matcher quoteMatcherAfter = quoteRegexPattern.matcher(s
				.getCoveredText().substring(lemma.getEnd()-s.getBegin()));
		if (quoteMatcherBefore.find()) {
			return true;
		}
		if (quoteMatcherAfter.find()) {
			return true;
		}
		return false;
	}
}
