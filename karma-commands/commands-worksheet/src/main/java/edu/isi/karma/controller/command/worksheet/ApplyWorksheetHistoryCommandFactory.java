package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

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
	protected Class<? extends Command> getCorrespondingCommand()
	{
		return ApplyWorksheetHistoryCommand.class;
	}
}
