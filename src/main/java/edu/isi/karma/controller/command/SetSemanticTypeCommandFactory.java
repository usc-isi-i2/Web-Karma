package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommandFactory extends CommandFactory {

	public enum Arguments {
		vWorksheetId, hNodeId, newType
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String type = request.getParameter(Arguments.newType.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		return new SetSemanticTypeCommand(getNewId(vWorkspace), vWorksheetId,
				hNodeId, type);
	}

}
