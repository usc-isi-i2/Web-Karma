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
/**
 * 
 */
package edu.isi.karma.view;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class records all the RowPathCounts for each column in a VTable
 * 
 * @author szekely
 * 
 */
public class RowPathCountsByColumn {

	private final Map<String, RowPathCounts> hNodePathId2RowPathCounts = new HashMap<String, RowPathCounts>();

	RowPathCounts getRowPathCounts(String hNodePathId) {
		return hNodePathId2RowPathCounts.get(hNodePathId);
	}

	void incrementCounts(String hNodePath, String rowPath) {
		RowPathCounts c = hNodePathId2RowPathCounts.get(hNodePath);
		if (c == null) {
			c = new RowPathCounts();
			hNodePathId2RowPathCounts.put(hNodePath, c);
		}
		c.incrementCounts(rowPath);
	}

	int getMaxCount(String rowPath) {
		int result = 0;
		for (RowPathCounts c : hNodePathId2RowPathCounts.values()) {
			result = Math.max(result, c.getCount(rowPath));
		}
		return result;
	}

	void clear() {
		hNodePathId2RowPathCounts.clear();
	}

	void prettyPrint(String prefix, PrintWriter pw) {
		for (String key : hNodePathId2RowPathCounts.keySet()) {
			pw.println(prefix + "HNodePath:" + key);
			hNodePathId2RowPathCounts.get(key).prettyPrint("  - ", pw);
		}
	}

}
