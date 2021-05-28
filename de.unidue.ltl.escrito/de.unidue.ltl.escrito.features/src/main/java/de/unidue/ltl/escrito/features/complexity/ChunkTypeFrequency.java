package de.unidue.ltl.escrito.features.complexity;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk",
"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})
public class ChunkTypeFrequency 
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{

	
	
	public static final String FN_NP_FREQ = "numberOfNounPrasesPerSentence";
	public static final String FN_VP_FREQ = "numberOfVerbPrasesPerSentence";
	public static final String FN_PP_FREQ = "numberOfPrepositionalPrasesPerSentence";
	
	
	@Override
	public Set<Feature> extract(JCas aJCas, TextClassificationTarget aTarget) throws TextClassificationException {
		Set<Feature> features = new HashSet<Feature>();
	
		int numPPs = 0;;
		int numVPs = 0;
		int numNPs = 0;
		for (Chunk chunk : JCasUtil.select(aJCas, Chunk.class)) {
			//System.out.println(chunk.getChunkValue()+" - "+chunk.getCoveredText());
			switch (chunk.getChunkValue()){
			case ("NP"): numNPs++; break;
			case ("VP"): numVPs++; break;
			case ("PP"): numPPs++; break;
			default: System.err.println("unknown chunktype "+chunk.getChunkValue());
			}
		}
		int numSentences = JCasUtil.select(aJCas, Sentence.class).size();
		features.add(new Feature(FN_NP_FREQ, (1.0*numNPs/numSentences),FeatureType.NUMERIC));
		features.add(new Feature(FN_VP_FREQ, (1.0*numVPs/numSentences),FeatureType.NUMERIC));
		features.add(new Feature(FN_PP_FREQ, (1.0*numPPs/numSentences),FeatureType.NUMERIC));
		return features;
	}

}
