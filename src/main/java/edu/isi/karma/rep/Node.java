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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szekely
 * 
 */
public class Node extends RepEntity {

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
	private CellValue value = StringCellValue.getEmptyString();

	private CellValue originalValue = StringCellValue.getEmptyString();

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

	public void setValue(CellValue value, NodeStatus status) {
		this.value = value;
		this.status = status;
		// Pedro 2012/09/14
		if (nestedTable != null) {
			logger.error("Node contains both value and nested table: "
					+ toString());
		}
	}

	public void clearValue(NodeStatus status) {
		this.value = null;
		this.status = status;
	}

	public void setValue(String value, NodeStatus status) {
		setValue(new StringCellValue(value), status);
	}

	public Table getNestedTable() {
		return nestedTable;
	}

	public void setNestedTable(Table nestedTable) {
		this.nestedTable = nestedTable;
		// mariam
		if (nestedTable != null)
			nestedTable.setNestedTableInNode(this);
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
}
