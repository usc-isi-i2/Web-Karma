/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 ******************************************************************************/
package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class PublishSpatialDataCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new PublishSpatialDataCommand(getNewId(workspace), worksheetId, 
				selectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishSpatialDataCommand.class;
	}
}
