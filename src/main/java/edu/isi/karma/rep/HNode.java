/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;

/**
 * @author szekely
 * 
 */
public class HNode extends RepEntity implements Comparable<HNode> {

	// The HTable I belong to.
	private final String hTableId;

	// The name of the column I represent.
	private String columnName;

	// A nested table, possibly null.
	private HTable nestedTable = null;

	HNode(String id, String hTableId, String columnName) {
		super(id);
		this.hTableId = hTableId;
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the ID of the HTable containing this HNode.
	 */
	public String getHTableId() {
		return hTableId;
	}
	
	public boolean hasNestedTable() {
		return nestedTable != null;
	}

	public HTable getNestedTable() {
		return nestedTable;
	}

	public void setNestedTable(HTable nestedTable) {
		this.nestedTable = nestedTable;
	}

	public HTable addNestedTable(String tableName, Worksheet worksheet, RepFactory factory) {
		nestedTable = factory.createHTable(tableName);
		worksheet.addNestedTableToDataTable(this, factory);
		return nestedTable;
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix + "- ");
		pw.print(columnName);
		pw.print("/" + id);
		if (nestedTable != null) {
			pw.println(": ");
			nestedTable.prettyPrint(prefix + "    ", pw, factory);
		} else {
			pw.println();
		}

	}

	public int compareTo(HNode other) {
		return columnName.compareTo(other.getColumnName());
	}

}
