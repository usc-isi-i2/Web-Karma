package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class FetchR2RMLModelsListCommandFactory extends CommandFactory{

	private enum Arguments {
		modelUrl, tripleStoreUrl, graphContext, worksheetId
	}
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String context = request.getParameter(Arguments.graphContext.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new FetchR2RMLModelsListCommand(getNewId(workspace), Command.NEW_MODEL, tripleStoreUrl, context, worksheetId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return FetchR2RMLModelsListCommand.class;
	}

}
