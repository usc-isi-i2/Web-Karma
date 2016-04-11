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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;

/**
 * @author szekely
 * 
 */
public class Node extends RepEntity implements Neighbor {

	private static Logger logger = LoggerFactory.getLogger(Node.class);

	public enum NodeStatus {
		original("O"), edited("E");

		private String codedValue;

		private NodeStatus(String userLabel) {
			this.codedValue = userLabel;
		}

		public String getCodedStatus() {
			return codedValue;
		}
	}

	// the HNode that defines my properties.
	private final String hNodeId;

	// possibly has a nested table, null if none
	private Table nestedTable = null;

	private NodeStatus status = NodeStatus.original;

	// The value stored in this cell.
	private CellValue value = CellValue.getEmptyValue();

	private CellValue originalValue = CellValue.getEmptyValue();

	// mariam
	/**
	 * The row that this node belongs to
	 */
	private Row belongsToRow;

	Node(String id, String hNodeId) {
		super(id);
		this.hNodeId = hNodeId;
	}

	// mariam
	public void setBelongsToRow(Row row) {
		belongsToRow = row;
	}

	public Row getBelongsToRow() {
		return belongsToRow;
	}

	/**
	 * Return the table that this node belongs to.
	 * 
	 * @return the table that this node belongs to.
	 */
	public Table getParentTable() {
		return belongsToRow.getBelongsToTable();
	}

	// /////////////

	public String getHNodeId() {
		return hNodeId;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public CellValue getOriginalValue() {
		return originalValue;
	}

	public CellValue getValue() {
		return value;
	}

	public void setValue(CellValue value, NodeStatus status, RepFactory factory) {
		// Pedro 2012/09/14
		if (nestedTable != null) {
			logger.debug("Node in column '"
					+ factory.getColumnName(hNodeId)
					+ "' contains a nested table and we are trying to set a value: '"
					+ value.asString() + "'. Adding as orphan in nested table");
			nestedTable.addOrphanValue(value, hNodeId, factory);
		} else {
			this.value = value;
			this.status = status;
		}
	}

	public void clearValue(NodeStatus status) {
		// pedro 2012-09-15: this was wrong because it was setting the value to
		// null.
		this.value = CellValue.getEmptyValue();
		this.status = status;
	}

	public void setValue(String value, NodeStatus status, RepFactory factory) {
		setValue(new StringCellValue(value), status, factory);
	}

	public Table getNestedTable() {
		return nestedTable;
	}

	public void setNestedTable(Table nestedTable, RepFactory factory) {
		this.nestedTable = nestedTable;
		// mariam
		if (nestedTable != null) {
			nestedTable.setNestedTableInNode(this);
			// pedro 2012-09-15
			if (!value.isEmptyValue()) {
				logger.debug("Adding nested table to node in column '"
						+ factory.getColumnName(hNodeId)
						+ "' already contains a value: '"
						+ value.asString()
						+ "'. Clearing value and adding as orphan in nested table. ");
				CellValue orphanValue = value;
				value = CellValue.getEmptyValue();
				nestedTable.addOrphanValue(orphanValue, hNodeId, factory);
			}
		}
	}

	public boolean hasNestedTable() {
		return nestedTable != null;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("N(");
		b.append(getId() + ",");
		b.append(hNodeId + ",");
		if (nestedTable != null) {
			b.append("*" + nestedTable.getId() + "/"
					+ nestedTable.getHTableId() + ")");
		} else {
			b.append(value.asString() + ")");
		}
		return b.toString();
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix + "  - ");
		pw.print(factory.getHNode(hNodeId).getColumnName() + "/" + id + "/"
				+ hNodeId + ":");
		if (nestedTable != null) {
			pw.println();
			nestedTable.prettyPrint(prefix + "      ", pw, factory);
		} else {
			pw.println("<" + value.asString() + ">");
		}
	}

	@Override
	public boolean canReachNeighbor(String hNodeId) {
		return belongsToRow.canReachNeighbor(hNodeId);
	}

	@Override
	public Node getNeighbor(String hNodeId) {
		return belongsToRow.getNeighbor(hNodeId);
	}

	@Override
	public Node getNeighborByColumnName(String columnName, RepFactory factory) {
		return belongsToRow.getNeighborByColumnName(columnName, factory);
	}

	public Object serializeToJSON(SuperSelection selection, RepFactory factory) {
		if (this.hasNestedTable()) {
			JSONArray array = new JSONArray();
			Table t = this.getNestedTable();
			HTable ht = factory.getHTable(t.getHTableId());
			for (Row r : t.getRows(0, t.getNumRows(), selection)) {
				JSONObject obj = new JSONObject();
				for (HNode hNode : ht.getHNodes()) {
					obj.put(hNode.getColumnName(), r.getNeighbor(hNode.getId()).serializeToJSON(selection, factory));			
				}
				array.put(obj);
			}
			return array;
		}
		else {
			return this.getValue().asString();
		}
	}

	
	public Node getNeighborByColumnNameWithNestedColumnByRowIndex(String columnName, RepFactory factory, String nestedColumnName, int index) {
		return belongsToRow.getNeighborByColumnNameWithNestedColumnByRowIndex(columnName, factory, nestedColumnName, index);
	}
	
	public int getRowIndex()
	{
		return belongsToRow.getBelongsToTable().getRowIndex(belongsToRow);
	}
}
