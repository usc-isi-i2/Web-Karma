package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.alignment.GetClassesCommand.INTERNAL_NODES_RANGE;
import edu.isi.karma.rep.Workspace;

public class GetClassesCommandFactory extends CommandFactory {
	public enum Arguments {
		propertyURI, worksheetId, nodesRange
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String propertyURI = request.getParameter(Arguments.propertyURI.name());
		String worksheetId =request.getParameter(Arguments.worksheetId.name());
		INTERNAL_NODES_RANGE range = INTERNAL_NODES_RANGE.valueOf(
				request.getParameter(Arguments.nodesRange.name()));
		
		return new GetClassesCommand(getNewId(workspace), 
				Command.NEW_MODEL, worksheetId, range, propertyURI);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return GetClassesCommand.class;
	}

}
