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

package edu.isi.karma.controller.command.transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class SubmitPythonTransformationCommand extends MutatingPythonTransformationCommand {
	
	protected AddColumnCommand addColCmd;
	
	private static Logger logger = LoggerFactory
			.getLogger(SubmitPythonTransformationCommand.class);

	public SubmitPythonTransformationCommand(String id, String newColumnName, String transformationCode, 
			String worksheetId, String hNodeId, String errorDefaultValue) {
		super(id, newColumnName, transformationCode, worksheetId, hNodeId, errorDefaultValue);
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Python Transformation";
	}

	@Override
	public String getDescription() {
		return newColumnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		HTable hTable = worksheet.getHeaders();
		String hTableId = hTable.getId();
		RepFactory f = workspace.getFactory();
		HNode hNode = f.getHNode(hNodeId);
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(
				workspace.getId());
		// Invoke the add column command
		logger.info(hNodeId);
		try
		{
			if(null == addColCmd )
			{
			JSONArray addColumnInput = getAddColumnCommandInputJSON(hTableId);
			AddColumnCommandFactory addColumnFac = (AddColumnCommandFactory)ctrl.
					getCommandFactoryMap().get(AddColumnCommand.class.getSimpleName());
			addColCmd = (AddColumnCommand) addColumnFac.createCommand(addColumnInput, workspace);
			addColCmd.saveInHistory(false);
			addColCmd.doIt(workspace);
			}
			else if(null == hTable.getHNode(addColCmd.getNewHNodeId()))
			{
				addColCmd.doIt(workspace);
			}
			
		}
		catch (Exception e)
		{
			logger.error("Error occured during python transformation.",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while creating new column for applying Python transformation to the column."));
		}
		try
		{
			UpdateContainer c = applyPythonTransformation(workspace, worksheet, f,
				hNode, ctrl, addColCmd.getNewHNodeId());
			return c;
		}
		catch (Exception e )
		{
			addColCmd.undoIt(workspace);
			logger.error("Error occured during python transformation.",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while creating applying Python transformation to the column."));
		}
	}

	private JSONArray getAddColumnCommandInputJSON(String hTableId) throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.newColumnName.name(), newColumnName, ParameterType.other));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.hTableId.name(), hTableId, ParameterType.other));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.worksheetId.name(), worksheetId, ParameterType.worksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.hNodeId.name(), hNodeId, ParameterType.worksheetId));
		return arr;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		addColCmd.undoIt(workspace);
		UpdateContainer c = (WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

}
