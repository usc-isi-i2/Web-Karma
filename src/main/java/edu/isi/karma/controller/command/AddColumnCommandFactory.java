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
package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class AddColumnCommandFactory extends CommandFactory implements JSONInputCommandFactory {

	public enum Arguments {
		vWorksheetId, hTableId, hNodeId, newColumnName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String newColumnName = request.getParameter(Arguments.newColumnName.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		return new AddColumnCommand(getNewId(vWorkspace), vWorksheetId, getWorksheetId(request, vWorkspace), hTableId, hNodeId, newColumnName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structues to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String vWorksheetID = CommandInputJSONUtil.getStringValue(Arguments.vWorksheetId.name(), inputJson);
		String hTableId = CommandInputJSONUtil.getStringValue(Arguments.hTableId.name(), inputJson);
		String newColumnName = CommandInputJSONUtil.getStringValue(Arguments.newColumnName.name(), inputJson);
		return new AddColumnCommand(getNewId(vWorkspace), vWorksheetID, 
				vWorkspace.getViewFactory().getVWorksheet(vWorksheetID).getWorksheet().getId(),
				hTableId, hNodeID, newColumnName);
	}

}
