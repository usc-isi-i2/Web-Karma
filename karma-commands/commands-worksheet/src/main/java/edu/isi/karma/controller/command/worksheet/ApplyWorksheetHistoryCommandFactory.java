package edu.isi.karma.controller.command.worksheet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;

public class ApplyWorksheetHistoryCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request);
		return new ApplyWorksheetHistoryCommand(getNewId(workspace), uploadedFile, worksheetId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return ApplyWorksheetHistoryCommand.class;
	}
}
