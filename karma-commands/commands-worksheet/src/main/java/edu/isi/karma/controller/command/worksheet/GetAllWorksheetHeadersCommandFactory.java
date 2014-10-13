package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class GetAllWorksheetHeadersCommandFactory extends CommandFactory {

	public enum Arguments {
		worksheetId
	}


	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());

		return new GetAllWorksheetHeadersCommand(getNewId(workspace), worksheetId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return GetAllWorksheetHeadersCommand.class;
	}

}
