package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class CheckModelExistenceCommandFactory extends CommandFactory {
	
	private enum Arguments {
		worksheetId
	}
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new CheckModelExistenceCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return CheckModelExistenceCommand.class;
	}

}
