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
public class WorksheetHeadersUpdate extends AbstractUpdate {

	private final VWorksheet vWorksheet;

	public enum JsonKeys {
		worksheetId, columns, columnNameFull, columnNameShort, path
	}

	public WorksheetHeadersUpdate(VWorksheet vWorksheet) {
		super();
		this.vWorksheet = vWorksheet;
	}

	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		vWorksheet.generateWorksheetHeadersJson(prefix, pw,
				vWorkspace.getViewFactory());
	}

}
