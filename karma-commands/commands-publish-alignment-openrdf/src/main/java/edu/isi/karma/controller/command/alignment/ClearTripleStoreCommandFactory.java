package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class ClearTripleStoreCommandFactory extends CommandFactory{

	private enum Arguments {
		modelUrl, tripleStoreUrl, graphContext
	}
	
	public ClearTripleStoreCommandFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String context = request.getParameter(Arguments.graphContext.name());
		return new ClearTripleStoreCommand(getNewId(workspace), Command.NEW_MODEL,
				tripleStoreUrl, context);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return ClearTripleStoreCommand.class;
	}

}
