package edu.isi.karma.controller.command;

/**
 * Commands that operate on a worksheet.
 * 
 * @author szekely
 * 
 */
public abstract class WorksheetCommand extends Command {

	protected final String worksheetId;

	protected WorksheetCommand(String id, String worksheetId) {
		super(id);
		this.worksheetId = worksheetId;
	}

	public String getWorksheetId() {
		return worksheetId;
	}

}
