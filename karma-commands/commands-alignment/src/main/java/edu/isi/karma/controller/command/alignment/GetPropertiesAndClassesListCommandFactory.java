package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

public class GetPropertiesAndClassesListCommandFactory extends CommandFactory {

	private enum Arguments {
		worksheetId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new GetPropertiesAndClassesList(getNewId(workspace), worksheetId);
	}

	@Override
	protected Class<? extends Command> getCorrespondingCommand()
	{
		return GetPropertiesAndClassesList.class;
	}
}
