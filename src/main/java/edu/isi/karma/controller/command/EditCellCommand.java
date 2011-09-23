/**
 * 
 */
package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.NodeChangedUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.StringCellValue;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class EditCellCommand extends WorksheetCommand {

	private final String nodeIdArg;

	private CellValue previousValue = null;
	
	private Node.NodeStatus previousStatus;

	private final CellValue newValueArg;

	EditCellCommand(String id, String worksheetId, String nodeIdArg,
			String newValueArg) {
		super(id, worksheetId);
		this.nodeIdArg = nodeIdArg;
		this.newValueArg = new StringCellValue(newValueArg);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	public CellValue getNewValueArg() {
		return newValueArg;
	}

	public CellValue getPreviousValue() {
		return previousValue;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Node node = vWorkspace.getWorkspace().getFactory().getNode(nodeIdArg);
		previousValue = node.getValue();
		previousStatus = node.getStatus();
		if (node.hasNestedTable()) {
			throw new CommandException(this, "Cell " + nodeIdArg
					+ " has a nested table. It cannot be edited.");
		}
		node.setValue(newValueArg, Node.NodeStatus.edited);
		return new UpdateContainer(new NodeChangedUpdate(worksheetId,
				nodeIdArg, newValueArg, Node.NodeStatus.edited));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Node node = vWorkspace.getWorkspace().getFactory().getNode(nodeIdArg);
		node.setValue(previousValue, previousStatus);
		return new UpdateContainer(new NodeChangedUpdate(worksheetId,
				nodeIdArg, previousValue, previousStatus));
	}

	@Override
	public String getTitle() {
		return "Edit Cell";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			if(newValueArg.asString().length() > 20)
				return "Set value to " + newValueArg.asString().substring(0, 19) + "...";
			else
				return "Set value to " + newValueArg.asString();
		} else {
			return "";
		}
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

}
