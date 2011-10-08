/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author szekely
 * 
 */
public class Row extends RepEntity {
	// My nodes, columns containing cells or nested tables, a map from HNode ids
	// to Node.
	private final Map<String, Node> nodes = new HashMap<String, Node>();

	Row(String id) {
		super(id);
	}

	public Node getNode(String hNodeId) {
		return nodes.get(hNodeId);
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	void addNode(Node node) {
		nodes.put(node.gethNodeId(), node);
	}

	/**
	 * Convenience method to set values in nodes in rows.
	 * 
	 * @param hNodeId
	 * @param value
	 * @param status
	 *            specifies the status of the value
	 * @return the row containing the modified node.
	 */
	public Row setValue(String hNodeId, String value, Node.NodeStatus status) {
		getNode(hNodeId).setValue(value, status);
		return this;
	}

	/**
	 * Convenience method to set values with status = original.
	 * 
	 * @param hNodeId
	 * @param value
	 * @return the row containing the modified node.
	 */
	public Row setValue(String hNodeId, String value) {
		return setValue(hNodeId, value, Node.NodeStatus.original);
	}

	/**
	 * Convenience method to add nested rows
	 * 
	 * @param hNodeId
	 *            , that contains the nested table where we want to add a row.
	 * @param factory
	 * @return the added row.
	 */
	public Row addNestedRow(String hNodeId, RepFactory factory) {
		return getNode(hNodeId).getNestedTable().addRow(factory);
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix + "__");
		pw.println("/" + id);
		for (Node n : nodes.values()) {
			n.prettyPrint(prefix, pw, factory);
		}
	}

	void addNodeToDataTable(HNode newHNode, Table table, RepFactory factory) {
		HTable ht = factory.getHTable(table.getHTableId());
		if (ht.contains(newHNode)) {
			Node newNode = factory.createNode(newHNode.getId());
			addNode(newNode);
		} else {
			// We don't know where the nested table is, so we have to
			// try all of them.
			for (Node n : nodes.values()) {
				Table nestedTable = n.getNestedTable();
				if (nestedTable != null) {
					nestedTable.addNodeToDataTable(newHNode, factory);
				}
			}
		}
	}

	public void addNestedTableToDataTable(HNode hNode, Table table,
			RepFactory factory) {
		Node node = getNode(hNode.getId());
		if (node != null) {
			// This table does contain this hNode.
			Table nestedTable = factory.createTable(hNode.getNestedTable().getId());
			node.setNestedTable(nestedTable);
		} else {
			// The node may be in one of the nested tables. We have to look for it.
			for (Node n : nodes.values()) {
				Table nestedTable = n.getNestedTable();
				if (nestedTable != null) {
					nestedTable.addNestedTableToDataTable(hNode, factory);
				}
			}
		}
	}
}
