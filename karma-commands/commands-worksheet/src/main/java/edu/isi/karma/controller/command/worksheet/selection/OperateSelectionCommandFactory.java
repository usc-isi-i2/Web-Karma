package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class OperateSelectionCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, operation, 
		pythonCode, onError, selectionName
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String operation = CommandInputJSONUtil.getStringValue(Arguments.operation.name(), inputJson);
		String pythonCode = CommandInputJSONUtil.getStringValue(Arguments.pythonCode.name(), inputJson);
		boolean onError = Boolean.parseBoolean(CommandInputJSONUtil.getStringValue(Arguments.onError.name(), inputJson));
		Command cmd = new OperateSelectionCommand(getNewId(workspace), model, 
				worksheetId, selectionName, hNodeId, 
				operation, pythonCode, onError);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return OperateSelectionCommand.class;
	}

}
