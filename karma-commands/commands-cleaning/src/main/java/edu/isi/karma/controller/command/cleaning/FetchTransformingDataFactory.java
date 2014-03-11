package edu.isi.karma.controller.command.cleaning;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

public class FetchTransformingDataFactory extends CommandFactory {

	private enum Arguments {
		worksheetId, hNodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		return new FetchTransformingDataCommand(getNewId(workspace),
				getWorksheetId(request, workspace), hNodeId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return FetchTransformingDataCommand.class;
	}
}
