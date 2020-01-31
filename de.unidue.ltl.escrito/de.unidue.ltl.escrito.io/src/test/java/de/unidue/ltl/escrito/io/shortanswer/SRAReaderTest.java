package de.unidue.ltl.escrito.io.shortanswer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class SRAReaderTest {
	/*
	 * Test that we read 5 questions(2 of BeetleDummy, 3 of SciEntsBankDummy), 5 target answers and 30 student answers.
	 */
	@Test
	public void ReaderTest() throws Exception {

		String inputDataFile = "src/test/resources/shortanswer/sra/train/";
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				SRAReader.class,
				SRAReader.PARAM_INPUT_FILE, inputDataFile,
				SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
                SRAReader.PARAM_CORPUSNAME,"SRA",
				SRAReader.PARAM_QUESTION_PREFIX, "Q",
				SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
				);

		int i=0;
		for (JCas jcas : new JCasIterable(reader)) {
			//System.out.println(i+"\t"+jcas.getDocumentText().length());
			i++;
		}
		System.out.println(i+" documents");
		assertEquals(30, i);
	}
	/*
	 * Test that we read specific one prompt.
	 */
	@Test
    public void promptSetIdTest() throws Exception {
		String inputDataFile = "src/test/resources/shortanswer/sra/test/";
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                SRAReader.class,
                SRAReader.PARAM_INPUT_FILE, inputDataFile,
                SRAReader.PARAM_PREPROCESSING_OF_CONNECTED_TEXTS, false,
                SRAReader.PARAM_CORPUSNAME,"SRA",
                SRAReader.PARAM_PROMPT_SET_ID, "MX_1",
				SRAReader.PARAM_QUESTION_PREFIX, "Q",
				SRAReader.PARAM_TARGET_ANSWER_PREFIX, "TA"
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertTrue(jcas.getDocumentText().length() <150 );
            i++;
        }
        System.out.println(i);
        assertEquals(2, i);
    }

}
