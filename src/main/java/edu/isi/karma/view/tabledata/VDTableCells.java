/**
 * 
 */
package edu.isi.karma.view.tabledata;

import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.contentRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rowCells;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rowType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.rows;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.separatorRow;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.status;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.value;
import static edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys.worksheetId;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.CellType;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate.JsonKeys;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.VTableCssTags;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDCell.MinMaxDepth;
import edu.isi.karma.view.tabledata.VDCell.Position;
import edu.isi.karma.view.tabledata.VDCellStrokes.StrokeIterator;
import edu.isi.karma.view.tabledata.VDIndexTable.LeftRight;
import edu.isi.karma.view.tabledata.VDTriangle.TriangleLocation;

/**
 * @author szekely
 * 
 */
public class VDTableCells {

	Logger logger = LoggerFactory.getLogger(VDTableCells.class);

	private final VDCell[][] cells;
	private final int numRows;
	private final int numCols;

	private final VDIndexTable vdIndexTable;
	private final VDVerticalSeparators vdVerticalSeparators;

	private final String rootTableId;

	private final Set<Stroke> defaultStrokes = new HashSet<Stroke>();

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
		this.vdVerticalSeparators = vdTableData.getVdVerticalSeparators();
		populate(vdTableData, vWorksheet, vWorkspace);
		setDefaultStrokes(vdTableData, vWorkspace);
	}

	int getNumRows() {
		return numRows;
	}

	int getNumCols() {
		return numCols;
	}

	VDCell get(int rowIndex, int columnIndex) {
		return cells[rowIndex][columnIndex];
	}

	public int getNumHorizontalSeparators(int rowIndex, Position position) {
		int num = 0;
		for (int j = 0; j < numCols; j++) {
			VDCell c = cells[rowIndex][j];
			num = Math.max(num, c.getVdCellStrokes().getNumStrokes(position));
		}
		return num;
	}

	private void populate(VDTableData vdTableData, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		for (VDRow vdRow : vdTableData.getRows()) {
			populateFromVDRow(vdRow, vWorksheet, vWorkspace);
		}
		initializeVDCellStrokes();
	}

	private void initializeVDCellStrokes() {
		for (int i = 0; i < numRows; i++) {
			MinMaxDepth topCombinedMinMaxDepth = getMinMaxDepth(i, Position.top);
			MinMaxDepth bottomCombinedMinMaxDepth = getMinMaxDepth(i,
					Position.bottom);

			int numTop = Math.max(0, topCombinedMinMaxDepth.getDelta()) + 1;
			int minTop = topCombinedMinMaxDepth.getMinDepth();
			int numBottom = Math.max(0, bottomCombinedMinMaxDepth.getDelta()) + 1;
			int minBottom = bottomCombinedMinMaxDepth.getMinDepth();
			for (int j = 0; j < numCols; j++) {
				VDVerticalSeparator vdVS = vdVerticalSeparators
						.get(vdIndexTable.getHNodeId(j));
				VDCellStrokes vdcs = VDCellStrokes.create(vdVS, numTop, minTop,
						numBottom, minBottom);
				vdcs.populateFromVDCell(vdVS, cells[i][j]);
				cells[i][j].setVdCellStrokes(vdcs);
			}
		}
	}

	private void populateFromVDRow(VDRow vdRow, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		String fill = vdRow.getFillHTableId();
		LeftRight lr = vdIndexTable.get(vdRow.getContainerHNodeId(vWorkspace));

		// Set the depth and color of each cell.
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
			// All other rows but the last one.
			else { // a none border at the bottom to create a space when there
					// is a nested table.
				Stroke noneStroke = new Stroke(StrokeStyle.none, fill,
						vdRow.getDepth());
				for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
					cells[vdRow.getLastLevel()][j].addBottomStroke(noneStroke);
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

		{// inner vertical separator strokes.
			// We do them here to make sure they span the height of the row even
			// if each node is not as tall as the whole row.
			Stroke innerStroke = new Stroke(StrokeStyle.inner, fill,
					vdRow.getDepth());
			for (VDTreeNode n : vdRow.getNodes()) {
				LeftRight nodeLr = vdIndexTable.get(n.getHNode(vWorkspace)
						.getId());
				if (!n.isFirst() && n.getNumLevels() > 0) {
					for (int i = vdRow.getStartLevel(); i <= vdRow
							.getLastLevel(); i++) {
						VDCell c = cells[i][nodeLr.getLeft()];
						c.addLeftStroke(innerStroke);
					}
				}
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

		{// A none stroke on the right to create a space when there are nested
			// tables.
			// TODO: I suspect this needs to be done in the ROW part, the same
			// way that the inner strokes are being done.
			Stroke noneStroke = new Stroke(StrokeStyle.none,
					n.getContainerHTableId(vWorkspace), n.getDepth());
			if (!n.isLast() && n.getNumLevels() > 0) {
				for (int i = n.getStartLevel(); i <= n.getLastLevel(); i++) {
					VDCell c = cells[i][lr.getRight()];
					c.addRightStroke(noneStroke);
				}
			}
		}

		Table nestedTable = n.getNode().getNestedTable();
		if (nestedTable != null) {

			// The table is empty
			if (n.getNestedTableRows().isEmpty()) {
				String fillHTableId = n.getNode().getNestedTable()
						.getHTableId();
				Stroke stroke = new Stroke(StrokeStyle.outer, fillHTableId,
						n.getDepth() + 1);
				for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
					VDCell c = cells[n.getStartLevel()][j];
					// System.err.println("EMPTY TABLE row=" + n.getStartLevel()
					// + ", col=" + j);
					c.setDepth(n.getDepth() + 1);
					c.setFillHTableId(fillHTableId);
					c.addTopStroke(stroke);
					c.addBottomStroke(stroke);
					c.setNodeIdWhenPartOfEmptyTable(n.getNode());
				}
				cells[n.getStartLevel()][lr.getLeft()].addLeftStroke(stroke);
				cells[n.getStartLevel()][lr.getRight()].addRightStroke(stroke);
			}
			// The table is not empty.
			else {
				for (VDRow vdRow : n.getNestedTableRows()) {
					populateFromVDRow(vdRow, vWorksheet, vWorkspace);
				}
			}
		}
		// It is a leaf node.
		else {
			VDCell c = cells[n.getStartLevel()][lr.getLeft()];
			c.setDepth(n.getDepth());
			c.setNode(n.getNode());
		}

	}

	private void setDefaultStrokes(VDTableData vdTableData,
			VWorkspace vWorkspace) {
		for (VDRow vdRow : vdTableData.getRows()) {
			setDefaultStrokesVDRow(vdRow, vWorkspace);
		}
	}

	private void setDefaultStrokesVDRow(VDRow vdRow, VWorkspace vWorkspace) {
		String fill = vdRow.getFillHTableId();
		LeftRight lr = vdIndexTable.get(vdRow.getContainerHNodeId(vWorkspace));
		Stroke stroke = new Stroke(StrokeStyle.none, fill, vdRow.getDepth());
		for (int i = vdRow.getStartLevel(); i <= vdRow.getLastLevel(); i++) {
			for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
				cells[i][j].setDefaultStrokes(stroke, defaultStrokes);
			}
		}

		for (VDTreeNode n : vdRow.getNodes()) {
			setDefaultStrokesFromVDTreeNode(n, vWorkspace);
		}
	}

	private void setDefaultStrokesFromVDTreeNode(VDTreeNode n,
			VWorkspace vWorkspace) {
		Table nestedTable = n.getNode().getNestedTable();
		LeftRight lr = vdIndexTable.get(n.getHNode(vWorkspace).getId());
		if (nestedTable != null) {
			if (n.getNestedTableRows().isEmpty()) {
				String fill = n.getNode().getNestedTable().getHTableId();
				Stroke stroke = new Stroke(StrokeStyle.none, fill,
						n.getDepth() + 1);
				for (int j = lr.getLeft(); j <= lr.getRight(); j++) {
					cells[n.getStartLevel()][j].setDefaultStrokes(stroke,
							defaultStrokes);
				}
			} else {
				for (VDRow vdRow : n.getNestedTableRows()) {
					setDefaultStrokesVDRow(vdRow, vWorkspace);
				}
			}
		}

		// The table is not empty. Not sure this is needed.
		else {
			Stroke stroke = new Stroke(StrokeStyle.none,
					n.getContainerHTableId(vWorkspace), n.getDepth());
			cells[n.getStartLevel()][lr.getLeft()].setDefaultStrokes(stroke,
					defaultStrokes);
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

		VDCell c = cells[index][0];
		VDCellStrokes vdcs = c.getVdCellStrokes();
		StrokeIterator it = vdcs.iterator(position);

		while (it.hasNext()) {
			generateJsonOneSeparatorRow(index, it.next().getDepth(), position,
					jw, vWorksheet, vWorkspace);
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
	 * @param position
	 * @param combinedMinMaxDepth
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
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		jw.key(rowCells.name()).array();
		for (int j = 0; j < numCols; j++) {
			VDCell c = cells[index][j];
			int columnDepth = vdIndexTable.getColumnDepth(j) - 1;

			// vertical separators.
			generateJsonVerticalSeparators(Position.left, position, index, j,
					separatorDepth, jw, vWorksheet, vWorkspace);

			// Now the horizontal separator cell top/bottom strokes.
			Stroke strokeTB = c.getVdCellStrokes().getStroke(strokePosition,
					separatorDepth);

			// ///////////////////// This is incorrect when the cell is empty.
			// Now calculate the left and right strokes.
			StrokeStyle leftStrokeStyle = StrokeStyle.none;
			if (separatorDepth >= columnDepth) {
				leftStrokeStyle = c.getVdCellStrokes()
						.getStroke(Position.left, separatorDepth).getStyle();
			}

			StrokeStyle rightStrokeStyle = StrokeStyle.none;
			if (separatorDepth >= columnDepth) {
				rightStrokeStyle = c.getVdCellStrokes()
						.getStroke(Position.right, separatorDepth).getStyle();
			}

			StrokeStyles strokeStyles = new StrokeStyles();
			strokeStyles.setStrokeStyle(strokePosition, strokeTB.getStyle());
			strokeStyles.setStrokeStyle(Position.left, leftStrokeStyle);
			strokeStyles.setStrokeStyle(Position.right, rightStrokeStyle);

			String attributes = encodeForJson(CellType.rowSpace,
					strokeTB.getHTableId(),
					css.getCssTag(strokeTB.getHTableId(), c.getDepth()),
					strokeStyles);

			jw.object()
					.key(JsonKeys.attr.name())
					.value(attributes)
					//
					.key("_row")
					.value(index)
					//
					.key("_col")
					.value(j)
					//
					.key("_depth")
					.value(c.getDepth())
					//
					.key("_columnDepth")
					.value(columnDepth)
					//
					.key("_horizontalSeparatorDepth")
					.value(separatorDepth)
					//
					.key("_leftStrokes")
					.value(Stroke.toString(c.getLeftStrokes()))
					//
					.key("_rightStrokes")
					.value(Stroke.toString(c.getRightStrokes()))
					//
					.key("_topStrokes")
					.value(Stroke.toString(c.getTopStrokes()))
					//
					.key("_bottomStrokes")
					.value(Stroke.toString(c.getBottomStrokes()))//
					.key("_position").value(position.name())//
			;

			jw.key("_vdCellStrokes");
			c.getVdCellStrokes().prettyPrintJson(jw);

			jw.endObject();

			// Now the vertical separators on the right.
			generateJsonVerticalSeparators(Position.right, position, index, j,
					separatorDepth, jw, vWorksheet, vWorkspace);
		}
		jw.endArray();
		jw.endObject();
	}

	/**
	 * Generate the vertical separators, both in the horizontal separator rows
	 * and content rows.
	 * 
	 * @param leftRight
	 *            , is this separator left or right from its base cell.
	 * @param topBottom
	 *            , is this separator top or bottom from its base cell.
	 * @param rowIndex
	 *            , the row index of the base cell.
	 * @param columnIndex
	 *            , the column index of the base cell.
	 * @param horizontalSeparatorDepth
	 *            , when there are multiple horizontal separators, each one has
	 *            a depth.
	 * @param jw
	 * @param vWorksheet
	 * @param vWorkspace
	 * @throws JSONException
	 */
	private void generateJsonVerticalSeparators(Position leftRight,
			Position topBottom, int rowIndex, int columnIndex,
			int horizontalSeparatorDepth, JSONWriter jw, VWorksheet vWorksheet,
			VWorkspace vWorkspace) throws JSONException {
		VDCell c = cells[rowIndex][columnIndex];
		VDCellStrokes vdcs = c.getVdCellStrokes();
		StrokeIterator it = vdcs.iterator(leftRight);

		while (it.hasNext()) {
			generateJsonOneVerticalSeparators(leftRight, topBottom, rowIndex,
					columnIndex, horizontalSeparatorDepth, it.next(), jw,
					vWorksheet, vWorkspace);
		}
	}

	private void generateJsonOneVerticalSeparators(Position leftRight,
			Position topBottom, int rowIndex, int colIndex,
			int horizontalSeparatorDepth, Stroke columnSeparatorStroke,
			JSONWriter jw, VWorksheet vWorksheet, VWorkspace vWorkspace)
			throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		VDCell c = cells[rowIndex][colIndex];
		int columnDepth = vdIndexTable.getColumnDepth(colIndex) - 1;

		boolean isCorner = (columnSeparatorStroke.getDepth() == horizontalSeparatorDepth);
		boolean isLeftRightOfCorner = (columnSeparatorStroke.getDepth() > horizontalSeparatorDepth);
		boolean isTopBottomOfCorner = (horizontalSeparatorDepth > columnSeparatorStroke
				.getDepth());

		StrokeStyle leftRightStrokeStyle = StrokeStyle.none;
		StrokeStyle topBottomStrokeStyle = StrokeStyle.none;
		StrokeStyle leftRightOppositeStrokeStyle = StrokeStyle.none;
		StrokeStyle topBottomOppositeStrokeStyle = StrokeStyle.none;
		String hTableId = columnSeparatorStroke.getHTableId();

		String lrMessage = "";
		String tbMessage = "";
		if (isCorner) {
			Stroke strokeLR = c.getStroke(horizontalSeparatorDepth, leftRight);
			Stroke strokeTB = c.getStroke(columnSeparatorStroke.getDepth(),
					topBottom);
			if (strokeLR != null) {
				leftRightStrokeStyle = strokeLR.getStyle();
				hTableId = strokeLR.getHTableId();
			} else {
				logger.error("CRASH_isCorner_LR: row=" + rowIndex + ", column="
						+ colIndex + ", horizontalSeparatorDepth="
						+ horizontalSeparatorDepth);
				lrMessage = "*** ERROR, LR is empty for "
						+ horizontalSeparatorDepth;
			}
			if (strokeTB != null) {
				topBottomStrokeStyle = strokeTB.getStyle();
			} else {
				logger.error("CRASH_isCorner_TB: row" + rowIndex + ", column="
						+ colIndex + ", horizontalSeparatorDepth="
						+ horizontalSeparatorDepth);
				tbMessage = "*** ERROR, TB is empty for "
						+ columnSeparatorStroke.getDepth();
			}

			// For empty tables we need to draw the whole thing all around.
			if (c.isForEmptyTable() && horizontalSeparatorDepth == c.getDepth()) {
				topBottomOppositeStrokeStyle = topBottomStrokeStyle;
				String hNodeId = c.getNodeIdWhenPartOfEmptyTable().getHNodeId();
				LeftRight lf = vdIndexTable.get(hNodeId);
				switch (leftRight) {
				case left:
					if (lf.getLeft() == colIndex) {
						leftRightOppositeStrokeStyle = topBottomStrokeStyle;
					}
					break;
				case right:
					if (lf.getRight() == colIndex) {
						leftRightOppositeStrokeStyle = topBottomStrokeStyle;
					}
					break;
				}
			}
		}

		else if (isLeftRightOfCorner) {
			Stroke stroke = c.getStroke(horizontalSeparatorDepth, topBottom);
			leftRightStrokeStyle = StrokeStyle.none;
			if (stroke != null) {
				topBottomStrokeStyle = stroke.getStyle();
				hTableId = stroke.getHTableId();
			}
		}

		else if (isTopBottomOfCorner) {
			Stroke stroke = c.getStroke(columnSeparatorStroke.getDepth(),
					leftRight);
			if (stroke != null) {
				leftRightStrokeStyle = stroke.getStyle();
				topBottomStrokeStyle = StrokeStyle.none;
				hTableId = stroke.getHTableId();
			} else {
				logger.error("CRASH_isTopBottomOfCorner: row" + rowIndex
						+ ", column=" + colIndex
						+ ", horizontalSeparatorDepth="
						+ horizontalSeparatorDepth);
				lrMessage = "*** ERROR, LR is empty";
			}
		}

		StrokeStyles strokeStyles = new StrokeStyles();
		strokeStyles.setStrokeStyle(leftRight, leftRightStrokeStyle);
		strokeStyles.setStrokeStyle(topBottom, topBottomStrokeStyle);
		strokeStyles.setStrokeStyle(leftRight.getOpposite(),
				leftRightOppositeStrokeStyle);
		strokeStyles.setStrokeStyle(topBottom.getOpposite(),
				topBottomOppositeStrokeStyle);

		String attributes = encodeForJson(CellType.columnSpace, hTableId,
				css.getCssTag(hTableId, c.getDepth()), strokeStyles);

		String debugCorners = isCorner ? "corner"
				: (isLeftRightOfCorner ? "leftRight" : "topBottom");
		jw.object()
				.key(JsonKeys.attr.name())
				.value(attributes)
				//
				.key("_EmptyT")
				.value("Empty= " + c.isForEmptyTable())
				//
				.key("_row")
				.value(rowIndex)
				//
				.key("_col")
				.value(colIndex)
				//
				.key("_columnDepth")
				.value(columnDepth)
				//
				.key("_depth")
				.value(c.getDepth())
				//
				.key("_horizontalSeparatorDepth")
				.value(horizontalSeparatorDepth)
				//
				.key("columnSeparatorStroke")
				.value(columnSeparatorStroke.toString())
				//
				.key("_corner")
				.value(debugCorners)
				//
				.key("_LR")
				.value(leftRight.name())
				//
				.key("_LR_strokes")
				.value(Stroke.toString(c.getStrokeList(leftRight)) + lrMessage)
				//
				.key("_TB")
				.value(topBottom.name())
				//
				.key("_TB_Strokes")
				.value(Stroke.toString(c.getStrokeList(topBottom)) + tbMessage)
				//
				.key("_leftStrokes")
				.value(Stroke.toString(c.getLeftStrokes()))
				//
				.key("_rightStrokes")
				.value(Stroke.toString(c.getRightStrokes()))
				//
				.key("_topStrokes").value(Stroke.toString(c.getTopStrokes()))
				//
				.key("_bottomStrokes")
				.value(Stroke.toString(c.getBottomStrokes()))//
		;
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
			generateJsonContentCell(index, j, jw, vWorksheet, vWorkspace);
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
			JSONWriter jw, VWorksheet vWorksheet, VWorkspace vWorkspace)
			throws JSONException {
		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		VDCell c = cells[rowIndex][colIndex];
		// Cannot use columnDepth because the cell may have an empty table that
		// has the potential to have further nested tables. When the table is
		// empty, the columnDepth may be larger than the actual depth of the
		// cell.
		int columnDepth = vdIndexTable.getColumnDepth(colIndex) - 1;
		int cellDepth = c.isForEmptyTable() ? c.getDepth() : columnDepth;

		// vertical separators.
		// Using Position.top is arbitrary, just testing to see whether it
		// works.
		generateJsonVerticalSeparators(Position.left, Position.top, rowIndex,
				colIndex, cellDepth, jw, vWorksheet, vWorkspace);

		CellValue cellValue = c.getNode() == null ? null : c.getNode()
				.getValue();
		String valueString = cellValue == null ? "" : cellValue.asString();
		String codedStatus = c.getNode() == null ? "" : c.getNode().getStatus()
				.getCodedStatus();

		StrokeStyles strokeStyles = new StrokeStyles();
		c.getVdCellStrokes().populateStrokeStyles(strokeStyles);

		String attributes = encodeForJson(
				c.getNode() == null ? CellType.dummyContent : CellType.content,
				c.getFillHTableId(),
				css.getCssTag(c.getFillHTableId(), c.getDepth()), strokeStyles);

		jw.object().key(JsonKeys.attr.name())
				.value(attributes)
				//
				.key(value.name())
				.value(valueString)
				//
				.key(status.name())
				.value(codedStatus)
				//
				.key("_row")
				.value(rowIndex)
				//
				.key("_col")
				.value(colIndex)
				// //
				// .key("_topCombinedMinMaxDepth")
				// .value(topCombinedMinMaxDepth.toString())
				// //
				// .key("_columnDepth").value(columnDepth)
				//
				.key("_depth").value(c.getDepth())
				//
				.key("_leftStrokes").value(Stroke.toString(c.getLeftStrokes()))
				//
				.key("_rightStrokes")
				.value(Stroke.toString(c.getRightStrokes()))//
		;

		jw.key("_vdCellStrokes");
		c.getVdCellStrokes().prettyPrintJson(jw);

		jw.endObject();

		// vertical separators.
		// Using Position.top is arbitrary, just testing to see whether it
		// works.
		// Need to clean up the code.
		generateJsonVerticalSeparators(Position.right, Position.top, rowIndex,
				colIndex, cellDepth, jw, vWorksheet, vWorkspace);
	}

	private String encodeForJson(CellType cellType, String hTableId,
			String fillId, StrokeStyles strokeStyles) {
		return cellType.code()//
				+ ":" + hTableId + ":" + fillId//
				+ ":" + strokeStyles.getJsonEncoding();
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
