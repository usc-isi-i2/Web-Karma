/**
 * 
 */
package edu.isi.karma.view.tabledata;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rows;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.worksheetId;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.separatorRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.contentRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.normalCell;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.verticalPaddingCell;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rowType;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VWorksheet;
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
			generateAllJsonRows(jw, vWorksheet, vWorkspace);
			jw.endArray();
			jw.endObject();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Generate all the TRs for content and separators.
	 * 
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateAllJsonRows(JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		int index = 0;
		while (index < numRows) {
			generateJsonRows(index, jw, vWorksheet, vWorkspace);
			index++;
		}
	}

	/**
	 * Generate the TR rows for a specific row in the cells array.
	 * 
	 * @param index
	 *            in the cells array.
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonRows(int index, JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		generateJsonSeparatorRows(index, true /* top */, jw, vWorksheet,
				vWorkspace);
		generateJsonContentRow(index, jw, vWorksheet, vWorkspace);
		generateJsonSeparatorRows(index, false/* bottom */, jw, vWorksheet,
				vWorkspace);
	}

	/**
	 * Generate the separator rows for a specific row in the cells array.
	 * 
	 * @param index
	 *            in the cells array.
	 * @param isTopSeparator
	 *            , if true, the separators go on top, otherwise in the bottom.
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonSeparatorRows(int index, boolean isTopSeparator,
			JSONWriter jw, VWorksheet vWorksheet, VWorkspace vWorkspace)
			throws JSONException {
		// The number of separator rows is determined by the delta between the
		// max and min depth of the cells in the row.
		int maxDepth = 0;
		int minDepth = 0;
		for (int j = 0; j < numCols; j++) {
			int depth = cells[index][j].getDepth();
			maxDepth = Math.max(maxDepth, depth);
			minDepth = Math.min(minDepth, depth);
		}
		int numSeparatorRows = maxDepth - minDepth;

		// the top separators start with the minimum depth, and the ones below
		// correspond to larger depths. There is no top separator for the
		// max depth, as those strokes are part of the content. For bottom
		// margins it is the other way around.
		int currentDepth = isTopSeparator ? minDepth : maxDepth - 1;
		int depthIncrementer = isTopSeparator ? 1 : -1;
		int i = 0;
		while (i < numSeparatorRows) {
			generateJsonOneSeparatorRow(index, currentDepth, isTopSeparator,
					jw, vWorksheet, vWorkspace);
			i++;
			currentDepth += depthIncrementer;
		}
	}

	/**
	 * Generate a single separator TR row for a specific row in the cells array.
	 * 
	 * @param index
	 *            of the cells row we are generating separators for.
	 * @param separatorDepth
	 *            used to find the corresponding Stroke in the cell.
	 * @param isTopSeparator
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonOneSeparatorRow(int index, int separatorDepth,
			boolean isTopSeparator, JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.object().key(rowType.name()).value(separatorRow.name());

		jw.endObject();
	}

	private void generateJsonContentRow(int index, JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		jw.object().key(rowType.name()).value(contentRow.name());

		jw.endObject();
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
