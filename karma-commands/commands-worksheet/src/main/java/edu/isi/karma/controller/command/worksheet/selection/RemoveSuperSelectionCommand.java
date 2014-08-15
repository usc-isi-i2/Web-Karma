package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class RemoveSuperSelectionCommand extends WorksheetCommand {

	private String currentSelectionName;
	private SuperSelection sel;
	public RemoveSuperSelectionCommand(String id, String worksheetId, 
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
		return "Remove Selection";
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
		sel = worksheet.getSuperSelectionManager().getSuperSelection(currentSelectionName);
		if (sel == null)
			throw new CommandException(this, "Cannot find " + currentSelectionName);
		worksheet.getSuperSelectionManager().removeSelection(currentSelectionName);	
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (sel != null)
			worksheet.getSuperSelectionManager().defineSelection(currentSelectionName, sel);
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

}
