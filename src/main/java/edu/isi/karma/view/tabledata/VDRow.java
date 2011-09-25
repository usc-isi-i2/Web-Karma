/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.Row;

/**
 * @author szekely
 * 
 */
public class VDRow {

	private final Row row;

	private final List<VDTreeNode> nodes = new LinkedList<VDTreeNode>();

	public VDRow(Row row) {
		super();
		this.row = row;
	}

	public void add(VDTreeNode vdNode) {
		nodes.add(vdNode);
	}

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("isA").value("VDRow")//
				.key("rowId").value(row.getId())//
				.key("nodes").array();
		for (VDTreeNode n : nodes) {
			n.prettyPrintJson(jw);
		}
		jw.endArray().endObject();
	}
}
