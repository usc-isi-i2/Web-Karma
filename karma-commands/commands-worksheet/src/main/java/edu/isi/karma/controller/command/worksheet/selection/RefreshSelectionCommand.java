package edu.isi.karma.controller.command.worksheet.selection;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.command.selection.LargeSelection.Operation;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SelectionManager;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;

public class RefreshSelectionCommand extends WorksheetSelectionCommand {

	private String hNodeId;
	public RefreshSelectionCommand(String id, String model, String worksheetId, 
			String selectionId, String hNodeId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		addTag(CommandTag.Selection);
		addTag(CommandTag.IgnoreInBatch);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Update Selection";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		SuperSelection superSel = getSuperSelection(worksheet);
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = superSel.getSelection(hTable.getId());
		if (currentSel != null) {
			currentSel.updateSelection();
		}
		CommandHistory history = workspace.getCommandHistory();
		List<Command> tmp = gatherAllOperateSelectionCommands(history.getCommandsFromWorksheetId(worksheetId), workspace);
		if (!tmp.isEmpty()) {
			JSONArray inputJSON = new JSONArray();
			inputJSON.put(CommandInputJSONUtil.createJsonObject("worksheetId", worksheetId, ParameterType.worksheetId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("hNodeId", hNodeId, ParameterType.hNodeId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("operation", Operation.Intersect.name(), ParameterType.other));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("pythonCode", SelectionManager.defaultCode, ParameterType.other));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("onError", "false", ParameterType.other));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("selectionName", superSel.getName(), ParameterType.other));
			Command t = null;
			try {
				t = new OperateSelectionCommandFactory().createCommand(inputJSON, model, workspace);
			}catch(Exception e) {
				
			}
			if (t != null)
				history._getHistory().add(t);
			history._getHistory().addAll(tmp);
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel, workspace.getContextId());
		uc.add(new HistoryUpdate(history));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	public UpdateContainer getErrorUpdate(String msg) {
		return new UpdateContainer(new ErrorUpdate(msg));
	}

	private List<Command> gatherAllOperateSelectionCommands(List<Command> commands, Workspace workspace) {
		List<Command> operationCommands = new ArrayList<>();
		for (Command c : commands) {
			if (c instanceof OperateSelectionCommand) {
				OperateSelectionCommand t = (OperateSelectionCommand)c;
				if (isSamehTableId(t.getHNodeId(), hNodeId, workspace)) {
					JSONObject obj = workspace.getCommandHistory().getCommandJSON(workspace, t);
					Command tmp = generateCommandFromJSON(workspace, obj);
					if (tmp != null)
						operationCommands.add(tmp);
				}
			}
		}
		return operationCommands;	
	}

	private boolean isSamehTableId(String hNodeId1, String hNodeId2, Workspace workspace) {
		HNode hNode1 = workspace.getFactory().getHNode(hNodeId1);
		HNode hNode2 = workspace.getFactory().getHNode(hNodeId2);
		if (hNode1 == null || hNode2 == null)
			return false;
		return hNode1.getHTableId().equals(hNode2.getHTableId());

	}

	private Command generateCommandFromJSON(Workspace workspace, JSONObject obj) {
		JSONArray inputParamArr = (JSONArray) obj.get(HistoryArguments.inputParameters.name());
		String commandName = (String)obj.get(HistoryArguments.commandName.name());
		WorksheetCommandHistoryExecutor ex = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
		ex.normalizeCommandHistoryJsonInput(workspace, worksheetId, inputParamArr, commandName, true);
		try {
			String model = Command.NEW_MODEL;
			if(obj.has(HistoryArguments.model.name()))
				model = obj.getString(HistoryArguments.model.name());
			Command c = new OperateSelectionCommandFactory().createCommand(inputParamArr, model, workspace);
			return c;
		} catch (Exception e) {
			return null;
		} 
	}

}
