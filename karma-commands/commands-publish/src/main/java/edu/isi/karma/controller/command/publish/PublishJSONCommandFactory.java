package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class PublishJSONCommandFactory  extends CommandFactory {
	private enum Arguments {
		worksheetId, importAsWorksheet
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		String sImportAsWorksheet = request.getParameter(Arguments.importAsWorksheet.name());
		boolean importAsWorksheet = false;
		if(sImportAsWorksheet != null)
			importAsWorksheet = Boolean.valueOf(sImportAsWorksheet);
		return new PublishJSONCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, importAsWorksheet);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishJSONCommand.class;
	}
}
