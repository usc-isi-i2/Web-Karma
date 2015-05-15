package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.rep.Workspace;

public class SetKarmaHomeCommandFactory extends CommandFactory {

	private enum Arguments {
		directory
	}
	
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		SetKarmaHomeCommand comm = new SetKarmaHomeCommand(getNewId(workspace), 
				Command.NEW_MODEL,
				request.getParameter(Arguments.directory.name()));
		
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return SetKarmaHomeCommand.class;
	}

}
