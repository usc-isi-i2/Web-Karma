package edu.isi.karma.controller.command.worksheet.selection;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.LargeSelection;
import edu.isi.karma.controller.command.selection.LargeSelection.Operation;
import edu.isi.karma.controller.command.selection.MiniSelection;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SelectionManager;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetSuperSelectionListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class OperateSelectionCommand extends WorksheetSelectionCommand {

	private String hNodeId;
	private String pythonCode;
	private String operation;
	private Selection previousSelection;
	private boolean onError;
	public OperateSelectionCommand(String id, String worksheetId, String selectionId, 
			String hNodeId, String operation, 
			String pythonCode, boolean onError) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.pythonCode = pythonCode;
		this.operation = operation;
		this.onError = onError;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Operate Selection";
	}

	@Override
	public String getDescription() {
		return operation;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory factory = workspace.getFactory();
		SuperSelection superSel = this.getSuperSelection(worksheet);
		HTable hTable = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		Selection currentSel = superSel.getSelection(hTable.getId());
		Selection anotherSel = null;
		if (!operation.equalsIgnoreCase(Operation.Invert.name())) {
			anotherSel = new MiniSelection(workspace, worksheetId, hTable.getId(), factory.getNewId("SEL"), superSel.getName(), pythonCode, onError);
			worksheet.getSelectionManager().addSelection(anotherSel);
		}
		if (currentSel == null && operation.equalsIgnoreCase(Operation.Invert.name()) ) {
			return getErrorUpdate("No defined Selection");
		}
		if (currentSel == null) {
			currentSel = new MiniSelection(workspace, worksheetId, hTable.getId(), factory.getNewId("SEL"), superSel.getName(), SelectionManager.defaultCode, onError);
			worksheet.getSelectionManager().addSelection(currentSel);
		}
		try {
			Operation operation = Operation.valueOf(Operation.class, this.operation);
			Selection t = new LargeSelection(workspace, worksheetId, hTable.getId(), factory.getNewId("SEL"), superSel.getName(), currentSel, anotherSel, operation);
			worksheet.getSelectionManager().addSelection(t);
			outputColumns.addAll(t.getInputColumns());
			previousSelection = superSel.getSelection(t.getHTableId());
			if (previousSelection != null)
				superSel.removeSelection(previousSelection);
			superSel.addSelection(t);
			
		}catch (Exception e) {
			return getErrorUpdate("The operation is undefined");
		}
		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel);
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection superSel = getSuperSelection(worksheet);
		HNode hNode = workspace.getFactory().getHNode(hNodeId);
		Selection currentSel = superSel.getSelection(hNode.getHTableId());
		if (previousSelection != null) {
			superSel.addSelection(previousSelection);
			outputColumns.addAll(previousSelection.getInputColumns());
		}
		if (currentSel != null) {
			worksheet.getSelectionManager().removeSelection(currentSel);
			superSel.removeSelection(currentSel);
		}
		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, superSel);	
		uc.add(new WorksheetSuperSelectionListUpdate(worksheetId));
		return uc;
	}
	
	public UpdateContainer getErrorUpdate(String msg) {
		return new UpdateContainer(new ErrorUpdate(msg));
	}
	
	public String getHNodeId() {
		return hNodeId;
	}

}
