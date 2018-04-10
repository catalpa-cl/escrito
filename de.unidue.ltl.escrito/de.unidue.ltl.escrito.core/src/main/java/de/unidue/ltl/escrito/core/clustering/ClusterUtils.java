package de.unidue.ltl.escrito.core.clustering;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dkpro.tc.ml.weka.core._eka;

import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ClusterUtils {


	public static Instances removeInstanceIdAndClassLabel(Instances trainData, boolean multiLabel) throws Exception {
		trainData = _eka.removeInstanceId(trainData, multiLabel);

		// generate data for clusterer (w/o class)
		Remove filter = new Remove();
		filter.setAttributeIndices("" + (trainData.classIndex() + 1));
		filter.setInputFormat(trainData);
		Instances clusterTrainData = Filter.useFilter(trainData, filter);
		return clusterTrainData;
	}


	/**
	 * Returns a mapping from cluster IDs to instance offsets
	 * @return
	 */
	public static Map<Integer, Set<Integer>> getClusterMap(Instances data, Clusterer clusterer)
			throws Exception
	{
		Map<Integer, Set<Integer>> clusterMap = new HashMap<Integer, Set<Integer>>();

		@SuppressWarnings("rawtypes")
		Enumeration instanceEnumeration = data.enumerateInstances();
		int instanceOffset = 0;
		while (instanceEnumeration.hasMoreElements()) {
			Instance instance = (Instance) instanceEnumeration.nextElement();
			double[] distribution = clusterer.distributionForInstance(instance);
			int clusterId = 0;
			for (double value : distribution) {
				if (new Double(value).intValue() == 1) {
					Set<Integer> clusterInstances;
					if (!clusterMap.containsKey(clusterId)) {
						clusterInstances = new HashSet<Integer>();
						clusterMap.put(clusterId, clusterInstances);
					}
					clusterInstances = clusterMap.get(clusterId);
					clusterInstances.add(instanceOffset);
					clusterMap.put(clusterId, clusterInstances);
				}
				clusterId++;
			}
			instanceOffset++;
		}
		return clusterMap;
	}

}
