package edu.isi.karma.controller.command.worksheet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;

public class ApplyWorksheetHistoryCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request);
		return new ApplyWorksheetHistoryCommand(getNewId(vWorkspace), uploadedFile, vWorksheetId);
	}

}
