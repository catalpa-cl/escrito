package de.unidue.ltl.escrito.examples.preprocessing;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class Analyzer extends JCasAnnotator_ImplBase {


	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String id = "no Id";
		if (JCasUtil.exists(aJCas, DocumentMetaData.class)){
			DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
			id = meta.getDocumentId();
		}
		System.out.println("Printing essay "+id+": "
				+ JCasUtil.select(aJCas, Sentence.class).size()+ " sentences, "
				+ JCasUtil.select(aJCas, Token.class).size()+ " tokens, "
				+ JCasUtil.select(aJCas, Lemma.class).size()+ " lemmata, "
				+ JCasUtil.select(aJCas, Chunk.class).size()+ " chunks, "
				+ JCasUtil.select(aJCas, PennTree.class).size()+ " trees, "
				+ JCasUtil.select(aJCas, Constituent.class).size()+ " constituents, "
				+ JCasUtil.select(aJCas, Dependency.class).size()+ " dependencies, "				
				+ JCasUtil.select(aJCas, GrammarAnomaly.class).size()+ " grammar anomalies."
				);

		//				for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)){
		//					System.out.println("Sentence: "+sentence.getCoveredText()+"X"+sentence.getCoveredText().length());
		//				}
		//				
		//				for (Token token : JCasUtil.select(aJCas, Token.class)){
		//					System.out.println(token.getCoveredText() +  " "+ token.getPos().getPosValue() + " "+ token.getLemma().getValue());
		//				}
		//				
		//				for (Chunk chunk : JCasUtil.select(aJCas, Chunk.class)){
		//					System.out.println(chunk.getCoveredText() + " "+ chunk.getChunkValue());
		//				}
		//				
//						for (PennTree penntree : JCasUtil.select(aJCas, PennTree.class)){
//							System.out.println("TREE: "+penntree.toString());
//						}
		//				
						for (Dependency dep : JCasUtil.select(aJCas, Dependency.class)){
							System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType().toString() + " " + dep.getDependent().getCoveredText());
						}
		//				
		//				for (GrammarAnomaly ga : JCasUtil.select(aJCas, GrammarAnomaly.class)){
		//					System.out.println(ga.getCoveredText()+ ": "+ ga.getCategory()+ " - "+ga.getDescription());
		//				}

	}


}
