/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.view.tableheadings.VHTreeNode;

/**
 * @author szekely
 * 
 */
public class VDIndexTable {

	private final Map<String, List<Integer>> hNodeId2Indices = new HashMap<String, List<Integer>>();

	VDIndexTable() {
		super();
	}

	public void putFrontier(List<VHTreeNode> vhTreeNodes) {
		int index = 0;
		for (VHTreeNode n : vhTreeNodes) {
			putHNode(n.getHNode(), index);
			index++;
		}
	}

	/**
	 * Record the indices for the nested tables. Assumes that the frontier has
	 * been populated first.
	 * 
	 * @param hNode
	 * @param vhTreeNodes
	 */
	public void addIndex(HNode hNode, List<VHTreeNode> vhTreeNodes) {
		List<Integer> indices = new LinkedList<Integer>();
		for (VHTreeNode n : vhTreeNodes) {
			indices.add(hNodeId2Indices.get(n.getHNode().getId()).get(0));
		}
		hNodeId2Indices.put(hNode.getId(), indices);
	}

	private void putHNode(HNode hNode, int index) {
		List<Integer> list = new LinkedList<Integer>();
		list.add(index);
		hNodeId2Indices.put(hNode.getId(), list);
	}

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.array();
		for (String key : hNodeId2Indices.keySet()) {
			jw.object()//
					.key(key).array();
			for (Integer i : hNodeId2Indices.get(key)) {
				jw.value(i);
			}
			jw.endArray().endObject();
		}
		jw.endArray();
	}

}
