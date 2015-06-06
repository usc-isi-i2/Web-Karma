/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.NodeChangedUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.StringCellValue;
import edu.isi.karma.rep.Workspace;

/**
 * @author szekely
 * 
 */
public class EditCellCommand extends WorksheetSelectionCommand {

	private final String nodeIdArg;

	private CellValue previousValue = null;

	private Node.NodeStatus previousStatus;

	private final CellValue newValueArg;
	
	EditCellCommand(String id, String model, String worksheetId, String nodeIdArg,
			String newValueArg, String selectionId) {
		super(id, model, worksheetId, selectionId);
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Node node = workspace.getFactory().getNode(nodeIdArg);
		SuperSelection sel = getSuperSelection(workspace);
		inputColumns.clear();
		outputColumns.clear();
		inputColumns.add(node.getHNodeId());
		outputColumns.add(node.getHNodeId());
		previousValue = node.getValue();
		previousStatus = node.getStatus();
		if (node.hasNestedTable()) {
			throw new CommandException(this, "Cell " + nodeIdArg
					+ " has a nested table. It cannot be edited.");
		}		
		node.setValue(newValueArg, Node.NodeStatus.edited,
				workspace.getFactory());
		WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, sel, workspace.getContextId());
		uc.add(new NodeChangedUpdate(worksheetId,
				nodeIdArg, newValueArg, Node.NodeStatus.edited));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Node node = workspace.getFactory().getNode(nodeIdArg);
		SuperSelection sel = getSuperSelection(workspace);
		node.setValue(previousValue, previousStatus, workspace.getFactory());
		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, sel, workspace.getContextId());
		uc.add(new NodeChangedUpdate(worksheetId,
				nodeIdArg, previousValue, previousStatus));
		return uc;
	}

	@Override
	public String getTitle() {
		return "Edit Cell";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			if (newValueArg.asString().length() > 20)
				return "Set value to "
						+ newValueArg.asString().substring(0, 19) + "...";
			return "Set value to " + newValueArg.asString();
		}
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

}
