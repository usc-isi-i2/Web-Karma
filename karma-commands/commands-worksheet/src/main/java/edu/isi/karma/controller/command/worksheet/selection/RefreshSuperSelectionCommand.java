package edu.isi.karma.controller.command.worksheet.selection;

import java.util.Iterator;

import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;

public class RefreshSuperSelectionCommand extends WorksheetSelectionCommand {

	public RefreshSuperSelectionCommand(String id, String worksheetId, 
			String selectionId) {
		super(id, worksheetId, selectionId);
		addTag(CommandTag.Transformation);
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
		for (Selection sel : currentSel.getAllSelection()) {
			JSONArray inputJSON = new JSONArray();
			HTable ht = workspace.getFactory().getHTable(sel.getHTableId());
			Iterator<String> itr = ht.getHNodeIds().iterator();
			inputJSON.put(CommandInputJSONUtil.createJsonObject("worksheetId", worksheetId, ParameterType.worksheetId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("hNodeId", itr.next(), ParameterType.hNodeId));
			inputJSON.put(CommandInputJSONUtil.createJsonObject("selectionName", currentSel.getName(), ParameterType.other));
			try {
				Command c = new RefreshSelectionCommandFactory().createCommand(inputJSON, workspace);
				c.doIt(workspace);
			} catch (Exception e) {
				
			} 
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, currentSel);
		uc.add(new HistoryUpdate(workspace.getCommandHistory()));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {	
		return null;
	}

}
