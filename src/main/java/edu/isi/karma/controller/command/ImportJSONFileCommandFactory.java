package edu.isi.karma.controller.command;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;

public class ImportJSONFileCommandFactory extends CommandFactory {
	
	static Logger logger = LoggerFactory.getLogger(ImportJSONFileCommandFactory.class);
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
				
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request);
		return new ImportJSONFileCommand(getNewId(vWorkspace), uploadedFile);
	}
}
