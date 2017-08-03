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
package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AddColumnUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.Util;
import edu.isi.karma.webserver.KarmaException;

/**
 * Adds a new column to the table with hTableId.
 * hTableId may be empty if a hNodeId is provided.
 * If hNodeId is provided, adds the new column after hNodeId.
 * If no hNodeId is provided adds the new column as the first column in the table hTableId.
 * Returns the hNodeId of the newly created column.
 */
public class AddColumnCommand extends WorksheetSelectionCommand {
	//if null add column at beginning of table
	private final String hNodeId;
	//add column to this table
	private String hTableId;
	private final String newColumnName;
	private final String defaultValue;
	private String newColumnAbsoluteName;
	
	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(AddColumnCommand.class);

	protected AddColumnCommand(String id, String model, String worksheetId,
			String hTableId, String hNodeId, String newColumnName, 
			String defaultValue, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		this.newColumnName=newColumnName;
		this.defaultValue = defaultValue;
		this.newColumnAbsoluteName = newColumnName;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return AddColumnCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add New Column";
	}

	@Override
	public String getDescription() {
		if (newColumnAbsoluteName != null)
			return newColumnAbsoluteName;
		else
			return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		
		try{
			if(hTableId==null || hTableId.isEmpty()){
				//get table id based on the hNodeId
				if(hNodeId==null)
					throw new KarmaException("TableId and NodeId are empty. Can't add column.");
				hTableId = workspace.getFactory().getHNode(hNodeId).getHTableId();
			}
			HTable hTable = workspace.getFactory().getHTable(hTableId);
			if(hTable == null)
			{
				logger.error("No HTable for id "+ hTableId);
				throw new KarmaException("No HTable for id "+ hTableId );
			}
			if (null != hTable.getHNodeFromColumnName(newColumnName)) {
				logger.error("Add column failed to create " + newColumnName
						+ " because it already exists!");
				return new UpdateContainer(new ErrorUpdate(
						"Add column failed to create " + newColumnName
								+ " because it already exists!"));
			}   
			//add new column to this table
			//add column after the column with hNodeId
			HNode ndid = hTable.addNewHNodeAfter(hNodeId, HNodeType.Transformation, workspace.getFactory(), newColumnName, worksheet,true);
			if(ndid == null)
			{
				logger.error("Unable to add new HNode!");
				throw new KarmaException("Unable to add new HNode!");
			}
			//add as first column in the table if hNodeId is null
			//HNode ndid = currentTable.addNewHNodeAfter(null, vWorkspace.getRepFactory(), newColumnName, worksheet,true);

			//save the new hNodeId for undo
			newHNodeId = ndid.getId();
			
			// Populate the column with default value if default value is present
			if (this.defaultValue != null && !this.defaultValue.equals("")) {
				populateRowsWithDefaultValues(worksheet, workspace.getFactory());
			}
			
			//create container and return hNodeId of newly created column
			UpdateContainer c =  new UpdateContainer(new AddColumnUpdate(newHNodeId, worksheetId));
			
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			outputColumns.add(newHNodeId);
			newColumnAbsoluteName = ndid.getAbsoluteColumnName(workspace.getFactory());
			return c;
		} catch (Exception e) {
			logger.error("Error in AddColumnCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	private void populateRowsWithDefaultValues(Worksheet worksheet, RepFactory factory) {
		SuperSelection selection = getSuperSelection(worksheet);
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(newHNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<>(Math.max(1000, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(selectedPath, nodes, selection);	
		for (Node node : nodes) {
			node.setValue(this.defaultValue, NodeStatus.original, factory);
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HTable currentTable = workspace.getFactory().getHTable(hTableId);
		//remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);

		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId());
	}

	public String getNewHNodeId() {
		return newHNodeId;
	}
}
