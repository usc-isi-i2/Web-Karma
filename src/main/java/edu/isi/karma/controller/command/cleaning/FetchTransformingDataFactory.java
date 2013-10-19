package edu.isi.karma.controller.command.cleaning;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class FetchTransformingDataFactory extends CommandFactory {

	private enum Arguments {
		vWorksheetId, hNodeId
	}
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		return new FetchTransformingDataCommand(getNewId(vWorkspace), getWorksheetId(request, vWorkspace), hNodeId);
	}
}
