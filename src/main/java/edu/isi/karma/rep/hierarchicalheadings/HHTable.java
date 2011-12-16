package edu.isi.karma.rep.hierarchicalheadings;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;

public class HHTable {
	private HHCell[][] cells;
	private boolean NO_SEPARATORS_FLAG = true;

	public enum CellJsonKeys {
		cellType, hNodeId, colSpan, fillId, heading, headingPadding, contentCell, cells
	}

	public enum BorderJsonKeys {
		Border, topBorder, leftBorder, rightBorder
	}

	public HHCell[][] getCells() {
		return cells;
	}

	public void constructCells(HHTree hHtree) {
		// We construct a matrix of cells with height equal to the maximum depth
		// in the HHTree and width equal to number of columns i.e. root nodes
		int rootNodesCount = hHtree.countRootNodes();
		int maxDepth = hHtree.getMaxDepth();

//		System.out.println("Constructing matrix of height: " + (maxDepth + 1)
//				+ " and width: " + rootNodesCount);
		cells = new HHCell[maxDepth + 1][rootNodesCount];

		/*** Create HHCells using the HHTNode traversing HHTree top down ***/
		populateHHCells(hHtree.getRootNodes(), 0);

		// Debug, Print the cells array
//		for (int i = 0; i < cells.length; i++) {
//			System.out.println("Row: " + i);
//			for (int j = 0; j < cells[i].length; j++) {
//				System.out.println("Column: " + j);
//				// System.out.println(cells[i][j]);
//				if (cells[i][j] != null)
//					System.out.println(cells[i][j]);
//				else
//					System.out.println("null");
//			}
//		}
	}

	private void populateHHCells(List<HHTNode> nodes, int rowIndex) {
		for (HHTNode node : nodes) {
			HHCell cell = new HHCell();
			int colIndex = node.getStartCol();

			// Set the TNode
			cell.settNode(node.gettNode());

			// Set the depth
			cell.setDepth(node.getDepth());

			// Set the col span
			cell.setColspan(node.getEndCol() - node.getStartCol() + 1);

			// Set the top border
			cell.setTopBorder(new Stroke(StrokeStyle.outer, node.gettNode()
					.getId(), node.getDepth()));

			// Calculate the col span to be used in the HTML table
			cell.setHtmlTableColSpan(node.getHTMLColSpan());

			// Set the left borders
			Stroke[] leftStrokes = new Stroke[node.getLeftStrokes().size()];
			node.getLeftStrokes().toArray(leftStrokes);
			cell.setLeftBorders(leftStrokes);

			// Set the right borders
			Stroke[] rightStrokes = new Stroke[node.getRightStrokes().size()];
			node.getRightStrokes().toArray(rightStrokes);
			cell.setRightBorders(rightStrokes);

			// Set the cell in the right position in the HHTable
			cells[rowIndex][colIndex] = cell;

			// Copy the HHCell info to the HHCells below in the same column
			copyToHHCellsBelow(cell, rowIndex, colIndex);

			if (!node.isLeaf())
				populateHHCells(node.getChildren(), rowIndex + 1);
		}
	}

	private void copyToHHCellsBelow(HHCell cell, int rowIndex, int colIndex) {
		for (int i = rowIndex + 1; i < cells.length; i++) {
			HHCell newCell = new HHCell();
			// Copy the colspan
			newCell.setColspan(cell.getColspan());

			// Copy the HTML col span
			newCell.setHtmlTableColSpan(cell.getHtmlTableColSpan());

			// Copy the depth
			newCell.setDepth(cell.getDepth());

			// Copy the borders
			newCell.setLeftBorders(cell.getLeftBorders().clone());
			newCell.setRightBorders(cell.getRightBorders().clone());

			// Set the correct top border
			newCell.setTopBorder(new Stroke(StrokeStyle.none, cell.gettNode()
					.getId(), cell.getDepth()));

			newCell.setDummy(true);

			// Set it at the right place in the HHTable
			cells[i][colIndex] = newCell;
		}
	}

