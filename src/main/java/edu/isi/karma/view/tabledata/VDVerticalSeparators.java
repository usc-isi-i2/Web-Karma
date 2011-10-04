/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class VDVerticalSeparators {

	private final Map<String, VDVerticalSeparator> hNodeIdToVDSEparator = new HashMap<String, VDVerticalSeparator>();

	VDVerticalSeparators() {
		super();
	}

	public VDVerticalSeparator get(String hNodeId) {
		return hNodeIdToVDSEparator.get(hNodeId);
	}

	public void put(String hNodeId, VDVerticalSeparator vdVerticalSeparator) {
		hNodeIdToVDSEparator.put(hNodeId, vdVerticalSeparator);
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw, VWorkspace vWorkspace)
			throws JSONException {
		RepFactory f = vWorkspace.getRepFactory();
		jw.array();
		for (String key : hNodeIdToVDSEparator.keySet()) {
			String name = "root";
			HNode hn = f.getHNode(key);
			if (hn != null) {
				name = hn.getColumnName();
			}
			jw.object().key(key + "/" + name);
			hNodeIdToVDSEparator.get(key).prettyPrintJson(jw);
			jw.endObject();
		}
		jw.endArray();
	}
}
