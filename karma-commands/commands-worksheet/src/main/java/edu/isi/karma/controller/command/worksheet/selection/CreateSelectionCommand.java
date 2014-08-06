package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSelectionListUpdate;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class CreateSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String PythonCode;
	private String selectionName;
	public CreateSelectionCommand(String id, String worksheetId, 
			String hNodeId, String PythonCode, String selectionName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.PythonCode = PythonCode;
		this.selectionName = selectionName;
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
		HNode hNode = workspace.getFactory().getHNode(hNodeId);
		if (hNode == null) {
			return new UpdateContainer(new ErrorUpdate("Cannot find HNode" + hNodeId));
		}
		String hTableId = hNode.getHTableId();
		if (!worksheet.getSelectionManager().createSelection(workspace, worksheetId, hTableId, selectionName)) {
			return new UpdateContainer(new TrivialErrorUpdate(selectionName + " already exists!"));
		}
		Selection sel = worksheet.getSelectionManager().getSelection(hTableId, selectionName);
		try {
			sel.addSelections(PythonCode);
		} catch (Exception e) {
			return new UpdateContainer(new TrivialErrorUpdate("Cannot Create Selection for " + selectionName));
		}
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		HNode hNode = workspace.getFactory().getHNode(hNodeId);
		if (hNode == null) {
			return new UpdateContainer(new ErrorUpdate("Cannot find HNode" + hNodeId));
		}
		String hTableId = hNode.getHTableId();
		worksheet.getSelectionManager().removeSelection(hTableId, selectionName);
		return new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId));
	}

}
