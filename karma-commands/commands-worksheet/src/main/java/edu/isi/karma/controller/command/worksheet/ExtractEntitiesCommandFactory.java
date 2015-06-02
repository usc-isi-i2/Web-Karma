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

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class ExtractEntitiesCommandFactory extends JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hNodeId, 
		newColumnName, defaultValue, extractionURL, 
		entitiesToBeExt, selectionName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String extractionURL = request.getParameter(Arguments.extractionURL.name());
		String entitiesToBeExt = request.getParameter(Arguments.entitiesToBeExt.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new ExtractEntitiesCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				hNodeId, extractionURL, entitiesToBeExt, 
				selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String extractionURL = HistoryJsonUtil.getStringValue(Arguments.extractionURL.name(), inputJson);
		String entitiesToBeExt = HistoryJsonUtil.getStringValue(Arguments.entitiesToBeExt.name(), inputJson);
		String selectionName = HistoryJsonUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		ExtractEntitiesCommand cmd = new ExtractEntitiesCommand(getNewId(workspace), model, worksheetId, 
				hNodeId, extractionURL, entitiesToBeExt, 
				selectionName);
		
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}
	
	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return ExtractEntitiesCommand.class;
	}


}
