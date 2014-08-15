package edu.isi.karma.controller.command.worksheet.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class ClearSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String type;
	private Map<String, Selection> oldSelections;
	public ClearSelectionCommand(String id, String worksheetId, 
			String hNodeId, String type) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.type = type;
		oldSelections = new HashMap<String, Selection>();
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Clear Selection";
	}

	@Override
	public String getDescription() {
		return type;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		SuperSelection superSel = worksheet.getSuperSelectionManager().getSuperSelection("DEFAULT_TEST");
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		if (type.equals("Column")) {
			Selection currentSel = worksheet.getSelectionManager().getSelection(hTable.getId());
			if (currentSel != null) {
				oldSelections.put(currentSel.getHTableId(), currentSel);
				worksheet.getSelectionManager().updateCurrentSelection(currentSel.getHTableId(), null);
			}
		}
		if (type.equals("All")) {
			for (String s : superSel.getAllSelection()) {
				Selection currentSel = worksheet.getSelectionManager().getSelection(s);
				if (currentSel != null) {
					oldSelections.put(s, currentSel);
					worksheet.getSelectionManager().updateCurrentSelection(s, null);
				}
			}
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel);
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection superSel = worksheet.getSuperSelectionManager().getSuperSelection("DEFAULT_TEST");
		for (Entry<String, Selection> entry : oldSelections.entrySet()) {
			worksheet.getSelectionManager().updateCurrentSelection(entry.getKey(), entry.getValue());
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel);
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

	public UpdateContainer getErrorUpdate(String msg) {
		return new UpdateContainer(new ErrorUpdate(msg));
	}

}
