package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class TablePagerCommandFactory extends CommandFactory {

	public enum Arguments {
		vWorksheetId, direction, tableId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String tableId = request.getParameter(Arguments.tableId.name());
		String direction =request.getParameter(Arguments.direction.name());
		String vWorksheetId =request.getParameter(Arguments.vWorksheetId.name());
		return new TablePagerCommand(getNewId(vWorkspace), vWorksheetId, tableId, direction);
	}

}
