package de.unidue.ltl.escrito.io.generic;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.junit.Test;

import de.unidue.ltl.escrito.generic.GenericDatasetReader;


public class GenericReaderTest {
	@Test 
    public void genericReaderTest_utf8() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                GenericDatasetReader.class,
                GenericDatasetReader.PARAM_INPUT_FILE, "src/test/resources/generic/genericDataset_utf8.tsv",
                GenericDatasetReader.PARAM_IGNORE_FIRST_LINE, false,
                GenericDatasetReader.PARAM_ENCODING, "UTF-8",
                GenericDatasetReader.PARAM_LANGUAGE, "de",
                GenericDatasetReader.PARAM_SEPARATOR, "\t"
        );
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            i++;
        }
        assertEquals(5, i);
        
        
        
        reader = CollectionReaderFactory.createReaderDescription(
                GenericDatasetReader.class,
                GenericDatasetReader.PARAM_INPUT_FILE, "src/test/resources/generic/genericDataset_utf8.csv",
                GenericDatasetReader.PARAM_IGNORE_FIRST_LINE, false,
                GenericDatasetReader.PARAM_ENCODING, "UTF-8",
                GenericDatasetReader.PARAM_LANGUAGE, "de",
                GenericDatasetReader.PARAM_SEPARATOR, ","
        );

        i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            i++;
        }
        assertEquals(5, i);
    }
	
	
	@Test 
    public void genericReaderTest_latin1() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                GenericDatasetReader.class,
                GenericDatasetReader.PARAM_INPUT_FILE, "src/test/resources/generic/genericDataset_latin1.tsv",
                GenericDatasetReader.PARAM_IGNORE_FIRST_LINE, false,
                GenericDatasetReader.PARAM_ENCODING, "ISO-8859-1",
                GenericDatasetReader.PARAM_LANGUAGE, "de",
                GenericDatasetReader.PARAM_SEPARATOR, "\t"
        );
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            i++;
        }
        assertEquals(5, i);
        
        
        
        reader = CollectionReaderFactory.createReaderDescription(
                GenericDatasetReader.class,
                GenericDatasetReader.PARAM_INPUT_FILE, "src/test/resources/generic/genericDataset_latin1.csv",
                GenericDatasetReader.PARAM_IGNORE_FIRST_LINE, false,
                GenericDatasetReader.PARAM_ENCODING, "ISO-8859-1",
                GenericDatasetReader.PARAM_LANGUAGE, "de",
                GenericDatasetReader.PARAM_SEPARATOR, ","
        );

        i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            i++;
        }
        assertEquals(5, i);
    }
	
	
}
