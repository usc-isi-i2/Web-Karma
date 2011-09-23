package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class EditCellCommandFactory extends CommandFactory {

	public enum Arguments {
		vWorksheetId, nodeId, value
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		String nodeId = request.getParameter(Arguments.nodeId.name());
		String newValue = request.getParameter(Arguments.value.name());
		return new EditCellCommand(getNewId(vWorkspace), 
				getWorksheetId(request, vWorkspace), nodeId, newValue);
	}
}
