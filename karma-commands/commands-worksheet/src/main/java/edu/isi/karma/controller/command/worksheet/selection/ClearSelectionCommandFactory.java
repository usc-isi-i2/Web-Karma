package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ClearSelectionCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, type, selectionName
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String type = CommandInputJSONUtil.getStringValue(Arguments.type.name(), inputJson);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		Command cmd = new ClearSelectionCommand(getNewId(workspace), model,
				worksheetId, selectionName, hNodeId, type);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return ClearSelectionCommand.class;
	}

}
