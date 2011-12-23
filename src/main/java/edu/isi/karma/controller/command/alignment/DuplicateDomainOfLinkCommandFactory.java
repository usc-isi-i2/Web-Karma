package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class DuplicateDomainOfLinkCommandFactory extends CommandFactory {
	private enum Arguments {
		edgeId, alignmentId, vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String edgeId = request.getParameter(Arguments.edgeId.name());
		String alignmentId = request.getParameter(Arguments.alignmentId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		return new DuplicateDomainOfLinkCommand(getNewId(vWorkspace), edgeId,
				alignmentId, vWorksheetId);
	}

}
