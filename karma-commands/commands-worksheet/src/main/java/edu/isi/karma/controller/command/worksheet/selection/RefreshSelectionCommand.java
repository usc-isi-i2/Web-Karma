package edu.isi.karma.controller.command.worksheet.selection;

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

public class RefreshSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	public RefreshSelectionCommand(String id, String worksheetId, 
			String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		addTag(CommandTag.Transformation);
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
		SuperSelection superSel = worksheet.getSuperSelectionManager().getSuperSelection("DEFAULT_TEST");
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = worksheet.getSelectionManager().getSelection(hTable.getId());
		if (currentSel != null) {
			currentSel.updateSelection();
		}
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel);
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
	
	public UpdateContainer getErrorUpdate(String msg) {
		return new UpdateContainer(new ErrorUpdate(msg));
	}

}
