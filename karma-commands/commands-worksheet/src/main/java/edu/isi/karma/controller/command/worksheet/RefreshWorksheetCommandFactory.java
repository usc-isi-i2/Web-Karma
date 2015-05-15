package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class RefreshWorksheetCommandFactory extends CommandFactory {

	private enum Arguments {
		worksheetId, updates, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String updates = request.getParameter(Arguments.updates.name());
		JSONArray updatesArr = new org.json.JSONArray(updates);
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new RefreshWorksheetCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				updatesArr, selectionName);
	}

	@Override
	public Class<? extends Command>  getCorrespondingCommand() {
		return RefreshWorksheetCommand.class;
	}


}
