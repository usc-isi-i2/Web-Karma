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
