package de.unidue.ltl.edu.scoring.features.essay.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

/**
 * counts the appearance of subordinate clauses. Therefore each penntree is
 * checked for 'S' constituents. If the constituent has a causal indicators at
 * the first position considered as a causal clause.
 * 
 * @author Michael
 * 
 */
@TypeCapability(inputs = {
		"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class SubordinateClauseDFE 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	private List<String> causalIndicators;
	private List<String> temporalIndicators;

	public static final String NR_OF_SUBORDINATECLAUSES = "nrOfSubordinateClauses";
	public static final String NR_OF_CAUSALCLAUSES = "nrOfCausalClauses";
	public static final String NR_OF_TEMPORALCLAUSES = "nrOfTemporalClauses";
	
	public static final String PARAM_CAUSAL_INDICATORS_FILE_PATH = "causalIndicatorsFilePath";
    @ConfigurationParameter(name = PARAM_CAUSAL_INDICATORS_FILE_PATH, mandatory = true)
    private File causalIndicatorsFilePath;

    public static final String PARAM_TEMPORAL_INDICATORS_FILE_PATH = "temporalIndicatorsFilePath";
    @ConfigurationParameter(name = PARAM_TEMPORAL_INDICATORS_FILE_PATH, mandatory = true)
    private File temporalIndicatorsFilePath;
    
	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		causalIndicators = getIndicatorsList(causalIndicatorsFilePath);
		temporalIndicators=getIndicatorsList(temporalIndicatorsFilePath);
		
		return true;
	}

	private List<String> getIndicatorsList(File dictionary)
			throws ResourceInitializationException
	{
		try {
			List<String> list = new ArrayList<String>();
			for (String line : FileUtils.readLines(dictionary)) {
				list.add(line);
			}
			return list;
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{

		double nrOfSubordinateClauses = 0;
		double nrOfCausalClauses = 0;
		double nrOfTemporalClauses = 0;
		Collection<PennTree> trees = JCasUtil.select(jcas, PennTree.class);
		// foreach penntree

		for (PennTree tree : trees) {
			Collection<Constituent> constituents = JCasUtil.selectCovered(
					Constituent.class, tree);
			boolean firstSentence = true;
			for (Constituent constituent : constituents) {
				// foreach S constituent
				if (constituent.getConstituentType().equals("S")) {

					// counts each S in the Tree
					nrOfSubordinateClauses++;

					// the first S is the whole sentence
					// TODO sometimes the first clause isn't tagged with S
					if (firstSentence) {
						firstSentence = false;
					} else {
						Collection<Token> tokens = JCasUtil.selectCovered(
								Token.class, constituent);
						// get the first word in the S
						String firstToken = tokens.iterator().next().getCoveredText().toLowerCase();

						// check if the first word in constituent is a
						// causalIndicator
						if (causalIndicators.contains(firstToken)) {
							nrOfCausalClauses++;
						}
						// check if the first word in constituent is a
						// temporalIndicator
						if (temporalIndicators.contains(firstToken)) {
							nrOfTemporalClauses++;
						}
					}
				}
			}
			// minus 1 because the main-clauses should not be counted
			nrOfSubordinateClauses--;
		}
		//Normalization on total count of trees
		nrOfSubordinateClauses = (double) nrOfSubordinateClauses / trees.size();
		nrOfCausalClauses = (double) nrOfCausalClauses / trees.size();
		nrOfTemporalClauses=(double)nrOfTemporalClauses/trees.size();

		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(NR_OF_SUBORDINATECLAUSES,
				nrOfSubordinateClauses));
		featList.add(new Feature(NR_OF_CAUSALCLAUSES, nrOfCausalClauses));
		featList.add(new Feature(NR_OF_TEMPORALCLAUSES, nrOfTemporalClauses));
		return featList;
	}
}
