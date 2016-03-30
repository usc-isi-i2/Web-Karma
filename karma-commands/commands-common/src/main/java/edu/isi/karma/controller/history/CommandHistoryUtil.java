package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.controller.update.*;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;

public class CommandHistoryUtil {
	private final List<Command> commands = new ArrayList<>();
	private Workspace workspace;
	private String worksheetId;
	Map<String, CommandFactory> commandFactoryMap;
	static Logger logger = Logger.getLogger(CommandHistoryUtil.class);

	public CommandHistoryUtil(List<Command> commands, Workspace workspace, String worksheetId) {
		this.worksheetId = worksheetId;
		this.workspace = workspace;
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		commandFactoryMap = ctrl.getCommandFactoryMap();
		this.commands.addAll(commands);
	}
	private Map<Command, List<Command> > generateGraph() {

		Map<Command, List<Command> > dag = new HashMap<>();
		Map<String, List<Command> > outputMapping = new HashMap<>();

		for (Command command : commands) {
			Set<String> inputs = command.getInputColumns();
			for (String input : inputs) {
				List<Command> outputCommands = outputMapping.get(input);
				if (outputCommands != null) {
					List<Command> edges = dag.get(command);
					if (edges == null)
						edges = new ArrayList<>();
					for (Command tmp : outputCommands) {
						if (tmp != command)
							edges.add(tmp);
					}
					dag.put(command, edges);
				}
			}
			Set<String> outputs = command.getOutputColumns();
			for (String output : outputs) {
				List<Command> tmp = outputMapping.get(output);
				if (tmp == null)
					tmp = new ArrayList<>();
				tmp.add(command);
				outputMapping.put(output, tmp);
			}
		}

		return dag;
	}

	public Set<String> generateInputColumns() {
		Map<Command, List<Command> > dag = generateGraph();
		Set<String> inputColumns = new HashSet<>();
		for (Command t : commands) {
			if (t.getCommandName().equals("SetSemanticTypeCommand") || t.getCommandName().equals("SetMetaPropertyCommand")) {
				inputColumns.addAll(getParents(t, dag));
			}
		}
		return inputColumns;
	}

	public Set<String> generateOutputColumns() {
		Set<String> outputColumns = new HashSet<>();
		for (Command t : commands) {
			if (t.getOutputColumns() != null)
				outputColumns.addAll(t.getOutputColumns());
		}
		return outputColumns;
	}


	private Set<String> getParents(Command c, Map<Command, List<Command> >dag) {
		List<Command> parents = dag.get(c);
		Set<String> terminalColumns = new HashSet<>();
		if (parents == null || parents.size() == 0)
			terminalColumns.addAll(c.getInputColumns());
		else {
			for (Command t : parents) {
				terminalColumns.addAll(getParents(t, dag));
				for (String hNodeId : c.getInputColumns()) {
					HNode hn = workspace.getFactory().getHNode(hNodeId);
					if (hn != null && hn.getHNodeType() == HNodeType.Regular)
						terminalColumns.add(hNodeId);
				}
			}
		}
		return terminalColumns;
	}

	public UpdateContainer replayHistory() {
		JSONArray redoCommandsArray = new JSONArray();
		for (Command refined : commands) {
			redoCommandsArray.put(workspace.getCommandHistory().getCommandJSON(workspace, refined));
		}
		return replayHistory(redoCommandsArray);
	}

