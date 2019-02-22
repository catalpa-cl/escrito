package de.unidue.ltl.escrito.core.clustering;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.core._eka;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
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



	public static ConditionalFrequencyDistribution<Integer, String> writeClusterAssignments(TaskContext aContext,
			int numClusters, 
			List<String> trainOutcomeValues, 
			Instances copyTrainData,
			Map<Integer, Set<Integer>> clusterMap, 
			Map<String, String> instanceId2TextMap) {
		boolean isMultiPartAnswer = false;
		for  (String key : instanceId2TextMap.keySet()){
			if (instanceId2TextMap.get(key).contains("XXX")){
				isMultiPartAnswer = true;
			}
		}
		// build a table for the cluster assignments
		FlexTable<String> table = FlexTable.forClass(String.class);
		table.setSortRows(false);
		int rowCounter = 0;
		ConditionalFrequencyDistribution<Integer,String> clusterAssignments = new ConditionalFrequencyDistribution<Integer,String>();
		String[] entries = null;
		if (isMultiPartAnswer){
			entries = new String[]{"Beginn der Korrektur:", "_", "_", "BITTE HIER ZEIT EINTRAGEN", "_"};
		} else {
			entries = new String[]{"Beginn der Korrektur:", "", "BITTE HIER ZEIT EINTRAGEN"};
		}
		addRow(table, rowCounter, isMultiPartAnswer, entries);
		rowCounter++;
		addRow(table, rowCounter, isMultiPartAnswer, "");
		rowCounter++;
		for (Integer clusterId : clusterMap.keySet()) {
			System.out.println("CLUSTER: " + clusterId);
			System.out.println(clusterMap.get(clusterId).size()+" entries");
			addRow(table, rowCounter, isMultiPartAnswer, "CLUSTER: " + clusterId);
			rowCounter++;
			// for sorting clusters alphabetically
			Map<String, String> instanceIdToLabel = new HashMap<String, String>();
			Map<String, String> instanceIdToSurfaceForm = new HashMap<String, String>();
			for (Integer offset : clusterMap.get(clusterId)) {

				// get instance ID from instance
				Instance instance = copyTrainData.get(offset);

				Double classOffset = new Double(instance.value(copyTrainData.classAttribute()));
				String label = (String) trainOutcomeValues.get(classOffset.intValue());

				clusterAssignments.addSample(clusterId, label, 1);

				String instanceId = instance.stringValue(copyTrainData.attribute(Constants.ID_FEATURE_NAME).index());
				instanceId = instanceId.substring(instanceId.indexOf("_0_")+3);
				instanceIdToLabel.put(instanceId, label);
				instanceIdToSurfaceForm.put(instanceId, instanceId2TextMap.get(instanceId));
			}

			// sort Map by Value
			Map<String, String> sortedMap = 
					instanceIdToSurfaceForm.entrySet().stream()
					.sorted(Entry.comparingByValue())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue,
							(e1, e2) -> e1, LinkedHashMap::new));

			String lastSurfaceForm = "";
			for (String instanceId : sortedMap.keySet()){
				String label = instanceIdToLabel.get(instanceId);
				String surfaceForm = instanceId2TextMap.get(instanceId);
				if (!surfaceForm.equals(lastSurfaceForm)){
					System.out.println();
					addRow(table, rowCounter, isMultiPartAnswer, "");
					rowCounter++;
				}
				String[] entries3 = null;
				if (surfaceForm.contains("XXX")){
					String[] parts = surfaceForm.split(" XXX ");
					// TODO für mehr Teile ausbauen
					System.out.println(instanceId + "\t" + label + "\t" + parts[0] + "\t" + parts[1]);
					entries3 = new String[]{instanceId, parts[0], "", parts[1], ""};
				} else {
					System.out.println(instanceId + "\t" + label + "\t" + surfaceForm);
					entries3 = new String[]{instanceId, surfaceForm};
				}
				addRow(table, rowCounter, isMultiPartAnswer, entries3);
				rowCounter++;
				lastSurfaceForm = surfaceForm;
			}
			System.out.println("\n");
			addRow(table, rowCounter, isMultiPartAnswer, "");
			rowCounter++;
			addRow(table, rowCounter, isMultiPartAnswer, "");
			rowCounter++;

		}
		String[] entries2 = {"Ende der Korrektur:", "", "BITTE HIER ZEIT EINTRAGEN"};
		addRow(table, rowCounter, isMultiPartAnswer, entries2);
		rowCounter++;
		aContext.storeBinary("cluster_assignments_"+numClusters + ".xls", table.getExcelWriter());
		return clusterAssignments;
	}


	public static void addRow(FlexTable<String> table, int rowCounter, boolean multiPartAnswer, String ... entries) {
		Map<String, String> cells = new LinkedHashMap<String, String>();
		String[] columnIds = null;
		if (!multiPartAnswer){
			columnIds = new String[]{"Id ", "Antwort", "Score"};
		} else {
			columnIds = new String[]{"Id ", "Antwort1", "Score1", "Antwort2", "Score2"};
		}

		for (int i = 0; i<columnIds.length; i++){
			if (i>=entries.length){
				cells.put(columnIds[i], "");
			} else {
				cells.put(columnIds[i], entries[i]);
			}
		}
		table.addRow(String.valueOf(rowCounter), cells);
	}


	public static ConditionalFrequencyDistribution<Integer, String> writeClusterAssignments(TaskContext aContext, 
			int numClusters,
			List<String> trainOutcomeValues, 
			Instances copyTrainData, Map<Integer, 
			Set<Integer>> clusterMap,
			Map<String, String> instanceId2TextMap, 
			Map<Integer, String> labelPerCluster) {
		// build a table for the cluster assignments
		FlexTable<String> table = FlexTable.forClass(String.class);
		table.setSortRows(false);
		int rowCounter = 0;
		ConditionalFrequencyDistribution<Integer,String> clusterAssignments = new ConditionalFrequencyDistribution<Integer,String>();
		//				String[] entries = {"Beginn der Korrektur:", "", "BITTE HIER ZEIT EINTRAGEN"};
		//				addRow(table, rowCounter, entries);
		rowCounter++;
		addRow(table, rowCounter, false, "");
		rowCounter++;
		for (Integer clusterId : clusterMap.keySet()) {
			System.out.println("CLUSTER: " + clusterId);
			System.out.println(clusterMap.get(clusterId).size()+" entries");
			addRow(table, rowCounter, false, "CLUSTER: " + clusterId);
			rowCounter++;
			// for sorting clusters alphabetically
			Map<String, String> instanceIdToLabel = new HashMap<String, String>();
			Map<String, String> instanceIdToSurfaceForm = new HashMap<String, String>();
			for (Integer offset : clusterMap.get(clusterId)) {

				// get instance ID from instance
				Instance instance = copyTrainData.get(offset);

				Double classOffset = new Double(instance.value(copyTrainData.classAttribute()));
				String label = (String) trainOutcomeValues.get(classOffset.intValue());

				clusterAssignments.addSample(clusterId, label, 1);

				String instanceId = instance.stringValue(copyTrainData.attribute(Constants.ID_FEATURE_NAME).index());
				instanceId = instanceId.substring(instanceId.indexOf("_0_")+3);
				instanceIdToLabel.put(instanceId, label);
				instanceIdToSurfaceForm.put(instanceId, instanceId2TextMap.get(instanceId));
			}

			// sort Map by Value
			Map<String, String> sortedMap = 
					instanceIdToSurfaceForm.entrySet().stream()
					.sorted(Entry.comparingByValue())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue,
							(e1, e2) -> e1, LinkedHashMap::new));

			String lastSurfaceForm = "";
			for (String instanceId : sortedMap.keySet()){
				String label = instanceIdToLabel.get(instanceId);
				String surfaceForm = instanceId2TextMap.get(instanceId);
				if (!surfaceForm.equals(lastSurfaceForm)){
					System.out.println();
					addRow(table, rowCounter, false, "");
					rowCounter++;
				}
				String classLabel = labelPerCluster.get(clusterId);
				System.out.println(instanceId + "\t" + label + "\t" + surfaceForm + "\t" + classLabel);
				String[] entries3 = {instanceId, surfaceForm, classLabel};
				addRow(table, rowCounter, false, entries3);
				rowCounter++;
				lastSurfaceForm = surfaceForm;
			}
			System.out.println("\n");
			addRow(table, rowCounter, false, "");
			rowCounter++;
			addRow(table, rowCounter, false, "");
			rowCounter++;

		}
		//				String[] entries2 = {"Ende der Korrektur:", "", "BITTE HIER ZEIT EINTRAGEN"};
		//				addRow(table, rowCounter, entries2);
		rowCounter++;
		aContext.storeBinary("cluster_assignments_"+numClusters + ".xls", table.getExcelWriter());
		return clusterAssignments;		
	}



}
