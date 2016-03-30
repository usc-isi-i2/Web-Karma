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
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;

/**
 * @author szekely
 * 
 */
public class Row extends RepEntity implements Neighbor {

	private static Logger logger = LoggerFactory.getLogger(Row.class
			.getSimpleName());

	// My nodes, columns containing cells or nested tables, a map from HNode ids
	// to Node.
	private final Map<String, Node> nodes = new HashMap<>();

	// mariam
	/**
	 * The table that this row belongs to
	 */
	private Table belongsToTable;

	Row(String id) {
		super(id);
	}

	public Node getNode(String hNodeId) {
		return nodes.get(hNodeId);
	}

	// mariam
	public void removeNode(String hNodeId) {
		nodes.remove(hNodeId);
		for (Node n : nodes.values()) {
			Table nestedTable = n.getNestedTable();
			if (nestedTable != null) {
				nestedTable.removeNodeFromDataTable(hNodeId);
			}
		}
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	// mariam
	/**
	 * Returns all nodes and associated HNodeIds.
	 * 
	 * @return all nodes and associated HNodeIds.
	 */
	public Map<String, Node> getNodesMap() {
		return nodes;
	}

	public void setBelongsToTable(Table t) {
		belongsToTable = t;
	}

	public Table getBelongsToTable() {
		return belongsToTable;
	}

	public String getWorksheetId() {
		return belongsToTable.getWorksheetId();
	}

	// ///////////////

	void addNode(Node node) {
		nodes.put(node.getHNodeId(), node);
		// mariam
		node.setBelongsToRow(this);
	}

	/**
	 * @param hNodeId
	 * @param value
	 * @param status
	 * @return the row containing the modified node.
	 */
	public Row setValue(String hNodeId, CellValue value,
			Node.NodeStatus status, RepFactory factory) {
		getNode(hNodeId).setValue(value, status, factory);
		return this;
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
	public Row setValue(String hNodeId, String value, Node.NodeStatus status,
			RepFactory factory) {
		getNode(hNodeId).setValue(value, status, factory);
		return this;
	}

	/**
	 * Convenience method to set values with status = original.
	 * 
	 * @param hNodeId
	 * @param value
	 * @return the row containing the modified node.
	 */
	public Row setValue(String hNodeId, String value, RepFactory factory) {
		return setValue(hNodeId, value, Node.NodeStatus.original, factory);
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

	// mariam
	public String toString() {
		String s = "ROW:\n";
		for (Node n : nodes.values()) {
			s += n.toString();
		}
		return s;
	}

	void addNodeToDataTable(HNode newHNode, Table table, RepFactory factory) {
		HTable ht = factory.getHTable(table.getHTableId());
		if (ht.contains(newHNode)) {
			Node newNode = factory.createNode(newHNode.getId(),
					getWorksheetId());
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
			Table nestedTable = factory.createTable(hNode.getNestedTable()
					.getId(), getWorksheetId());
			node.setNestedTable(nestedTable, factory);
			// If the node has a value, we have to move the value to the
			// nestedTable given that we cannot have both.
			if (!node.getValue().isEmptyValue()) {
				logger.info("While adding nested table, found that column '"
						+ factory.getColumnName(hNode.getId())
						+ "' has a value: '" + node.getValue().asString()
						+ "'.");
				logger.info("Emptying value in node and trying to move it to the nested table.");
				nestedTable.addOrphanValue(node.getValue(), node.getHNodeId(),
						factory);
			}
		} else {
			// The node may be in one of the nested tables. We have to look for
			// it.
			for (Node n : nodes.values()) {
				Table nestedTable = n.getNestedTable();
				if (nestedTable != null) {
					nestedTable.addNestedTableToDataTable(hNode, factory);
				}
			}
		}
	}

	@Override
	public boolean canReachNeighbor(String hNodeId) {

		return nodes.containsKey(hNodeId)
				|| (belongsToTable.getNestedTableInNode() != null && belongsToTable
						.getNestedTableInNode().canReachNeighbor(hNodeId));
	}

	@Override
	public Node getNeighbor(String hNodeId) {
		if (nodes.containsKey(hNodeId)) {
			return nodes.get(hNodeId);
		} else if (belongsToTable.getNestedTableInNode() != null) {
			return belongsToTable.getNestedTableInNode().getNeighbor(hNodeId);
		}
		return null;
	}

	@Override
	public Node getNeighborByColumnName(String columnName, RepFactory factory) {
		String hTableId = belongsToTable.getHTableId();
		HTable hTable = factory.getHTable(hTableId);
		String hNodeId = hTable.getHNodeIdFromColumnName(columnName);
		if (null != hNodeId) {
			return getNeighbor(hNodeId);
		} else if (belongsToTable.getNestedTableInNode() != null) {
			return belongsToTable.getNestedTableInNode()
					.getNeighborByColumnName(columnName, factory);
		}
		return null;
	}

	public Node getNeighborWithNestedColumnByIndex(String hNodeId, RepFactory factory, String nestedColumnName, int index) {
		if (nodes.containsKey(hNodeId)) {
			Node nodeWithNestedColumn = nodes.get(hNodeId);
			Table nestedTable = nodeWithNestedColumn.getNestedTable();
			if(nestedTable != null)
			{
				String [] nestedColumnPath = nestedColumnName.split("/");
				String nestedHNodeId = factory.getHTable(nestedTable.getHTableId()).getHNodeIdFromColumnName(nestedColumnPath[0]);
				if (null != nestedHNodeId && nestedColumnPath.length ==1) {
					Row r = nestedTable.getRow(index);
					if(r != null)
					{
						return r.getNeighbor(nestedHNodeId);
					}
				}
				else
				{
					return nestedTable.getRow(0).getNeighborWithNestedColumnByIndex(nestedHNodeId, factory, nestedColumnName.substring(nestedColumnName.indexOf("/")+1), index);
				}
			}
		}
		return null;
	}

	public Node getNeighborByColumnNameWithNestedColumnByRowIndex(String columnName, RepFactory factory, String nestedColumnName, int index) {
		String hTableId = belongsToTable.getHTableId();
		HTable hTable = factory.getHTable(hTableId);
		String hNodeId = hTable.getHNodeIdFromColumnName(columnName);
		if (null != hNodeId) {
			return getNeighborWithNestedColumnByIndex(hNodeId, factory, nestedColumnName, index);
		} else if (belongsToTable.getNestedTableInNode() != null) {
			return belongsToTable.getNestedTableInNode()
					.getNeighborByColumnNameWithNestedColumnByRowIndex(columnName, factory, nestedColumnName, index);
		}
		return null;
	}


	public boolean collectNodes(HNodePath path, Collection<Node> nodes, SuperSelection sel) {
		if(path == null || path.getFirst() == null)
			return false;
		
		Node n = getNode(path.getFirst().getId());
		if (n == null) {
			return false;
		}
		// Check if the path has only one HNode
		if (path.getRest() == null || path.getRest().isEmpty()) {
			nodes.add(n);
			return true;
		}

		// Check if the node has a nested table
		HNodePath rest = path.getRest();
		if (n.hasNestedTable()) {
			int numRows = n.getNestedTable().getNumRows();
			if (numRows != 0)
			{
				List<Row> rowsNestedTable = n.getNestedTable().getRows(0,
						numRows, sel);
				if (rowsNestedTable != null && !rowsNestedTable.isEmpty()) {
					List<Row> rows = n.getNestedTable().getRows(0, 1, sel);
					if(!rows.isEmpty() && rows.get(0).getNode(rest.getFirst().getId()) != null)
					{
						return n.getNestedTable().collectNodes(path.getRest(), nodes, sel);
					}
				}
			}
		}
		if(n.getBelongsToRow().getNode(rest.getFirst().getId()) != null)
		{
			return n.getBelongsToRow().collectNodes(rest, nodes, sel);
		}
		
		if(n.getBelongsToRow().getBelongsToTable().getNestedTableInNode() != null)
		{
			return n.getBelongsToRow().getBelongsToTable().getNestedTableInNode().getBelongsToRow().collectNodes(rest, nodes, sel);
		}
		return false;
		
	}
}
