package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class GetDomainsForDataPropertyCommandFactory extends CommandFactory {

	public enum Arguments {
		URI
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String uri = request.getParameter(Arguments.URI.name());
		return new GetDomainsForDataPropertyCommand(getNewId(vWorkspace), uri);
	}

}
