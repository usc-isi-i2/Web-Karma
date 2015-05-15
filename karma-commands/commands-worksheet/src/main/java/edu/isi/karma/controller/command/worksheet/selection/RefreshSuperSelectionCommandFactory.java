package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class RefreshSuperSelectionCommandFactory extends JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, selectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String currentSelectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		Command cmd = new RefreshSuperSelectionCommand(getNewId(workspace), model,
				worksheetId, currentSelectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String currentSelectionName = request.getParameter(Arguments.selectionName.name());
		return new RefreshSuperSelectionCommand(getNewId(workspace), Command.NEW_MODEL,
				worksheetId, currentSelectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return RefreshSuperSelectionCommand.class;
	}

}
