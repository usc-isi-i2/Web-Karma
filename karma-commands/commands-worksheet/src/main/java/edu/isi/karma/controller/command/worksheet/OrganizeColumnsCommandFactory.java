package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class OrganizeColumnsCommandFactory extends 
		JSONInputCommandFactory {

	enum Arguments {
		worksheetId, orderedColumns
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		
		JSONArray orderedColumns = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue(
				Arguments.orderedColumns.name(), inputJson));

		OrganizeColumnsCommand cmd = new OrganizeColumnsCommand(
				getNewId(workspace), model, workspace.getId(), worksheetId, orderedColumns);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return OrganizeColumnsCommand.class;
	}
}
