package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class SplitByCommaCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId, hNodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		return new SplitByCommaCommand(getNewId(vWorkspace), getWorksheetId(
				request, vWorkspace), hNodeId, vWorksheetId);
	}
}
