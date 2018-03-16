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

import java.util.ArrayList;
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
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class WorksheetCommandHistoryExecutor {

	private final String worksheetId;
	private final Workspace workspace;

	private static Logger logger = LoggerFactory.getLogger(WorksheetCommandHistoryExecutor.class);
	private static String[] commandsIgnoreNodeBefore = { "AddColumnCommand",
		"SubmitPythonTransformationCommand"
	};
	
	public WorksheetCommandHistoryExecutor(String worksheetId, Workspace workspace) {
		super();
		this.worksheetId = worksheetId;
		this.workspace = workspace;
	}

	public UpdateContainer executeCommandsByTags(
			List<CommandTag> tagsToAdd, List<CommandTag> tagsToRemove, JSONArray historyJson) throws JSONException,
			KarmaException, CommandException {
		JSONArray filteredCommands = HistoryJsonUtil.filterCommandsByTag(tagsToAdd, historyJson);
		filteredCommands = HistoryJsonUtil.removeCommandsByTag(tagsToRemove, filteredCommands);
		return executeAllCommands(filteredCommands);
	}
	
	public UpdateContainer executeAllCommands(JSONArray historyJson) 
			throws JSONException, KarmaException, CommandException {
		UpdateContainer uc =new UpdateContainer();
		boolean saveToHistory = false;
		
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			if(i == historyJson.length() - 1) saveToHistory = true;
			UpdateContainer update = executeCommand(commObject, saveToHistory);
			if(update != null)
				uc.append(update);
		}
		
		return uc;
	}

	private UpdateContainer executeCommand(JSONObject commObject, boolean saveToHistory) 
			throws JSONException, KarmaException, CommandException {
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();

		JSONArray inputParamArr = (JSONArray) commObject.get(HistoryArguments.inputParameters.name());
		String commandName = (String)commObject.get(HistoryArguments.commandName.name());
		logger.debug("Command in history: " + commandName);

		// Change the hNode ids, vworksheet id to point to the current worksheet ids
		try {
			UpdateContainer uc = normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, true);
			// Invoke the command
			if (uc == null) {
				uc = new UpdateContainer();
			}
			CommandFactory cf = commandFactoryMap.get(commObject.get(HistoryArguments.commandName.name()));
			if(cf != null) {
				try { // This is sort of a hack the way I did this, but could not think of a better way to get rid of the dependency
					String model = Command.NEW_MODEL;
					if(commObject.has(HistoryArguments.model.name()))
						model = commObject.getString(HistoryArguments.model.name());
					Command comm = cf.createCommand(inputParamArr, model, workspace);
					if(comm != null){
						try {
							comm.setExecutedInBatch(true);
							logger.debug("Executing command: " + commandName);
							uc.append(workspace.getCommandHistory().doCommand(comm, workspace, saveToHistory));
							comm.setExecutedInBatch(false);
						} catch(Exception e) {
							logger.error("Error executing command: "+ commandName + ". Please notify this error. \nInputs:" + inputParamArr, e);
							//make these InfoUpdates so that the UI can still process the rest of the model
							return new UpdateContainer(new TrivialErrorUpdate("Error executing command " + commandName + " from history"));
						}
					}
					else {
						logger.error("Error occured while creating command (Could not create Command object): " 
								+ commObject.get(HistoryArguments.commandName.name()));
						return new UpdateContainer(new TrivialErrorUpdate("Error executing command " + commandName + " from history"));
					}
				} catch (UnsupportedOperationException ignored) {

				}
			}

			return uc;
		} catch(Exception e) {
			logger.error("Error executing command: "+ commandName + ".", e);
			//make these InfoUpdates so that the UI can still process the rest of the model
			return new UpdateContainer(new TrivialErrorUpdate("Error executing command " + commandName + " from history"));
		}
		
		
	}

	private boolean ignoreIfBeforeColumnDoesntExist(String commandName) {
		boolean ignore = false;
		for(String ignoreCom : commandsIgnoreNodeBefore) {
			if(commandName.equals(ignoreCom)) {
				ignore = true;
				break;
			}
		}
		return ignore;
	}

	public UpdateContainer normalizeCommandHistoryJsonInput(Workspace workspace, String worksheetId, 
			JSONArray inputArr, String commandName, boolean addIfNonExist) throws JSONException {
		UpdateContainer uc = null;
		HTable hTable = workspace.getWorksheet(worksheetId).getHeaders();
		for (int i = 0; i < inputArr.length(); i++) {
			JSONObject inpP = inputArr.getJSONObject(i);
			if (inpP.getString(ClientJsonKeys.name.name()).equals("outputColumns") || inpP.getString(ClientJsonKeys.name.name()).equals("inputColumns"))
				continue;
			/*** Check the input parameter type and accordingly make changes ***/
			if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeId) {
				JSONArray hNodeJSONRep = new JSONArray(inpP.get(ClientJsonKeys.value.name()).toString());
				for (int j=0; j<hNodeJSONRep.length(); j++) {
					JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
					if(hTable == null) {
						AbstractUpdate update = new TrivialErrorUpdate("null HTable while normalizing JSON input for the command " + commandName);
						if (uc == null)
							uc = new UpdateContainer(update);
						else
							uc.add(update);
						continue;
					}
					String nameObjColumnName = cNameObj.getString("columnName");
					logger.debug("Column being normalized: "+ nameObjColumnName);
					HNode node = hTable.getHNodeFromColumnName(nameObjColumnName);
					if(node == null) { //Because add column can happen even if the column after which it is to be added is not present
						if (addIfNonExist) {
							node = hTable.addHNode(nameObjColumnName, HNodeType.Transformation, workspace.getWorksheet(worksheetId), workspace.getFactory());		
						}
						else {
							continue;
						}
					}

					if (j == hNodeJSONRep.length()-1) {		// Found!
						if(node != null)
							inpP.put(ClientJsonKeys.value.name(), node.getId());
						else {
							//Get the id of the last node in the table
							ArrayList<String> allNodeIds = hTable.getOrderedNodeIds();
							//TODO check for allNodeIds.size == 0
							String lastNodeId = allNodeIds.get(allNodeIds.size()-1);
							inpP.put(ClientJsonKeys.value.name(), lastNodeId);
						}
						hTable = workspace.
								getWorksheet(worksheetId).getHeaders();
					} else if(node != null) {
						hTable = node.getNestedTable();
						if (hTable == null && addIfNonExist) {
							hTable = node.addNestedTable("NestedTable", workspace.getWorksheet(worksheetId), workspace.getFactory());
						}
					}
				}
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.linkWithHNodeId) {
				JSONObject link = new JSONObject(inpP.get(ClientJsonKeys.value.name()).toString());
				Object subject = link.get("subject");
				String predicate = link.getString("predicate");
				Object object = link.get("object");
				
				String objectHNodeId, subjectHNodeId;
				if(subject instanceof JSONArray) {
					subjectHNodeId = generateHNodeId((JSONArray)subject, hTable, addIfNonExist, uc);
				} else {
					subjectHNodeId = subject.toString();
				}
				if(object instanceof JSONArray) {
					objectHNodeId = generateHNodeId((JSONArray)object, hTable, addIfNonExist, uc);
				} else {
					objectHNodeId = object.toString();
				}
				
				inpP.put(ClientJsonKeys.value.name(), subjectHNodeId + "---" + predicate + "---" + objectHNodeId);
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.worksheetId) {
				inpP.put(ClientJsonKeys.value.name(), worksheetId);
			} else if (HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeIdList) {
				JSONArray hNodes = new JSONArray(inpP.get(ClientJsonKeys.value.name()).toString());
				for (int k = 0; k < hNodes.length(); k++) {
					JSONObject hnodeJSON = hNodes.getJSONObject(k);
					JSONArray hNodeJSONRep = new JSONArray(hnodeJSON.get(ClientJsonKeys.value.name()).toString());
					for (int j=0; j<hNodeJSONRep.length(); j++) {
						JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
						if(hTable == null) {
							AbstractUpdate update = new TrivialErrorUpdate("null HTable while normalizing JSON input for the command " + commandName);
							if (uc == null)
								uc = new UpdateContainer(update);
							else
								uc.add(update);
							continue;
						}
						String nameObjColumnName = cNameObj.getString("columnName");
						logger.debug("Column being normalized: "+ nameObjColumnName);
						HNode node = hTable.getHNodeFromColumnName(nameObjColumnName);
						if(node == null) { //Because add column can happen even if the column after which it is to be added is not present
							AbstractUpdate update = new TrivialErrorUpdate(nameObjColumnName + " does not exist, using empty values");
							if (uc == null)
								uc = new UpdateContainer(update);
							else
								uc.add(update);
							if (addIfNonExist) {
								hTable.addHNode(nameObjColumnName, HNodeType.Regular, workspace.getWorksheet(worksheetId), workspace.getFactory());
							}
							else {
								continue;
							}
						}

						if (j == hNodeJSONRep.length()-1) {		// Found!
							if(node != null)
								hnodeJSON.put(ClientJsonKeys.value.name(), node.getId());
							else {
								//Get the id of the last node in the table
								ArrayList<String> allNodeIds = hTable.getOrderedNodeIds();
								String lastNodeId = allNodeIds.get(allNodeIds.size()-1);
								hnodeJSON.put(ClientJsonKeys.value.name(), lastNodeId);
							}
							hTable = workspace.
									getWorksheet(worksheetId).getHeaders();
						} else if(node != null) {
							hTable = node.getNestedTable();
							if (hTable == null && addIfNonExist) {
								hTable = node.addNestedTable("NestedTable", workspace.getWorksheet(worksheetId), workspace.getFactory());
							}
						}
					}
				}
				inpP.put(ClientJsonKeys.value.name(), hNodes.toString());
			}
			else if (HistoryJsonUtil.getParameterType(inpP) == ParameterType.orderedColumns) {
				JSONArray hNodes = new JSONArray(inpP.get(ClientJsonKeys.value.name()).toString());
				for (int k = 0; k < hNodes.length(); k++) {
					JSONObject hnodeJSON = hNodes.getJSONObject(k);
					JSONArray hNodeJSONRep = new JSONArray(hnodeJSON.get(ClientJsonKeys.id.name()).toString());
					processHNodeId(hNodeJSONRep, hTable, commandName, hnodeJSON);
					if (hnodeJSON.has(ClientJsonKeys.children.name())) {
						JSONArray children = new JSONArray(hnodeJSON.get(ClientJsonKeys.children.name()).toString());
						hnodeJSON.put(ClientJsonKeys.children.name(), processChildren(children, hTable, commandName));
					}

				}
				inpP.put(ClientJsonKeys.value.name(), hNodes.toString());
			}
		}
		
		return uc;
	}

	private String generateHNodeId(JSONArray hNodeJSONRep, HTable hTable, boolean addIfNonExist, UpdateContainer uc) {
		String hNodeId = null;
		for (int j=0; j<hNodeJSONRep.length(); j++) {
			JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
			if(hTable == null) {
				AbstractUpdate update = new TrivialErrorUpdate("null HTable while normalizing JSON input for the command");
				if (uc == null)
					uc = new UpdateContainer(update);
				else
					uc.add(update);
				continue;
			}
			String nameObjColumnName = cNameObj.getString("columnName");
			logger.debug("Column being normalized: "+ nameObjColumnName);
			HNode node = hTable.getHNodeFromColumnName(nameObjColumnName);
			if(node == null) { //Because add column can happen even if the column after which it is to be added is not present
				AbstractUpdate update = new TrivialErrorUpdate(nameObjColumnName + " does not exist, using empty values");
				if (uc == null)
					uc = new UpdateContainer(update);
				else
					uc.add(update);
				if (addIfNonExist) {
					node = hTable.addHNode(nameObjColumnName, HNodeType.Regular, workspace.getWorksheet(worksheetId), workspace.getFactory());		
				}
				else {
					continue;
				}
			}

			if (j == hNodeJSONRep.length()-1) {		// Found!
				if(node != null)
					hNodeId = node.getId();
				else {
					//Get the id of the last node in the table
					ArrayList<String> allNodeIds = hTable.getOrderedNodeIds();
					//TODO check for allNodeIds.size == 0
					String lastNodeId = allNodeIds.get(allNodeIds.size()-1);
					hNodeId = lastNodeId;
				}
				hTable = workspace.
						getWorksheet(worksheetId).getHeaders();
			} else if(node != null) {
				hTable = node.getNestedTable();
				if (hTable == null && addIfNonExist) {
					hTable = node.addNestedTable("NestedTable", workspace.getWorksheet(worksheetId), workspace.getFactory());
				}
			}
		}
		return hNodeId;
	}
	private boolean processHNodeId(JSONArray hNodeJSONRep, HTable hTable, String commandName, JSONObject hnodeJSON) {
		for (int j=0; j<hNodeJSONRep.length(); j++) {
			JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
			if(hTable == null) {
				return false;
			}
			String nameObjColumnName = cNameObj.getString("columnName");
			logger.debug("Column being normalized: "+ nameObjColumnName);
			HNode node = hTable.getHNodeFromColumnName(nameObjColumnName);
			if(node == null && !ignoreIfBeforeColumnDoesntExist(commandName)) { //Because add column can happen even if the column after which it is to be added is not present
				logger.info("null HNode " + nameObjColumnName + " while normalizing JSON input for the command " + commandName);
				return false;
			}

			if (j == hNodeJSONRep.length()-1) {		// Found!
				if(node != null)
					hnodeJSON.put(ClientJsonKeys.id.name(), node.getId());
				else {
					//Get the id of the last node in the table
					ArrayList<String> allNodeIds = hTable.getOrderedNodeIds();
					String lastNodeId = allNodeIds.get(allNodeIds.size()-1);
					hnodeJSON.put(ClientJsonKeys.id.name(), lastNodeId);
				}
				hTable = workspace.
						getWorksheet(worksheetId).getHeaders();
			} else if(node != null) {
				hTable = node.getNestedTable();
			}
		}
		return true;
	}
	private JSONArray processChildren(JSONArray children, HTable hTable, String commandName) {
		for (int i = 0; i < children.length(); i++) {
			JSONObject obj = children.getJSONObject(i);
			JSONArray array = new JSONArray(obj.get(ClientJsonKeys.id.name()).toString());
			processHNodeId(array, hTable, commandName, obj);
			if (obj.has(ClientJsonKeys.children.name())) {
				obj.put(ClientJsonKeys.children.name(), processChildren(new JSONArray(obj.get(ClientJsonKeys.children.name()).toString()), hTable, commandName));
			}
		}
		return children;
	}
}
