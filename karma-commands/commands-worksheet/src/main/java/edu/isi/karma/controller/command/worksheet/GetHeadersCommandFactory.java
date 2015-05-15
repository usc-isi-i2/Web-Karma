package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class GetHeadersCommandFactory extends CommandFactory {

	public enum Arguments {
		worksheetId,
		hNodeId,
		commandName;
	}


	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String commandName = request.getParameter(Arguments.commandName.name());
		return new GetHeadersCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, hNodeId, commandName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return GetHeadersCommand.class;
	}

}
