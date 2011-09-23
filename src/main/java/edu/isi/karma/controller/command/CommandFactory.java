/**
 * 
 */
package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.EditCellCommandFactory.Arguments;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public abstract class CommandFactory {

	public abstract Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace);

	public String getWorksheetId(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		return vWorkspace.getViewFactory().getVWorksheet(vWorksheetId)
				.getWorksheet().getId();
	}
	
	protected String getNewId(VWorkspace vWorkspace){
		return vWorkspace.getWorkspace().getFactory().getNewId("C");
	}
}
