package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class RefreshSVGAligmentCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId, alignmentId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String alignmentId = request.getParameter(Arguments.alignmentId.name());
		return new RefreshSVGAlignmentCommand(getNewId(workspace), 
				Command.NEW_MODEL, worksheetId, alignmentId);
	}

	@Override
	public Class<? extends Command>  getCorrespondingCommand() {
		return RefreshSVGAlignmentCommand.class;
	}

}
