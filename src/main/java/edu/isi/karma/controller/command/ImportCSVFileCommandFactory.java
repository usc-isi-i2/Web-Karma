package edu.isi.karma.controller.command;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;

public class ImportCSVFileCommandFactory extends CommandFactory{
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request);
		return new ImportCSVFileCommand(getNewId(vWorkspace), uploadedFile, vWorkspace);
	}

}
