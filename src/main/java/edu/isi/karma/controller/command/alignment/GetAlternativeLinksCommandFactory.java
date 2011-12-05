package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class GetAlternativeLinksCommandFactory extends CommandFactory {
	private enum Arguments {
		nodeId, alignmentId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String nodeId =request.getParameter(Arguments.nodeId.name());
		String alignmentId =request.getParameter(Arguments.alignmentId.name());
		return new GetAlternativeLinksCommand(getNewId(vWorkspace),nodeId, alignmentId);
	}

}
