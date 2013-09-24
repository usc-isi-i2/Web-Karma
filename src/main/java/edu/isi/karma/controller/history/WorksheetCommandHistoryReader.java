package edu.isi.karma.controller.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.CommandHistoryWriter.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class WorksheetCommandHistoryReader {
	private final String worksheetId;
	private final Workspace workspace;
	
	private static Logger logger = LoggerFactory.getLogger(WorksheetCommandHistoryReader.class);
	
	public WorksheetCommandHistoryReader(String worksheetId, Workspace workspace) {
		super();
		this.worksheetId = worksheetId;
		this.workspace = workspace;
	}
	
	public HashMap<CommandTag, Integer> readAndExecuteCommands(List<CommandTag> tags) 
			throws FileNotFoundException, JSONException, KarmaException, CommandException {
		String worksheetName = workspace.getWorksheet(worksheetId).getTitle();
		File historyFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(worksheetName, workspace.getCommandPreferencesId()));
		JSONArray historyJson = (JSONArray) JSONUtil.createJson(new FileReader(historyFile));
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		
		HashMap<CommandTag, Integer> commandsExecutedCountByTag = new HashMap<Command.CommandTag, Integer>();
		for (CommandTag tag: tags) {
			commandsExecutedCountByTag.put(tag, 0);
		}
		
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			JSONArray commandTags = commObject.getJSONArray(HistoryArguments.tags.name());
			for (int j=0; j<commandTags.length(); j++) {
				CommandTag tag = CommandTag.valueOf(commandTags.getString(j));
				if(tags.contains(tag)) {
					executeCommand(commObject, commandFactoryMap);
					commandsExecutedCountByTag.put(tag, commandsExecutedCountByTag.get(tag).intValue()+1);
					break;
				}
			}
		}
		return commandsExecutedCountByTag;
	}
	
	public void readAndExecuteAllCommandsFromFile(File historyFile) 
			throws JSONException, KarmaException, CommandException, FileNotFoundException {
		JSONArray historyJson = (JSONArray) JSONUtil.createJson(new FileReader(historyFile));
		readAndExecuteAllCommands(historyJson);
	}
	
	public void readAndExecuteAllCommands(JSONArray historyJson) 
			throws JSONException, KarmaException, CommandException {
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			executeCommand(commObject, commandFactoryMap);
		}
	}
	
	public void executeListOfCommands(List<String> commandsJsonList) throws JSONException, KarmaException, CommandException {
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		
		for(String commJson : commandsJsonList) {
			JSONObject commObject = new JSONObject(commJson);
			executeCommand(commObject, commandFactoryMap);
		}
	}
	
	private void executeCommand(JSONObject commObject, HashMap<String, CommandFactory> commandFactoryMap) 
			throws JSONException, KarmaException, CommandException {
		JSONArray inputParamArr = (JSONArray) commObject.get(HistoryArguments.inputParameters.name());
		
		logger.info("Command in history: " + commObject.get(HistoryArguments.commandName.name()));
		// Change the hNode ids, vworksheet id to point to the current worksheet ids
		if(normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr)) {
			// Invoke the command
			CommandFactory cf = commandFactoryMap.get(commObject.get(HistoryArguments.commandName.name()));
			if(cf != null && cf instanceof JSONInputCommandFactory) {
//				logger.info("Executing command from history: " + commObject.get(HistoryArguments.commandName.name()));
				JSONInputCommandFactory scf = (JSONInputCommandFactory)cf;
				Command comm = scf.createCommand(inputParamArr, workspace);
				if(comm != null){
//					logger.info("Executing command: " + commObject.get(HistoryArguments.commandName.name()));
					workspace.getCommandHistory().doCommand(comm, workspace);
				}
				else
					logger.error("Error occured while creating command (Could not create Command object): " 
							+ commObject.get(HistoryArguments.commandName.name()));
			}
		}
	}

	public List<String> getJSONForCommands(CommandTag tag) {
		List<String> commandsJSON = new ArrayList<String>();
		String worksheetName = workspace.getWorksheet(worksheetId).getTitle();
		File historyFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(worksheetName, workspace.getCommandPreferencesId()));
		
		try {
			JSONArray historyJson = (JSONArray) JSONUtil.createJson(new FileReader(historyFile));
			
			for (int i = 0; i< historyJson.length(); i++) {
				JSONObject commObject = (JSONObject) historyJson.get(i);
				JSONArray tags = commObject.getJSONArray(HistoryArguments.tags.name());
				for (int j=0; j< tags.length(); j++) {
					String tag2 = tags.getString(j);
					if(tag2.equals(tag.name()))
						commandsJSON.add(commObject.toString());
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("History file not found!", e);
			return commandsJSON;
		} catch (JSONException e) {
			logger.error("Error occured while working with JSON!", e);
		}
		
		return commandsJSON;
	}
	
	public static boolean normalizeCommandHistoryJsonInput(Workspace workspace, String worksheetId, 
			JSONArray inputArr) throws JSONException {
		HTable hTable = workspace.getWorksheet(worksheetId).getHeaders();
		for (int i = 0; i < inputArr.length(); i++) {
			JSONObject inpP = inputArr.getJSONObject(i);
			
			/*** Check the input parameter type and accordingly make changes ***/
			if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeId) {
				JSONArray hNodeJSONRep = new JSONArray(inpP.getString(ClientJsonKeys.value.name()));
				for (int j=0; j<hNodeJSONRep.length(); j++) {
					JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
					if(hTable == null) {
						logger.error("null HTable while normalizing JSON input for the command.");
						return false;
					}
					logger.debug("Column being normalized: "+ cNameObj.getString("columnName"));
					HNode node = hTable.getHNodeFromColumnName(cNameObj.getString("columnName"));
					if(node == null) {
						logger.error("null HNode while normalizing JSON input for the command.");
						return false;
					}
					
					if (j == hNodeJSONRep.length()-1) {		// Found!
						inpP.put(ClientJsonKeys.value.name(), node.getId());
						hTable = workspace.
								getWorksheet(worksheetId).getHeaders();
					} else {
						hTable = node.getNestedTable();
					}
				}
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.worksheetId) {
				inpP.put(ClientJsonKeys.value.name(), worksheetId);
			}
		}
		return true;
	}
}
