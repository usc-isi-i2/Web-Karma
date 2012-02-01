package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishRDFCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		return new PublishRDFCommand(getNewId(vWorkspace), vWorksheetId,
				ServletContextParameterMap
				.getParameterValue(ContextParameter.PUBLIC_RDF_ADDRESS),
				ServletContextParameterMap
				.getParameterValue(ContextParameter.RDF_SOURCE_PREFIX));
	}

}
