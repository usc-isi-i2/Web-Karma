package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class FoldCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hTableId, hNodeId, 
		newColumnName, defaultValue, selectionName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new FoldCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				"", hNodeId, 
				selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		FoldCommand foldCmd = new FoldCommand(getNewId(workspace), model, worksheetId,
				"", hNodeID, 
				selectionName);
		foldCmd.setInputParameterJson(inputJson.toString());
		return foldCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
	
		return FoldCommand.class;
	}

}
