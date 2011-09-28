/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tableheadings.VHTreeNode;
import edu.isi.karma.view.tableheadings.VTableHeadings;

/**
 * @author szekely
 * 
 */
public class VDTableData {

	// private final VTableHeadings vtHeadings;

	private final String rootTableId;

	private final List<VDRow> rows = new LinkedList<VDRow>();

	private final List<VHTreeNode> frontier = new LinkedList<VHTreeNode>();

	private final VDIndexTable vdIndexTable = new VDIndexTable();

	private final VDTableCells vdTableCells;

	public VDTableData(VTableHeadings vtHeadings, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		super();
		// this.vtHeadings = vtHeadings;
		this.rootTableId = vWorksheet.getWorksheet().getDataTable().getId();

		// Record the column indices for all HNodes, except the fake root HNode.
		vtHeadings.getRootVHNode().collectLeaves(frontier);
		vdIndexTable.putFrontier(frontier);
		vtHeadings.getRootVHNode().populateVDIndexTable(vdIndexTable);

		// Build the VDRows and their content.
		vtHeadings.getRootVHNode().populateVDRows(null, rows,
				vWorksheet.getTopTablePager(), vWorksheet);
		for (VDRow r : rows) {
			r.setFillHTableId(vWorkspace.getRepFactory().getTable(rootTableId)
					.gethTableId());
			r.firstPassTopDown(vWorkspace);
		}
		for (VDRow r : rows) {
			r.secondPassBottomUp(vWorkspace);
		}
		int startLevel = 0;
		for (VDRow r : rows) {
			r.setStartLevel(startLevel);
			r.thirdPassTopDown(vWorkspace);
			startLevel += r.getNumLevels();
		}

		vdTableCells = new VDTableCells(this, vWorksheet, vWorkspace);
	}

	String getRootTableId() {
		return rootTableId;
	}

	VDIndexTable getVdIndexTable() {
		return vdIndexTable;
	}

	int getNumLevels() {
		return rows.size() == 0 ? 0
				: rows.get(rows.size() - 1).getLastLevel() + 1;
	}

	List<VDRow> getRows() {
		return rows;
	}

	public void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		vdTableCells.generateJson(jw, vWorksheet, vWorkspace);
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	JSONWriter prettyPrintJson(JSONWriter jw, boolean verbose)
			throws JSONException {
		jw.object()//
				.key("rootTableId").value(rootTableId)//
				.key("rows").array();
		for (VDRow r : rows) {
			r.prettyPrintJson(jw, verbose);
		}
		jw.endArray();

		jw.key("frontier").array();
		for (VHTreeNode vhn : frontier) {
			jw.value(vhn.getHNode().getId());
		}
		jw.endArray();

		jw.key("indexTable");
		vdIndexTable.prettyPrintJson(jw);

		jw.key("cells");
		vdTableCells.prettyPrintJson(jw);

		jw.endObject();
		return jw;
	}

}
