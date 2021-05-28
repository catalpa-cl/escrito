package de.unidue.ltl.escrito.features.complexity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class SyntacticVariability 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{
	public static final String SYNTAX_TYPE_RATIO_POSLEVEL = "SyntaxTypeRatioPosLevel";
	public static final String SYNTAX_TYPE_RATIO_PHRASELEVEL = "SyntaxTypeRatioPhraseLevel";
	public static final String SYNTAX_TYPE_RATIO_SENTENCELEVEL = "SyntaxTypeRatioSentenceLevel";
	public static final String PAIRWISE_SYNTACTIC_SIMILARITY_PHRASELEVEL = "pairwiseSyntacticSimilarityPhraseLevel";
	public static final String PAIRWISE_SYNTACTIC_SIMILARITY_POSLEVEL = "pairwiseSyntacticSimilarityPosLevel";
	public static final String PAIRWISE_SYNTACTIC_SIMILARITY_SENTENCELEVEL = "pairwiseSyntacticSimilaritySentenceLevel";
	
	public enum Level {
		pos,
		phrase,
		sentence
	}
	
    @Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
        throws TextClassificationException
    {
    	FrequencyDistribution<String> fdPosLevel = new FrequencyDistribution<String>();
        FrequencyDistribution<String> fdPhraseLevel = new FrequencyDistribution<String>();
        FrequencyDistribution<String> fdSentenceLevel = new FrequencyDistribution<String>();

        double similarityPosLevel = 0;
		double similarityPhraseLevel = 0;
		double similaritySentenceLevel = 0;
        
        List<PennTree> trees=new ArrayList<PennTree>(JCasUtil.select(jcas, PennTree.class));
        
        for (int i = 0; i < trees.size(); i++) {
        	fdPhraseLevel.inc(getStringRepresentation(trees.get(i),Level.phrase));
        	fdPosLevel.inc(getStringRepresentation(trees.get(i),Level.pos));
        	fdSentenceLevel.inc(getStringRepresentation(trees.get(i),Level.sentence));
//        	System.out.println(getStringRepresentation(trees.get(i),Level.pos));
//        	System.out.println(getStringRepresentation(trees.get(i),Level.phrase));
//        	System.out.println(getStringRepresentation(trees.get(i),Level.sentence));
    		
        	
        	//compare pairwise
        	if (i < trees.size() - 1) {
        		similarityPhraseLevel+=compare(getStringRepresentation(trees.get(i),Level.phrase),getStringRepresentation(trees.get(i+1),Level.phrase));
        		similarityPosLevel+=compare(getStringRepresentation(trees.get(i),Level.pos),getStringRepresentation(trees.get(i+1),Level.pos));
        		similaritySentenceLevel+=compare(getStringRepresentation(trees.get(i),Level.sentence),getStringRepresentation(trees.get(i+1),Level.sentence));
        	}
        }

        //normalize
        if (trees.size() > 1) {
			// -1 because we count pairs (last sentence has no partner)
        	similarityPhraseLevel = similarityPhraseLevel / (trees.size() - 1);
        	similarityPosLevel = similarityPosLevel / (trees.size() - 1);
        	similaritySentenceLevel = similaritySentenceLevel / (trees.size() - 1);
		}
        
        Set<Feature> featList = new HashSet<Feature>();
        featList.add(new Feature(SYNTAX_TYPE_RATIO_PHRASELEVEL, getRatio(fdPhraseLevel), FeatureType.NUMERIC));
        featList.add(new Feature(SYNTAX_TYPE_RATIO_POSLEVEL, getRatio(fdPosLevel), FeatureType.NUMERIC));
        featList.add(new Feature(SYNTAX_TYPE_RATIO_SENTENCELEVEL, getRatio(fdSentenceLevel), FeatureType.NUMERIC));
        featList.add(new Feature(PAIRWISE_SYNTACTIC_SIMILARITY_PHRASELEVEL, similarityPhraseLevel, FeatureType.NUMERIC));
        featList.add(new Feature(PAIRWISE_SYNTACTIC_SIMILARITY_POSLEVEL, similarityPosLevel, FeatureType.NUMERIC));
        featList.add(new Feature(PAIRWISE_SYNTACTIC_SIMILARITY_SENTENCELEVEL, similaritySentenceLevel, FeatureType.NUMERIC));
        
        return featList;
    }

	private double compare(String stringRepresentation,
			String stringRepresentation2) {
		if(stringRepresentation.equals(stringRepresentation2)){
			return 1.0;
		}
		return 0;
	}

	private double getRatio(FrequencyDistribution<String> fd) {
		//Normalization on total count of words
		if (fd.getN() > 0) {
            return (double) fd.getB() / fd.getN();
        }else{
        	return 0.0;
        }
	}
/**
 * get the string representation of a parsing tree on phrase or token level
 * @param tree
 * @param posLevel
 * @return
 */
	private String getStringRepresentation(PennTree tree, Level level) {
	//	System.out.println(tree);
		List<String> representation=new ArrayList<String>();
		if(level.equals(Level.pos)){
			for (Token t : JCasUtil.selectCovered(
					Token.class, tree)){
	    		representation.add(t.getPos().getPosValue());
	    	}
		}
		else if(level.equals(Level.phrase)){
	    	for (Constituent c : JCasUtil.selectCovered(
					Constituent.class, tree)){
	    		representation.add(c.getConstituentType());
	    	}
		}
		else{
			for (Constituent c : JCasUtil.selectCovered(
					Constituent.class, tree)){
				if (c.getConstituentType().equals("S")) {
					representation.add("S");
				}
	    	}
		}
		return StringUtils.join(representation, " ");
	}
}
