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
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AddColumnUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
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
public class AddRowCommand extends WorksheetCommand {
	//if null add column at beginning of table
	private final String hNodeId;
	//add column to this table
	private String hTableId;


	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(AddRowCommand.class);
	
	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}
	
	protected AddRowCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return AddRowCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Row";
	}

	@Override
	public String getDescription() {
			return "New Row";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		
		try{
			worksheet.addRow(workspace.getFactory());
			UpdateContainer c =  new UpdateContainer();		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in AddColumnCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HTable currentTable = workspace.getFactory().getHTable(hTableId);
		//remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);

		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}
	

}
