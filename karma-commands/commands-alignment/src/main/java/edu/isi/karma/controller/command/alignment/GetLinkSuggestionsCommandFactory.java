package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GetLinkSuggestionsCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, selectionName
	}
	
	@Override
	public Command createCommand(JSONArray inputJson, String model,
			Workspace workspace) throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		
		return new GetLinkSuggestionsCommand(getNewId(workspace), model, 
				worksheetId, hNodeId, selectionName);
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		
		return new GetLinkSuggestionsCommand(getNewId(workspace), 
				Command.NEW_MODEL, worksheetId, hNodeId, selectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return GetLinkSuggestionsCommand.class;
	}

}
