package edu.isi.karma.controller.command;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;



public abstract class WorksheetSelectionCommand extends WorksheetCommand {
	protected String selectionId;
	public WorksheetSelectionCommand(String id, String model, String worksheetId, String selectionId) {
		super(id, model, worksheetId);
		this.selectionId = selectionId;
	}
	
	public SuperSelection getSuperSelection(Workspace workspace) {
		if (selectionId == null || selectionId.trim().isEmpty())
			return SuperSelectionManager.DEFAULT_SELECTION;
		return workspace.getWorksheet(worksheetId).getSuperSelectionManager().getSuperSelection(selectionId);
	}
	
	public SuperSelection getSuperSelection(Worksheet worksheet) {
		if (selectionId == null || selectionId.trim().isEmpty())
			return SuperSelectionManager.DEFAULT_SELECTION;
		return worksheet.getSuperSelectionManager().getSuperSelection(selectionId);
	}

}
