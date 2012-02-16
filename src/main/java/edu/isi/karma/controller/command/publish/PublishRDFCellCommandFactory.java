package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishRDFCellCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId,
		nodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		String rdfPrefix = "http://localhost/source/";
		String nodeId = request.getParameter(Arguments.nodeId.name());
		return new PublishRDFCellCommand(getNewId(vWorkspace), vWorksheetId, nodeId,
				rdfPrefix);
	}

}
