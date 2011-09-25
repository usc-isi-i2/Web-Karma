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

	public VDTreeNode(Node node) {
		super();
		this.node = node;

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

	void firstTopDownPass(VDRow containerVDRow, VWorkspace vWorkspace) {
		if (hasNestedTable()) {
			margin = new Margin(getHNode(vWorkspace).getHTableId(), 0);
		}

		depth = containerVDRow.getDepth();
		
		for (VDRow r : nestedTableRows) {
			r.firstTopDownPass(this, vWorkspace);
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
