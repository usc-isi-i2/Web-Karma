package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class UpdateSelectionCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, operation, selectionName, anotherSelectionName
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String operation = CommandInputJSONUtil.getStringValue(Arguments.operation.name(), inputJson);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		String anotherSelectionName = CommandInputJSONUtil.getStringValue(Arguments.anotherSelectionName.name(), inputJson);
		return null;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String operation = request.getParameter(Arguments.operation.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		String anotherSelectionName = request.getParameter(Arguments.anotherSelectionName.name());

		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return UpdateSelectionCommand.class;
	}

}
