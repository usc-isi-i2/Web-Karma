/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 ******************************************************************************/
package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

public class PublishCSVCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		return new PublishCSVCommand(getNewId(workspace), worksheetId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishCSVCommand.class;
	}
}
