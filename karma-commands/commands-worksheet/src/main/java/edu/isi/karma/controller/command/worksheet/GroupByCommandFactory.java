package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GroupByCommandFactory extends JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hTableId, hNodeId, 
		newColumnName, defaultValue, selectionName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new GroupByCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				hTableId, hNodeId, 
				selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hTableId = "";
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		//System.out.println(worksheetId);
		GroupByCommand unfoldCmd = new GroupByCommand(getNewId(workspace), model, worksheetId,
				hTableId, hNodeID, 
				selectionName);
		unfoldCmd.setInputParameterJson(inputJson.toString());
		return unfoldCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return GroupByCommand.class;
	}

}
