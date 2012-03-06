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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.util.JSONUtil;

/**
 * @author szekely
 * 
 */
public class VRow {

	/**
	 * The entries for this row, in the same order as the headers. When there
	 * are nested tables, entries may contain a column of cells.
	 * 
	 */
	private final List<VRowEntry> entries = new LinkedList<VRowEntry>();

	private final String rowId;

	VRow(String rowId) {
		this.rowId = rowId;
	}

	void addRowEntry(VRowEntry e) {
		entries.add(e);
	}

	List<VRowEntry> _getEntries() {
		return entries;
	}

	public void prettyPrint(String prefix, PrintWriter pw) {
		pw.println(prefix + "- " + rowId);
		for (VRowEntry e : entries) {
			e.prettyPrint(prefix + "    ", pw);
		}
	}

	/**
	 * We use an integer index to distinguish all the rows we generate in case
	 * of nested tables.
	 * 
	 * @param prefix
	 * @param pw
	 * @param factory
	 * @param rowPathCounts
	 * @param generateComma
	 */
	private void generateJsonForIndex(int rowIndex, String prefix,
			PrintWriter pw, VWorksheet vWorksheet, ViewFactory factory,
			RowPathCountsByColumn rowPathCounts, boolean generateComma) {
		pw.println(prefix + "{ ");

		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.jsonStartList(WorksheetDataUpdate.JsonKeys.cells));
		Iterator<VRowEntry> it = entries.iterator();
		while (it.hasNext()) {
			VRowEntry re = it.next();
			re.generateJson(rowIndex, newPref + "  ", pw, vWorksheet, factory,
					it.hasNext());
		}
		pw.println(newPref + "]");

		pw.print(prefix + "}");
		if (generateComma) {
			pw.println(" ,");
		} else {
			pw.println("");
		}

	}

	public void generateJson(String prefix, PrintWriter pw, VWorksheet vWorksheet,
			ViewFactory factory, RowPathCountsByColumn rowPathCounts,
			boolean generateComma) {
		int numSubRows = rowPathCounts.getMaxCount(rowId);
		for (int i = 0; i < numSubRows; i++) {
			generateJsonForIndex(i, prefix, pw, vWorksheet, factory, rowPathCounts,
					i != numSubRows - 1);
		}
		if (generateComma) {
			pw.println(prefix + " ,");
		}
	}

	void assignRowIndices(RowPathCountsByColumn rowPathCounts) {
		for (VRowEntry e : entries) {
			
			e.assignRowIndices(rowPathCounts);
		}
	}
}
