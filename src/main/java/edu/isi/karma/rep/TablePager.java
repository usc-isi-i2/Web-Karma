/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.List;

import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.util.Util;

/**
 * @author szekely
 * 
 */
public class TablePager {

	private final Table table;

	private int startIndex;

	private int desiredSize;

	private int size;

	public TablePager(Table table, int startIndex, int desiredSize) {
		super();
		this.table = table;
		this.startIndex = startIndex;
		this.desiredSize = desiredSize;
		setSize();
	}

	/**
	 * The size is adjusted in case the table is smaller than the desired size.
	 */
	private void setSize() {
		if (desiredSize > table.getNumRows()) {
			size = table.getNumRows();
		} else {
			size = desiredSize;
		}

		if (startIndex != 0 && (startIndex + 1) % desiredSize != 0) {
			startIndex = Math.max(0, startIndex - (startIndex % desiredSize));
		}

		// If the # rows to get is bigger than the number of max rows that can
		// be retrieved
		int numAfter = getNumRecordsAfter();
		if (numAfter < 0) {
			size = table.getNumRows() - startIndex;
		}
	}

	public List<Row> getRows() {
		return table.getRows(startIndex, size);
	}

	public int getNumRecordsBefore() {
		return startIndex;
	}

	public int getNumRecordsAfter() {
		return table.getNumRows() - startIndex - size;
	}

	public int getDesiredSize() {
		return desiredSize;
	}

	public void setDesiredSize(int desiredSize) {
		this.desiredSize = desiredSize;
		setSize();
	}

	public int getStartIndex() {
		return startIndex;
	}

	public boolean isAllRowsShown() {
		return 0 == (getNumRecordsAfter() + getNumRecordsBefore());
	}

	public void moveToPreviousPage() {
		size = Math.max(size, desiredSize);
		startIndex = Math.max(0, startIndex - size);
	}

	public void moveToNextPage() {
		startIndex = Math.max(size, startIndex + size);
		size = Math.min(size, table.getNumRows() - startIndex);
	}

	public void generateJson(String prefix, PrintWriter pw) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ Util.json(WorksheetDataUpdate.JsonKeys.numRecordsShown, size));
		pw.println(newPref
				+ Util.json(WorksheetDataUpdate.JsonKeys.numRecordsBefore,
						getNumRecordsBefore()));
		pw.println(newPref
				+ Util.json(WorksheetDataUpdate.JsonKeys.numRecordsAfter,
						getNumRecordsAfter()));
		pw.println(newPref
				+ Util.json(
						WorksheetDataUpdate.JsonKeys.desiredNumRecordsShown,
						desiredSize));
		pw.println(newPref
				+ Util.jsonLast(WorksheetDataUpdate.JsonKeys.tableId,
						table.getId()));
		pw.println(prefix + "}");
	}
}
