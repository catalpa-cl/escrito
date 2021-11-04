//package de.unidue.ltl.escrito.features.similarity;
//
////import java.util.Collection;
//import java.util.List;
//
//import edu.cmu.lti.jawjaw.pobj.POS;
//import edu.cmu.lti.lexical_db.ILexicalDatabase;
//import edu.cmu.lti.lexical_db.NictWordNet;
//import edu.cmu.lti.lexical_db.data.Concept;
//import edu.cmu.lti.ws4j.Relatedness;
//import edu.cmu.lti.ws4j.RelatednessCalculator;
//import edu.cmu.lti.ws4j.impl.JiangConrath;
//
//
//public class TestWordNetSimilarity {
//
//	private static ILexicalDatabase db = new NictWordNet();
//	private static RelatednessCalculator[] rcs = {
//		//	new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
//		//	new Resnik(db), 
//		new JiangConrath(db)
//		//, new Lin(db), new Path(db)
//	};
//
//	private static void run( String word1, String word2 ) {
//		System.out.println("Comparing "+word1+" and "+word2);
//		// WS4JConfiguration.getInstance().setMFS(true);
//		for ( RelatednessCalculator rc : rcs ) {
//			double s = rc.calcRelatednessOfWords(word1, word2);
//			List<POS[]> posPairs = rc.getPOSPairs();
//			double maxScore = -1D;
//			for(POS[] posPair: posPairs) {
//				List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
//				List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());
//				for(Concept synset1: synsets1) {
//					for (Concept synset2: synsets2) {
//						Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
//						double score = relatedness.getScore();
//						System.out.println(synset1.toString());
//						System.out.println(synset2.toString());
//						if (score >= 1.0){
//							System.out.println("XXX: "+score);
//							score = 1.0;
//						} else {
//							System.out.println(score);
//						}
//						if (score > maxScore) { 
//							maxScore = score;
//						}
//					}
//				}
//			}
//			if (maxScore == -1D) {
//				maxScore = 0.0;
//			}
//			System.out.println( rc.getClass().getName()+"\t"+s+"\t"+maxScore);
//		}
//	}
//	public static void main(String[] args) {
//		long t0 = System.currentTimeMillis();
//		//Collection<Concept> synsets = db.getAllConcepts("house", "n");
//		//Collection<Concept> synsets2 = db.getAllConcepts("home", "n");
//		/*for (Concept synset1 : synsets){
//			System.out.println("Gloss: "+db.getGloss(synset1, ""));
//			System.out.println(synset1.getSynset());
//			for (Concept synset2 : synsets2){
//				System.out.println(synset1.getSynset()+"\t"+synset2.getSynset()+"\t"+(new Resnik(db)).calcRelatednessOfSynset(synset1, synset2).getScore());
//			}
//		}*/
//		run( "act","moderate" );
//		run( "moderate","act" );
//		run( "act","act" );
//		run( "take","need" );
//		long t1 = System.currentTimeMillis();
//		System.out.println( "Done in "+(t1-t0)+" msec." );
//	}
//
//
//
//
//
//}
