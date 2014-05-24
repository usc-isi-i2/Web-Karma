package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class ApplyModelFromURLCommandFactory extends CommandFactory{

	private enum Arguments {
		worksheetId, graphContext
	}
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String context = request.getParameter(Arguments.graphContext.name());
		return new ApplyModelFromURLCommand(getNewId(workspace), worksheetId, context);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return ApplyModelFromURLCommand.class;
	}

}
