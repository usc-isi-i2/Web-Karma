package edu.isi.karma.controller.command;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;



public abstract class WorksheetSelectionCommand extends WorksheetCommand {
	protected String selectionId;
	public WorksheetSelectionCommand(String id, String worksheetId, String selectionId) {
		super(id, worksheetId);
		this.selectionId = selectionId;
	}
	
	public SuperSelection getSuperSelection(Workspace workspace) {
		return workspace.getWorksheet(worksheetId).getSuperSelectionManager().getSuperSelection(selectionId);
	}
	
	public SuperSelection getSuperSelection(Worksheet worksheet) {
		return worksheet.getSuperSelectionManager().getSuperSelection(selectionId);
	}

}
