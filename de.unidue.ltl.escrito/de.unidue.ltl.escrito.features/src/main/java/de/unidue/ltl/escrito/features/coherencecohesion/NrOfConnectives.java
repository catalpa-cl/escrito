package de.unidue.ltl.escrito.features.coherencecohesion;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

/**
 * Counts the appearance of the specified connectives, checks if they appear at
 * first position of a S in a penntree constituent
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", 
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"})
public class NrOfConnectives extends FeatureExtractorResource_ImplBase
		implements FeatureExtractor {

	public static final String NR_OF_CONNECTIVES = "nrOfConnectives";

	public static final String PARAM_CONNECTIVES_FILE_PATH = "connectivesFilePath";
    @ConfigurationParameter(name = PARAM_CONNECTIVES_FILE_PATH, mandatory = true)
    private String connectivesFilePath;

	private List<String> connectives;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		connectives = getConnectives(connectivesFilePath);
		return true;
	}

	private List<String> getConnectives(String connectivesFile) {
		List<String> list = new ArrayList<String>();
		Scanner s;
		try {
			s = new Scanner(new File(connectivesFile));
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();
			System.out.println("Read "+list.size()+" connective files from "+connectivesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return list;
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target) 
			throws TextClassificationException 
	{

		double nrOfConnectives = 0;
		int n = 0;
		for (Lemma lemma : JCasUtil.select(jcas, Lemma.class)) {
			if (connectives.contains(lemma.getValue().toLowerCase())) {
				nrOfConnectives++;
			}
			n++;
		}
		double ratio = (double) nrOfConnectives / n;
		return new Feature(NR_OF_CONNECTIVES, ratio, FeatureType.NUMERIC).asSet();
	}
	/**
	 * for testing only
	 * @param connectivesFilePath
	 */
	public void init(String connectivesFilePath){
		connectives = getConnectives(connectivesFilePath);
	}
}
