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
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class AddRowCommandFactory extends JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hTableId, hNodeId, newColumnName, defaultValue
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new AddRowCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, hNodeId);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		AddRowCommand rowCmd = new AddRowCommand(getNewId(workspace), model, worksheetId, hNodeId);
		rowCmd.setInputParameterJson(inputJson.toString());
		return rowCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return AddRowCommand.class;
	}
}
