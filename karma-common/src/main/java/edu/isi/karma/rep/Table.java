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

import edu.isi.karma.rep.Node.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author szekely
 * 
 */
public class Table extends RepEntity {

	private static Logger logger = LoggerFactory.getLogger(Table.class
			.getSimpleName());

	// The worksheet where I am defined.
	private final String worksheetId;

	// the HTable that defines my properties
	private final String hTableId;

	// My rows.
	private final ArrayList<Row> rows = new ArrayList<Row>();

	// mariam
	/**
	 * The node that this table is a nested table in.
	 */
	private Node nestedTableInNode;

	Table(String myWorksheetId, String id, String hTableId) {
		super(id);
		this.worksheetId = myWorksheetId;
		this.hTableId = hTableId;
	}

	// mariam
	public void setNestedTableInNode(Node n) {
		nestedTableInNode = n;
	}

	public Node getNestedTableInNode() {
		return nestedTableInNode;
	}

	public String getHTableId() {
		return hTableId;
	}

	public String getWorksheetId() {
		return worksheetId;
	}

	public Row addRow(RepFactory factory) {
		Row r = factory.createRow(hTableId, worksheetId);
		rows.add(r);
		// mariam
		r.setBelongsToTable(this);
		return r;
	}

	public int getNumRows() {
		return rows.size();
	}

	/**
	 * We need to add a new Node to each row in this table to provide a place
	 * holder to store it's values.
	 * 
	 * @param newHNode
	 *            added, assume to be part of this table.
	 * @param factory
	 */
	void addNodeToDataTable(HNode newHNode, RepFactory factory) {
		for (Row r : rows) {
			r.addNodeToDataTable(newHNode, this, factory);
		}
	}

	// mariam
	public void removeNodeFromDataTable(String hNodeId) {
		for (Row r : rows) {
			r.removeNode(hNodeId);
		}
	}

	/**
	 * The HNode acquired a nested HTable, so we need to add placeholders on all
	 * rows for it.
	 * 
	 * @param hNode
	 * @param factory
	 */
	public void addNestedTableToDataTable(HNode hNode, RepFactory factory) {
		for (Row r : rows) {
			r.addNestedTableToDataTable(hNode, this, factory);
		}
	}

	/**
	 * @param startIndex
	 *            , first row at index 0.
	 * @param count
	 * @return the requested number of rows or less if the count or startIndex
	 *         are out of bounds.
	 */
	public ArrayList<Row> getRows(int startIndex, int count) {
		ArrayList<Row> result = new ArrayList<Row>();
		if (rows.size() > 0) {
			for (int i = Math.min(startIndex, rows.size() - 1); i < Math.min(
					startIndex + count, rows.size()); i++) {
				result.add(rows.get(i));
			}
		}
		return result;
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix);
		pw.print("Table/" + id + "/" + hTableId + ": ");
		pw.println(factory.getHTable(hTableId).getTableName());

		for (Row r : rows) {
			r.prettyPrint(prefix, pw, factory);
		}
	}

	/**
	 * Populates the nodes Collection (present in the argument) with nodes from
	 * the table that satisfy the given path.
	 * 
	 * @param path
	 *            Path to a given column
	 * @param nodes
	 *            Collection of nodes that satisfy the path
	 */
	public void collectNodes(HNodePath path, Collection<Node> nodes) {
		if (nodes == null) {
			nodes = new ArrayList<Node>();
		}
		collectNodes(path, nodes, rows);
	}

	private void collectNodes(HNodePath path, Collection<Node> nodes,
			List<Row> rows) {
		RowIterator: for (Row r : rows) {

			Node n = r.getNode(path.getFirst().getId());
			if (n == null) {
				continue RowIterator;
			}
			// Check if the path has only one HNode
			if (path.getRest() == null || path.getRest().isEmpty()) {
				nodes.add(n);
				continue RowIterator;
			}

			// Check if the node has a nested table
			if (n.hasNestedTable()) {
				int numRows = n.getNestedTable().getNumRows();
				if (numRows == 0)
					continue RowIterator;

				List<Row> rowsNestedTable = n.getNestedTable().getRows(0,
						numRows);
				if (rowsNestedTable != null && rowsNestedTable.size() != 0) {
					collectNodes(path.getRest(), nodes, rowsNestedTable);
					continue RowIterator;
				}
			}

		}
	}

	public void setCollectedNodeValues(HNodePath path, List<String> nodes,
			RepFactory factory) {
		setCollectedNodeValues(path, nodes, rows, 0, factory);
	}

	private void setCollectedNodeValues(HNodePath path, List<String> nodes,
			List<Row> rows, int nodeIdx, RepFactory factory) {

		RowIterator: for (Row r : rows) {

			Node n = r.getNode(path.getFirst().getId());
			if (n == null) {
				continue RowIterator;
			}
			// Check if the path has only one HNode
			if (path.getRest() == null || path.getRest().isEmpty()) {
				n.setValue(nodes.get(nodeIdx++), NodeStatus.original, factory);
				continue RowIterator;
			}

			// Check if the node has a nested table
			if (n.hasNestedTable()) {
				int numRows = n.getNestedTable().getNumRows();
				if (numRows == 0)
					continue RowIterator;

				List<Row> rowsNestedTable = n.getNestedTable().getRows(0,
						numRows);
				if (rowsNestedTable != null && rowsNestedTable.size() != 0) {
					setCollectedNodeValues(path.getRest(), nodes,
							rowsNestedTable, nodeIdx, factory);
					continue RowIterator;
				}
			}

		}
	}

	/**
	 * 2013-12-07: Pedro modified this code so that we don't create orphans but
	 * rather add another row to the existing table.
	 * 
	 * @param hNodeIdWhereValueWas
	 *            the HNode that just acquired a nested table
	 * @param value
	 *            an orphan value is a value that was the value of a node, but
	 *            the node acquired a nested table, so the value now needs to be
	 *            recorded in the nested table (this table).
	 */
	public void addOrphanValue(CellValue value, String hNodeIdWhereValueWas,
			RepFactory factory) {
		Row newRow = addRow(factory);
		HNode columnForOrphan = factory.getHTable(hTableId)
				.getHNodeFromColumnName(HTable.VALUES_COLUMN);
		if (columnForOrphan == null) {
			// There is no "values" column? This tends to happen because we need
			// to add the nested table before we know what we are going to put
			// in it.
			HTable headers = factory.getHTable(hTableId);
			columnForOrphan = headers.addHNode(HTable.VALUES_COLUMN, factory.getWorksheet(worksheetId), factory);
			logger.warn("Cannot find 'values' in nested table inside column '"
					+ factory.getColumnName(hNodeIdWhereValueWas)
					+ "' Cannot find a column to assign the orphan value '"
					+ value.asString() + ". Discarding it.");
		}
	
		logger.info("Adding orphan value '" + value.asString()
				+ "' to column '" + factory.getColumnName(hNodeIdWhereValueWas)
				+ "'.");
		newRow.setValue(columnForOrphan.getId(), value,
				Node.NodeStatus.original, factory);
	}

	public void setValueInAllRows(String hNodeId, CellValue value,
			RepFactory factory) {
		// logger.info("Setting value of column " +
		// factory.getColumnName(hNodeId) + " to "
		// + value.asString());
		for (Row r : rows) {
			// logger.info("Setting value of column " +
			// factory.getColumnName(hNodeId) + " in row "
			// + r.getId() + " to " + value.asString());
			r.setValue(hNodeId, value, Node.NodeStatus.original, factory);
		}
	}
}
