/**
 * 
 */
package edu.isi.karma.view;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * RowPaths are of the form R1/R2/R3. This table records the count for each
 * prefix of any row path recorded in it. For example, suppose that R1/R2/R3 and
 * R1/R2/R4 are recorded. The table would have the following entries:
 * 
 * R1:2
 * 
 * R1/R2:2
 * 
 * R1/R2/R3:1
 * 
 * R1/R2/R4:1
 * 
 * @author szekely
 * 
 */
public class RowPathCounts {
	private final Map<String, Integer> rowPathCounts = new HashMap<String, Integer>();

	void incrementCounts(String rowPath) {
		String[] elements = rowPath.split("/");
		String path = "";
		for (int i = 0; i < elements.length; i++) {
			if (!"".equals(path)) {
				path = path + "/";
			}
			path = path + elements[i];
			increment(path);
		}
	}

	int getCount(String path) {
		Integer result = rowPathCounts.get(path);
		if (result != null) {
			return result;
		} else {
			return 0;
		}
	}

	private void increment(String path) {
		Integer count = rowPathCounts.get(path);
		if (count == null) {
			count = 0;
		}
		rowPathCounts.put(path, count + 1);
	}

	public void prettyPrint(String prefix, PrintWriter pw) {
		for (String key : rowPathCounts.keySet()) {
			pw.println(prefix + key + "=" + rowPathCounts.get(key));
		}
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		Iterator<String> it = rowPathCounts.keySet().iterator();
		while (it.hasNext()) {
			String k = it.next();
			b.append(k);
			b.append(":");
			b.append(rowPathCounts.get(k));
			if (it.hasNext()) {
				b.append(", ");
			}
		}
		return b.toString();
	}
}
