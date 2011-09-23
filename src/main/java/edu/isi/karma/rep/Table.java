/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author szekely
 * 
 */
public class Table extends RepEntity {
	// the HTable that defines my properties
	private final String hTableId;

	// My rows.
	private final ArrayList<Row> rows = new ArrayList<Row>();

	Table(String id, String hTableId) {
		super(id);
		this.hTableId = hTableId;
	}

	public String gethTableId() {
		return hTableId;
	}

	public Row addRow(RepFactory factory) {
		Row r = factory.createRow(hTableId);
		rows.add(r);
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

	/**
	 * The HNode acquired a nested HTable, so we need to add placeholders on all rows for it.
	 * 
	 * @param hNode
	 * @param factory
	 */
	public void addNestedTableToDataTable(HNode hNode, RepFactory factory) {
		for (Row r : rows) {
			r.addNestedTableToDataTable(hNode, this, factory);
		}
	}

	public List<Row> getRandomRows(int maxNumber) {
		List<Row> result = new LinkedList<Row>();
		int count = 0;
		for (Row r : rows) {
			result.add(r);
			count++;
			if (count >= maxNumber)
				return result;
		}
		return result;
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
		pw.print("Table/" + id + ": ");
		pw.println(factory.getHTable(hTableId).getTableName());

		for (Row r : rows) {
			r.prettyPrint(prefix, pw, factory);
		}
	}

}
