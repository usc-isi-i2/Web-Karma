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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tabledata.VDCell;

/**
 * @author szekely
 * 
 */
public class WorksheetHierarchicalDataUpdate extends AbstractUpdate {

	private final String worksheetId;

	/**
	 * The types of cells in the display of data.
	 * 
	 */
	public enum CellType {
		content("c"), // A cell containing a value.
		columnSpace("cs"), // A cell with the space between adjacent cells in a
							// row when space is needed for nested tables.
		dummyContent("_"), // A cell below a content cell when one is needed.
		rowSpace("rs") // A cell for space between rows.
		;

		private String code;

		private CellType(String code) {
			this.code = code;
		}

		public String code() {
			return code;
		}
	}

	public enum JsonKeys {
		worksheetId, rows, hTableId,
		//
		rowCells,
		//
		cellType, fillId, topBorder, leftBorder, rightBorder,
		// row types
		rowType/* key */, separatorRow, contentRow,
		// for content cells
		value, status, attr, nodeId, isTruncated, fullValue
	}

	public WorksheetHierarchicalDataUpdate(String worksheetId) {
		super();
		this.worksheetId = worksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		vWorksheet.generateWorksheetHierarchicalDataJson(pw, vWorkspace);
	}

	public static String getStrokePositionKey(VDCell.Position position) {
		return position.name() + "Stroke";
	}

}
