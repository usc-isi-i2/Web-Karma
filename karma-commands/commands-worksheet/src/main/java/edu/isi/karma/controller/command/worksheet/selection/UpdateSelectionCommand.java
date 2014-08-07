package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class UpdateSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String selectionName;
	private String anotherSelectionName;
	private String operation;
	
	private enum DefinedOpreations {
		invert, union, intersect, subtract
	}
	public UpdateSelectionCommand(String id, String worksheetId, 
			String hNodeId, String operation, 
			String selectionName, String anotherSelectionName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.selectionName = selectionName;
		this.anotherSelectionName = anotherSelectionName;
		this.operation = operation;
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
		if (anotherSelectionName != null)
			return operation + " with " + anotherSelectionName;
		else
			return operation;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		String hTableId = workspace.getFactory().getHNode(hNodeId).getHTableId();
		Selection currentSel = worksheet.getSelectionManager().getSelection(hTableId, selectionName);
		Selection anotherSel = worksheet.getSelectionManager().getSelection(hTableId, anotherSelectionName);
		if (anotherSel == null && !operation.equalsIgnoreCase(DefinedOpreations.invert.name())) {
			throw new CommandException(this, "The other selection is undefined");
		}
		try {
			DefinedOpreations operation = DefinedOpreations.valueOf(DefinedOpreations.class, this.operation);
			switch(operation) {
			case intersect:
				Selection newSel = worksheet.getSelectionManager().createSelection(currentSel);
				newSel.Intersect(anotherSel);
				break;
			case invert:
				break;
			case subtract:
				break;
			case union:
				break;
			default:
				break;
			
			}
		}catch (Exception e) {
			throw new CommandException(this, "The operation is undefined");
		}
		return null;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
