package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class CloseWorkspaceCommandFactory extends CommandFactory {
	
	private enum Arguments {
		workspaceId;
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String workspaceId = request.getParameter(Arguments.workspaceId.name());
		return new CloseWorkspaceCommand(getNewId(vWorkspace), workspaceId);
	}

}
