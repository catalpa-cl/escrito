package de.unidue.ltl.escrito.io.shortanswer;


import static org.junit.Assert.*;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;



public class CregReaderTest
{


	/*
	 * Test that we read 1032 learner answers overall
	 */
	@Test 
	public void cregReaderTestGesamt() throws Exception {

		String inputDataFile = System.getenv("DKPRO_HOME")+"/datasets/CREG/CREG-1032/";
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				CregReader.class,
				CregReader.PARAM_INPUT_FILE, inputDataFile,
				CregReader.PARAM_CORPUSNAME, "CREG",
				CregReader.PARAM_QUESTION_PREFIX, "Q",
				CregReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
		//	System.out.println(i+"\t"+jcas.getDocumentText().length());
			i++;
		}
		System.out.println(i+" documents");
		assertEquals(1032, i);
	}
	
	/*
	 * Test that we read 610 learner answers
	 */
	@Test 
	public void cregReaderTest() throws Exception {

		String inputDataFile = System.getenv("DKPRO_HOME")+"/datasets/CREG/CREG-1032/KU-data.xml";
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				CregReader.class,
				CregReader.PARAM_INPUT_FILE, inputDataFile,
				CregReader.PARAM_CORPUSNAME, "CREG",
				CregReader.PARAM_QUESTION_PREFIX, "Q",
				CregReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
		//	System.out.println(i+"\t"+jcas.getDocumentText().length());
			i++;
		}
		System.out.println(i+" documents");
		assertEquals(610, i);
	}

	
	/*
	 * Test that we read 422 learner answers
	 */
	@Test 
	public void cregReaderTest2() throws Exception {

		String inputDataFile = System.getenv("DKPRO_HOME")+"/datasets/CREG/CREG-1032/OSU-data.xml";
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				CregReader.class,
				CregReader.PARAM_INPUT_FILE, inputDataFile,
				CregReader.PARAM_CORPUSNAME, "CREG",
				CregReader.PARAM_QUESTION_PREFIX, "Q",
				CregReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
		//	System.out.println(i+"\t"+jcas.getDocumentText().length());
			i++;
		}
		System.out.println(i+" documents");
		assertEquals(422, i);
	}
}
