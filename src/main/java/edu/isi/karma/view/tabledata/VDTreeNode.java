/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.view.Margin;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class VDTreeNode {

	private final Node node;

	private final VDRow containerVDRow;

	private final List<VDRow> nestedTableRows = new LinkedList<VDRow>();

	/**
	 * The depth of this tree node. The root table has depth 0.
	 */
	private int depth = 0;

	/**
	 * If there is a nested table, record the style of margin that will be shown
	 * above and below.
	 */
	private Margin margin = null;

	/**
	 * List of margins around nested tables.
	 */
	private List<Margin> marginList = new LinkedList<Margin>();

	public VDTreeNode(Node node, VDRow containerVDRow) {
		super();
		this.node = node;
		this.containerVDRow = containerVDRow;

	}

	public List<VDRow> getNestedTableRows() {
		return nestedTableRows;
	}

	public boolean hasNestedTable() {
		return !nestedTableRows.isEmpty();
	}

	int getDepth() {
		return depth;
	}

	void setDepth(int depth) {
		this.depth = depth;
	}

	Node getNode() {
		return node;
	}

	Margin getMargin() {
		return margin;
	}

	HNode getHNode(VWorkspace vWorkspace) {
		return vWorkspace.getRepFactory().getHNode(node.gethNodeId());
	}

	void firstPassTopDown(VWorkspace vWorkspace) {
		if (hasNestedTable()) {
			margin = new Margin(getHNode(vWorkspace).getHTableId(), 0);
			marginList.add(margin);
		}

		depth = containerVDRow.getDepth();

		// Now go top down.
		for (VDRow r : nestedTableRows) {
			r.firstPassTopDown(vWorkspace);
		}
	}

	void secondPassBottomUp(VWorkspace vWorkspace) {
		// First recurse.
		for (VDRow r : nestedTableRows) {
			r.firstPassTopDown(vWorkspace);
		}

		if (containerVDRow != null) {
			// Propagate the margin list up.
			if (containerVDRow != null) {
				VDTreeNode parent = containerVDRow.getContainerVDNode();
				parent.marginList.addAll(marginList);
			}
		}
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	private String getMarginsString() {
		if (margin == null) {
			return "none";
		} else {
			return margin.toString();
		}
	}

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("node").value(node.toString())//
				.key("depth").value(depth)//
				.key("margin").value(getMarginsString())//
				.key("marginList").value(Margin.toString(marginList))//
		;
		if (!nestedTableRows.isEmpty()) {
			jw.key("rows").array();
			for (VDRow r : nestedTableRows) {
				r.prettyPrintJson(jw);
			}
			jw.endArray();
		}

		jw.endObject();
	}

}
