package de.unidue.ltl.escrito.examples.models;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;

public class JCasIdSetter
    extends JCasAnnotator_ImplBase
{

    int jcasId;

    @Override
    public void process(JCas arg0) throws AnalysisEngineProcessException
    {
        boolean exists = JCasUtil.exists(arg0, JCasId.class);
        if (!exists) {
            JCasId id = new JCasId(arg0);
            id.setId(jcasId++);
            id.addToIndexes();
        }

    }

}