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
package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class SetSemanticTypeCommandFactory extends JSONInputCommandFactory {

	protected enum Arguments {
		worksheetId, hNodeId,
		SemanticTypesArray, trainAndShowUpdates, rdfLiteralType, language,
		selectionName
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String arrStr = request.getParameter(SemanticTypesUpdate.JsonKeys.SemanticTypesArray.name());
		String rdfLiteralType = request.getParameter(Arguments.rdfLiteralType.name());
		String language = request.getParameter(Arguments.language.name());
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new SetSemanticTypeCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, hNodeId, 
				arr, true, rdfLiteralType, language, 
				selectionName);
	}

	public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String arrStr = HistoryJsonUtil.getStringValue(Arguments.SemanticTypesArray.name(), inputJson);
		boolean train = HistoryJsonUtil.getBooleanValue(Arguments.trainAndShowUpdates.name(), inputJson);
		String rdfLiteralType = HistoryJsonUtil.getStringValue(Arguments.rdfLiteralType.name(), inputJson);
		String language = null;
		if(HistoryJsonUtil.valueExits(Arguments.language.name(), inputJson))
			language = HistoryJsonUtil.getStringValue(Arguments.language.name(), inputJson);
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		SetSemanticTypeCommand comm = new SetSemanticTypeCommand(getNewId(workspace), model,
				worksheetId, hNodeId, arr, 
				train, rdfLiteralType, language, 
				selectionName);
		
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
	
	public Command createCommand(String model, Workspace workspace, String worksheetId, String hNodeId, 
			boolean isPartOfKey, JSONArray arr, boolean train, 
			String rdfLiteralType, String language, String selectionId) {
		return new SetSemanticTypeCommand(getNewId(workspace), model, worksheetId, hNodeId, 
				arr, train, rdfLiteralType, language, selectionId);
	}
	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SetSemanticTypeCommand.class;
	}
}
