package edu.isi.karma.controller.command;



public abstract class WorksheetSelectionCommand extends WorksheetCommand {
	protected String selectionId;
	public WorksheetSelectionCommand(String id, String worksheetId, String selectionId) {
		super(id, worksheetId);
		this.selectionId = selectionId;
	}

}