	public UpdateContainer replayHistory(JSONArray redoCommandsArray) {
		UpdateContainer uc = new UpdateContainer();
		Worksheet oldWorksheet = workspace.getFactory().getWorksheet(worksheetId);
		oldWorksheet.getImportMethod().createWorksheet(oldWorksheet.getTitle(), workspace, oldWorksheet.getEncoding());
		try {
			Worksheet newWorksheet = oldWorksheet.getImportMethod().generateWorksheet();
			Alignment alignment = AlignmentManager.Instance().createAlignment(workspace.getId(), newWorksheet.getId(), workspace.getOntologyManager());
			workspace.removeWorksheet(worksheetId);
			uc.add(new WorksheetDeleteUpdate(worksheetId));
			this.worksheetId = newWorksheet.getId();
			uc.add(new WorksheetListUpdate());
			uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
			commands.clear();
			for (int i = 0; i < redoCommandsArray.length(); i++) {
				JSONObject commandObject = redoCommandsArray.getJSONObject(i);
				Command command = getCommandFromHistoryJSON(commandObject, uc);
				if (command != null) {
					commands.add(command);
					command.setExecutedInBatch(true);
					try {
						uc.append(workspace.getCommandHistory().doCommand(command, workspace, true));
					} catch (Exception e) {
						uc.add(new TrivialErrorUpdate("Error occurred in command " + command.getCommandName()));
					}
					command.setExecutedInBatch(false);
				}
			}
			if(alignment != null) {
				alignment.align();
				uc.removeUpdateByClass(AlignmentSVGVisualizationUpdate.class);
				uc.add(new AlignmentSVGVisualizationUpdate(worksheetId));
			}
			workspace.getCommandHistory().clearCurrentCommand(worksheetId);
			workspace.getCommandHistory().clearRedoCommand(worksheetId);
		} catch (Exception e) {
			commands.clear();
			logger.error("Fail to replay history", e);
		}
		return uc;
	}

	public List<Command> getCommands() {
		return new ArrayList<>(commands);
	}

	private Command getCommandFromHistoryJSON(JSONObject historyJSON, UpdateContainer uc) {
		JSONArray inputParamArr = (JSONArray) historyJSON.get(HistoryArguments.inputParameters.name());
		String commandName = (String)historyJSON.get(HistoryArguments.commandName.name());
		WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
		UpdateContainer errors = ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, false);
		if (errors != null) {
			uc.append(errors);
		}
		String tmp = CommandInputJSONUtil.getStringValue("outputColumns", inputParamArr);
		Set<String> newOutputColumns = new HashSet<>();
		if (tmp != null) {
			JSONArray array = new JSONArray(tmp);
			for (int j = 0; j < array.length(); j++) {
				JSONObject obj = new JSONObject(array.get(j).toString());
				newOutputColumns.add(obj.get("value").toString());
			}
		}
		logger.debug(inputParamArr.toString(4));
		CommandFactory cf = commandFactoryMap.get(historyJSON.get(HistoryArguments.commandName.name()));
		if(cf != null) {
			try { // This is sort of a hack the way I did this, but could not think of a better way to get rid of the dependency
				String model = Command.NEW_MODEL;
				if(historyJSON.has(HistoryArguments.model.name())) {
					model = historyJSON.getString(HistoryArguments.model.name());
				}
				Command comm = cf.createCommand(inputParamArr, model, workspace);
				comm.setOutputColumns(newOutputColumns);
				if(comm != null){
					return comm;
				}

			} catch (Exception ignored) {
				logger.error("Error in executing command", ignored);
			}
		}
		return null;
	}

	public void removeCommands(Set<String> commandIds) {
		Iterator<Command> commandItr = commands.iterator();
		while (commandItr.hasNext()) {
			Command command = commandItr.next();
			if (commandIds.contains(command.getId())) {
				commandItr.remove();
			}
		}
	}

	public void retainCommands(Set<String> commandIds) {
		Iterator<Command> commandItr = commands.iterator();
		while (commandItr.hasNext()) {
			Command command = commandItr.next();
			if (!commandIds.contains(command.getId())) {
				commandItr.remove();
			}
		}
	}

	public String getWorksheetId() {
		return worksheetId;
	}

	public JSONArray getCommandsJSON() {
		JSONArray commandsArray = new JSONArray();
		for (Command command : commands) {
			commandsArray.put(workspace.getCommandHistory().getCommandJSON(workspace, command));
		}
		return commandsArray;
	}

}
