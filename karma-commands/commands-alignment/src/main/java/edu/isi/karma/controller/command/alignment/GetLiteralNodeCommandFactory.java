package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;

public class GetLiteralNodeCommandFactory extends CommandFactory {

	public enum Arguments {
		worksheetId, nodeId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		
		String nodeId = request.getParameter(Arguments.nodeId.name());
		String worksheetId =request.getParameter(Arguments.worksheetId.name());
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		
		return new GetLiteralNodeCommand(getNewId(workspace), Command.NEW_MODEL, 
				worksheetId, alignmentId, nodeId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return GetLiteralNodeCommand.class;
	}

}
