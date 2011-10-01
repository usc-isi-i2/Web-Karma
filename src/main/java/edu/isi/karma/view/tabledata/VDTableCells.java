/**
 * 
 */
package edu.isi.karma.view.tabledata;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.getStrokePositionKey;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rows;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.worksheetId;

import java.util.LinkedList;
import java.util.List;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.separatorRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.contentRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.valueCell;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.cellType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.verticalPaddingCell;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.valuePaddingCell;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.horizontalPaddingCell;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.fillId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.hTableId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.value;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.status;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rowType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rowCells;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VTableCssTags;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDCell.MinMaxDepth;
import edu.isi.karma.view.tabledata.VDCell.Position;
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
			c.setNode(n.getNode());
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
		MinMaxDepth topCombinedMinMaxDepth = getMinMaxDepth(index, Position.top);
		MinMaxDepth bottomCombinedMinMaxDepth = getMinMaxDepth(index,
				Position.bottom);
		generateJsonSeparatorRows(index, Position.top, topCombinedMinMaxDepth,
				jw, vWorksheet, vWorkspace);
		generateJsonContentRow(index, jw, topCombinedMinMaxDepth,
				bottomCombinedMinMaxDepth, vWorksheet, vWorkspace);
		generateJsonSeparatorRows(index, Position.bottom,
				bottomCombinedMinMaxDepth, jw, vWorksheet, vWorkspace);
	}

	/**
	 * Generate the separator rows for a specific row in the cells array.
	 * 
	 * @param index
	 *            of a row in the cells array.
	 * @param position
	 *            either top or bottom.
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonSeparatorRows(int index, Position position,
			MinMaxDepth combinedMinMaxDepth, JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {

		// The number of separator rows is determined by the delta between the
		// max and min depth of the cells in the row.
		int numSeparatorRows = Math.max(0, combinedMinMaxDepth.getDelta());

		// the top separators start with the minimum depth, and the ones below
		// correspond to larger depths. There is no top separator for the
		// max depth, as those strokes are part of the content. For bottom
		// margins it is the other way around.
		int currentDepth;
		int increment;
		{
			switch (position) {
			case top:
				currentDepth = combinedMinMaxDepth.getMinDepth();
				increment = 1;
				break;
			case bottom:
				currentDepth = combinedMinMaxDepth.getMaxDepth() - 1;
				increment = -1;
				break;
			default:
				throw new Error("Should call only with top or bottom");
			}

			int i = 0;
			while (i < numSeparatorRows) {
				generateJsonOneSeparatorRow(index, currentDepth, position, jw,
						vWorksheet, vWorkspace);
				i++;
				currentDepth += increment;
			}
		}
	}

	/**
	 * Analyze the whole row and find the depths of all the strokes on all the
	 * cells. We need to know how many separator rows we need to generate.
	 * 
	 * @param index
	 *            of a row.
	 * @param position
	 *            either top or bottom.
	 * @return objet containing the minimum and maximum depths of all the
	 *         strokes (in a given position) in all the cells of a row.
	 */
	private MinMaxDepth getMinMaxDepth(int index, Position position) {
		List<MinMaxDepth> rowMinMaxDepths = new LinkedList<MinMaxDepth>();
		for (int j = 0; j < numCols; j++) {
			rowMinMaxDepths.add(cells[index][j].getMinMaxStrokeDepth(position));
		}
		MinMaxDepth combinedMinMaxDepth = MinMaxDepth.combine(rowMinMaxDepths);
		return combinedMinMaxDepth;
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
			Position position, JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.object().key(rowType.name()).value(separatorRow.name());

		Position strokePosition = position;
		Position positionOfNoBorder = position.getOpposite();
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		jw.key(rowCells.name()).array();
		for (int j = 0; j < numCols; j++) {
			VDCell c = cells[index][j];
			Stroke stroke = c.getStroke(separatorDepth, strokePosition);
			String strokeHTableId = (stroke == null) ? c.getFillHTableId()
					: stroke.getHTableId();
			StrokeStyle style = StrokeStyle.none;
			if (stroke != null && stroke.getDepth() == separatorDepth) {
				style = stroke.getStyle();
			}

			jw.object().key(hTableId.name()).value(strokeHTableId)
					//
					.key(fillId.name()).value(css.getCssTag(strokeHTableId))
					//
					.key(getStrokePositionKey(strokePosition)).value(style)
					//
					.key(getStrokePositionKey(positionOfNoBorder))
					.value(StrokeStyle.none)//
					.key(rowType.name()).value(separatorRow.name())//
					.key(cellType.name()).value(horizontalPaddingCell.name())//
					.key("_row").value(index)//
					.key("_col").value(j)//
					.key("_separatorDepth").value(separatorDepth)//
			;
			jw.endObject();
		}
		jw.endArray();
		jw.endObject();
	}

	/**
	 * The content rows are the TRs that hold the cell values.
	 * 
	 * @param index
	 *            of the row to be generated.
	 * @param jw
	 * @param topCombinedMinMaxDepth
	 * @param bottomCombinedMinMaxDepth
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonContentRow(int index, JSONWriter jw,
			MinMaxDepth topCombinedMinMaxDepth,
			MinMaxDepth bottomCombinedMinMaxDepth, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		jw.object().key(rowType.name()).value(contentRow.name())//
				.key("_row").value(index)//
		;

		jw.key(rowCells.name()).array();
		for (int j = 0; j < numCols; j++) {
			generateJsonContentCell(index, j, topCombinedMinMaxDepth,
					bottomCombinedMinMaxDepth, jw, vWorksheet, vWorkspace);
		}

		jw.endArray().endObject();
	}

	/**
	 * Generate the representation of what will be a TD in the browser.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param topCombinedMinMaxDepth
	 * @param bottomCombinedMinMaxDepth
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonContentCell(int rowIndex, int colIndex,
			MinMaxDepth topCombinedMinMaxDepth,
			MinMaxDepth bottomCombinedMinMaxDepth, JSONWriter jw,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		VDCell c = cells[rowIndex][colIndex];
		CellValue cellValue = c.getNode() == null ? null : c.getNode()
				.getValue();
		String valueString = cellValue == null ? "" : cellValue.asString();
		String codedStatus = c.getNode() == null ? "" : c.getNode().getStatus()
				.getCodedStatus();

		// Even though the TDCell has top strokes, we need to figure out whether
		// those are being drawn in the separators or there is one that should
		// be drawn by the cell itself. We draw such a stroke if the depth of
		// the cell is the same as the depth of the stroke.
		StrokeStyle topStrokeStyle = StrokeStyle.none;
		if (topCombinedMinMaxDepth.getMaxDepth() == c.getDepth()) {
			Stroke topStroke = c.getStroke(c.getDepth(), Position.top);
			if (topStroke != null) {
				topStrokeStyle = topStroke.getStyle();
			}
		}

		StrokeStyle bottomStrokeStyle = StrokeStyle.none;
		if (bottomCombinedMinMaxDepth.getMaxDepth() == c.getDepth()) {
			Stroke bottomStroke = c.getStroke(c.getDepth(), Position.bottom);
			if (bottomStroke != null) {
				bottomStrokeStyle = bottomStroke.getStyle();
			}
		}

		jw.object()
				.key(rowType.name())
				.value(contentRow.name())
				//
				.key(cellType.name())
				.value(c.getNode() == null ? valuePaddingCell.name()
						: valueCell.name())
				//
				.key(value.name())
				.value(valueString)
				//
				.key(status.name())
				.value(codedStatus)
				//
				.key(hTableId.name())
				.value(c.getFillHTableId())
				//
				.key(getStrokePositionKey(Position.top))
				.value(topStrokeStyle)
				//
				.key(getStrokePositionKey(Position.bottom))
				.value(bottomStrokeStyle)
				//
				.key(fillId.name()).value(css.getCssTag(c.getFillHTableId()))
				//
				.key("_row").value(rowIndex)
				//
				.key("_col").value(colIndex)
				//
				.key("_topCombinedMinMaxDepth")
				.value(topCombinedMinMaxDepth.toString())//
				.key("_depth").value(c.getDepth())//
				.endObject();
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
