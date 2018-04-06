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
public class SubordinateClauseDFE_en 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	private List<String> causalIndicators;

	public static final String NR_OF_SUBORDINATECLAUSES = "nrOfSubordinateClauses";
	public static final String NR_OF_CAUSALCLAUSES = "nrOfCausalClauses";
	
	public static final String PARAM_CAUSAL_INDICATORS_FILE_PATH = "causalIndicatorsFilePath";
    @ConfigurationParameter(name = PARAM_CAUSAL_INDICATORS_FILE_PATH, mandatory = true)
    private String causalIndicatorsFilePath;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		causalIndicators = getCausalIndicators(causalIndicatorsFilePath);

		return true;
	}

	private List<String> getCausalIndicators(String dictionary) {
		List<String> list = new ArrayList<String>();
		Scanner s;
		try {
			s = new Scanner(new File(dictionary));
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

		double nrOfSubordinateClauses = 0;
		double nrOfCausalClauses = 0;
		Collection<PennTree> trees = JCasUtil.select(jcas, PennTree.class);
		// foreach penntree

		for (PennTree tree : trees) {
			Collection<Constituent> constituents = JCasUtil.selectCovered(
					Constituent.class, tree);
			for (Constituent constituent : constituents) {
				// foreach  SBAR, SBARQ, SINV constituent
				//TODO: no S? --> how to deal with double countings?
				if (constituent.getConstituentType().equals("SBAR")||
						constituent.getConstituentType().equals("SBARQ")||constituent.getConstituentType().equals("SINV")) {
					
					// counts each S in the Tree
					nrOfSubordinateClauses++;
					// the first S is the whole sentence
					// TODO sometimes the first clause isn't tagged with S
						Collection<Token> tokens = JCasUtil.selectCovered(
								Token.class, constituent);
						// get the first word in the S
						Object[] t = tokens.toArray();
						Token firstToken = (Token) t[0];

						// check if the first word in constituent is a
						// causalIndicator
						if (causalIndicators.contains(firstToken
								.getCoveredText().toLowerCase())) {
							nrOfCausalClauses++;
						}
				}
			}
		}
		//Normalization on total count of trees
		nrOfSubordinateClauses = (double) nrOfSubordinateClauses / trees.size();
		nrOfCausalClauses = (double) nrOfCausalClauses / trees.size();

		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(NR_OF_SUBORDINATECLAUSES,
				nrOfSubordinateClauses));
		featList.add(new Feature(NR_OF_CAUSALCLAUSES, nrOfCausalClauses));
		return featList;
	}
	/**
	 * for testing only
	 * @param causalIndicatorsFilePath
	 */
	public void init(String causalIndicatorsFilePath){
		causalIndicators = getCausalIndicators(causalIndicatorsFilePath);
	}
}
