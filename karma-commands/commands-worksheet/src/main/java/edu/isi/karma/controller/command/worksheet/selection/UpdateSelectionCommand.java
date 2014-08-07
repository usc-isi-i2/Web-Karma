package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.LargeSelection.Operation;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSelectionListUpdate;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class UpdateSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String currentSelectionName;
	private String anotherSelectionName;
	private String operation;
	private String newSelectionName;
	public UpdateSelectionCommand(String id, String worksheetId, 
			String hNodeId, String operation, 
			String selectionName, String anotherSelectionName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.currentSelectionName = selectionName;
		this.anotherSelectionName = anotherSelectionName;
		this.operation = operation;
		addTag(CommandTag.Transformation);
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
		RepFactory factory = workspace.getFactory();
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = worksheet.getSelectionManager().getSelection(hTable.getId(), currentSelectionName);
		Selection anotherSel = worksheet.getSelectionManager().getSelection(hTable.getId(), anotherSelectionName);
		newSelectionName = hTable.getNewColumnName(currentSel.getId());
		if (anotherSel == null && !operation.equalsIgnoreCase(Operation.Invert.name())) {
			throw new CommandException(this, "The other selection is undefined");
		}
		try {
			Operation operation = Operation.valueOf(Operation.class, this.operation);
			boolean t = worksheet.getSelectionManager().createLargeSelection(currentSel, anotherSel, operation, newSelectionName);
			if (!t)
				throw new CommandException(this, "Creation unsuccessful");
		}catch (Exception e) {
			throw new CommandException(this, "The operation is undefined");
		}
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTable.getId()));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		HNode hNode = workspace.getFactory().getHNode(hNodeId);
		if (hNode == null) {
			return new UpdateContainer(new ErrorUpdate("Cannot find HNode" + hNodeId));
		}
		worksheet.getSelectionManager().removeSelection(hNode.getHTableId(), newSelectionName);
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hNode.getHTableId()));
	}

}
