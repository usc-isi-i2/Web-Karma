package edu.isi.karma.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Util {
	/**
	 * Sorts a HashMap based on the values with Double data type
	 * 
	 * @param input
	 * @return
	 */
	public static HashMap<String, Double> sortHashMap(HashMap<String, Double> input) {
		Map<String, Double> tempMap = new HashMap<String, Double>();
		for (String wsState : input.keySet()) {
			tempMap.put(wsState, input.get(wsState));
		}

		List<String> mapKeys = new ArrayList<String>(tempMap.keySet());
		List<Double> mapValues = new ArrayList<Double>(tempMap.values());
		HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
		Object[] sortedArray = sortedSet.toArray();
		
		int size = sortedArray.length;
		for (int i = 0; i < size; i++) {
			sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
					(Double) sortedArray[i]);
		}
		return sortedMap;
	}
}
