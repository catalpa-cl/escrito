package de.unidue.ltl.edu.scoring.features.essay.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.NC;

/**
 * counts appearance of core-concepts strings
 * 
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
public class CoreConceptMentionDFE extends FeatureExtractorResource_ImplBase
		implements FeatureExtractor {
	public static final String NR_OF_MENTIONS = "nrOfMentions";

	public static final String PARAM_CORECONCEPTS_FILE_PATH = "conceptsFilePath";
	@ConfigurationParameter(name = PARAM_CORECONCEPTS_FILE_PATH, mandatory = true)
	private File conceptsFilePath;

	// core concept lemmas
	private Set<String> coreConceptsLemmas = new HashSet<String>();

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			coreConceptsLemmas = getCoreConcepts(conceptsFilePath);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		return true;
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException 
	{
		double nrOfMentions = 0;
		for (NC nc : JCasUtil.selectCovered(jcas, NC.class, target)) {
			List<Lemma> lemmas = JCasUtil.selectCovered(jcas, Lemma.class, nc);
			String chunkLemmaString = getLemmatizedString(lemmas);

			for (String coreConcept : coreConceptsLemmas) {
				// contains instead of "equals" check in order to account for
				// articles etc.
				// that might be added to a chunk by the chunker
				if (chunkLemmaString.contains(coreConcept)) {
					nrOfMentions++;
				}
			}
		}

		//Normalization on total count of words
		int nrOfTokens = JCasUtil.select(jcas, Token.class).size();
		double ratio = (double) nrOfMentions / nrOfTokens;

		return new Feature(NR_OF_MENTIONS, ratio).asSet();
	}

	/**
	 * for every token with a POS that is not 'ART' or '$*LRB*' under the given
	 * constituent a lemma is computed. Then the lemmas are joined to one string
	 * 
	 * @param constituent
	 * @param jcas
	 * @return a string of lemmas for a the given constituent
	 */
	private String getLemmatizedString(List<Lemma> lemmas) {
		List<String> lemmaStrings = new ArrayList<>();
		for (Lemma lemma : lemmas) {
			lemmaStrings.add(lemma.getValue().toLowerCase());
		}
		return StringUtils.join(lemmaStrings, " ");
	}

	@SuppressWarnings("resource")
	private Set<String> getCoreConcepts(File conceptsFilePath) 
			throws IOException
	{
		Set<String> set = new HashSet<String>();
		for (String line : FileUtils.readLines(conceptsFilePath)) {
			set.add(line);
		}
		return set;
	}
}
