/**
 * 
 */
package edu.isi.karma.view;

import java.io.PrintWriter;

import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.util.JSONUtil;

/**
 * @author szekely
 * 
 */
public class VCell {

	private final String nodeId;
	private final String value;
	private final String status;
	private final String rowPath;
	private final boolean isFirstRow;
	private final boolean isLastRow;
	private final TablePager tablePager;

	private int rowIndex = -1;
	private int rowSpan = -1;
	@SuppressWarnings("unused")
	private String counts = ""; // Only used for debugging.

	public VCell(Node node, String rowPath, boolean isFirstRow,
			boolean isLastRow, TablePager tablePager) {
		this.nodeId = node.getId();
		this.value = node.getValue().asString();
		this.status = node.getStatus().getCodedStatus();
		this.rowPath = rowPath;
		this.isFirstRow = isFirstRow;
		this.isLastRow = isLastRow;
		this.tablePager = tablePager;
	}

	String getValue() {
		return value;
	}

	int getRowIndex() {
		return rowIndex;
	}

	void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	String getNodeId() {
		return nodeId;
	}

	String getStatus() {
		return status;
	}

	String getRowPath() {
		return rowPath;
	}

	int getRowSpan() {
		return rowSpan;
	}

	void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	boolean isLastRow() {
		return isLastRow;
	}

	TablePager getTablePager() {
		return tablePager;
	}

	void setCounts(String counts) {
		this.counts = counts;
	}

	void generateJson(String hNodePath, String tableCssTag, String prefix,
			PrintWriter pw, VWorksheet vWorksheet, ViewFactory factory,
			boolean generateComma) {

		pw.print(prefix + "{");
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.path, hNodePath));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.nodeId, nodeId));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.value, value));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.status, status));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.isDummy, false));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.isFirstRow, isFirstRow));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.isLastRow, isLastRow));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.rowSpan, rowSpan));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.rowIndex, rowIndex));
		pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.rowPath, rowPath));

		if (isLastRow && !tablePager.isAllRowsShown()
				&& tablePager != vWorksheet.getTopTablePager()) {
			String newPref = prefix + "  ";
			pw.println();
			pw.println(newPref
					+ JSONUtil.jsonStartObject(WorksheetDataUpdate.JsonKeys.pager));
			tablePager.generateJson(newPref + "  ", pw);
			pw.println(newPref + ", ");
			pw.print(newPref);
		}

		//pw.println(Util.json(WorksheetDataUpdate.JsonKeys.counts, counts));

		pw.print(JSONUtil.jsonLast(WorksheetDataUpdate.JsonKeys.tableCssTag,
				tableCssTag));
		if (generateComma) {
			pw.println(" } ,");
		} else {
			pw.println(" }");
		}

	}

	public void prettyPrint(String prefix, PrintWriter pw) {
		pw.println(prefix + ".. rowPath:" + rowPath + ", nodeId:" + nodeId
				+ ", value:" + value + ", status:" + status + ", rowSpan:"
				+ rowSpan + ", rowIndex:" + rowIndex);
	}

}
