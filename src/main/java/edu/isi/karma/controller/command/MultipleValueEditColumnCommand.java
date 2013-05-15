package edu.isi.karma.controller.command;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class MultipleValueEditColumnCommand extends Command {
	private String hNodeID;
	private final String vWorksheetId;
	private Map<String, String> newRowValueMap;
	private Map<String, String> oldRowValueMap = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(MultipleValueEditColumnCommand.class);
	
	protected MultipleValueEditColumnCommand(String id, String vWorksheetID, String hNodeID, Map<String, String> rowValueMap) {
		super(id);
		this.hNodeID = hNodeID;
		this.vWorksheetId = vWorksheetID;
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		RepFactory factory = vWorkspace.getRepFactory();
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
		vw.update(c);
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		UpdateContainer c = new UpdateContainer();
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		RepFactory factory = vWorkspace.getRepFactory();
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
		vw.update(c);
		return c;
	}

}
