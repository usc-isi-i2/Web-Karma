package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class TablePagerResizeCommandFactory extends CommandFactory {

	public enum Arguments {
		vWorksheetId, newPageSize, tableId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String tableId = request.getParameter(Arguments.tableId.name());
		String newPageSize =request.getParameter(Arguments.newPageSize.name());
		String vWorksheetId =request.getParameter(Arguments.vWorksheetId.name());
		return new TablePagerResizeCommand(getNewId(vWorkspace), vWorksheetId, tableId, newPageSize);
	}

}
