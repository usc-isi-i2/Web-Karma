/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class WorksheetHierarchicalHeadersUpdate extends AbstractUpdate {

	private final VWorksheet vWorksheet;

	public enum JsonKeys {
		worksheetId, rows, hTableId,
		//
		hNodeId, columnNameFull, columnNameShort, path,
		//
		cells,
		//
		cellType, fillId, topBorder, leftBorder, rightBorder, colSpan,
		//
		border, heading, headingPadding
	}

	public WorksheetHierarchicalHeadersUpdate(VWorksheet vWorksheet) {
		super();
		this.vWorksheet = vWorksheet;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		vWorksheet.generateWorksheetHierarchicalHeadersJson(pw, vWorkspace);
	}

}
