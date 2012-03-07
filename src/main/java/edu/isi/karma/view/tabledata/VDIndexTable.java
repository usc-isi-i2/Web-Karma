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

	/**
	 * Records the indices of the leftmost and rightmost column of an HNode.
	 * 
	 * @author szekely
	 *
	 */
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

	private int[] columnDepths;

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

	public int getColumnDepth(int columnIndex) {
		return columnDepths[columnIndex];
	}

	public void putFrontier(List<VHTreeNode> vhTreeNodes) {
		numColumns = vhTreeNodes.size();
		hNodeIds = new String[numColumns];
		columnDepths = new int[numColumns];

		int index = 0;
		for (VHTreeNode n : vhTreeNodes) {
			hNodeIds[index] = n.getHNode().getId(); // TODO: null pointer in
													// this line while loading
													// the F6.json file.
			// java.lang.NullPointerException
			// at
			// edu.isi.karma.view.tabledata.VDIndexTable.putFrontier(VDIndexTable.java:75)
			// at
			// edu.isi.karma.view.tabledata.VDTableData.<init>(VDTableData.java:45)
			// at edu.isi.karma.view.VWorksheet.<init>(VWorksheet.java:106)
			// at
			// edu.isi.karma.view.ViewFactory.createVWorksheet(ViewFactory.java:64)

			columnDepths[index] = n.getDepth();
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
