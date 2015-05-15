package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class AddValuesCommandFactory extends JSONInputCommandFactory{
	private enum Arguments {
		worksheetId, hTableId, hNodeId, 
		newColumnName, defaultValue, selectionName
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hTableId = CommandInputJSONUtil.getStringValue(Arguments.hTableId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), model, worksheetId,
				hTableId, hNodeID, HNodeType.Transformation, 
				selectionName);
		valCmd.setInputParameterJson(inputJson.toString());
		return valCmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new AddValuesCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				hTableId, hNodeId, HNodeType.Transformation, 
				selectionName);
	}
	
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace, 
			String hNodeID, String worksheetId, 
			String hTableId, HNodeType type, 
			String selectionId)
			throws JSONException, KarmaException {
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), model, worksheetId,
				hTableId, hNodeID, type, selectionId);
		valCmd.setInputParameterJson(inputJson.toString());
		valCmd.setColumnName("");
		return valCmd;
	}
	
	
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace, 
			String hNodeID, String worksheetId, 
			String hTableId, String newColumnName, 
			HNodeType type, String selectionId)
			throws JSONException, KarmaException {
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), model, worksheetId,
				hTableId, hNodeID, type, selectionId);
		valCmd.setInputParameterJson(inputJson.toString());
		valCmd.setColumnName(newColumnName);
		return valCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return AddValuesCommand.class;
	}

}
