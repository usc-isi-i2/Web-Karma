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
import edu.isi.karma.controller.update.InfoUpdate;
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
 * Adds extract entities commands to the column menu.
 */

public class ExtractEntitiesCommand extends WorksheetCommand {
		
	private static Logger logger = LoggerFactory
	.getLogger(ExtractEntitiesCommand.class);
	
	
	
	protected ExtractEntitiesCommand(String id,String worksheetId) {
		super(id, worksheetId);
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return ExtractEntitiesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Extract Entities";
	}

	@Override
	public String getDescription() {
			return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		//Worksheet worksheet = workspace.getWorksheet(worksheetId);
		System.out.println("in do it");
		return new UpdateContainer(new InfoUpdate("Extracted Entities"));
		
			}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		
		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}

	

}
