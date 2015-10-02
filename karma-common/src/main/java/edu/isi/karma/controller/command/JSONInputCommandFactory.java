package edu.isi.karma.controller.command;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public abstract class JSONInputCommandFactory extends CommandFactory {
	private enum Arguments {
		selectionName		
	}
	@Override
	public abstract Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException;
	
	//TODO For backward compatibility
	protected void normalizeSelectionId(String worksheetId, JSONArray inputJson, Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		if (worksheet == null)
			return;
		SuperSelection sel = worksheet.getSuperSelectionManager().getSuperSelection(selectionName);
		JSONObject obj = CommandInputJSONUtil.getJSONObjectWithName(selectionName, inputJson);
		if (obj != null)
			obj.put("value", sel.getName());
		else
			CommandInputJSONUtil.createJsonObject(Arguments.selectionName.name(), sel.getName(), ParameterType.other);
	}
}
