/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.HashMap;
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

	class LeftRight {
		private final int left, right;

		public LeftRight(int left, int right) {
			super();
			this.left = left;
			this.right = right;
		}

		int getLeft() {
			return left;
		}

		int getRight() {
			return right;
		}
	}

	private final Map<String, LeftRight> hNodeId2Indices = new HashMap<String, LeftRight>();

	private int numColumns;

	private String[] hNodeIds;

	VDIndexTable() {
		super();
	}

	int getNumColumns() {
		return numColumns;
	}

	LeftRight get(String hNodeId) {
		return hNodeId2Indices.get(hNodeId);
	}

	public String getHNodeId(int columnIndex) {
		return hNodeIds[columnIndex];
	}

	public void putFrontier(List<VHTreeNode> vhTreeNodes) {
		numColumns = vhTreeNodes.size();
		hNodeIds = new String[numColumns];

		int index = 0;
		for (VHTreeNode n : vhTreeNodes) {
			hNodeIds[index] = n.getHNode().getId();
			hNodeId2Indices.put(n.getHNode().getId(), new LeftRight(index,
					index));
			index++;
		}

		hNodeId2Indices.put("root", new LeftRight(0, numColumns - 1));
	}

	/**
	 * Record the indices for the nested tables. Assumes that the frontier has
	 * been populated first.
	 * 
	 * @param hNode
	 * @param vhTreeNodes
	 */
	public void addIndex(HNode hNode, List<VHTreeNode> vhTreeNodes) {
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (VHTreeNode n : vhTreeNodes) {
			LeftRight lr = hNodeId2Indices.get(n.getHNode().getId());
			min = Math.min(min, lr.left);
			max = Math.max(max, lr.right);
		}
		hNodeId2Indices.put(hNode.getId(), new LeftRight(min, max));
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.array();
		for (String key : hNodeId2Indices.keySet()) {
			LeftRight lr = hNodeId2Indices.get(key);
			jw.object()//
					.key(key).object()//
					.key("left").value(lr.left).key("right").value(lr.right)//
					.endObject()//
					.endObject();
		}
		jw.endArray();
	}

}
