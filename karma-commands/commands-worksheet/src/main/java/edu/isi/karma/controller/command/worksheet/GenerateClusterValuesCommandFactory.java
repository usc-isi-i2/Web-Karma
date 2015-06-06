package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GenerateClusterValuesCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		hNodeId, worksheetId, hTableID, 
		selectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		
		String hNodeId = HistoryJsonUtil.getStringValue(
				Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		GenerateClusterValuesCommand comm = new GenerateClusterValuesCommand(
				getNewId(workspace), model, hNodeId, worksheetId, 
				selectionName);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return GenerateClusterValuesCommand.class;
	}

}