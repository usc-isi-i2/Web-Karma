package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class ApplyModelFromURLCommandFactory extends CommandFactory{

	private enum Arguments {
		worksheetId, modelUrl, modelContext, modelRepository, override
	}
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String modelUrl = request.getParameter(Arguments.modelUrl.name());
		String modelContext = request.getParameter(Arguments.modelContext.name());
		String modelRepository = request.getParameter(Arguments.modelRepository.name());
		boolean override = Boolean.parseBoolean(request.getParameter(Arguments.override.name()));
		String baseURL = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("RequestController")) + "R2RMLMapping/local/repository/";		
		return new ApplyModelFromURLCommand(getNewId(workspace), Command.NEW_MODEL,
				worksheetId, modelUrl, modelContext,modelRepository, baseURL, override);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return ApplyModelFromURLCommand.class;
	}

}
