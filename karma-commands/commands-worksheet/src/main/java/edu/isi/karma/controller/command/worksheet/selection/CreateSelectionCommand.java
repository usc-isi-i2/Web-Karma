package edu.isi.karma.controller.command.worksheet.selection;

import java.io.IOException;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSelectionListUpdate;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class CreateSelectionCommand extends WorksheetCommand {

	private String hTableId;
	private String PythonCode;
	private Selection addedSelection;
	protected CreateSelectionCommand(String id, String worksheetId, 
			String hTableId, String PythonCode) {
		super(id, worksheetId);
		this.hTableId = hTableId;
		this.PythonCode = PythonCode;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Create Selection";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		Selection sel = new Selection(workspace, worksheetId, hTableId);
		try {
			sel.addSelections(PythonCode);
		} catch (IOException e) {
			return new UpdateContainer(new ErrorUpdate("Cannot Create Selection for " + hTableId));
		}
		worksheet.getSelectionManager().addSelection(sel);
		addedSelection = sel;
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (addedSelection != null)
			worksheet.getSelectionManager().removeSelection(addedSelection);
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId));
	}

}
