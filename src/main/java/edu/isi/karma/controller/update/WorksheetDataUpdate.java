/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * Includes all the table data to be shown in the interface.
 * 
 * @author szekely
 * 
 */
public class WorksheetDataUpdate extends AbstractUpdate {

	private final VWorksheet vWorksheet;

	public enum JsonKeys {
		worksheetId, rows, cells, path, nodeId, value, status, tableCssTag, 
		isDummy, isFirstRow, isLastRow, tablePager, pager,
		//
		rowIndex, rowSpan, counts, rowPath,
		// The following keys are used in the pager section.
		numRecordsShown, numRecordsBefore, numRecordsAfter, tableId, desiredNumRecordsShown
	}

	public WorksheetDataUpdate(VWorksheet vWorksheet) {
		super();
		this.vWorksheet = vWorksheet;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		vWorksheet.generateWorksheetDataJson(prefix, pw,
				vWorkspace.getViewFactory());
	}

}
