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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SplitByCommaCommandFactory extends CommandFactory implements
		JSONInputCommandFactory {
	
	public enum Arguments {
		vWorksheetId, hNodeId, delimiter, checkHistory
	}
	
	private static Logger logger = LoggerFactory.getLogger(SplitByCommaCommandFactory.class);

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		String delimiter = request.getParameter(Arguments.delimiter.name());

		return new SplitByCommaCommand(getNewId(vWorkspace), getWorksheetId(
				request, vWorkspace), hNodeId, vWorksheetId, delimiter);
	}

	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace)
			throws JSONException, KarmaException {
		String vWorksheetId = HistoryJsonUtil.getStringValue(Arguments.vWorksheetId.name(), inputJson);
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String delimiter = HistoryJsonUtil.getStringValue(Arguments.delimiter.name(), inputJson);
		boolean checkHist = HistoryJsonUtil.getBooleanValue(Arguments.checkHistory.name(), inputJson);
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		
		// TODO This logic needs to be refactored and this should be moved from here
		if(checkHist) {
			// Check if any command history exists for the worksheet
			if(HistoryJsonUtil.historyExists(worksheet.getTitle(), vWorkspace.getPreferencesId())) {
				WorksheetCommandHistoryReader commReader = new WorksheetCommandHistoryReader(vWorksheetId, vWorkspace);
				try {
					List<CommandTag> tags = new ArrayList<CommandTag>();
					tags.add(CommandTag.Modeling);
					commReader.readAndExecuteCommands(tags);
				} catch (Exception e) {
					 logger.error("Error occured while reading model commands from history!", e);
					e.printStackTrace();
				}
			}
		}

		HistoryJsonUtil.setArgumentValue(Arguments.checkHistory.name(), false, inputJson);
		
		SplitByCommaCommand comm = new SplitByCommaCommand(getNewId(vWorkspace), 
				vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheetId(), hNodeId,
				vWorksheetId, delimiter);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
}
