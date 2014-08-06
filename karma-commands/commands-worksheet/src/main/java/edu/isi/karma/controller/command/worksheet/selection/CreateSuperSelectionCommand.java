package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class CreateSuperSelectionCommand extends WorksheetCommand {

	private String newSelectionName;
	public CreateSuperSelectionCommand(String id, String worksheetId, 
			String newSelectionName) {
		super(id, worksheetId);
		this.newSelectionName = newSelectionName;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Create Super Selection";
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
		if (!worksheet.getSuperSelectionManager().hasSelection(newSelectionName))
			worksheet.getSuperSelectionManager().defineSelection(newSelectionName);
		else
			return new UpdateContainer(new ErrorUpdate("Already have selection " + newSelectionName));
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		worksheet.getSuperSelectionManager().removeSelection(newSelectionName);
		return new UpdateContainer(new WorksheetSuperSelectionListUpdate(worksheetId));
	}

}
