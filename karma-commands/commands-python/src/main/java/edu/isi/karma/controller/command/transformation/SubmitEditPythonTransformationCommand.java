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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class SubmitEditPythonTransformationCommand extends SubmitPythonTransformationCommand {

	private final String targetHNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(SubmitEditPythonTransformationCommand.class);

	public SubmitEditPythonTransformationCommand(String id, String model, String newColumnName, String transformationCode, 
			String worksheetId, String hNodeId, 
			String errorDefaultValue, String targetHNodeId, String selectionId, boolean isJSONOutput) {
		super(id, model, newColumnName, transformationCode, worksheetId, hNodeId, errorDefaultValue, selectionId, isJSONOutput);
		this.targetHNodeId = targetHNodeId;
		this.pythonNodeId = targetHNodeId;
		logger.debug("SubmitEditPythonTransformationCommand:" + id + " newColumnName:" + newColumnName + ", code=" + transformationCode);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Edit Python Transformation";
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
		inputColumns.clear();
		outputColumns.clear();
		outputColumns.add(targetHNodeId);
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(
				workspace.getId());

		//this.previousPythonTransformationCommand = ctrl.getWorkspace().getCommandHistory().getCommand(previousCommandId);
		this.saveOrResetColumnValues(workspace, ctrl);

		try
		{
			UpdateContainer c = applyPythonTransformation(workspace, worksheet, f,
					hNode, ctrl, targetHNodeId);
			if (isJSONOutput) {
				HNode targetHNode = workspace.getFactory().getHNode(targetHNodeId);
				Map<String, String> mapping = gatherTransformedResults(workspace, targetHNodeId);
				handleJSONOutput(workspace, mapping, targetHNode);
			}
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
			return c;
		}
		catch (Exception e )
		{
			logger.error("Error occured during python transformation.",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while creating applying Python transformation to the column."));
		}
	}


	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		for (Node node : affectedNodes) {
			HNode hNode = workspace.getFactory().getHNode(node.getHNodeId());
			hNode.removeNestedTable();
			node.setNestedTable(null, workspace.getFactory());
			node.clearValue(NodeStatus.original);
		}
		affectedNodes.clear();
		try {
			if(previousPythonTransformationCommand instanceof SubmitPythonTransformationCommand)
			{
				SubmitPythonTransformationCommand prevCommand = (SubmitPythonTransformationCommand)previousPythonTransformationCommand;
				//Previous python command exists, lets reset the values, and then start again
				prevCommand.resetColumnValues(workspace);
			}
			UpdateContainer uc = previousPythonTransformationCommand.doIt(workspace);
			return uc;
		} catch (CommandException e) {
			return new UpdateContainer(new ErrorUpdate("Error occured while  applying previous Python transformation to the column."));

		}
	}

}
