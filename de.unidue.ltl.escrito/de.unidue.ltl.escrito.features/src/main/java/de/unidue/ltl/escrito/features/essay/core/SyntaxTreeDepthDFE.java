package de.unidue.ltl.edu.scoring.features.essay.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

/**
 * Calculates the average and total depths of berkley parsing trees via counting
 * underlying constituents
 */
@TypeCapability(inputs = { "import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree" })
public class SyntaxTreeDepthDFE 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String AVG_SYNTAX_TREE_DEPTH = "syntaxTreeDepthAvg";
	public static final String TOTAL_SYNTAX_TREE_DEPTH = "syntaxTreeDepthMax";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException
	{
		double totalTreeDepth = 0;

		Collection<PennTree> trees = JCasUtil.select(jcas, PennTree.class);
		// check every penntree for the root element and calculate the depth of
		// the tree from there
		for (PennTree tree : trees) {
			Collection<Constituent> constituents = JCasUtil.selectCovered(
					Constituent.class, tree);
			for (Constituent constituent : constituents) {
				if (constituent.getConstituentType().equals("ROOT")) {
					totalTreeDepth += depthOfTree(constituent);
				}
			}
		}
		//Normalization on total count of trees
		double avgTreeDepth = (double) totalTreeDepth / trees.size();

		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(AVG_SYNTAX_TREE_DEPTH, avgTreeDepth));
		featList.add(new Feature(TOTAL_SYNTAX_TREE_DEPTH, totalTreeDepth));
		return featList;
	}

	// TODO due to the structure of the PennTree for a sentence is min 2 (-->
	// ROOT, PSEUDO, S) !?
	/**
	 * recursive function that adds 1 for every tree-level decides which child
	 * note to take by computing their depths
	 * 
	 * @param constituent
	 *            the root constituent
	 * @return the depth
	 */
	public double depthOfTree(Constituent constituent) {
		if (constituent == null) {
			return 0;
		} else {
			return 1 + maxDepthOfSubtree(constituent.getChildren().toArray());
		}
	}

	private double maxDepthOfSubtree(FeatureStructure[] children) {
		double max = 0;
		for (FeatureStructure child : children) {
			if (!child.getType().getShortName().equals("Token")) {
				double temp = depthOfTree((Constituent) child);
				if (max < temp)
					max = temp;
			}
		}
		return max;
	}
}
