package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class SearchForDataToAugmentCommandFactory extends CommandFactory{

	private enum Arguments {
		tripleStoreUrl, context, nodeType, alignmentId
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String url = request.getParameter(Arguments.tripleStoreUrl.name());
		String context = request.getParameter(Arguments.context.name());
		String nodeType = request.getParameter(Arguments.nodeType.name());
		String alignmentId = request.getParameter(Arguments.alignmentId.name());
		return new SearchForDataToAugmentCommand(getNewId(workspace), url, context, nodeType, alignmentId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return SearchForDataToAugmentCommand.class;
	}

}
