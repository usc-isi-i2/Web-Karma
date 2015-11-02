package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ImportJSONLinesFileCommandFactory extends CommandFactory {

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		File uploadedFile = FileUtil.downloadFileFromHTTPRequest(request, contextParameters.getParameterValue(ContextParameter.USER_UPLOADED_DIR));
		
		if(request.getParameter("revisedWorksheet") == null){
			return new ImportJSONLinesFileCommand(getNewId(workspace), Command.NEW_MODEL, uploadedFile);
		}
		return new ImportJSONLinesFileCommand(getNewId(workspace), request.getParameter("revisedWorksheet"), uploadedFile);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return ImportJSONLinesFileCommand.class;
	}

}
