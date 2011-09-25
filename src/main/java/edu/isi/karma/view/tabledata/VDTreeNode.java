/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.Node;

/**
 * @author szekely
 * 
 */
public class VDTreeNode {

	private final Node node;

	private final List<VDRow> nestedTableRows = new LinkedList<VDRow>();

	public VDTreeNode(Node node) {
		super();
		this.node = node;
	}

	public List<VDRow> getNestedTableRows() {
		return nestedTableRows;
	}

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("node").value(node.toString())//
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
