package de.unidue.ltl.escrito.examples.models;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.weka.core.WekaPredictor;

public class PretrainedModelExample {

	/*
	 * Example that shows how to apply a stored model for ASAP prompt 1
	 */
	
	static File modelFileAsap1 = new File("target/pretrainedModels/Asap1.model");	
	static File luceneFolderAsap1 = new File("target/pretrainedModels/luceneFolderASAP1");
	

	public static void main(String[] args) throws Exception{
		String exampleAnswer = "You need to know the amount of vinegar";
		String score = predictScore(exampleAnswer);
		System.out.println("Score: "+score);

	}

	private static String predictScore(String answer) throws Exception {
		// store each new answer in a separate file for further processing
		String test = "target/newAnswers";
		new File(test).mkdirs();
		FileUtils.cleanDirectory(new File(test));
        String outputPathTest = "target/tn_raw_output/test";
        File answerFile = new File("target/newAnswers/answer_"+System.currentTimeMillis()+".txt");
        FileUtils.writeStringToFile(answerFile, answer);
        
        ensureFolderExistence(outputPathTest);
        Object[] ngramParameter = new Object[] { WordNGram.PARAM_NGRAM_USE_TOP_K, "10000",
                WordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123", WordNGram.PARAM_SOURCE_LOCATION,
                luceneFolderAsap1.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
                luceneFolderAsap1.toString() };
        // run FeatureExtraction is shared between model training and model application
        File extractedTestData = Utils.runFeatureExtractionAsap(test, ngramParameter,
                outputPathTest, false, 1); 
        
        //We always predict only 1 score, so we return the first (and only) one in the list
        TcPredictor predictor = new WekaPredictor();
        List<String> predictions = predictor.predict(extractedTestData, modelFileAsap1);
        for (String pred : predictions){
        	System.out.println(pred);
        }
        return predictions.get(0);
	}




	private static void ensureFolderExistence(String outputPathTrain)
	{
		File file = new File(outputPathTrain);
		if (file.exists()) {
			return;
		}
		boolean creationSuccessful = file.mkdirs();
		if (!creationSuccessful) {
			throw new IllegalStateException(
					"Could not create the folder path [" + file.getAbsolutePath() + "]");
		}
	}




}
