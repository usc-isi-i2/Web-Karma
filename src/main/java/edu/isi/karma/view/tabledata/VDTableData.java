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

	private final VDVerticalSeparators vdVerticalSeparators = new VDVerticalSeparators();

	public VDTableData(VTableHeadings vtHeadings, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		super();
		// this.vtHeadings = vtHeadings;
		this.rootTableId = vWorksheet.getWorksheet().getDataTable().getId();

		// Record the column indices for all HNodes, except the fake root HNode.
		vtHeadings.getRootVHNode().collectLeaves(frontier);
		vdIndexTable.putFrontier(frontier);
		vtHeadings.getRootVHNode().populateVDIndexTable(vdIndexTable);

		vtHeadings.populateVDVerticalSeparators(vdVerticalSeparators);

		// When the rows change, we need to recompute all the stuff below
		
		// Build the VDRows and their content.
		vtHeadings.getRootVHNode().populateVDRows(null, rows,
				vWorksheet.getTopTablePager(), vWorksheet);
		for (VDRow r : rows) {
			r.setFillHTableId(vWorkspace.getRepFactory().getTable(rootTableId)
					.getHTableId());
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

	VDVerticalSeparators getVdVerticalSeparators() {
		return vdVerticalSeparators;
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

	public JSONWriter prettyPrintJson(JSONWriter jw, boolean verbose,
			VWorkspace vWorkspace) throws JSONException {
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

		jw.key("verticalSeparators");
		vdVerticalSeparators.prettyPrintJson(jw, vWorkspace);

		jw.key("cells");
		vdTableCells.prettyPrintJson(jw);

		jw.endObject();
		return jw;
	}

}
