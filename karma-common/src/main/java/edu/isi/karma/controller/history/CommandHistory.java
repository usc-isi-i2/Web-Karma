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
/**
 * 
 */
package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.steiner.topk.Graph;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author szekely
 * 
 */
public class CommandHistory implements Cloneable{

	private WorksheetCommandHistory worksheetCommandHistory = new WorksheetCommandHistory();
	private Command previewCommand;
	/**
	 * If the last command was undo, and then we do a command that goes on the
	 * history, then we need to send the browser the full history BEFORE we send
	 * the update for the command the user just did. The reason is that the
	 * history may contain undoable commands, and the browser does not know how
	 * to reset the history.
	 */

	/**
	 * Used to keep a pointer to the command which require user-interaction
	 * through multiple HTTP requests.
	 */

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static Map<String, IHistorySaver> historySavers = new HashMap<>();

	private final static Set<CommandConsolidator> consolidators = new HashSet<>();

	static {
		Reflections reflections = new Reflections("edu.isi.karma");
		Set<Class<? extends CommandConsolidator>> subTypes =
				reflections.getSubTypesOf(CommandConsolidator.class);
		for (Class<? extends CommandConsolidator> subType : subTypes)
		{
			if(!Modifier.isAbstract(subType.getModifiers()) && !subType.isInterface()) {
				try {
					consolidators.add(subType.newInstance());
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public enum HistoryArguments {
		worksheetId, commandName, inputParameters, hNodeId, tags, model
	}

	public CommandHistory() {
	}

	public CommandHistory(WorksheetCommandHistory worksheetCommandHistory) {
		this.worksheetCommandHistory = worksheetCommandHistory;
	}

	public List<ICommand> _getHistory() {
		return worksheetCommandHistory.getAllCommands();
	}

	public CommandHistory clone() {
		return new CommandHistory(worksheetCommandHistory.clone());
	}

	/**
	 * Commands go onto the history when they have all their arguments and are
	 * ready to be executed. If a command needs multiple user interactions to
	 * define the parameters, then the command object must be created,
	 * interacted with and then executed. 
	 * 
	 * @param command
	 * @param workspace
	 * @return UpdateContainer with all the changes done by the command.
	 * @throws CommandException
	 */
	public UpdateContainer doCommand(Command command, Workspace workspace)
			throws CommandException {
		return doCommand(command, workspace, true);
	}

	public UpdateContainer doCommand(Command command, Workspace workspace, boolean saveToHistory)
			throws CommandException {
		UpdateContainer effects = new UpdateContainer();
		Pair<ICommand, Object> consolidatedCommand = null;
		String consolidatorName = null;
		String worksheetId = worksheetCommandHistory.getWorksheetId(command);
		RepFactory factory = workspace.getFactory();
		List<ICommand> potentialConsolidateCommands = worksheetCommandHistory.getCommandsFromWorksheetIdAndCommandTag(worksheetId, command.getTagFromPriority());
		for (CommandConsolidator consolidator : consolidators) {
			consolidatedCommand = consolidator.consolidateCommand(potentialConsolidateCommands, command, workspace);
			if (consolidatedCommand != null) {
				consolidatorName = consolidator.getConsolidatorName();
				break;
			}
		}
		
		if (consolidatedCommand != null) {
			worksheetCommandHistory.setStale(worksheetId, true);
			if (consolidatorName.equals("PyTransformConsolidator")) {
				Command runCommand = (Command)consolidatedCommand.getLeft();
				effects.append(runCommand.doIt(workspace));
				
				if(runCommand.getInputColumns().size() > 0 && 
						!runCommand.getInputColumns().equals(command.getInputColumns())) {
					Pair<Boolean, Set<String>> cycleDetect = detectTransformCycle(worksheetId);
					if(cycleDetect.getLeft() == false) {
						
						placeTransformCommandByInput(runCommand);
					} else {
						StringBuilder columns = new StringBuilder();
						String sep = "";
						System.out.println(cycleDetect.getRight());
						for(String nodeId : cycleDetect.getRight()) {
							HNode node = factory.getHNode(nodeId);
							columns.append(sep);
							if(node != null)
								columns.append(node.getAbsoluteColumnName(factory));
							else {
								Node repNode = factory.getNode(nodeId);
								if(repNode != null)
									columns.append(repNode.prettyPrint(factory));
								else
									columns.append(nodeId);
							}
							sep = ", ";
						}
						AbstractUpdate update = new TrivialErrorUpdate("A cycle occurs in the Column Transforms for Columns: " + columns.toString() + ". Please fix the cycle");
						effects.add(update);
					}
				}
			}
			if (consolidatorName.equals("UnassignSemanticTypesConsolidator")) {
				worksheetCommandHistory.removeCommandFromHistory(Arrays.asList(consolidatedCommand.getLeft()));
				effects.append(command.doIt(workspace));
			}
			if (consolidatorName.equals("SemanticTypesConsolidator")) {
				worksheetCommandHistory.replaceCommandFromHistory(consolidatedCommand.getKey(), (ICommand)consolidatedCommand.getRight());
				effects.append(((ICommand) consolidatedCommand.getRight()).doIt(workspace));
			}
			if (consolidatorName.equals("OrganizeColumnsConsolidator")) {
				effects.append(((ICommand) consolidatedCommand.getRight()).doIt(workspace));
				worksheetCommandHistory.replaceCommandFromHistory(consolidatedCommand.getKey(), (ICommand)consolidatedCommand.getRight());
			}
			if (consolidatorName.equals("DeleteNodeConsolidator")) {
				worksheetCommandHistory.removeCommandFromHistory(Arrays.asList(consolidatedCommand.getLeft()));
				effects.append(command.doIt(workspace));
			}
			if (consolidatorName.equals("AddLiteralNodeConsolidator")) {
				worksheetCommandHistory.replaceCommandFromHistory(consolidatedCommand.getKey(), (ICommand)consolidatedCommand.getRight());
				effects.append(((ICommand) consolidatedCommand.getRight()).doIt(workspace));
			}
			if (consolidatorName.equals("DeleteLinkConsolidator")) {
				worksheetCommandHistory.removeCommandFromHistory(Arrays.asList(consolidatedCommand.getLeft()));
				effects.append(command.doIt(workspace));
			}
			if (consolidatorName.equals("AddLinkConsolidator")) {
				worksheetCommandHistory.replaceCommandFromHistory(consolidatedCommand.getKey(), (ICommand)consolidatedCommand.getRight());
				effects.append(((ICommand) consolidatedCommand.getRight()).doIt(workspace));
			}
			if (consolidatorName.equals("AddColumnConsolidator")) {
				worksheetCommandHistory.removeCommandFromHistory(Arrays.asList(consolidatedCommand.getLeft()));
				ICommand runCommand = ((ICommand) consolidatedCommand.getRight());
				worksheetCommandHistory.insertCommandToHistory(runCommand);
				effects.append(runCommand.doIt(workspace));
			}
		}
		else {
			effects.append(command.doIt(workspace));
		}
		command.setExecuted(true);

		
		
		if (command.getCommandType() != CommandType.notInHistory) {
			worksheetId = worksheetCommandHistory.getWorksheetId(command);
			worksheetCommandHistory.clearRedoCommand(worksheetId);
			worksheetCommandHistory.setCurrentCommand(command, consolidatedCommand);
			if (consolidatedCommand == null) {
				worksheetCommandHistory.insertCommandToHistory(command);
			}
			effects.add(new HistoryUpdate(this));
		}

		if(saveToHistory) {
			// Save the modeling commands
			if (!(instanceOf(command, "ResetKarmaCommand"))) {
				try {
					if(isHistoryWriteEnabled && command.isSavedInHistory() &&
							(command.hasTag(CommandTag.Modeling)
							|| command.hasTag(CommandTag.Transformation)
							|| command.hasTag(CommandTag.Selection)
							|| command.hasTag(CommandTag.SemanticType)
							) && historySavers.get(workspace.getId()) != null) {
						writeHistoryPerWorksheet(workspace, historySavers.get(workspace.getId()));
					}
				} catch (Exception e) {
					logger.error("Error occured while writing history!" , e);
					logger.error("Error with this command: {}, Input params: {}", command.getCommandName(), command.getInputParameterJson());
				}
			}
		}

		return effects;
	}

	private void placeTransformCommandByInput(Command command) {
		ICommand placeAfterCmd = null;
		Set<String> cmdInputs = command.getInputColumns();
		Set<String> cmdOutputs = command.getOutputColumns();
		List<Command> commandsToMove = new ArrayList<>();
		for (ICommand afterCmd : worksheetCommandHistory.getCommandsAfterCommand(command, CommandTag.Transformation)) {
			Set<String> afterCmdOutputs = ((Command)afterCmd).getOutputColumns();
			Set<String> afterCmdInputs = ((Command)afterCmd).getInputColumns();
			for(String input : cmdInputs) {
				if(afterCmdOutputs.contains(input)) {
					placeAfterCmd = afterCmd;
					break;
				}
			}
			for(String output : cmdOutputs) {
				if(afterCmdInputs.contains(output)) {
					commandsToMove.add((Command)afterCmd);
					break;
				}
			}
		}
		
		if (placeAfterCmd != null) {
			worksheetCommandHistory.removeCommandFromHistory(Arrays.asList((ICommand)command));
			JSONArray inputJSON = new JSONArray(command.getInputParameterJson());
			JSONArray placeAfterCmdInputJSON = new JSONArray(command.getInputParameterJson());
			String placeHNodeId = HistoryJsonUtil.getStringValue("hNodeId", placeAfterCmdInputJSON);
			HistoryJsonUtil.setArgumentValue("hNodeId", placeHNodeId, inputJSON);
			command.setInputParameterJson(inputJSON.toString());
			worksheetCommandHistory.insertCommandToHistoryAfterCommand(command, placeAfterCmd);
		}
		
		for(Command cmd : commandsToMove)
			placeTransformCommandByInput(cmd);
	}
	
	private Pair<Boolean, Set<String>> detectTransformCycle(String worksheetId) {
		DirectedGraph<String, DefaultEdge> historyInputOutputGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		for (ICommand cmd : worksheetCommandHistory.getCommandsFromWorksheetIdAndCommandTag(worksheetId, CommandTag.Transformation)) {
			Set<String> outputs = ((Command)cmd).getOutputColumns();
			Set<String> inputs = ((Command)cmd).getInputColumns();
			String commandId = "Command-" + cmd.getId();
			historyInputOutputGraph.addVertex(commandId);
			for(String input : inputs) {
				historyInputOutputGraph.addVertex(input);
				historyInputOutputGraph.addEdge(input, commandId);
			}
			for(String output : outputs) {
				historyInputOutputGraph.addVertex(output);
				historyInputOutputGraph.addEdge(commandId, output);
			}
		}
		CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(historyInputOutputGraph);
		if(cycleDetector.detectCycles()) {
			Set<String> cycleVertices = cycleDetector.findCycles();
			Set<String> nodeVertices = new HashSet<>();
			Iterator<String> iterator = cycleVertices.iterator();
			while(iterator.hasNext()) {
				String node = iterator.next();
				if(!node.startsWith("Command-"))
					nodeVertices.add(node);
			}
			
			return new ImmutablePair<>(true, nodeVertices);
		}
		return new ImmutablePair<Boolean, Set<String>>(false, null);
	}
	
	private void writeHistoryPerWorksheet(Workspace workspace, IHistorySaver historySaver) throws Exception {
		String workspaceId = workspace.getId();
		Map<String, JSONArray> comMap = new HashMap<>();

		for(ICommand command : _getHistory()) {
			if(command.isSavedInHistory() &&
					(command.hasTag(CommandTag.Modeling)
					|| command.hasTag(CommandTag.Transformation)
					|| command.hasTag(CommandTag.Selection)
					|| command.hasTag(CommandTag.SemanticType)
					)) {
				JSONArray json = new JSONArray(command.getInputParameterJson());
				String worksheetId = HistoryJsonUtil.getStringValue(HistoryArguments.worksheetId.name(), json);
				if(workspace.getWorksheet(worksheetId) != null)
				{ 
					try {
						if(comMap.get(worksheetId) == null)
							comMap.put(worksheetId, new JSONArray());
						comMap.get(worksheetId).put(getCommandJSON(workspace, command));
					} catch(Exception e) {
						logger.error("Error serializing command {} to history, Input:{}", command.getCommandName(), command.getInputParameterJson());
					}
				}
			}
		}

		for(Map.Entry<String, JSONArray> stringJSONArrayEntry : comMap.entrySet()) {
			JSONArray comms = stringJSONArrayEntry.getValue();
			historySaver.saveHistory(workspaceId, stringJSONArrayEntry.getKey(), comms);
		}
	}


	public JSONObject getCommandJSON(Workspace workspace, ICommand comm) {
		JSONObject commObj = new JSONObject();
		commObj.put(HistoryArguments.commandName.name(), comm.getCommandName());
		commObj.put(HistoryArguments.model.name(), comm.getModel());
		
		// Populate the tags
		JSONArray tagsArr = new JSONArray();
		for (CommandTag tag : comm.getTags())
			tagsArr.put(tag.name());
		commObj.put(HistoryArguments.tags.name(), tagsArr);

		JSONArray inputArr = new JSONArray(comm.getInputParameterJson() == null ? "[]" : comm.getInputParameterJson());
		for (int i = 0; i < inputArr.length(); i++) {
			JSONObject inpP = inputArr.getJSONObject(i);

			/*** Check the input parameter type and accordingly make changes ***/
			if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeIdList) {
				JSONArray hnodes = (JSONArray) JSONUtil.createJson(inpP.getString(ClientJsonKeys.value.name()));
				for (int j = 0; j < hnodes.length(); j++) {
					JSONObject obj = (JSONObject)hnodes.get(j);
					Object value = obj.get(ClientJsonKeys.value.name());
					if (value instanceof String) {
						String hNodeId = (String) value;
						HNode node = workspace.getFactory().getHNode(hNodeId);
						JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
						obj.put(ClientJsonKeys.value.name(), hNodeRepresentation);
					}
				}
				inpP.put(ClientJsonKeys.value.name(), hnodes);
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.orderedColumns) {
				Object tmp = inpP.get(ClientJsonKeys.value.name());
				JSONArray hnodes = (JSONArray) JSONUtil.createJson(tmp.toString());
				for (int j = 0; j < hnodes.length(); j++) {
					JSONObject obj = (JSONObject)hnodes.get(j);
					String hNodeId = obj.getString(ClientJsonKeys.id.name());
					HNode node = workspace.getFactory().getHNode(hNodeId);
					JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
					obj.put(ClientJsonKeys.id.name(), hNodeRepresentation);
					if (obj.has(ClientJsonKeys.children.name()))
						obj.put(ClientJsonKeys.children.name(), parseChildren(obj.get(ClientJsonKeys.children.name()).toString(), workspace));
				}
				inpP.put(ClientJsonKeys.value.name(), hnodes);
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeId) {
				String hNodeId = inpP.getString(ClientJsonKeys.value.name());
				HNode node = workspace.getFactory().getHNode(hNodeId);
				JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
				inpP.put(ClientJsonKeys.value.name(), hNodeRepresentation);

			} else if (HistoryJsonUtil.getParameterType(inpP) == ParameterType.worksheetId) {
				inpP.put(ClientJsonKeys.value.name(), "W");
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.linkWithHNodeId) {
				String link = inpP.getString(ClientJsonKeys.value.name());
				String[] linkParts = link.split("---");
				String subject = linkParts[0];
				String predicate = linkParts[1];
				String object = linkParts[2];
				
				JSONObject linkObj = new JSONObject();
				HNode subjectNode = workspace.getFactory().getHNode(subject);
				if(subjectNode != null) {
					JSONArray hNodeRepresentation = subjectNode.getJSONArrayRepresentation(workspace.getFactory());
					linkObj.put("subject", hNodeRepresentation);
				} else {
					linkObj.put("subject", subject);
				}
				linkObj.put("predicate", predicate);
				HNode objectNode = workspace.getFactory().getHNode(object);
				if(objectNode != null) {
					JSONArray hNodeRepresentation = objectNode.getJSONArrayRepresentation(workspace.getFactory());
					linkObj.put("object", hNodeRepresentation);
				} else {
					linkObj.put("object", object);
				}
				
				inpP.put(ClientJsonKeys.value.name(), linkObj);
			} else {
				// do nothing
			}
		}
		if (comm instanceof Command) {
			Command tmp = (Command)comm;
			JSONArray inputArray = new JSONArray();
			for (String hNodeId : tmp.getInputColumns()) {
				HNode node = workspace.getFactory().getHNode(hNodeId);
				JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
				JSONObject obj1 = new JSONObject();
				obj1.put(ClientJsonKeys.value.name(), hNodeRepresentation);
				inputArray.put(obj1);
			}
			JSONObject obj = HistoryJsonUtil.getJSONObjectWithName("inputColumns", inputArr);			
			if (obj == null) {
				obj = new JSONObject();
				obj.put(ClientJsonKeys.name.name(), "inputColumns");
				obj.put(ClientJsonKeys.value.name(), inputArray.toString());
				obj.put(ClientJsonKeys.type.name(), ParameterType.hNodeIdList.name());
				inputArr.put(obj);
			}
			else {
				obj.put(ClientJsonKeys.value.name(), inputArray.toString());
			}

			JSONArray outputArray = new JSONArray();
			for (String hNodeId : tmp.getOutputColumns()) {
				HNode node = workspace.getFactory().getHNode(hNodeId);
				JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
				JSONObject obj1 = new JSONObject();
				obj1.put(ClientJsonKeys.value.name(), hNodeRepresentation);
				outputArray.put(obj1);
			}
			obj = HistoryJsonUtil.getJSONObjectWithName("outputColumns", inputArr);						
			if (obj == null) {
				obj = new JSONObject();
				obj.put(ClientJsonKeys.name.name(), "outputColumns");
				obj.put(ClientJsonKeys.value.name(), outputArray.toString());
				obj.put(ClientJsonKeys.type.name(), ParameterType.hNodeIdList.name());
				inputArr.put(obj);
			}
			else {
				obj.put(ClientJsonKeys.value.name(), outputArray.toString());
			}
		}
		commObj.put(HistoryArguments.inputParameters.name(), inputArr);

		return commObj;
	}

	private boolean instanceOf(Object o, String className) { // TODO this is a hack, but instanceof doesn't really seem appropriate here
		return o.getClass().getName().toLowerCase().contains(className.toLowerCase());
	}

	private String parseChildren(String inputJSON, Workspace workspace) {
		JSONArray array = (JSONArray) JSONUtil.createJson(inputJSON);
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String hNodeId = obj.getString(ClientJsonKeys.id.name());
			HNode node = workspace.getFactory().getHNode(hNodeId);
			JSONArray hNodeRepresentation = node.getJSONArrayRepresentation(workspace.getFactory());
			obj.put(ClientJsonKeys.id.name(), hNodeRepresentation);
			if (obj.has(ClientJsonKeys.children.name()))
				obj.put(ClientJsonKeys.children.name(), parseChildren(obj.get(ClientJsonKeys.children.name()).toString(), workspace));
		}
		return array.toString();
	}

	/**
	 * @param workspace
	 *            is the id of a command that should be either in the undo or
	 *            redo histories. If it is in none, then nothing will be done.
	 * @return the effects of the undone or redone commands.
	 * @throws CommandException
	 */
	public UpdateContainer undoOrRedoCommand(Workspace workspace, String worksheetId) throws CommandException {
		RedoCommandObject currentCommand = worksheetCommandHistory.getCurrentRedoCommandObject(worksheetId);
		RedoCommandObject lastCommand = worksheetCommandHistory.getLastRedoCommandObject(worksheetId);
		UpdateContainer container = new UpdateContainer();
		if (lastCommand == null) {
			worksheetCommandHistory.setLastRedoCommandObject(currentCommand);
			Pair<ICommand, Object> pair = currentCommand.getConsolidatedCommand();
			if (pair == null) {
				container.append(currentCommand.getCommand().undoIt(workspace));
				worksheetCommandHistory.removeCommandFromHistory(Arrays.asList(currentCommand.getCommand()));
			} else {
				if (pair.getLeft().getCommandName().equals("SubmitPythonTransformationCommand")) {
					pair.getLeft().setInputParameterJson(pair.getRight().toString());
					try {
						Method method = pair.getLeft().getClass().getMethod("setTransformationCode", String.class);
						method.invoke(pair.getLeft(), HistoryJsonUtil.getStringValue("transformationCode", (JSONArray)pair.getRight()));
						container.append(pair.getLeft().doIt(workspace));
					} catch (Exception e) {
						logger.warn("Method invocation failure", e);
					}
				}
				else if (pair.getLeft().getCommandName().equals("SetSemanticTypeCommand") || pair.getLeft().getCommandName().equals("SetMetaPropertyCommand")) {
					container.append(pair.getLeft().doIt(workspace));
					worksheetCommandHistory.insertCommandToHistory(pair.getLeft());
				}
			}
		}
		else {
			container.append(doCommand((Command) lastCommand.getCommand(), workspace));
		}
		container.add(new HistoryUpdate(this));
		return container;
	}

	public void generateFullHistoryJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		boolean isFirst = true;
		for (String worksheetId : worksheetCommandHistory.getAllWorksheetId()) {
			Iterator<ICommand> histIt = worksheetCommandHistory.getCommandsFromWorksheetId(worksheetId).iterator();
			RedoCommandObject currentCommand = worksheetCommandHistory.getCurrentRedoCommandObject(worksheetId);
			RedoCommandObject redoCommandObject = worksheetCommandHistory.getLastRedoCommandObject(worksheetId);
			while (histIt.hasNext()) {
				ICommand command = histIt.next();
				if (isFirst) {
					isFirst = false;
				}
				else {
					pw.println(prefix + ",");
				}
				if (currentCommand != null && command == currentCommand.getCommand()) {
					command.generateJson(prefix, pw, vWorkspace,
							Command.HistoryType.lastRun);
				}
				else if (currentCommand != null && currentCommand.getConsolidatedCommand() != null && currentCommand.getConsolidatedCommand().getKey() == command) {
					command.generateJson(prefix, pw, vWorkspace,
							Command.HistoryType.lastRun);
				}
				else {
					command.generateJson(prefix, pw, vWorkspace,
							Command.HistoryType.normal);
				}
			}
			if (redoCommandObject != null) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					pw.println(prefix + ",");
				}
				redoCommandObject.getCommand().generateJson(prefix, pw, vWorkspace,
						Command.HistoryType.redo);
			}
		}
	}

	public void removeCommands(String worksheetId) {
		List<ICommand> commandsFromWorksheet = worksheetCommandHistory.getCommandsFromWorksheetId(worksheetId);
		this.worksheetCommandHistory.removeCommandFromHistory(commandsFromWorksheet);
	}

	public List<Command> getCommandsFromWorksheetId(String worksheetId) {
		List<Command> commandsFromWorksheet = new ArrayList<>();
		List<ICommand> history = worksheetCommandHistory.getCommandsFromWorksheetId(worksheetId);
		for(ICommand command : history) {
			if(command instanceof Command && command.isSavedInHistory() &&
					(command.hasTag(CommandTag.Modeling)
					|| command.hasTag(CommandTag.Transformation)
					|| command.hasTag(CommandTag.Selection)
					|| command.hasTag(CommandTag.SemanticType)
					)) {
				commandsFromWorksheet.add((Command) command);
			}

		}
		return commandsFromWorksheet;
	}

	public void addPreviewCommand(Command c) {
		previewCommand = c;
	}

	public boolean isStale(String worksheetId) {
		return worksheetCommandHistory.isStale(worksheetId);
	}

	public void setStale(String worksheetId, boolean stale) {
		worksheetCommandHistory.setStale(worksheetId, stale);
	}

	public void clearCurrentCommand(String worksheetId) {
		worksheetCommandHistory.clearCurrentCommand(worksheetId);
	}

	public void clearRedoCommand(String worksheetId) {
		worksheetCommandHistory.clearRedoCommand(worksheetId);
	}

	public void removeWorksheetHistory(String worksheetId) {
		worksheetCommandHistory.removeWorksheet(worksheetId);
	}

	public Command getPreviewCommand(String commandId) {
		if (previewCommand.getId().equals(commandId))
			return previewCommand;
		return null;
	}

	private static boolean isHistoryWriteEnabled = false;

	public static void setIsHistoryEnabled(boolean isHistoryWriteEnabled)
	{
		CommandHistory.isHistoryWriteEnabled = isHistoryWriteEnabled;
	}

	public static void setHistorySaver(String workspaceId, IHistorySaver saver) {
		historySavers.put(workspaceId, saver);
	}

	public static IHistorySaver getHistorySaver(String workspaceId) {
		return historySavers.get(workspaceId);
	}

}
