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

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.Row;
import edu.isi.karma.view.Margin;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tableheadings.VHTreeNode;

/**
 * @author szekely
 * 
 */
public class VDRow {

	private final VDTreeNode containerVDNode;

	private final Row row;

	private final VHTreeNode vhTreeNode;

	private final List<VDTreeNode> nodes = new LinkedList<VDTreeNode>();

	/**
	 * Each VDRow knows whether it is the first or last row in the table that
	 * contains it.
	 */
	private final boolean isFirst, isLast;

	/**
	 * The depth of the table that contains this row. The root table has depth
	 * 0.
	 */
	private int depth = 0;

	/**
	 * This row requires a margin if this value is non-null, in which case it
	 * means that at least one of the nodes in the row is a nested table.
	 */
	private Margin margin = null;

	/**
	 * List of margins from all containers, ordered from top to bottom.
	 */
	private final List<Margin> allMargins = new LinkedList<Margin>();

	/**
	 * The HTableId that defines the fill color for this row.
	 */
	private String fillHTableId = "UNDEFINED";

	/**
	 * Number of levels of data in this row.
	 */
	private int numLevels = -1;

	/**
	 * The level where the first TR of nodes in this level with start. Zero
	 * based.
	 */
	private int startLevel = -1;

	public VDRow(Row row, VHTreeNode vhTreeNode, VDTreeNode containerVDNode,
			boolean isFirst, boolean isLast) {
		super();
		this.row = row;
		this.vhTreeNode = vhTreeNode;
		this.containerVDNode = containerVDNode;
		this.isFirst = isFirst;
		this.isLast = isLast;
	}

	Row getRow() {
		return row;
	}

	VDTreeNode getContainerVDNode() {
		return containerVDNode;
	}

	boolean isFirst() {
		return isFirst;
	}

	boolean isLast() {
		return isLast;
	}

	int getDepth() {
		return depth;
	}

	Margin getMargin() {
		return margin;
	}

	List<Margin> getAllMargins() {
		return allMargins;
	}

	String getFillHTableId() {
		return fillHTableId;
	}

	void setFillHTableId(String fillHTableId) {
		this.fillHTableId = fillHTableId;
	}

	int getNumLevels() {
		return numLevels;
	}

	int getStartLevel() {
		return startLevel;
	}

	void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}

	int getLastLevel() {
		return getStartLevel() + getNumLevels() - 1;
	}

	String getContainerHNodeId(VWorkspace vWorkspace) {
		return containerVDNode == null ? "root" : containerVDNode.getHNode(
				vWorkspace).getId();
	}

	List<VDTreeNode> getNodes() {
		return nodes;
	}

	void accumulateMargin(Margin margin) {
		if (margin != null) {
			this.margin = margin;
		}
	}

	boolean isMiddle() {
		return !isFirst && !isLast;
	}

	public void add(VDTreeNode vdNode) {
		nodes.add(vdNode);
	}

	void firstPassTopDown(VWorkspace vWorkspace) {
		if (containerVDNode != null) {
			depth = containerVDNode.getDepth() + 1;
			allMargins.addAll(containerVDNode.getAllMargins());
		}

		// Now go top down.
		for (VDTreeNode n : nodes) {
			n.firstPassTopDown(vWorkspace);
		}
	}

	void secondPassBottomUp(VWorkspace vWorkspace) {
		// First go bottom up.
		for (VDTreeNode n : nodes) {
			n.secondPassBottomUp(vWorkspace);
		}

		// Calculate numLevels.
		int maxLevels = 0;
		for (VDTreeNode n : nodes) {
			maxLevels = Math.max(maxLevels, n.getNumLevels());
		}
		numLevels = maxLevels;
	}

	void thirdPassTopDown(VWorkspace vWorkspace) {
		if (containerVDNode != null) {

		}

		// Now go top down.
		for (VDTreeNode n : nodes) {
			n.setStartLevel(startLevel);
			n.thirdPassTopDown(vWorkspace);
		}
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw, boolean verbose) throws JSONException {
		jw.object()//
				.key("_isA").value("VDRow")//
				.key("__rowId").value(row.getId())//
				.key("_isFirst/isLast").value("" + isFirst + "/" + isLast)//
				.key("_depth").value(depth)//
				.key("_numLevels").value(numLevels)//
				.key("_startLevel").value(startLevel)//
				.key("_margin").value(Margin.getMarginsString(margin))//
				.key("_allMargins").value(Margin.toString(allMargins))//
				.key("_fillHTableId").value(fillHTableId)//
				.key("nodes").array();
		for (VDTreeNode n : nodes) {
			n.prettyPrintJson(jw, verbose);
		}
		jw.endArray();
		if (verbose) {
			jw.key("hTreeNode");
			vhTreeNode.prettyPrintJson(jw, false, false);
		}
		jw.endObject();
	}
}
