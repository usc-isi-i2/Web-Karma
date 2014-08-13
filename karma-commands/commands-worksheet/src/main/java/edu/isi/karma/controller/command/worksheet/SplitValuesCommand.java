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

import java.io.IOException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class SplitValuesCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String delimiter;
	private String newhNodeId;
	private String columnName;
	private String newColName;
	private Command splitCommaCommand;
	//	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected SplitValuesCommand(String id, String worksheetId,
			String hNodeId, String delimiter, String newColName) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.delimiter = delimiter;
		this.newColName = newColName;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Split By Comma";
	}

	@Override
	public String getDescription() {
		return columnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet wk = workspace.getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();

		// Get the HNode
		HNode hNode = workspace.getFactory().getHNode(hNodeId);
		columnName = hNode.getColumnName();
		// The column should not have a nested table but check to make sure!
		if (hNode.hasNestedTable()) {
			c.add(new ErrorUpdate("Cannot split column with nested table!"));
			return c;
		}
		
		if (columnName.equals(newColName)) {
			splitCommaCommand = new SplitByCommaCommand(workspace.getFactory().getNewId("C"), worksheetId, hNodeId, delimiter);
			return splitCommaCommand.doIt(workspace);
		}

		HNode newhNode;
		HTable hTable = workspace.getFactory().getHTable(hNode.getHTableId());
		newhNode = hTable.addHNode(newColName, HNodeType.Transformation, wk, workspace.getFactory());
		HTable newTable = newhNode.addNestedTable("Comma Split Values", wk, workspace.getFactory());
		newTable.addHNode("Values", HNodeType.Transformation, wk, workspace.getFactory());
		newhNodeId = newhNode.getId();
		SplitColumnByDelimiter split = new SplitColumnByDelimiter(hNodeId, newhNode.getId(), wk, delimiter, workspace);
		try {
			split.split();
		} catch (IOException e) {
			c.add(new ErrorUpdate("Cannot split column! csv reader error"));
			return c;
		}
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));

		/** Add the alignment update **/
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));

		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		if (splitCommaCommand != null)
			return splitCommaCommand.undoIt(workspace);
		RepFactory factory = workspace.getFactory();
		HNode hNode = factory.getHNode(newhNodeId);
		HTable hTable = factory.getHTable(hNode.getHTableId());
		hTable.removeHNode(newhNodeId, factory.getWorksheet(worksheetId));
		hNode.removeNestedTable();
		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}
}
