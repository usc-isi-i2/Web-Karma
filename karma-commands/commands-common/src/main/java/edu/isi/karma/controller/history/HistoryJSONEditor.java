package edu.isi.karma.controller.history;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		Object organizeCommand = null;
		for (int i = 0; i < historyJSON.length(); i++) {
			JSONArray inputParamArr = new JSONArray(historyJSON.getJSONObject(i).get(HistoryArguments.inputParameters.name()).toString());
			WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
			String commandName = (String)historyJSON.getJSONObject(i).get(HistoryArguments.commandName.name());
			JSONArray commandTag = (JSONArray)historyJSON.getJSONObject(i).get(HistoryArguments.tags.name());
			if (isCommandTag(commandTag, CommandTag.Transformation)) {
				if(commandName.equals("OrganizeColumnsCommand")) {
					organizeCommand = historyJSON.get(i);
					continue;
				}
				
				ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, false);
				String tmp = CommandInputJSONUtil.getStringValue("outputColumns", inputParamArr);
				
				if (tmp == null) {
					newHistoryJSON.put(historyJSON.get(i));
				}
				else {
					JSONArray array = new JSONArray(tmp);
					Set<String> newOutputColumns = new HashSet<>();
					for (int j = 0; j < array.length(); j++) {
						JSONObject obj = new JSONObject(array.get(j).toString());
						newOutputColumns.add(obj.get("value").toString());
					}
					logger.debug(commandTag.toString(4));
					newOutputColumns.retainAll(outputColumns);
					if (newOutputColumns.isEmpty())
						newHistoryJSON.put(historyJSON.get(i));
				}
			} else { 
				if(organizeCommand != null) {
					newHistoryJSON.put(organizeCommand);
					organizeCommand = null;
				}
				newHistoryJSON.put(historyJSON.get(i));
			}
		}
		System.out.println("HISTORY:" + newHistoryJSON.toString(2));
		historyJSON = newHistoryJSON;
	}
	
	public void deleteExistingTransformationAndModelingCommands() {
		CommandHistory history = workspace.getCommandHistory();
		CommandHistoryUtil util = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
		Set<String> outputColumns = util.generateOutputColumns();
		JSONArray newHistoryJSON = new JSONArray();
		List<Object> orderedColumnCommands = new ArrayList<>();
		
		for (int i = 0; i < historyJSON.length(); i++) {
			JSONArray inputParamArr = new JSONArray(historyJSON.getJSONObject(i).get(HistoryArguments.inputParameters.name()).toString());
			WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
			String commandName = (String)historyJSON.getJSONObject(i).get(HistoryArguments.commandName.name());
			
			JSONArray commandTag = (JSONArray)historyJSON.getJSONObject(i).get(HistoryArguments.tags.name());
			if (isCommandTag(commandTag, CommandTag.Transformation)) {
				if(CommandInputJSONUtil.getStringValue("orderedColumns", inputParamArr) != null) {
					//We add commands that need orderedColumns into a list and these
					//are the last executed at the end of all transformation commands
					//as it could be that they order columns that are created by other
					//Py Transformations
					orderedColumnCommands.add(historyJSON.get(i));	
					continue;
				}
				ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, false);
				String tmp = CommandInputJSONUtil.getStringValue("outputColumns", inputParamArr);
				if (tmp == null) {
					newHistoryJSON.put(historyJSON.get(i));
				}
				else {
					JSONArray array = new JSONArray(tmp);
					Set<String> newOutputColumns = new HashSet<>();
					for (int j = 0; j < array.length(); j++) {
						JSONObject obj = new JSONObject(array.get(j).toString());
						newOutputColumns.add(obj.get("value").toString());
					}
					logger.debug(commandTag.toString(4));
					newOutputColumns.retainAll(outputColumns);
					if (newOutputColumns.isEmpty())
						newHistoryJSON.put(historyJSON.get(i));
				}
			} else if (!isCommandTag(commandTag, CommandTag.Modeling)) {
				for(Object orderedColCommand : orderedColumnCommands)
					newHistoryJSON.put(orderedColCommand);
				orderedColumnCommands.clear();
				newHistoryJSON.put(historyJSON.get(i));
			} else {
				for(Object orderedColCommand : orderedColumnCommands)
					newHistoryJSON.put(orderedColCommand);
				orderedColumnCommands.clear();
			}
		}
		historyJSON = newHistoryJSON;
	}

	public void updateModelUrlInCommands(String modelUrl) {
		for (int i = 0; i < historyJSON.length(); i++) {
			historyJSON.getJSONObject(i).put(HistoryArguments.model.name(), modelUrl);
		}
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
