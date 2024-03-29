package de.unidue.ltl.escrito.features.similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.unidue.ltl.escrito.core.IoUtils;
import de.unidue.ltl.escrito.core.types.LearnerAnswerWithReferenceAnswer;



public class PairwiseFeatureWrapper 
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor, MetaDependent
{


	// determines how we aggregate the final value of a feature if several target answers are available
	// options: 
	//    * maximum for similarities
	//    * minimum (for distances), 
	//    * average, 
	//    * singleFeature (i.e. every reference answer option triggers n features, not just the best one)


	public static final String PARAM_ADDITIONAL_TEXTS_LOCATION = "locationOfAdditionalTexts";
	@ConfigurationParameter(name = PARAM_ADDITIONAL_TEXTS_LOCATION, mandatory = true)
	protected String locationOfAdditionalTexts;

	public static final String PARAM_TARGET_ANSWER_PREFIX = "TargetAnswerPrefix";
	@ConfigurationParameter(name = PARAM_TARGET_ANSWER_PREFIX, mandatory = true)
	protected String targetAnswerPrefix;

	public static final String PARAM_PROMPTNAME = "promptName";
	@ConfigurationParameter(name = PARAM_PROMPTNAME, mandatory = true)
	protected String promptName;

	public static final String PARAM_PAIRWISE_FEATURE_EXTRACTOR = "embeddedPairwiseFeatureExtractor";
	@ConfigurationParameter(name = PARAM_PAIRWISE_FEATURE_EXTRACTOR, mandatory = true)
	protected String embeddedPairwiseFeatureExtractor;
	protected PairFeatureExtractor PFE;
	protected LRUCache<String, JCas> comparisonViewCache;

	public static final String PARAM_AGGREGATION_METHOD = "aggregationMethod";
	@ConfigurationParameter(name = PARAM_AGGREGATION_METHOD, mandatory = true, defaultValue = ConfigurationParameter.NO_DEFAULT_VALUE)
	protected AggregationMethod aggregationMethod;
	
	public static final String PARAM_NUMBER_TARGETANSWERS = "numberOfTargetAnswers";
	@ConfigurationParameter(name = PARAM_NUMBER_TARGETANSWERS, mandatory = false, defaultValue = "1")
	protected int numberOfTargetAnswers;

	public enum AggregationMethod {
		MAXIMUM, MINIMUM, AVERAGE, INDIVIDUAL_FEATURES,   
	}



	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}
		try {
			PFE = (PairFeatureExtractor) Class.forName(embeddedPairwiseFeatureExtractor).newInstance();
			((FeatureExtractorResource_ImplBase) PFE).initialize(aSpecifier, aAdditionalParams);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		comparisonViewCache = new LRUCache<String, JCas>(50);
		return true;
	}






	// multiple target answers are to be handled here
	@Override
	public Set<Feature> extract(JCas view, TextClassificationTarget target)
			throws TextClassificationException
	{
		Set<Feature> featuresForOneReferenceAnswer = null;
		Set<Feature> featureForOnereferenceAnswerBest = null;
		Set<Feature> featureForAllReferenceAnswers = new HashSet<Feature>();
//		System.out.println(promptName);
//		System.out.println(aggregationMethod);
//		System.out.println(numberOfTargetAnswers);
		
		if (!(JCasUtil.exists(view, LearnerAnswerWithReferenceAnswer.class))){
			if(this.aggregationMethod.equals(AggregationMethod.INDIVIDUAL_FEATURES)) {
				Set<Feature> features = PFE.extract(view, view);
				Set<Feature> all = new HashSet<Feature>();
				for (Feature f : features) {
					for (int i = 1; i<=numberOfTargetAnswers; i++) {
						all.add(new Feature(f.getName()+"_"+promptName+"_"+i, f.getValue(),f.getType()));
					}
				}
//				System.out.println(all.size());
				return all;
			} else {
				return PFE.extract(view, view);
			}
		}
		
		LearnerAnswerWithReferenceAnswer learnerAnswer = JCasUtil.selectSingle(view, LearnerAnswerWithReferenceAnswer.class);
		JCas comparisonView = null;
		Map<String, List<Feature>> allValuesPerFeature = new HashMap<String, List<Feature>>();

		System.out.println("Found "+learnerAnswer.getReferenceAnswerIds().size()+" reference answers");
		System.out.println("Aggregation Method: "+this.aggregationMethod);
		for (int index = 0; index < learnerAnswer.getReferenceAnswerIds().size(); index++){
			String refAnsId = learnerAnswer.getReferenceAnswerIds(index);
			System.out.println("Reference answer ID: "+refAnsId);
			if (comparisonViewCache.containsKey(refAnsId)){
				comparisonView = comparisonViewCache.get(refAnsId); 
			} else {
				try {
					comparisonView = IoUtils.loadJCasFromFile(refAnsId, locationOfAdditionalTexts, targetAnswerPrefix);
				} catch (UIMAException | IOException e) {
					throw new TextClassificationException(e);
				}
				comparisonViewCache.put(refAnsId, comparisonView);
			}
			System.out.println("COMPARING "+view.getDocumentText()+" WITH "+comparisonView.getDocumentText());
			featuresForOneReferenceAnswer = PFE.extract(view, comparisonView);
//			System.out.println("featuresFound: "+featuresForOneReferenceAnswer.size());
			for (Iterator<Feature> iter1 = featuresForOneReferenceAnswer.iterator(); iter1.hasNext();){
				Feature feature = iter1.next();
				String featureName = feature.getName();
//				System.out.println("featureName = "+featureName);
				if (this.aggregationMethod.equals(AggregationMethod.MAXIMUM)){
					if(featureForOnereferenceAnswerBest!=null)
						for (Iterator<Feature> iter2 = featureForOnereferenceAnswerBest.iterator(); iter2.hasNext();){
							Feature prevFeature = iter2.next();
							if(prevFeature.getName().equals(featureName)){
								if (feature.getValue() instanceof Integer){
									int maxVal = (int)feature.getValue() > (int)prevFeature.getValue() ? (int)feature.getValue() : (int)prevFeature.getValue();
//									System.out.println(featureName+": val = "+feature.getValue());
//									System.out.println(featureName+": maxVal (int) = "+maxVal);
									feature.setValue(maxVal);
								} else
									if(feature.getValue() instanceof Double){
										double maxVal = (double)feature.getValue() > (double)prevFeature.getValue() ? (double)feature.getValue() : (double)prevFeature.getValue();
//										System.out.println(featureName+": val = "+feature.getValue());
//										System.out.println(featureName+": maxVal (double) = "+maxVal);
										feature.setValue(maxVal);
									}
							}
						} else {
							System.out.println(featureName+": val = "+feature.getValue());
						}
				} else if (this.aggregationMethod.equals(AggregationMethod.MINIMUM)) {
					if(featureForOnereferenceAnswerBest!=null)
						for (Iterator<Feature> iter2 = featureForOnereferenceAnswerBest.iterator(); iter2.hasNext();){
							Feature prevFeature = iter2.next();
							if(prevFeature.getName().equals(featureName)){
								if (feature.getValue() instanceof Integer){
									int minVal = (int)feature.getValue() < (int)prevFeature.getValue() ? (int)feature.getValue() : (int)prevFeature.getValue();
									// System.out.println(featureName+": val = "+feature.getValue());
									// System.out.println(featureName+": minVal (int)= "+minVal);
									feature.setValue(minVal);
								} else
									if(feature.getValue() instanceof Double){
										double minVal = (double)feature.getValue() < (double)prevFeature.getValue() ? (double)feature.getValue() : (double)prevFeature.getValue();
										// System.out.println(featureName+": val = "+feature.getValue());
										// System.out.println(featureName+": minVal (double) = "+minVal);
										feature.setValue(minVal);
									}
							}
						}	else {
							// System.out.println(featureName+": val = "+feature.getValue());
						}
				} else if (this.aggregationMethod.equals(AggregationMethod.AVERAGE)) {
					if (!allValuesPerFeature.containsKey(featureName)){
						allValuesPerFeature.put(featureName, new ArrayList<Feature>());
					}
					allValuesPerFeature.get(featureName).add(feature);
				} else if (this.aggregationMethod.equals(AggregationMethod.INDIVIDUAL_FEATURES)) {
					// make sure that all feature names are unique
					featureForAllReferenceAnswers.add(new Feature(feature.getName()+"_"+refAnsId, feature.getValue(), feature.getType()));
				} else {
					System.err.println("Unknown aggregtion method "+this.aggregationMethod);
					System.exit(-1);
				}
			}
			featureForOnereferenceAnswerBest = featuresForOneReferenceAnswer;
		}
		
		if (true) {
			featureForAllReferenceAnswers.add(new Feature("dummy", 1.0, FeatureType.NUMERIC));
			return(featureForAllReferenceAnswers);
		}
		
		System.out.println("============================");
		if (this.aggregationMethod.equals(AggregationMethod.INDIVIDUAL_FEATURES)){
//			System.out.println("all features: "+featureForAllReferenceAnswers);
//			System.out.println("all features: "+featureForAllReferenceAnswers.size());
			return featureForAllReferenceAnswers;
		} else if (this.aggregationMethod.equals(AggregationMethod.AVERAGE)){
			Set<Feature> averagedFeatures = new HashSet<Feature>();
			for (String featureName : allValuesPerFeature.keySet()){
				double average = getAverage(allValuesPerFeature.get(featureName));
				averagedFeatures.add(new Feature(featureName, average, FeatureType.NUMERIC));
				// System.out.println(allValuesPerFeature.get(featureName));
			}
			// System.out.println(averagedFeatures);
			return averagedFeatures;
		} else {
			// System.out.println(featuresForOneReferenceAnswer);
			return featuresForOneReferenceAnswer;
		}
	}


	private double getAverage(List<Feature> list) {
		double sum = 0.0;
		for (Feature feature : list){
			sum += (double)feature.getValue();
		}
		return sum/list.size();
	}

	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
			throws ResourceInitializationException
	{
		PairFeatureExtractor pfe;
		try {
			pfe = (PairFeatureExtractor) Class.forName((String) parameterSettings.get(PARAM_PAIRWISE_FEATURE_EXTRACTOR)).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new ResourceInitializationException(e);
		}

		if( pfe instanceof MetaDependent){
			return ((MetaDependent) pfe).getMetaCollectorClasses(parameterSettings);
		} else {
			return new ArrayList<>();
		}
	}
}
