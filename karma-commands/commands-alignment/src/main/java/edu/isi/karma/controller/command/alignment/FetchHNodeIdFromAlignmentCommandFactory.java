package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class FetchHNodeIdFromAlignmentCommandFactory extends CommandFactory {
	private enum Arguments {
		alignmentId, columnUri
	}
	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String columnUri = request.getParameter(Arguments.columnUri.name());
		String alignmentId = request.getParameter(Arguments.alignmentId.name());
		return new FetchHNodeIdFromAlignmentCommand(getNewId(workspace), 
				Command.NEW_MODEL, alignmentId, columnUri);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return FetchHNodeIdFromAlignmentCommand.class;
	}

}
