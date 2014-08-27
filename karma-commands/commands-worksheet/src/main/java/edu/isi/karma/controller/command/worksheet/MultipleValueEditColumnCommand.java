package edu.isi.karma.controller.command.worksheet;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Workspace;

public class MultipleValueEditColumnCommand extends WorksheetCommand {
	private String hNodeID;
	private Map<String, String> newRowValueMap;
	private Map<String, String> oldRowValueMap = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(MultipleValueEditColumnCommand.class);
	
	protected MultipleValueEditColumnCommand(String id, String worksheetId, String hNodeID, Map<String, String> rowValueMap) {
		super(id, worksheetId);
		this.hNodeID = hNodeID;
		this.newRowValueMap = rowValueMap;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Multiple Cell Value Edit Command";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory factory = workspace.getFactory();
		for (String rowID: newRowValueMap.keySet()) {
			Row row = factory.getRow(rowID);
			Node existingNode = row.getNode(hNodeID);
			if (existingNode.hasNestedTable()) {
				logger.error("Existing node has a nested table. Cannot overwrite such node with new value. NodeID: " + existingNode.getId());
				continue;
			}
			String existingCellValue = existingNode.getValue().asString();
			oldRowValueMap.put(rowID, existingCellValue);
			String newCellValue = newRowValueMap.get(rowID);
			row.setValue(hNodeID, newCellValue, factory);
		}
		return WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(this.worksheetId, SuperSelectionManager.DEFAULT_SELECTION);
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		RepFactory factory = workspace.getFactory();
		for (String rowID: oldRowValueMap.keySet()) {
			Row row = factory.getRow(rowID);
			
			Node existingNode = row.getNode(hNodeID);
			if (existingNode.hasNestedTable()) {
				logger.error("Existing node has a nested table. Cannot overwrite such node with new value. NodeID: " + existingNode.getId());
				continue;
			}
			String oldCellValue = oldRowValueMap.get(rowID);
			row.setValue(hNodeID, oldCellValue, factory);
		}
		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION);
	}

}
