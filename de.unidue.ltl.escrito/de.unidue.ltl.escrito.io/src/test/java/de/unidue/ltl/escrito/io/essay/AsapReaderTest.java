package de.unidue.ltl.escrito.io.essay;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.junit.Ignore;
import org.junit.Test;

import de.unidue.ltl.escrito.io.essay.AsapEssayReader;
import de.unidue.ltl.escrito.io.essay.AsapEssayReader.RatingBias;

public class AsapReaderTest {
	//!note that the values in the test-essays need to be in the range of the essayset (e.g. for asap 1 between 2 and 12)
	//else the normalization won't compute values between 0 and 9!
	@Test @Ignore
    public void asapReaderTestMergedDomains() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                AsapEssayReader.class,
                AsapEssayReader.PARAM_INPUT_FILE, "src/test/resources/essay/asap/asap1",
                AsapEssayReader.PARAM_PATTERNS, "*.xml",
                AsapEssayReader.PARAM_TARGET_LABEL, "score",
                AsapEssayReader.PARAM_ASAP_NUMBER, 1
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            System.out.println(JCasUtil.selectSingle(jcas, TextClassificationOutcome.class));

            i++;
        }
        assertEquals(10, i);
    }
}
