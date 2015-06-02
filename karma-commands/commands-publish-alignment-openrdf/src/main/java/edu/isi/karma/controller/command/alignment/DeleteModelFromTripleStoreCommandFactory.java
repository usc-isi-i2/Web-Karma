package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class DeleteModelFromTripleStoreCommandFactory extends CommandFactory {

	private enum Arguments {
		mappingURI, tripleStoreUrl, graphContext
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String context = request.getParameter(Arguments.graphContext.name());
		String mappingURI = request.getParameter(Arguments.mappingURI.name());
		return new DeleteModelFromTripleStoreCommand(getNewId(workspace), 
				Command.NEW_MODEL, tripleStoreUrl, context, mappingURI);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return DeleteModelFromTripleStoreCommand.class;
	}

}
