package de.unidue.ltl.escrito.core.tc.stacking;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Stacking;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/*
 * Main feature of this class: some of the features in the data can be left unstacked
 * 
 */


public class FeatureStacking extends Stacking{

	private Integer[] unstackedFeatures = {};

	public Integer[] getUnstackedFeatures() {
		return unstackedFeatures;
	}

	public void setUnstackedFeatures(Integer[] unstackedFeatures2) {
		this.unstackedFeatures = unstackedFeatures2;
	}



	//TODO overwrite String representation


	/**
	 * Makes the format for the level-1 data.
	 *
	 * @param instances the level-0 format
	 * @return the format for the meta data
	 * @throws Exception if the format generation fails
	 */
	@Override
	protected Instances metaFormat(Instances instances) throws Exception {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		Instances metaFormat;

		for (int k = 0; k < m_Classifiers.length; k++) {
			Classifier classifier = (Classifier) getClassifier(k);
			System.out.println(classifier.getClass().getName());
			String name = classifier.getClass().getName() + "-" + (k+1);
			if (m_BaseFormat.classAttribute().isNumeric()) {
				attributes.add(new Attribute(name));
			} else {
				for (int j = 0; j < m_BaseFormat.classAttribute().numValues(); j++) {
					attributes.add(
							new Attribute(
									name + ":" + m_BaseFormat.classAttribute().value(j)));
				}
			}
		}
		// add the unstacked features to the format
		for (int i = 0; i<this.unstackedFeatures.length; i++){
			attributes.add(instances.attribute(unstackedFeatures[i]));
		}
		attributes.add((Attribute) m_BaseFormat.classAttribute().copy());
		System.out.println(attributes.toString());
		metaFormat = new Instances("Meta format", attributes, 0);
		metaFormat.setClassIndex(metaFormat.numAttributes() - 1);
		return metaFormat;
	}



	/**
	 * Makes a level-1 instance from the given instance.
	 * 
	 * @param instance the instance to be transformed
	 * @return the level-1 instance
	 * @throws Exception if the instance generation fails
	 */
	@Override
	protected Instance metaInstance(Instance instance) throws Exception {

		double[] values = new double[m_MetaFormat.numAttributes()];
		Instance metaInstance;
		int i = 0;
		for (int k = 0; k < m_Classifiers.length; k++) {
			Classifier classifier = getClassifier(k);
			if (m_BaseFormat.classAttribute().isNumeric()) {
				values[i++] = classifier.classifyInstance(instance);
			} else {
				double[] dist = classifier.distributionForInstance(instance);
				for (int j = 0; j < dist.length; j++) {
					values[i++] = dist[j];
				}
			}
		}
		for (int j = 0; j<this.unstackedFeatures.length; j++){
			values[i++] = instance.value(this.unstackedFeatures[j]);
		}
		
		values[i] = instance.classValue();
		metaInstance = new DenseInstance(1, values);
		metaInstance.setDataset(m_MetaFormat);
	//	System.out.println("\nold instance: "+instance);
	//	System.out.println("new instance:  "+metaInstance);
		return metaInstance;
	}

	
	 /**
	   * Returns combined capabilities of the base classifiers, i.e., the
	   * capabilities all of them have in common.
	   *
	   * @return      the capabilities of the base classifiers
	   */
	  public Capabilities getCapabilities() {
		  return this.m_MetaClassifier.getCapabilities();
	 //   return super.getCapabilities();
	  }
	
	
	
}
