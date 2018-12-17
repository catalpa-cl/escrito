//package de.unidue.ltl.escrito.core.tc.stacking;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.uima.fit.descriptor.ConfigurationParameter;
//import org.apache.uima.fit.util.JCasUtil;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.apache.uima.resource.ResourceSpecifier;
//import org.dkpro.tc.api.exception.TextClassificationException;
//import org.dkpro.tc.api.features.Feature;
//import org.dkpro.tc.api.features.FeatureExtractor;
//import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
//import org.dkpro.tc.api.features.PairFeatureExtractor;
//import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
//import org.dkpro.tc.api.features.meta.MetaDependent;
//import org.dkpro.tc.api.type.TextClassificationTarget;
//
//
//public class StackingFeatureExtractorWrapper
//extends FeatureExtractorResource_ImplBase
//implements FeatureExtractor, MetaDependent
//{
//
//
//
//	public static final String PARAM_STACKING_GROUP_ID = "stackingGroupId";
//	@ConfigurationParameter(name = PARAM_STACKING_GROUP_ID, mandatory = true)
//	protected int stackingGroupId;
//	
//
//	public static final String PARAM_WRAPPED_FEATURE_EXTRACTOR = "embeddedFeatureExtractor";
//	@ConfigurationParameter(name = PARAM_WRAPPED_FEATURE_EXTRACTOR, mandatory = true)
//	protected String embeddedFeatureExtractor;
//	protected FeatureExtractor FE;
//
//
//	@Override
//	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
//			throws ResourceInitializationException {
//		if (!super.initialize(aSpecifier, aAdditionalParams)) {
//			return false;
//		}
//		try {
//			FE = (FeatureExtractor) Class.forName(embeddedFeatureExtractor).newInstance();
//			((FeatureExtractorResource_ImplBase) FE).initialize(aSpecifier, aAdditionalParams);
//		} catch (InstantiationException | IllegalAccessException
//				| ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return true;
//	}
//
//
//
//	@Override
//	public Set<Feature> extract(JCas arg0, TextClassificationTarget arg1) throws TextClassificationException {
//		Set<Feature> features = FE.extract(arg0, arg1);
//		Set<Feature> stackingFeatures = new HashSet<Feature>();
//		for (Feature feature : features){
//			StackingFeature stackingFeature = new StackingFeature(feature.getName(), feature.getValue(), stackingGroupId);
//			stackingFeatures.add(stackingFeature);
//		}
//		return stackingFeatures;
//	}
//	
//
//	@Override
//	public List<MetaCollectorConfiguration> getMetaCollectorClasses(
//			Map<String, Object> parameterSettings)
//					throws ResourceInitializationException {
//		try {
//			FeatureExtractor fe = (FeatureExtractor) Class.forName((String) parameterSettings.get(PARAM_WRAPPED_FEATURE_EXTRACTOR)).newInstance();
//			if( fe instanceof MetaDependent){
//				return ((MetaDependent) fe).getMetaCollectorClasses(parameterSettings);
//			} else {
//				return new ArrayList<>();
//			}
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return new ArrayList<>();
//	}
//}
//
//
