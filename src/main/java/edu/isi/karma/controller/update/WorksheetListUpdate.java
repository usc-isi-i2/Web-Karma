/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorksheetList;
import edu.isi.karma.view.VWorkspace;

/**
 * Contains the list of worksheets to be shown in the window. The list may
 * include new worksheets that were not previously displayed, and may not
 * inlcude worksheets that were previously displayed. Those should be removed.
 * 
 * @author szekely
 * 
 */
public class WorksheetListUpdate extends AbstractUpdate {

	public enum JsonKeys {
		worksheets, worksheetId, title, isUpToDate, isCollapsed
	}

	private final VWorksheetList vWorksheetList;

	public WorksheetListUpdate(VWorksheetList vWorksheetList) {
		super();
		this.vWorksheetList = vWorksheetList;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String prefix1 = prefix + "  ";
		pw.println(prefix1
				+ Util.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(prefix1 + Util.jsonStartList(JsonKeys.worksheets));
		vWorksheetList.generateJson(prefix1, pw, vWorkspace.getViewFactory());
		pw.println(prefix1 + "]");
		pw.println(prefix + "}");

	}

}
