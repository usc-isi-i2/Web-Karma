package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ChangeNodeCommandFactory extends JSONInputCommandFactory {

	enum Arguments {
		alignmentId, worksheetId, oldNodeId, newNodeId, newNodeUri, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		String oldNodeId = HistoryJsonUtil.getStringValue(
				Arguments.oldNodeId.name(), inputJson);
		String newNodeId = HistoryJsonUtil.getStringValue(
				Arguments.newNodeId.name(), inputJson);
		String newNodeUri = HistoryJsonUtil.getStringValue(
				Arguments.newNodeUri.name(), inputJson);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		
		ChangeNodeCommand cmd = new ChangeNodeCommand(getNewId(workspace), model, worksheetId, 
				alignmentId, oldNodeId, newNodeId, newNodeUri, selectionName);
		
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}
	
	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return ChangeNodeCommand.class;
	}
}
