package de.unidue.ltl.edu.scoring.features.essay.en;

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
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SpeechThoughtWritingRepresentationDFE_en 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String NR_OF_DIRECT_REPRESENTATION = "nrOfDirectRepresentation";

	public static final String PARAM_REPORTING_VERBS_FILE_PATH = "reportingVerbsFilePath";

	public static final String NR_OF_INDIRECT_REPRESENTATION = "nrOfIndirectRepresentation";

	public static final String NR_OF_REPORTED_REPRESENTATION = "nrOfReportedRepresentation";

	@ConfigurationParameter(name = PARAM_REPORTING_VERBS_FILE_PATH, mandatory = true)
	private String reportingVerbsFilePath;

	private List<String> reportingVerbs;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
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
		// reported representation, which can be a mere mentioning of a speech,
		// thought, or writing act
		double ratioReportedRepresentation = 0;

		// TODO: free indirect representation, which takes characteristics of
		// the
		// character’s voice as well as the narrator’s (‘Well,where would he get
		// something to eat now?’) is missing
		// --> very sparse, hard to detect with rule based systems

		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		// System.out.println("tokens "+JCasUtil.select(jcas,
		// Token.class).size()+ " lemmas "+JCasUtil.select(jcas,
		// Lemma.class).size() );
		for (Token t : tokens) {
			if (reportingVerbs.contains(t.getLemma().getValue())) {

				for (Sentence s : JCasUtil.selectCovering(Sentence.class,
						t.getLemma())) {
					boolean direct = false;
					boolean indirect = false;
					if (containsDirectSpeech(s, t.getLemma())) {
						ratioDirectRepresentation++;
						direct = true;
					}
					if (containsIndirectSpeech(s, t.getLemma())) {
						ratioIndirectRepresentation++;
						indirect = true;
					}
					// if the sentence is neither direct nor inderect we assume
					// reported speech
					if (!direct && !indirect) {
						ratioReportedRepresentation++;
					}
				}
			}
		}
		double numOfSentences = JCasUtil.select(jcas, Sentence.class).size();

		//Normalization on total count of sentences
		ratioDirectRepresentation = ratioDirectRepresentation / numOfSentences;
		ratioIndirectRepresentation = ratioIndirectRepresentation
				/ numOfSentences;
		ratioReportedRepresentation = ratioReportedRepresentation
				/ numOfSentences;

		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(NR_OF_DIRECT_REPRESENTATION,
				ratioDirectRepresentation));
		featList.add(new Feature(NR_OF_INDIRECT_REPRESENTATION,
				ratioIndirectRepresentation));
		featList.add(new Feature(NR_OF_REPORTED_REPRESENTATION,
				ratioReportedRepresentation));
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
	private boolean containsIndirectSpeech(Sentence sentence, Lemma lemma) {
		List<Token> allPos = JCasUtil.selectCovered(Token.class, sentence);
		List<Token> pos = JCasUtil.selectBetween(Token.class, lemma,
				allPos.get(allPos.size() - 1));
		// TODO: how to deal with phrases like "er erklärt ihr, dass" the PRF
		// "ihr" will fool the indices
		// subordinate clause with asking word(how, when, ...) : first token== WRB, last token
		// verb
		if (pos.size() > 2 && pos.get(0).getPos().getPosValue().equals("WRB")
				&& pos.get(pos.size() - 1).getPos().getPosValue().equals("VBZ")) {
			return true;
		}
		// infinitive clause: last token is inifitive verb, the token before is
		// TO
		if (pos.size() > 2
				&& pos.get(pos.size() - 1).getPos().getPosValue().equals("VB")
				&& pos.get(pos.size() - 2).getPos().getPosValue().equals("TO")) {
			return true;
		}
		// subordinate clause in subjunctive mode: second or third token is MD
		// (would, should....)
		if (pos.size() > 2
				&& (pos.get(1).getPos().getPosValue().equals("MD") || pos
						.get(2).getPos().getPosValue().equals("MD"))) {
			return true;
		}
		return false;
	}

	/**
	 * returns 1.0 if the pattern ".*?" is found before or after the given
	 * signalword
	 * 
	 * @param sentence
	 *            s
	 * @param lemma
	 * @return
	 */
	private boolean containsDirectSpeech(Sentence s, Lemma lemma) {
		Pattern quoteRegexPattern = Pattern.compile("\".*?\"");
		Matcher quoteMatcherBefore = quoteRegexPattern
				.matcher(s.getCoveredText().substring(0,
						lemma.getBegin() - s.getBegin()));
		Matcher quoteMatcherAfter = quoteRegexPattern.matcher(s
				.getCoveredText().substring(lemma.getEnd() - s.getBegin()));
		if (quoteMatcherBefore.find()) {
			return true;
		}
		if (quoteMatcherAfter.find()) {
			return true;
		}
		return false;
	}

	/**
	 * for testing only
	 * 
	 * @param reportingVerbsFilePath
	 */
	public void init(String reportingVerbsFilePath) {
		reportingVerbs = getReportingVerbs(reportingVerbsFilePath);
	}
}
