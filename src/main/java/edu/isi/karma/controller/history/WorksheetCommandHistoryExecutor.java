/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.controller.history;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.history.CommandHistoryWriter.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class WorksheetCommandHistoryExecutor {
	
	private final String worksheetId;
	private final Workspace workspace;
	
	private static Logger logger = LoggerFactory.getLogger(WorksheetCommandHistoryExecutor.class);
	
	public WorksheetCommandHistoryExecutor(String worksheetId, Workspace workspace) {
		super();
		this.worksheetId = worksheetId;
		this.workspace = workspace;
	}
	
	public UpdateContainer executeCommandsByTags(
			List<CommandTag> tags, JSONArray historyJson) throws JSONException,
			KarmaException, CommandException {
		JSONArray filteredCommands = HistoryJsonUtil.filterCommandsByTag(tags, historyJson);
		return executeAllCommands(filteredCommands);
	}
	public UpdateContainer executeAllCommands(JSONArray historyJson) 
			throws JSONException, KarmaException, CommandException {
		UpdateContainer uc =new UpdateContainer();
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			UpdateContainer update = executeCommand(commObject);
			if(update != null)
				uc.append(update);
		}
		return uc;
	}
	
	private UpdateContainer executeCommand(JSONObject commObject) 
			throws JSONException, KarmaException, CommandException {
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		
		JSONArray inputParamArr = (JSONArray) commObject.get(HistoryArguments.inputParameters.name());
		String commandName = (String)commObject.get(HistoryArguments.commandName.name());
		logger.info("Command in history: " + commandName);
		// Change the hNode ids, vworksheet id to point to the current worksheet ids
		if(normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr)) {
			// Invoke the command
			CommandFactory cf = commandFactoryMap.get(commObject.get(HistoryArguments.commandName.name()));
			if(cf != null && cf instanceof JSONInputCommandFactory) {

				JSONInputCommandFactory scf = (JSONInputCommandFactory)cf;
				Command comm = scf.createCommand(inputParamArr, workspace);
				if(comm != null){
					try {
						logger.info("Executing command: " + commandName);
						workspace.getCommandHistory().doCommand(comm, workspace);
					} catch(Exception e) {
						logger.error("Error executing command: "+ commandName + ". Please notify this error");
						return new UpdateContainer(new ErrorUpdate("Error executing command " + commandName + " from history"));
					}
				}
				else {
					logger.error("Error occured while creating command (Could not create Command object): " 
							+ commObject.get(HistoryArguments.commandName.name()));
					return new UpdateContainer(new ErrorUpdate("Error executing command " + commandName + " from history"));
				}
			}
		} else {
			return new UpdateContainer(new ErrorUpdate("null HTable while normalizing JSON input for the command " + commandName));
		}
		return null;
	}
	
	private boolean normalizeCommandHistoryJsonInput(Workspace workspace, String worksheetId, 
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
