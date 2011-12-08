package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class UnassignSemanticTypeCommandFactory extends CommandFactory {
	private enum Arguments {
		hNodeId, vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		return new UnassignSemanticTypeCommand(getNewId(vWorkspace), hNodeId, vWorksheetId);
	}
}