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

package edu.isi.karma.controller.command.cleaning;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SubmitCleaningCommandFactory extends CommandFactory implements JSONInputCommandFactory {

	private enum Arguments {
		hNodeID, worksheetID, hTableID, vWorksheetId,examples
	}
	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeid = request.getParameter(Arguments.hNodeID.name());
		String vw = request.getParameter(Arguments.vWorksheetId.name());
		String exps = request.getParameter(Arguments.examples.name());
		
		SubmitCleaningCommand sCleanningCommand = new SubmitCleaningCommand(getNewId(vWorkspace), hNodeid, vw, exps);
		return sCleanningCommand;
	}
	@Override
	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace)
			throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeID.name(), inputJson);
		String vWorksheetId = HistoryJsonUtil.getStringValue(Arguments.vWorksheetId.name(), inputJson);
		String examples = HistoryJsonUtil.getStringValue(Arguments.examples.name(), inputJson);
		SubmitCleaningCommand comm = new SubmitCleaningCommand(getNewId(vWorkspace), hNodeId, vWorksheetId, examples);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

}
