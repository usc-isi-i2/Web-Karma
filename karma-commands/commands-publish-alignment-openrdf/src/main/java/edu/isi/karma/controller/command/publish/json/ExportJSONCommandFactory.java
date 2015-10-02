package edu.isi.karma.controller.command.publish.json;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ExportJSONCommandFactory extends JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, alignmentNodeId, selectionName, contextJSON, contextFromModel, contextURL
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		// Not needed
		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String alignmentNodeId = HistoryJsonUtil.getStringValue(Arguments.alignmentNodeId.name(), inputJson);
		String contextJSON = HistoryJsonUtil.getStringValue(Arguments.contextJSON.name(), inputJson);
		String contextURL = HistoryJsonUtil.getStringValue(Arguments.contextURL.name(), inputJson);
		boolean contextFromModel = Boolean.parseBoolean(HistoryJsonUtil.getStringValue(Arguments.contextFromModel.name(), inputJson));
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		ExportJSONCommand comm = new ExportJSONCommand(
				getNewId(workspace), model, alignmentNodeId, worksheetId, 
				selectionName, contextFromModel, contextJSON, contextURL);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return ExportJSONCommand.class;
	}

}
