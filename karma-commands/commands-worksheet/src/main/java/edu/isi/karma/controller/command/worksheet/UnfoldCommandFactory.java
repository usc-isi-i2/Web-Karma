package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class UnfoldCommandFactory extends JSONInputCommandFactory{

	public enum Arguments {
		worksheetId, keyhNodeId, valuehNodeId, 
		selectionName
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String keyHNodeid = CommandInputJSONUtil.getStringValue("keyhNodeId", inputJson);
		String valueHNodeid = CommandInputJSONUtil.getStringValue("valuehNodeId", inputJson);
		String keyName = workspace.getFactory().getHNode(keyHNodeid).getColumnName();
		String valueName = workspace.getFactory().getHNode(valueHNodeid).getColumnName();
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		//System.out.println(worksheetId);
		UnfoldCommand unfoldCmd = new UnfoldCommand(getNewId(workspace), worksheetId, 
				keyHNodeid, valueHNodeid, 
				selectionName);
		unfoldCmd.setInputParameterJson(inputJson.toString());
		unfoldCmd.setKeyName(keyName);
		unfoldCmd.setValueName(valueName);
		return unfoldCmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String keyHNodeid = request.getParameter(Arguments.keyhNodeId.name());
		String valueHNodeid = request.getParameter(Arguments.valuehNodeId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new UnfoldCommand(getNewId(workspace), worksheetId, keyHNodeid, valueHNodeid, 
				selectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return UnfoldCommand.class;
	}

}
