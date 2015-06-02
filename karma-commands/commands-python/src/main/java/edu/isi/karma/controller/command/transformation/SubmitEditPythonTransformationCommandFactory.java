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

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class SubmitEditPythonTransformationCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		newColumnName, transformationCode, worksheetId, 
		hNodeId, errorDefaultValue, previousCommandId, 
		targetHNodeId, selectionName, isJSONOutput
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String newColumnName = HistoryJsonUtil.getStringValue(Arguments.newColumnName.name(), inputJson);
		String code = HistoryJsonUtil.getStringValue(Arguments.transformationCode.name(), inputJson);
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String errorDefaultValue = HistoryJsonUtil.getStringValue(Arguments.errorDefaultValue.name(), inputJson);
		//String previousCommandId = HistoryJsonUtil.getStringValue(Arguments.previousCommandId.name(), inputJson);
		String targetHNodeId = HistoryJsonUtil.getStringValue(Arguments.targetHNodeId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		boolean isJSONOutput = false;
		try {
			isJSONOutput = Boolean.parseBoolean(CommandInputJSONUtil.getStringValue(Arguments.isJSONOutput.name(), inputJson));
		}
		catch(Exception e)
		{}
		SubmitEditPythonTransformationCommand comm = new SubmitEditPythonTransformationCommand(
				getNewId(workspace), model,
				newColumnName, code, worksheetId, hNodeId, errorDefaultValue, targetHNodeId, 
				selectionName, isJSONOutput);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SubmitEditPythonTransformationCommand.class;
	}
}
