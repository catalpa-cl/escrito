package de.unidue.ltl.escrito.io.shortanswer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

public class Asap2ReaderTest {


	@Test
	public void powerGradingReaderTest() throws Exception {

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				Asap2Reader.class,
				Asap2Reader.PARAM_INPUT_FILE, "src/test/resources/shortanswer/asap2/asap2_dummy.tsv"
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertTrue(jcas.getDocumentText().length() < 500);
			i++;
		}
		assertEquals(5, i);
	}

	@Test 
	public void promptIdTest() throws Exception {

		Integer[] requestedPromptIds = {1, 2};
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				Asap2Reader.class,
				Asap2Reader.PARAM_INPUT_FILE, "src/test/resources/shortanswer/asap2/asap2_dummy.tsv",
				Asap2Reader.PARAM_PROMPT_IDS, requestedPromptIds
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertTrue(jcas.getDocumentText().length() < 500);
			i++;
		}
		assertEquals(3, i);
	}


	// only test locally
	@Test @Ignore
	public void tempTest() throws Exception {

		Integer[] requestedPromptIds = {1,2,3,4,5,6,7,8,9,10};
		for (int id : requestedPromptIds){
			System.out.println(id);
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					Asap2Reader.class,
					Asap2Reader.PARAM_INPUT_FILE, "/Users/andrea/dkpro/datasets/asap/originalData/train.tsv",
					Asap2Reader.PARAM_PROMPT_IDS, id
					);
			int i = 0;
			for (JCas jcas : new JCasIterable(reader)) {
				assertTrue(jcas.getDocumentText().length() < 5000);
				i++;
			}
		}
		assertEquals(3, 3);
	}

}
