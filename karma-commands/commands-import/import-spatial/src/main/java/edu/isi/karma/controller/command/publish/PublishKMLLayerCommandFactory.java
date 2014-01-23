/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 ******************************************************************************/
package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

import javax.servlet.http.HttpServletRequest;

public class PublishKMLLayerCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		return new PublishKMLLayerCommand(getNewId(workspace), worksheetId,
				ServletContextParameterMap
						.getParameterValue(ContextParameter.PUBLIC_KML_ADDRESS),
				ServletContextParameterMap
						.getParameterValue(ContextParameter.KML_TRANSFER_SERVICE));
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishKMLLayerCommand.class;
	}
}
