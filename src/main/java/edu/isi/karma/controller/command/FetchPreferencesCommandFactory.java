package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.ImportDatabaseTableCommand;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class FetchPreferencesCommandFactory extends CommandFactory {
	private enum Arguments {
		preferenceCommand
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		FetchPreferencesCommand comm = new FetchPreferencesCommand(getNewId(vWorkspace), 
				request.getParameter(Arguments.preferenceCommand.name()));
		
		return comm;
	}

}
