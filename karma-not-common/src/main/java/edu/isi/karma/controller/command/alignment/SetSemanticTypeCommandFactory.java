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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class SetSemanticTypeCommandFactory extends JSONInputCommandFactory {

	private enum Arguments {
		worksheetId, hNodeId, isKey, SemanticTypesArray, trainAndShowUpdates, rdfLiteralType
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		boolean isPartOfKey = Boolean.parseBoolean(request.getParameter(Arguments.isKey.name()));
		String arrStr = request.getParameter(SemanticTypesUpdate.JsonKeys.SemanticTypesArray.name());
		String rdfLiteralType = request.getParameter(Arguments.rdfLiteralType.name());
		
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}

		return new SetSemanticTypeCommand(getNewId(workspace), worksheetId, hNodeId, 
				isPartOfKey, arr, true, rdfLiteralType);
	}

	public Command createCommand(JSONArray inputJson, Workspace workspace) throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String arrStr = HistoryJsonUtil.getStringValue(Arguments.SemanticTypesArray.name(), inputJson);
		boolean isPartOfKey = HistoryJsonUtil.getBooleanValue(Arguments.isKey.name(), inputJson);
		boolean train = HistoryJsonUtil.getBooleanValue(Arguments.trainAndShowUpdates.name(), inputJson);
		String rdfLiteralType = HistoryJsonUtil.getStringValue(Arguments.rdfLiteralType.name(), inputJson);
		
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}
		
		SetSemanticTypeCommand comm = new SetSemanticTypeCommand(getNewId(workspace), 
				worksheetId, hNodeId, isPartOfKey, arr, train, rdfLiteralType);
		
		// Change the train flag, so that it does not train while reading from history
		HistoryJsonUtil.setArgumentValue(Arguments.trainAndShowUpdates.name(), false, inputJson);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
}
