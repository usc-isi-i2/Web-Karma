package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSelectionListUpdate;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class CreateSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String pythonCode;
	private String selectionName;
	public CreateSelectionCommand(String id, String worksheetId, 
			String hNodeId, String pythonCode, String selectionName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.pythonCode = pythonCode;
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
		String hTableId = hNode.getHTableId();
		if (!worksheet.getSelectionManager().createMiniSelection(workspace, worksheetId, hTableId, selectionName, pythonCode)) {
			throw new CommandException(this, selectionName + " already exists!");
		}
		UpdateContainer uc = new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId)); 
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
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
		UpdateContainer uc = new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTableId)); 
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

}
