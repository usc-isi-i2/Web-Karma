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

import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Workspace;

public class RenameColumnCommand extends WorksheetCommand {
	final private String newColumnName;
	private String newColumnAbsoluteName;
	final private String hNodeId;
	private String oldColumnName;

	private static Logger logger = LoggerFactory
			.getLogger(RenameColumnCommand.class);
	
	public RenameColumnCommand(String id, String model, String newColumnName, String hNodeId, String worksheetId) {
		super(id, model, worksheetId);
		this.newColumnName = newColumnName;
		this.newColumnAbsoluteName = newColumnName;
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
		return newColumnAbsoluteName;
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
		newColumnAbsoluteName = columnNode.getAbsoluteColumnName(workspace.getFactory());
		
		CommandHistory history = workspace.getCommandHistory();
		List<ICommand>  allCommands = history._getHistory();
		for(ICommand command : allCommands) {
			String json = command.getInputParameterJson();
			if(json != null && json.length() > 0) {
				JSONArray inputs = new JSONArray(json);
				for(int i=0; i<inputs.length(); i++) {
					JSONObject input = inputs.getJSONObject(i);
					replaceColumnName(input);
				}
				command.setInputParameterJson(inputs.toString());
			}
		}
		
		
		// Prepare the output to be sent
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId());
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	private void replaceColumnName(JSONObject object) {
		
		for(Object key : IteratorUtils.toList(object.keys())) {
			String strKey = (String)key;
			
			Object value = object.get(strKey);
			if(value instanceof String) {
				String strValue = (String)value;
				if(strValue.equals(oldColumnName)) {
					object.put(strKey, newColumnName);
					logger.info("Change column name in " + strKey + " of " + object.toString());
				}
			} else if(value instanceof JSONObject) {
				replaceColumnName((JSONObject) value);
			} else if(value instanceof JSONArray) {
				JSONArray valueArr = (JSONArray) value;
				for(int i=0; i<valueArr.length(); i++) {
					JSONObject input = valueArr.getJSONObject(i);
					replaceColumnName(input);
				}
			}
		}
		
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		HNode columnNode = workspace.getFactory().getHNode(hNodeId);
		// Change the column name
		columnNode.setColumnName(oldColumnName);
		
		// Prepare the output to be sent
	
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId());
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}
	
}
