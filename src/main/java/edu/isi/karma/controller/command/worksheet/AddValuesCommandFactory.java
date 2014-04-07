package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.worksheet.AddRowCommandFactory.Arguments;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class AddValuesCommandFactory extends CommandFactory implements JSONInputCommandFactory{

	public AddValuesCommandFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hTableId = CommandInputJSONUtil.getStringValue(Arguments.hTableId.name(), inputJson);
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), worksheetId,
				hTableId, hNodeID);
		valCmd.setInputParameterJson(inputJson.toString());
		return valCmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new AddValuesCommand(getNewId(workspace), worksheetId, 
				hTableId, hNodeId);
	}
	
	public Command createCommand(JSONArray inputJson, Workspace workspace, String hNodeID, String worksheetId, String hTableId)
			throws JSONException, KarmaException {
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), worksheetId,
				hTableId, hNodeID);
		valCmd.setInputParameterJson(inputJson.toString());
		valCmd.setColumnName("");
		return valCmd;
	}
	
	
	public Command createCommand(JSONArray inputJson, Workspace workspace, String hNodeID, String worksheetId, String hTableId, String newColumnName)
			throws JSONException, KarmaException {
		AddValuesCommand valCmd = new AddValuesCommand(getNewId(workspace), worksheetId,
				hTableId, hNodeID);
		valCmd.setInputParameterJson(inputJson.toString());
		valCmd.setColumnName(newColumnName);
		return valCmd;
	}

}
