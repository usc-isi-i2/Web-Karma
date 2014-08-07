package edu.isi.karma.controller.command.worksheet.selection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class CreateSelectionCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, pythonCode, selectionName
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String pythonCode = CommandInputJSONUtil.getStringValue(Arguments.pythonCode.name(), inputJson);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		Command cmd = new CreateSelectionCommand(getNewId(workspace), worksheetId, 
				hNodeId, pythonCode, selectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String pythonCode = request.getParameter(Arguments.pythonCode.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new CreateSelectionCommand(getNewId(workspace), worksheetId, 
				hNodeId, pythonCode, selectionName);
		
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return CreateSelectionCommand.class;
	}

}
