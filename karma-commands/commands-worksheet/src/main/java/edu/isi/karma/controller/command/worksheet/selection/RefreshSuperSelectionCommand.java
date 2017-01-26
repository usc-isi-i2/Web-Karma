package edu.isi.karma.controller.command.worksheet.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshSuperSelectionCommand extends WorksheetSelectionCommand {

	private static Logger logger = LoggerFactory
			.getLogger(WorksheetSelectionCommand.class);
	public RefreshSuperSelectionCommand(String id, String model, String worksheetId,
			String selectionId) {
		super(id, model, worksheetId, selectionId);
		addTag(CommandTag.Selection);
		addTag(CommandTag.IgnoreInBatch);
	}
	
	private class SelectionWithPriority implements Comparable<SelectionWithPriority>{
		private Selection sel;
		private int priority;
		public SelectionWithPriority(Selection sel, int priority) {
			this.sel = sel;
			this.priority = priority;
		}
		@Override
		public int compareTo(SelectionWithPriority o) {
			return o.priority - priority;
		}
		
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Refresh Selection";
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
		SuperSelection currentSel = getSuperSelection(worksheet);
		List<SelectionWithPriority> selections = new ArrayList<>();
		for (Selection sel : currentSel.getAllSelection()) {
			HTable ht = workspace.getFactory().getHTable(sel.getHTableId());
			selections.add(new SelectionWithPriority(sel, hTableDepth(ht, workspace.getFactory())));
		}
		Collections.sort(selections);
		for (SelectionWithPriority s : selections) {
			Selection sel = s.sel;
			JSONArray inputJSON = new JSONArray();
			HTable ht = workspace.getFactory().getHTable(sel.getHTableId());
			Iterator<String> itr = ht.getHNodeIds().iterator();
			inputJSON.put(CommandInputJSONUtil.createJsonObject("worksheetId", worksheetId, ParameterType.worksheetId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("hNodeId", itr.next(), ParameterType.hNodeId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("selectionName", currentSel.getName(), ParameterType.other));
			try {
				Command c = new RefreshSelectionCommandFactory().createCommand(inputJSON, model, workspace);
				c.doIt(workspace);
			} catch (Exception e) {
				logger.error("Exception happened", e);
			} 
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, currentSel, workspace.getContextId());
		uc.add(new HistoryUpdate(workspace.getCommandHistory()));
		return uc;
	}
	
	private int hTableDepth(HTable hTable, RepFactory factory) {
		int depth = 0;
		while (hTable != factory.getWorksheet(worksheetId).getHeaders()) {
			depth++;
			hTable = hTable.getParentHNode().getHTable(factory);
		}
		return depth;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {	
		return null;
	}

}
