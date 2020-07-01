package edu.isi.karma.controller.history;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class CommandHistoryUtil {
	private final List<Command> commands = new ArrayList<>();
	private Workspace workspace;
	private String worksheetId;
	Map<String, CommandFactory> commandFactoryMap;
	static Logger logger = LogManager.getLogger(CommandHistoryUtil.class.getName());

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
		logger.info("***** Replay History for worksheet :" + worksheetId + " ****");
		try {
			Worksheet newWorksheet = oldWorksheet.getImportMethod().duplicate().generateWorksheet();
			Alignment alignment = AlignmentManager.Instance().createAlignment(workspace.getId(), newWorksheet.getId(), workspace.getOntologyManager());
			workspace.removeWorksheet(worksheetId);
			uc.add(new WorksheetDeleteUpdate(worksheetId));
			this.worksheetId = newWorksheet.getId();
			uc.add(new WorksheetListUpdate());
			uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
			commands.clear();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			newWorksheet.getHeaders().prettyPrint("", pw, new RepFactory());
			logger.debug("New worksheet headers:" + sw.toString());
			
			//Use the WorksheetCommandHistoryExecutor. It takes care of creating missing columns,
			//saving history only at the end of teh batch, etc
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);
			UpdateContainer hc = histExecutor.executeAllCommands(redoCommandsArray);
			uc.append(hc);
	
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
