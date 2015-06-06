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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;

public class AddUserLinkToAlignmentCommandFactory extends JSONInputCommandFactory {
	private enum Arguments {
		edgeId, alignmentId, worksheetId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String edgeId =request.getParameter(Arguments.edgeId.name());
		String alignmentId =request.getParameter(Arguments.alignmentId.name());
		String worksheetId =request.getParameter(Arguments.worksheetId.name());
		return new AddUserLinkToAlignmentCommand(getNewId(workspace),
				Command.NEW_MODEL, edgeId, alignmentId, worksheetId);
	}

	public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException {
		String edgeId = HistoryJsonUtil.getStringValue(Arguments.edgeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		
		AddUserLinkToAlignmentCommand comm = new AddUserLinkToAlignmentCommand(getNewId(workspace),
				model, edgeId, alignmentId, worksheetId); 
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return AddUserLinkToAlignmentCommand.class;
	}
}
