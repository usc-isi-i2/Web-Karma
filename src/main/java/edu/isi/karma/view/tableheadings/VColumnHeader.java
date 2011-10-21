/**
 * 
 */
package edu.isi.karma.view.tableheadings;

import java.io.PrintWriter;

import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.ViewFactory;

/**
 * 
 * @author szekely
 * 
 */
public class VColumnHeader {

	/**
	 * The name of the column I represent, possibly shortened so the table looks
	 * nice.
	 */
	private final String columnNameShort;

	/**
	 * The full column name as it appears in the HNode.
	 */
	private final String columnNameFull;

	/**
	 * The path to the data shown in this column
	 */
	private final String hNodePathId;

	public VColumnHeader(String hNodeId, String columnNameFull, String columnNameShort) {
		this.hNodePathId = hNodeId;
		this.columnNameFull = columnNameFull;
		this.columnNameShort = columnNameShort;
	}

	public String getColumnNameShort() {
		return columnNameShort;
	}

	public String getColumnNameFull() {
		return columnNameFull;
	}

	public String getHNodePathId() {
		return hNodePathId;
	}

	public void generateJson(String prefix, PrintWriter pw,
			ViewFactory factory, boolean generateComma) {
		pw.print(prefix + "{ ");
		pw.print(JSONUtil.json(WorksheetHeadersUpdate.JsonKeys.path, hNodePathId));
		pw.print(JSONUtil.json(WorksheetHeadersUpdate.JsonKeys.columnNameFull,
				columnNameFull));
		pw.print(JSONUtil.jsonLast(WorksheetHeadersUpdate.JsonKeys.columnNameShort,
				columnNameShort));
		pw.print(" }");
		if (generateComma) {
			pw.println(" ,");
		} else {
			pw.println();
		}

	}
}