	private void populateLeftBorders(HHCell cell, JSONArray cellArray,
			ColorKeyTranslator translator, int rowIndex) {
		// Go through each left border
		for (int i = cell.getLeftBorders().length - 1; i >= 0; i--) {
			Stroke border = cell.getLeftBorders()[i];
			// Special case for the first left border
			// In case below, the left border is drawn outside the content cell
			if ((i == 0 && cell.hasLeafTNode()) || (i == 0 && cell.isDummy())) {
				continue;
			}

			try {
				JSONObject borderObj = new JSONObject();

				borderObj.put(CellJsonKeys.cellType.name(),
						BorderJsonKeys.Border.name());
				borderObj.put(CellJsonKeys.fillId.name(),
						translator.getCssTag("", border.getDepth()));

				// Fill the top border
				if (rowIndex == border.getDepth()) {
					borderObj.put(BorderJsonKeys.topBorder.name(), "outer:"
							+ translator.getCssTag("", border.getDepth()));
				} else {
					borderObj.put(BorderJsonKeys.topBorder.name(), "none:"
							+ translator.getCssTag("", border.getDepth()));
				}

				// Fill the left border
				borderObj.put(
						BorderJsonKeys.leftBorder.name(),
						border.getStyle().name() + ":"
								+ translator.getCssTag("", border.getDepth()));

				// Fill the right border
				borderObj.put(BorderJsonKeys.rightBorder.name(), "none:"
						+ translator.getCssTag("", border.getDepth()));

				cellArray.put(borderObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void populateContentCell(HHCell cell, JSONArray cellArray,
			ColorKeyTranslator translator) {
		JSONObject cellObj = new JSONObject();

		try {
			if (cell.isDummy()) { // Generate heading padding
				cellObj.put(CellJsonKeys.cellType.name(),
						CellJsonKeys.headingPadding.name());
				// Fill the top border
				cellObj.put(BorderJsonKeys.topBorder.name(), "none:"
						+ translator.getCssTag("", cell.getDepth()));
			} else { // Generate heading
				cellObj.put(CellJsonKeys.cellType.name(),
						CellJsonKeys.heading.name());
				cellObj.put(CellJsonKeys.hNodeId.name(), cell.gettNode()
						.getId());
				// Fill the top border
				cellObj.put(BorderJsonKeys.topBorder.name(), "outer:"
						+ translator.getCssTag("", cell.getDepth()));
			}

			if(!NO_SEPARATORS_FLAG)
				cellObj.put(CellJsonKeys.colSpan.name(), cell.getHtmlTableColSpan());
			else {
				cellObj.put(CellJsonKeys.colSpan.name(), cell.getColspan());
			}
			
			cellObj.put(CellJsonKeys.fillId.name(),
					translator.getCssTag("", cell.getDepth()));

			// Populate with the content (if not dummy)
			if (!cell.isDummy())
				cellObj.put(CellJsonKeys.contentCell.name(), cell.gettNode()
						.generateJsonObject());

			// Fill the left and right border
			if (cell.hasLeafTNode() || cell.isDummy()) {
				Stroke leftBorder = cell.getLeftBorders()[0];
				Stroke rightBorder = cell.getRightBorders()[0];

				cellObj.put(
						BorderJsonKeys.leftBorder.name(),
						leftBorder.getStyle().name() + ":"
								+ translator.getCssTag("", cell.getDepth()));
				cellObj.put(
						BorderJsonKeys.rightBorder.name(),
						rightBorder.getStyle().name() + ":"
								+ translator.getCssTag("", cell.getDepth()));
			} else {
				if(!NO_SEPARATORS_FLAG) {
					cellObj.put(BorderJsonKeys.leftBorder.name(), "none:"
							+ translator.getCssTag("", cell.getDepth()));
					cellObj.put(BorderJsonKeys.rightBorder.name(), "none:"
							+ translator.getCssTag("", cell.getDepth()));
				} else {
					cellObj.put(BorderJsonKeys.leftBorder.name(), "outer:"
							+ translator.getCssTag("", cell.getDepth()));
					cellObj.put(BorderJsonKeys.rightBorder.name(), "outer:"
							+ translator.getCssTag("", cell.getDepth()));
				}
			}

			cellArray.put(cellObj);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void populateRightBorders(HHCell cell, JSONArray cellArray,
			ColorKeyTranslator translator, int rowIndex) {
		// Go through each right border
		for (int i = 0; i < cell.getRightBorders().length; i++) {
			Stroke border = cell.getRightBorders()[i];
			// Special case for the first right border
			// In case below, the right border is drawn outside the content cell
			if ((i == 0 && cell.hasLeafTNode()) || (i == 0 && cell.isDummy())) {
				continue;
			}

			try {
				JSONObject borderObj = new JSONObject();

				borderObj.put(CellJsonKeys.cellType.name(),
						BorderJsonKeys.Border.name());
				borderObj.put(CellJsonKeys.fillId.name(),
						translator.getCssTag("", border.getDepth()));

				// Fill the top border
				if (rowIndex == border.getDepth()) {
					borderObj.put(BorderJsonKeys.topBorder.name(), "outer:"
							+ translator.getCssTag("", border.getDepth()));
				} else {
					borderObj.put(BorderJsonKeys.topBorder.name(), "none:"
							+ translator.getCssTag("", border.getDepth()));
				}

				// Fill the left border
				borderObj.put(BorderJsonKeys.leftBorder.name(), "none:"
						+ translator.getCssTag("", border.getDepth()));

				// Fill the right border
				borderObj.put(
						BorderJsonKeys.rightBorder.name(),
						border.getStyle().name() + ":"
								+ translator.getCssTag("", border.getDepth()));

				cellArray.put(borderObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void generateJson(PrintWriter pw, ColorKeyTranslator translator, boolean NO_SEPARATOR_FLAG_VALUE) {
		NO_SEPARATORS_FLAG = NO_SEPARATOR_FLAG_VALUE;
		JSONArray rows = new JSONArray();
		try {
			for (int row = 0; row < cells.length; row++) {
				JSONObject rowObj = new JSONObject();
				JSONArray cellArray = new JSONArray();

				for (int col = 0; col < cells[row].length;) {
					HHCell cell = cells[row][col];

					// Print the left borders
					if(!NO_SEPARATORS_FLAG)
						populateLeftBorders(cell, cellArray, translator, row);

					// Print the content cell (if any)
					populateContentCell(cell, cellArray, translator);

					// Print the right borders
					if(!NO_SEPARATORS_FLAG)
						populateRightBorders(cell, cellArray, translator, row);

					col += cell.getColspan();
				}
				rowObj.put(CellJsonKeys.cells.name(), cellArray);
				rows.put(rowObj);
			}

			pw.println(rows.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
