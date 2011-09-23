package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class UndoRedoCommandFactory extends CommandFactory {

	public enum Arguments {
		commandId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String commandId = request.getParameter(Arguments.commandId.name());
		return new UndoRedoCommand(getNewId(vWorkspace), commandId);
	}

}
