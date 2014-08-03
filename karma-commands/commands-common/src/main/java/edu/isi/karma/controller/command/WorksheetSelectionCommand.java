package edu.isi.karma.controller.command;

import edu.isi.karma.controller.command.selection.SuperSelection;


public abstract class WorksheetSelectionCommand extends WorksheetCommand {
	protected SuperSelection selection;
	public WorksheetSelectionCommand(String id, String worksheetId, SuperSelection sel) {
		super(id, worksheetId);
		this.selection = sel;
	}

}
