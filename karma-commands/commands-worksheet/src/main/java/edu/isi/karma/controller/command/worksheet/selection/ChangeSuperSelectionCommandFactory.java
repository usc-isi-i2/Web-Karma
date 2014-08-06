package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ChangeSuperSelectionCommandFactory extends JSONInputCommandFactory {
	public enum Arguments {
		worksheetId, newSelectionName, oldSelectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String oldSelectionName = CommandInputJSONUtil.getStringValue(Arguments.oldSelectionName.name(), inputJson);
		String newSelectionName = CommandInputJSONUtil.getStringValue(Arguments.newSelectionName.name(), inputJson);
		Command cmd = new ChangeSuperSelectionCommand(getNewId(workspace), worksheetId, oldSelectionName, newSelectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String oldSelectionName = request.getParameter(Arguments.oldSelectionName.name());
		String newSelectionName = request.getParameter(Arguments.newSelectionName.name());
		return new ChangeSuperSelectionCommand(getNewId(workspace), worksheetId, oldSelectionName, newSelectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return ChangeSuperSelectionCommand.class;
	}

}
