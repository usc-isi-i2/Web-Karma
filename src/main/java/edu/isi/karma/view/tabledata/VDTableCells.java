/**
 * 
 */
package edu.isi.karma.view.tabledata;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.worksheetId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.rows;


import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDIndexTable.LeftRight;
import edu.isi.karma.view.tabledata.VDTriangle.TriangleLocation;

/**
 * @author szekely
 * 
 */
public class VDTableCells {

	private final VDCell[][] cells;
	private final int numRows;
	private final int numCols;

	private final VDIndexTable vdIndexTable;

	private final String rootTableId;

	VDTableCells(VDTableData vdTableData, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		this.numCols = vdTableData.getVdIndexTable().getNumColumns();
		this.numRows = vdTableData.getNumLevels();
		cells = new VDCell[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				cells[i][j] = new VDCell();
			}
		}
		this.vdIndexTable = vdTableData.getVdIndexTable();
		this.rootTableId = vWorksheet.getWorksheet().getDataTable().getId();

		populate(vdTableData, vWorksheet, vWorkspace);
	}

	private void populate(VDTableData vdTableData, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		for (VDRow vdRow : vdTableData.getRows()) {
			populateFromVDRow(vdRow, vWorksheet, vWorkspace);
		}
	}

	private void populateFromVDRow(VDRow vdRow, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		String fill = vdRow.getFillHTableId();
		LeftRight lr = vdIndexTable.get(vdRow.getContainerHNodeId(vWorkspace));

		for (int i = vdRow.getStartLevel(); i <= vdRow.getLastLevel(); i++) {
			for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
				VDCell c = cells[i][j];
				c.setFillHTableId(fill);
				c.setDepth(vdRow.getDepth());
			}

		}

		{// top strokes
			Stroke topStroke = new Stroke(vdRow.isFirst() ? StrokeStyle.outer
					: StrokeStyle.inner, fill, vdRow.getDepth());
			for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
				VDCell c = cells[vdRow.getStartLevel()][j];
				c.addTopStroke(topStroke);
			}
		}

		{// bottom strokes
			if (vdRow.isLast()) {
				Stroke bottomStroke = new Stroke(StrokeStyle.outer, fill,
						vdRow.getDepth());
				for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
					VDCell c = cells[vdRow.getLastLevel()][j];
					c.addBottomStroke(bottomStroke);
				}
			}
		}

		{// left/right outer strokes
			// Inner strokes are computed in populateFromVDTreeNode.
			Stroke outerStroke = new Stroke(StrokeStyle.outer, fill,
					vdRow.getDepth());
			for (int i = vdRow.getStartLevel(); i <= vdRow.getLastLevel(); i++) {
				cells[i][lr.getLeft()].addLeftStroke(outerStroke);
				cells[i][lr.getRight()].addRightStroke(outerStroke);
			}
		}

		{// triangles
			cells[vdRow.getStartLevel()][lr.getLeft()]
					.addTriangle(new VDTriangle(fill, vdRow.getRow().getId(),
							vdRow.getDepth(), TriangleLocation.topLeft));
			cells[vdRow.getStartLevel()][lr.getRight()]
					.addTriangle(new VDTriangle(fill, vdRow.getRow().getId(),
							vdRow.getDepth(), TriangleLocation.topRight));
			cells[vdRow.getLastLevel()][lr.getLeft()]
					.addTriangle(new VDTriangle(fill, vdRow.getRow().getId(),
							vdRow.getDepth(), TriangleLocation.bottomLeft));
			cells[vdRow.getLastLevel()][lr.getRight()]
					.addTriangle(new VDTriangle(fill, vdRow.getRow().getId(),
							vdRow.getDepth(), TriangleLocation.bottomRight));
		}

		{// table pagers
			if (vdRow.isLast()) {
				VDTreeNode vdNode = vdRow.getContainerVDNode();
				String tableId = (vdNode == null) ? rootTableId : vdNode
						.getNode().getNestedTable().getId();
				TablePager pager = vWorksheet.getTablePager(tableId);
				if (!pager.isAllRowsShown()) {
					cells[vdRow.getLastLevel()][lr.getLeft()].addPager(pager);
				}
			}
		}

		for (VDTreeNode n : vdRow.getNodes()) {
			populateFromVDTreeNode(n, vWorksheet, vWorkspace);
		}
	}

	private void populateFromVDTreeNode(VDTreeNode n, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {

		LeftRight lr = vdIndexTable.get(n.getHNode(vWorkspace).getId());

		{// inner vertical separator strokes
			Stroke innerStroke = new Stroke(StrokeStyle.inner,
					n.getContainerHTableId(vWorkspace), n.getDepth());
			if (!n.isFirst() && n.getNumLevels() > 0) {
				for (int i = n.getStartLevel(); i <= n.getLastLevel(); i++) {
					VDCell c = cells[i][lr.getLeft()];
					c.addLeftStroke(innerStroke);
				}
			}
		}

		if (n.hasNestedTable()) {
			for (VDRow vdRow : n.getNestedTableRows()) {
				populateFromVDRow(vdRow, vWorksheet, vWorkspace);
			}
		}
		// It is a leaf node.
		else {
			VDCell c = cells[n.getStartLevel()][lr.getLeft()];
			c.setDepth(n.getDepth());
			c.setValue(n.getNode().getValue());
		}

	}

	public void generateJson(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		try {
			jw.object()
					.key(AbstractUpdate.GenericJsonKeys.updateType.name())
					.value(WorksheetHierarchicalDataUpdate.class
							.getSimpleName())
					//
					.key(worksheetId.name())
					.value(vWorksheet.getWorksheet().getId())//
					.key(rows.name()).array()//
			;
			jw.endArray();
			jw.endObject();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
						.key("_ row").value(i)// //Want rows displayed before in
												// the JSON output.
						.key("__col").value(j)//
				;
				cells[i][j].prettyPrintJson(jw);
				jw.endObject();
			}
		}

		jw.endArray();
	}

}
