package edu.isi.karma.controller.command.worksheet.selection;

import java.io.IOException;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class RefreshSuperSelectionCommand extends WorksheetCommand {

	private String currentSelectionName;
	public RefreshSuperSelectionCommand(String id, String worksheetId, 
			String currentSelectionName) {
		super(id, worksheetId);
		this.currentSelectionName = currentSelectionName;
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
		return currentSelectionName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection currentSel = worksheet.getSuperSelectionManager().getSuperSelection(currentSelectionName);
		try {
			currentSel.updateSelection();
		} catch (IOException e) {
			throw new CommandException(this, "update selection failure");
		}
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {	
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

}
