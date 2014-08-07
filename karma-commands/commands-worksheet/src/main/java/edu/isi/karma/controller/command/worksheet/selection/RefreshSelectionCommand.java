package edu.isi.karma.controller.command.worksheet.selection;

import java.io.IOException;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSelectionListUpdate;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class RefreshSelectionCommand extends WorksheetCommand {

	private String hNodeId;
	private String currentSelectionName;
	public RefreshSelectionCommand(String id, String worksheetId, 
			String hNodeId, String currentSelectionName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
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
		RepFactory factory = workspace.getFactory();
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = worksheet.getSelectionManager().getSelection(hTable.getId(), currentSelectionName);
		try {
			currentSel.updateSelection();
		} catch (IOException e) {
			throw new CommandException(this, "update selection failure");
		}
		UpdateContainer uc = new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTable.getId()));
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = worksheet.getSelectionManager().getSelection(hTable.getId(), currentSelectionName);
		currentSel.invalidateSelection();
		UpdateContainer uc = new UpdateContainer(new WorksheetSelectionListUpdate(worksheetId, hTable.getId()));
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}

}
