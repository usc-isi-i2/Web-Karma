package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.rep.Workspace;

public class GetR2RMLModelURLsCommandFactory extends CommandFactory {

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		return new GetR2RMLModelURLsCommand(getNewId(workspace), Command.NEW_MODEL);
	}
	

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return GetR2RMLModelURLsCommand.class;
	}

}
