package edu.isi.karma.controller.command.worksheet.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
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

public class ClearSelectionCommand extends WorksheetSelectionCommand {

	private String hNodeId;
	private String type;
	private Map<String, Selection> oldSelections;
	public ClearSelectionCommand(String id, String model, String worksheetId, String selectionId, 
			String hNodeId, String type) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.type = type;
		oldSelections = new HashMap<>();
		addTag(CommandTag.Selection);
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
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		SuperSelection superSel = this.getSuperSelection(worksheet);
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		if (type.equals("Column")) {
			Selection currentSel = superSel.getSelection(hTable.getId());
			if (currentSel != null) {
				outputColumns.addAll(currentSel.getInputColumns());
				oldSelections.put(currentSel.getHTableId(), currentSel);
				worksheet.getSelectionManager().removeSelection(currentSel);
				superSel.removeSelection(currentSel);
			}
		}
		if (type.equals("All")) {
			for (Selection sel : superSel.getAllSelection()) {
				if (sel != null) {
					oldSelections.put(sel.getHTableId(), sel);
					worksheet.getSelectionManager().removeSelection(sel);
					superSel.removeSelection(sel);
				}
			}
		}
		
		worksheet.getMetadataContainer().getColumnMetadata().removeSelectionPythonCode(hTable.getId());
		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		if(!this.isExecutedInBatch()) {
			UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel, workspace.getContextId());
			return uc;
		} else {
			return new UpdateContainer();
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection superSel = getSuperSelection(worksheet);
		for (Entry<String, Selection> entry : oldSelections.entrySet()) {
			outputColumns.addAll(entry.getValue().getInputColumns());
			superSel.addSelection(entry.getValue());
			worksheet.getSelectionManager().addSelection(entry.getValue());
		}
		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel, workspace.getContextId());
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

	public UpdateContainer getErrorUpdate(String msg) {
		return new UpdateContainer(new ErrorUpdate(msg));
	}

}
