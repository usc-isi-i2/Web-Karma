/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.util.JSONUtil;

/**
 * Entries are generated for "cells" in the top table. Each cell may be a fat
 * cell, containing multiple cells corresponding to possibly multiple levels of
 * nested tables.
 * 
 * @author szekely
 * 
 */
public class VRowEntry {

	private final String hNodePath;
	private final String tableCssTag;

	private final ArrayList<VCell> cells = new ArrayList<VCell>();

	VRowEntry(String path, String tableCssTag) {
		this.hNodePath = path;
		this.tableCssTag = tableCssTag;
	}

	String getHNodePath() {
		return hNodePath;
	}

	void addCell(VCell cell) {
		cells.add(cell);
	}

	ArrayList<VCell> _getCells() {
		return cells;
	}

	private static String getRowPathRoot(String rowPath) {
		int indexOfSlash = rowPath.indexOf("/");
		return indexOfSlash == -1 ? rowPath : rowPath
				.substring(0, indexOfSlash);
	}

	/**
	 * Assign rowSpans and rowIndices to each cell in the row entry. The span is
	 * the max number of times the rowPath gets mentioned in the rowPathCounts.
	 * 
	 * @param rowPathCounts
	 */
	void assignRowIndices(RowPathCountsByColumn rowPathCounts) {
		int totalSpan = 0;
		Iterator<VCell> it = cells.iterator();
		while (it.hasNext()) {
			VCell vc = it.next();
			boolean isLast = !it.hasNext();
			int span = rowPathCounts.getMaxCount(vc.getRowPath());
			// If it is the last row, we grow the span to cover the rest of the dummy cells.
			if (isLast) {
				String rootRowPath = getRowPathRoot(vc.getRowPath());
				span = rowPathCounts.getMaxCount(rootRowPath) - totalSpan;
			}
			vc.setRowSpan(span);

			vc.setRowIndex(totalSpan);
			totalSpan += span;
			vc.setCounts(rowPathCounts.getRowPathCounts(hNodePath).toString());
		}
	}

	private VCell getCellWithRowIndex(int rowIndex) {
		for (VCell vc : cells) {
			if (rowIndex == vc.getRowIndex()) {
				return vc;
			}
		}
		return null;
	}

	public void generateJson(int rowIndex, String prefix, PrintWriter pw,
			VWorksheet vWorksheet, ViewFactory factory, boolean generateComma) {
		VCell cell = getCellWithRowIndex(rowIndex);
		if (cell != null) {
			cell.generateJson(hNodePath, tableCssTag, prefix, pw, vWorksheet,
					factory, generateComma);
		} else {
			pw.print(prefix + "{");
			pw.print(JSONUtil.json(WorksheetDataUpdate.JsonKeys.isDummy, true));
			pw.print(JSONUtil.jsonLast(WorksheetDataUpdate.JsonKeys.tableCssTag,
					tableCssTag));
			if (generateComma) {
				pw.println(" } ,");
			} else {
				pw.println(" }");
			}
		}
	}

	public void prettyPrint(String prefix, PrintWriter pw) {
		pw.println(prefix + "RE -- hNodePath:" + hNodePath + ", tableCssTag:"
				+ tableCssTag);
		for (VCell vc : cells) {
			vc.prettyPrint(prefix + "  ", pw);
		}
	}
}
