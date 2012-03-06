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
package edu.isi.karma.view;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;

/**
 * @author szekely
 * 
 */
public class VTable {

	/**
	 * The hTableId for this table. Used to identify nested tables that use the
	 * same hTableId across columns.
	 */
	private final String hTableId;

	/**
	 * The rows contained in this table. The rows correspond to rows in the top
	 * table of the worksheet.
	 */
	private final List<VRow> vRows = new LinkedList<VRow>();

	/**
	 * We use this to figure out the row spans of each cell to account for
	 * nested tables.
	 */
	private final RowPathCountsByColumn rowPathCounts = new RowPathCountsByColumn();

	VTable(String hTableId) {
		this.hTableId = hTableId;
	}

	String gethTableId() {
		return hTableId;
	}

	List<VRow> getRows() {
		return vRows;
	}

	public void clear() {
		vRows.clear();
		rowPathCounts.clear();
	}

	void generateJson(String prefix, PrintWriter pw, VWorksheet vWorksheet,
			ViewFactory factory) {
		Iterator<VRow> itRows = vRows.iterator();
		while (itRows.hasNext()) {
			VRow r = itRows.next();
			r.generateJson(prefix + "  ", pw, vWorksheet, factory,
					rowPathCounts, itRows.hasNext());
		}
	}

	public void addRows(List<Row> rowsToAdd, List<HNodePath> columns,
			VWorksheet vWorksheet, ViewFactory viewFactory) {
		Iterator<Row> it = rowsToAdd.iterator();
		boolean isFirstRow = true;
		while (it.hasNext()) {
			Row r = it.next();
			VRow vr = new VRow(r.getId());
			vRows.add(vr);

			for (HNodePath path : columns) {

				// Define the VRowEntry for this column.
				String tag = viewFactory.getTableCssTags().getCssTag(
						path.getLeaf().getHTableId(), 0);
				if (tag == null)
					tag = VTableCssTags.CSS_TOP_LEVEL_TABLE_CELL;
				// System.err.println("VTable.addRows.TAG=" + tag + "%%");
				VRowEntry re = new VRowEntry(path.toString(), tag);
				vr.addRowEntry(re);

				// Populate the VRowEntry with Cells for each multirow in nested
				// tables.
				Node node = r.getNode(path.getFirst().getId());
				addCellToRowEntry(re, r.getId(), path.getRest(), node,
						path.toString(), isFirstRow, !it.hasNext(),
						vWorksheet.getTopTablePager(), vWorksheet);
				isFirstRow = false;
			}
		}

		// Now assign row indices to every VCell so we know where they go
		// vertically.
		for (VRow vr : vRows) {
			vr.assignRowIndices(rowPathCounts);
		}
	}

	private void addCellToRowEntry(VRowEntry rowEntry, String rowPath,
			HNodePath path, Node node, String columnPath, boolean isFirstRow,
			boolean isLastRow, TablePager tablePager, VWorksheet vWorksheet) {
		if (node.getNestedTable() != null) {
			addCellsForNestedTable(node.getNestedTable(), rowPath, path,
					rowEntry, columnPath, vWorksheet);
		} else {
			VCell cell = new VCell(node, rowPath, isFirstRow, isLastRow,
					tablePager);
			rowPathCounts.incrementCounts(columnPath, rowPath);
			rowEntry.addCell(cell);
		}
	}

	private void addCellsForNestedTable(Table nestedTable, String rowPath,
			HNodePath path, VRowEntry rowEntry, String columnPath,
			VWorksheet vWorksheet) {
		TablePager tp = vWorksheet.getNestedTablePager(nestedTable);
		List<Row> rows = tp.getRows();
		Iterator<Row> it = rows.iterator();
		boolean isFirstRow = true;
		while (it.hasNext()) {
			Row r = it.next();
			Node node = r.getNode(path.getFirst().getId());
			addCellToRowEntry(rowEntry, rowPath + "/" + r.getId(),
					path.getRest(), node, columnPath, isFirstRow,
					!it.hasNext(), tp, vWorksheet);
			isFirstRow = false;
		}
		// TODO: if the nested table is empty, the while loop does not add any
		// cells, and the browser won't know to draw a horizontal line for it.
	}

	void prettyPrint(String prefix, PrintWriter pw) {
		pw.println("VTable/" + hTableId);
		for (VRow vr : vRows) {
			vr.prettyPrint(prefix + "  ", pw);
		}
		pw.println("RowPathCounts");
		rowPathCounts.prettyPrint("", pw);
	}
}
