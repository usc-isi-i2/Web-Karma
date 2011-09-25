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

/**
 * @author szekely
 * 
 */
public class VDRow {

	private final VDTreeNode containerVDNode;

	private final Row row;

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
	private int depth = 1;

	/**
	 * This row requires a margin if this value is non-null, in which case it
	 * means that at least one of the nodes in the row is a nested table.
	 */
	private Margin margin = null;

	/**
	 * The HTableId that defines the fill color for this row.
	 */
	private String fillHTableId = "UNDEFINED";

	public VDRow(Row row, VDTreeNode containerVDNode, boolean isFirst,
			boolean isLast) {
		super();
		this.row = row;
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

	String getFillHTableId() {
		return fillHTableId;
	}

	void setFillHTableId(String fillHTableId) {
		this.fillHTableId = fillHTableId;
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
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("isA").value("VDRow")//
				.key("_rowId").value(row.getId())//
				.key("isFirst/isLast").value("" + isFirst + "/" + isLast)//
				.key("depth").value(depth)//
				.key("margin").value(Margin.getMarginsString(margin))//
				.key("fillHTableId").value(fillHTableId)//
				.key("nodes").array();
		for (VDTreeNode n : nodes) {
			n.prettyPrintJson(jw);
		}
		jw.endArray().endObject();
	}
}
