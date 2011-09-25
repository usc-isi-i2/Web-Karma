/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;

/**
 * @author szekely
 * 
 */
public class Node extends RepEntity {

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

	Node(String id, String hNodeId) {
		super(id);
		this.hNodeId = hNodeId;
	}

	public String gethNodeId() {
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
	}

	public void setValue(String value, NodeStatus status) {
		setValue(new StringCellValue(value), status);
	}

	public Table getNestedTable() {
		return nestedTable;
	}

	public void setNestedTable(Table nestedTable) {
		this.nestedTable = nestedTable;
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
			b.append("*)");
		} else {
			b.append(value.asString() + ")");
		}
		return b.toString();
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix + "  - ");
		pw.print(factory.getHNode(hNodeId).getColumnName() + ":");
		if (nestedTable != null) {
			pw.println();
			nestedTable.prettyPrint(prefix + "      ", pw, factory);
		} else {
			pw.println(value.asString());
		}
	}
}
