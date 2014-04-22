package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandHistoryWriter {
	private final List<ICommand> history;
	private Workspace workspace;
	
	public enum HistoryArguments {
		worksheetId, commandName, inputParameters, hNodeId, tags
	}

	public CommandHistoryWriter(List<ICommand> history, Workspace workspace) {
		this.history = history;
		this.workspace = workspace;
	}

	public void writeHistoryPerWorksheet() throws JSONException {
		if(!isHistoryEnabled)
		{
			return;
		}
		HashMap<String, List<ICommand>> comMap = new HashMap<String, List<ICommand>>();
		for(ICommand command : history) {
			if(command.isSavedInHistory() && (command.hasTag(CommandTag.Modeling) 
					|| command.hasTag(CommandTag.Transformation))) {
				JSONArray json = new JSONArray(command.getInputParameterJson());
				String worksheetId = HistoryJsonUtil.getStringValue(HistoryArguments.worksheetId.name(), json);
				String worksheetName = workspace.getWorksheet(worksheetId).getTitle(); 
				if(comMap.get(worksheetName) == null)
					comMap.put(worksheetName, new ArrayList<ICommand>());
				comMap.get(worksheetName).add(command);
			}
		}
		
		for(String wkName : comMap.keySet()) {
			List<ICommand> comms = comMap.get(wkName);
			JSONArray commArr = new JSONArray();
			for(ICommand comm : comms) {
				JSONObject commObj = new JSONObject();
				commObj.put(HistoryArguments.commandName.name(), comm.getCommandName());
				
				// Populate the tags
				JSONArray tagsArr = new JSONArray();
				for (CommandTag tag : comm.getTags())
					tagsArr.put(tag.name());
				commObj.put(HistoryArguments.tags.name(), tagsArr);
				
				JSONArray inputArr = new JSONArray(comm.getInputParameterJson());
				for (int i = 0; i < inputArr.length(); i++) {
					JSONObject inpP = inputArr.getJSONObject(i);
					
					/*** Check the input parameter type and accordingly make changes ***/
					if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeId) {
						String hNodeId = inpP.getString(ClientJsonKeys.value.name());
						HNode node = workspace.getFactory().getHNode(hNodeId);
						JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
						inpP.put(ClientJsonKeys.value.name(), hNodeRepresentation);
					
					} else if (HistoryJsonUtil.getParameterType(inpP) == ParameterType.worksheetId) {
						inpP.put(ClientJsonKeys.value.name(), "W");
					} else {
						// do nothing
					}
				}
				commObj.put(HistoryArguments.inputParameters.name(), inputArr);
				commArr.put(commObj);
			}
			JSONUtil.writeJsonFile(commArr, HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(wkName, 
					workspace.getCommandPreferencesId()));
		}
	}
	
	private static boolean isHistoryEnabled = false;
	public static boolean isHistoryEnabled()
	{
		return isHistoryEnabled;
	}
	
	public static void setIsHistoryEnabled(boolean isHistoryEnabled)
	{
		CommandHistoryWriter.isHistoryEnabled = isHistoryEnabled;
	}
}
