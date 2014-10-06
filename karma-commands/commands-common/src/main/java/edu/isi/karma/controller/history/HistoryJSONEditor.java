package edu.isi.karma.controller.history;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;

public class HistoryJSONEditor {
	private JSONArray historyJSON;
	private Workspace workspace;
	private String worksheetId;
	static Logger logger = Logger.getLogger(HistoryJSONEditor.class);
	public HistoryJSONEditor(JSONArray historyJSON, Workspace workspace, String worksheetId) {
		this.worksheetId = worksheetId;
		this.workspace = workspace;
		this.historyJSON = historyJSON;
	}

	public void deleteExistingTransformationCommands() {
		CommandHistory history = workspace.getCommandHistory();
		CommandHistoryUtil util = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
		Set<String> outputColumns = util.generateOutputColumns();
		JSONArray newHistoryJSON = new JSONArray();
		for (int i = 0; i < historyJSON.length(); i++) {
			JSONArray inputParamArr = new JSONArray(historyJSON.getJSONObject(i).get(HistoryArguments.inputParameters.name()).toString());
			WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
			String commandName = (String)historyJSON.getJSONObject(i).get(HistoryArguments.commandName.name());
			JSONArray commandTag = (JSONArray)historyJSON.getJSONObject(i).get(HistoryArguments.tags.name());
			if (isCommandTag(commandTag, CommandTag.Transformation)) {
				ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, false);
				String tmp = CommandInputJSONUtil.getStringValue("outputColumns", inputParamArr);
				if (tmp == null) {
					newHistoryJSON.put(historyJSON.get(i));
				}
				else {
					JSONArray array = new JSONArray(tmp);
					Set<String> newOutputColumns = new HashSet<String>();
					for (int j = 0; j < array.length(); j++) {
						JSONObject obj = new JSONObject(array.get(j).toString());
						newOutputColumns.add(obj.get("value").toString());
					}
					System.out.println(commandTag.toString(4));
					newOutputColumns.retainAll(outputColumns);
					if (newOutputColumns.size() == 0)
						newHistoryJSON.put(historyJSON.get(i));
				}
			}
			else
				newHistoryJSON.put(historyJSON.get(i));
		}
		historyJSON = newHistoryJSON;
	}
	
	public void deleteExistingTransformationAndModelingCommands() {
		CommandHistory history = workspace.getCommandHistory();
		CommandHistoryUtil util = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
		Set<String> outputColumns = util.generateOutputColumns();
		JSONArray newHistoryJSON = new JSONArray();
		for (int i = 0; i < historyJSON.length(); i++) {
			JSONArray inputParamArr = new JSONArray(historyJSON.getJSONObject(i).get(HistoryArguments.inputParameters.name()).toString());
			WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
			String commandName = (String)historyJSON.getJSONObject(i).get(HistoryArguments.commandName.name());
			JSONArray commandTag = (JSONArray)historyJSON.getJSONObject(i).get(HistoryArguments.tags.name());
			if (isCommandTag(commandTag, CommandTag.Transformation)) {
				ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, false);
				String tmp = CommandInputJSONUtil.getStringValue("outputColumns", inputParamArr);
				if (tmp == null) {
					newHistoryJSON.put(historyJSON.get(i));
				}
				else {
					JSONArray array = new JSONArray(tmp);
					Set<String> newOutputColumns = new HashSet<String>();
					for (int j = 0; j < array.length(); j++) {
						JSONObject obj = new JSONObject(array.get(j).toString());
						newOutputColumns.add(obj.get("value").toString());
					}
					System.out.println(commandTag.toString(4));
					newOutputColumns.retainAll(outputColumns);
					if (newOutputColumns.size() == 0)
						newHistoryJSON.put(historyJSON.get(i));
				}
			}
			else if (!isCommandTag(commandTag, CommandTag.Modeling))
				newHistoryJSON.put(historyJSON.get(i));
		}
		historyJSON = newHistoryJSON;
	}

	private boolean isCommandTag(JSONArray tags, CommandTag tag) {
		for (int i = 0; i < tags.length(); i++) {
			if (tags.getString(i).equals(tag.name()))
				return true;
		}
		return false;
	}

	public JSONArray getHistoryJSON() {
		return historyJSON;
	}
}
