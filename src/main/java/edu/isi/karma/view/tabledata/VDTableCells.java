/**
 * 
 */
package edu.isi.karma.view.tabledata;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDIndexTable.LeftRight;

/**
 * @author szekely
 * 
 */
public class VDTableCells {

	private final VDCell[][] cells;
	private final int numRows;
	private final int numCols;

	private final VDIndexTable vdIndexTable;

	VDTableCells(VDTableData vdTableData, VWorkspace vWorkspace) {
		this.numCols = vdTableData.getVdIndexTable().getNumColumns();
		this.numRows = vdTableData.getNumLevels();
		cells = new VDCell[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				cells[i][j] = new VDCell();
			}
		}
		this.vdIndexTable = vdTableData.getVdIndexTable();
		populate(vdTableData, vWorkspace);
	}

	private void populate(VDTableData vdTableData, VWorkspace vWorkspace) {
		for (VDRow vdRow : vdTableData.getRows()) {
			populateFromVDRow(vdRow, vWorkspace);
		}

	}

	private void populateFromVDRow(VDRow vdRow, VWorkspace vWorkspace) {
		String fill = vdRow.getFillHTableId();
		LeftRight lr = vdIndexTable.get(vdRow.getContainerHNodeId(vWorkspace));

		for (int i = vdRow.getStartLevel(); i <= vdRow.getLastLevel(); i++) {
			for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
				VDCell c = cells[i][j];
				c.setFillHTableId(fill);
				c.setDepth(vdRow.getDepth());
			}
		}

		for (VDTreeNode n : vdRow.getNodes()) {
			populateFromVDTreeNode(n, vWorkspace);
		}
	}

	private void populateFromVDTreeNode(VDTreeNode n, VWorkspace vWorkspace) {

		if (n.hasNestedTable()) {
			for (VDRow vdRow : n.getNestedTableRows()) {
				populateFromVDRow(vdRow, vWorkspace);
			}
		}
		// It is a leaf node.
		else {
			LeftRight lr = vdIndexTable.get(n.getHNode(vWorkspace).getId());
			VDCell c = cells[n.getStartLevel()][lr.getLeft()];
			c.setDepth(n.getDepth());
			c.setValue(n.getNode().getValue());
		}

	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.array();

		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				jw.object()//
						.key("_row").value(i)//
						.key("_col").value(j)//
				;
				cells[i][j].prettyPrintJson(jw);
				jw.endObject();
			}
		}

		jw.endArray();
	}
}
