/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.Row;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class VDRow {

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
	private int depth = 0;

	public VDRow(Row row, boolean isFirst, boolean isLast) {
		super();
		this.row = row;
		this.isFirst = isFirst;
		this.isLast = isLast;
	}

	Row getRow() {
		return row;
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

	boolean isMiddle() {
		return !isFirst && !isLast;
	}

	public void add(VDTreeNode vdNode) {
		nodes.add(vdNode);
	}

	void firstTopDownPass(VDTreeNode containerVDNode, VWorkspace vWorkspace) {
		if (containerVDNode != null) {
			depth = containerVDNode.getDepth() + 1;
		}

		for (VDTreeNode n : nodes) {
			n.firstTopDownPass(this, vWorkspace);
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
				.key("rowId").value(row.getId())//
				.key("isFirst/isLast").value("" + isFirst + "/" + isLast)//
				.key("depth").value(depth)//
				.key("nodes").array();
		for (VDTreeNode n : nodes) {
			n.prettyPrintJson(jw);
		}
		jw.endArray().endObject();
	}
}
