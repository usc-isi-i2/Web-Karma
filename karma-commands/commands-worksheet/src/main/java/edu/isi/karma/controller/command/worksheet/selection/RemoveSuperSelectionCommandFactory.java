package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class RemoveSuperSelectionCommandFactory extends JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, currentSelectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String currentSelectionName = CommandInputJSONUtil.getStringValue(Arguments.currentSelectionName.name(), inputJson);
		Command cmd = new RemoveSuperSelectionCommand(getNewId(workspace), worksheetId, currentSelectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String currentSelectionName = request.getParameter(Arguments.currentSelectionName.name());
		return new RemoveSuperSelectionCommand(getNewId(workspace), worksheetId, currentSelectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return RefreshSelectionCommand.class;
	}

}
