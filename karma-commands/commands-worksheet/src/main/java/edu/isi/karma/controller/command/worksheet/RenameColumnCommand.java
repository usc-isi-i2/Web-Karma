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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Workspace;

public class RenameColumnCommand extends WorksheetCommand {
	final private String newColumnName;
	final private String hNodeId;
	private String oldColumnName;

	public RenameColumnCommand(String id, String newColumnName, String hNodeId, String worksheetId) {
		super(id, worksheetId);
		this.newColumnName = newColumnName;
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Rename Column";
	}

	
	@Override
	public String getDescription() {
		if (newColumnName.length() > 20)
			return newColumnName.substring(0, 19) + "...";
		return newColumnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		HNode columnNode = workspace.getFactory().getHNode(hNodeId);
		oldColumnName = columnNode.getColumnName();
		
		// Change the column name
		columnNode.setColumnName(newColumnName);
		
		// Prepare the output to be sent
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		HNode columnNode = workspace.getFactory().getHNode(hNodeId);
		// Change the column name
		columnNode.setColumnName(oldColumnName);
		
		// Prepare the output to be sent
	
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}
	
}
