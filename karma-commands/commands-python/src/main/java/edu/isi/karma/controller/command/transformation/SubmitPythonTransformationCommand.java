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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommandFactory;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.*;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubmitPythonTransformationCommand extends MutatingPythonTransformationCommand {
	protected ICommand previousPythonTransformationCommand;
	protected AddColumnCommand addColCmd;
	protected ArrayList<String> originalColumnValues;
	protected String pythonNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(SubmitPythonTransformationCommand.class);

	public SubmitPythonTransformationCommand(String id, String newColumnName, String transformationCode, 
			String worksheetId, String hNodeId, String errorDefaultValue) {
		super(id, newColumnName, transformationCode, worksheetId, hNodeId, errorDefaultValue);
		//logger.info("SubmitPythonTranformationCommand:" + id + " newColumnName:" + newColumnName + ", code=" + transformationCode);
		this.pythonNodeId = hNodeId;
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
		RepFactory f = workspace.getFactory();
		HNode hNode = f.getHNode(hNodeId);
		String hTableId = hNode.getHTableId();
		HTable hTable = hNode.getHTable(f);
		String nodeId = hTable.getHNodeIdFromColumnName(newColumnName);
		inputColumns.clear();
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(
				workspace.getId());

		// Invoke the add column command
		logger.debug("SubmitPythonTranformation: " + hNodeId + ":" + nodeId);
		try
		{
			HNode newColumnNameHNode = hTable.getHNodeFromColumnName(newColumnName);
			if(null != newColumnNameHNode ) //Column name already exists
			{
				pythonNodeId = nodeId;

				saveOrResetColumnValues(workspace, ctrl);

				logger.debug("SubmitPythonTranformation: Tranform Existing Column" + hNodeId + ":" + nodeId);
				UpdateContainer c = applyPythonTransformation(workspace, worksheet, f,
						newColumnNameHNode, ctrl, nodeId);
				return c;
			} else {
				saveColumnValues(workspace);
			}

			if (null == addColCmd) {
				JSONArray addColumnInput = getAddColumnCommandInputJSON(hTableId);
				AddColumnCommandFactory addColumnFac = (AddColumnCommandFactory) ctrl
						.getCommandFactoryMap().get(
								AddColumnCommand.class.getSimpleName());
				addColCmd = (AddColumnCommand) addColumnFac.createCommand(
						addColumnInput, workspace);
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

	protected  void saveOrResetColumnValues(Workspace workspace, ExecutionController ctrl) {
		this.previousPythonTransformationCommand = this.extractPreviousCommand(ctrl);
		if(previousPythonTransformationCommand != null) {
			SubmitPythonTransformationCommand prevCommand = (SubmitPythonTransformationCommand)previousPythonTransformationCommand;
			//Previous python command exists, lets reset the values, and then start again
			this.originalColumnValues = prevCommand.getOriginalColumnValues();
			this.resetColumnValues(workspace);
		} else {
			saveColumnValues(workspace);
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
		if(addColCmd != null) {
			addColCmd.undoIt(workspace);
		} else if(previousPythonTransformationCommand != null) {
			try {
				if(previousPythonTransformationCommand instanceof SubmitPythonTransformationCommand)
				{
					SubmitPythonTransformationCommand prevCommand = (SubmitPythonTransformationCommand)previousPythonTransformationCommand;
					//Previous python command exists, lets reset the values, and then start again
					prevCommand.resetColumnValues(workspace);
				}
				return previousPythonTransformationCommand.doIt(workspace);
			} catch (CommandException e) {
				return new UpdateContainer(new ErrorUpdate("Error occured while  applying previous Python transformation to the column."));

			}
		} else if(this.originalColumnValues != null) {
			resetColumnValues(workspace);
		}

		UpdateContainer c = (WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		// TODO is it necessary to compute alignment and semantic types for everything?
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	protected void saveColumnValues(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory f = workspace.getFactory();
		HNode hNode = f.getHNode(pythonNodeId);

		this.originalColumnValues = new ArrayList<String>();
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(hNode.getHNodePath(f), nodes);
		for(Node node : nodes) {
			originalColumnValues.add(node.getValue().asString());
		}
	}

	public void resetColumnValues(Workspace workspace) {
		if(this.originalColumnValues != null) {
			Worksheet worksheet = workspace.getWorksheet(worksheetId);
			RepFactory f = workspace.getFactory();
			HNode hNode = f.getHNode(pythonNodeId);

			worksheet.getDataTable().setCollectedNodeValues(hNode.getHNodePath(f), this.originalColumnValues, f);
		}
	}

	public ArrayList<String> getOriginalColumnValues() {
		return this.originalColumnValues;
	}

	protected ICommand extractPreviousCommand(ExecutionController ctrl) {

		CommandHistory commandHistory = ctrl.getWorkspace().getCommandHistory();
		List<ICommand> commands = commandHistory._getHistory();
		for(int i = commands.size() -1 ; i>=0; i--) {
			ICommand command = commands.get(i);
			if(command instanceof SubmitPythonTransformationCommand) {
				SubmitPythonTransformationCommand pyCommand = (SubmitPythonTransformationCommand)command;
				if(pyCommand.worksheetId.equals(this.worksheetId)) {
					if(pyCommand.pythonNodeId.equals(this.pythonNodeId)) {
						return command;
					}
				}
			}
		}

		return null;
	}

	@Override
	public Set<String> getInputColumns() {
		Set<String> t = new HashSet<String>();
		t.addAll(inputColumns);
		return t;
	}

	@Override
	public Set<String> getOutputColumns() {
		Set<String> t = new HashSet<String>();
		if (addColCmd != null)
			t.add(addColCmd.getNewHNodeId());
		else
			t.add(hNodeId);
		return t;
	}
}
