/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;

public class Util {
	/**
	 * Sorts a HashMap based on the values with Double data type
	 * 
	 * @param input
	 * @return
	 */
	public static HashMap<String, Double> sortHashMap(
			HashMap<String, Double> input) {
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
		for (int i = size - 1; i >= 0; i--) {
			sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
					(Double) sortedArray[i]);
		}
		return sortedMap;
	}

	public static void logException(Logger logger, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}
}
