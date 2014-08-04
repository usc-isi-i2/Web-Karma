/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 ******************************************************************************/
package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishKMLLayerCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		Worksheet ws = workspace.getWorksheet(worksheetId);
		return new PublishKMLLayerCommand(getNewId(workspace), worksheetId,
				ServletContextParameterMap
						.getParameterValue(ContextParameter.PUBLIC_KML_ADDRESS),
				ServletContextParameterMap
						.getParameterValue(ContextParameter.KML_TRANSFER_SERVICE),
				ws.getSuperSelectionManager().getSuperSelection(selectionName));
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishKMLLayerCommand.class;
	}
}
