package edu.isi.karma.controller.command.cleaning;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class FetchTransformingDataFactory extends CommandFactory {

	private enum Arguments {
		worksheetId, hNodeId
	}
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		return new FetchTransformingDataCommand(getNewId(workspace), getWorksheetId(request, workspace), hNodeId);
	}
}
