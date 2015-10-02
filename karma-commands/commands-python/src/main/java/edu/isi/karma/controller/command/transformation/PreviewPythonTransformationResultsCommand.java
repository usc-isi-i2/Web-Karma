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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.PythonPreviewResultsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class PreviewPythonTransformationResultsCommand extends PythonTransformationCommand {

	private static Logger logger = LoggerFactory
			.getLogger(PreviewPythonTransformationResultsCommand.class.getSimpleName());

	
	protected PreviewPythonTransformationResultsCommand(String id, String model, String worksheetId, 
			String transformationCode, String errorDefaultValue, 
			String hNodeId, String selectionId) {
		super(id, model, transformationCode, worksheetId, hNodeId,  errorDefaultValue, selectionId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Preview Transformation Results";
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
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		RepFactory f = workspace.getFactory();
		HNode hNode = f.getHNode(hNodeId);
		final JSONArray transformedRows = new JSONArray();
		final JSONArray errorValues = new JSONArray();
		try {
			this.generateTransformedValues(workspace, worksheet, f, hNode, transformedRows, errorValues, 5);
			return new UpdateContainer(new PythonPreviewResultsUpdate(transformedRows, errorValues));
		} catch (Exception e) {
			logger.error("Error while creating python results preview", e);
			String message = e.toString();
			return new UpdateContainer(new ErrorUpdate("Error executing python script: " + message));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
