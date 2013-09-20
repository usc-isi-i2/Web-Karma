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

import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * Includes all the table data to be shown in the interface.
 * 
 * @author szekely
 * 
 */
public class WorksheetDataUpdate extends AbstractUpdate {

	private String worksheetId;
	private String tableId;
	private String direction;
	private Integer size;

	public enum JsonKeys {
		worksheetId, rows, cells, path, nodeId, value, status, tableCssTag, 
		isDummy, isFirstRow, isLastRow, tablePager, pager,
		//
		rowIndex, rowSpan, counts, rowPath,
		// The following keys are used in the pager section.
		numRecordsShown, numRecordsBefore, numRecordsAfter, tableId, desiredNumRecordsShown
	}

	public WorksheetDataUpdate(String worksheetId, String tableId, String direction, Integer size) {
		super();
		this.worksheetId = worksheetId;
		this.tableId = tableId;
		this.direction = direction;
		this.size = size;
	}
	
	@Override
	public void applyUpdate(VWorkspace vWorkspace)
	{
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		TablePager pager = vWorksheet.getTablePager(tableId);
		if(null != direction)
		{
		if(direction.equalsIgnoreCase("up"))
			pager.moveToPreviousPage();
		if(direction.equalsIgnoreCase("down"));
			pager.moveToNextPage();
		}
		if(size != null)
		{
			pager.setDesiredSize(size);
		}
		vWorksheet.udateDataTable(vWorkspace.getViewFactory());
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		 vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId).generateWorksheetDataJson(prefix, pw,
				vWorkspace.getViewFactory());
	}

}
