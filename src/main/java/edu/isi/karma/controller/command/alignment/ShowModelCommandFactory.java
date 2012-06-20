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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.modeling.alignment.URI;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticType.Origin;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class ShowModelCommandFactory extends CommandFactory implements JSONInputCommandFactory {
	
	private static Logger logger = LoggerFactory.getLogger(ShowModelCommandFactory.class);
	
	private enum Arguments {
		vWorksheetId, checkHistory
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		return new ShowModelCommand(getNewId(vWorkspace), getWorksheetId(request, vWorkspace), vWorksheetId);
	}

	@Override
	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace)
			throws JSONException, KarmaException {
		String vWorksheetId = HistoryJsonUtil.getStringValue(Arguments.vWorksheetId.name(), inputJson);
		boolean checkHist = HistoryJsonUtil.getBooleanValue(Arguments.checkHistory.name(), inputJson);
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		
		if(checkHist) {
			// Check if any command history exists for the worksheet
			if(HistoryJsonUtil.historyExists(worksheet.getTitle(), vWorkspace.getPreferencesId())) {
				WorksheetCommandHistoryReader commReader = new WorksheetCommandHistoryReader(vWorksheetId, vWorkspace);
				try {
					commReader.readAndExecuteCommands();
				} catch (Exception e) {
					 logger.error("Error occured while reading model commands from history!", e);
					e.printStackTrace();
				}
			}
			return new ShowModelCommand(getNewId(vWorkspace), worksheet.getId(), vWorksheetId);
		}
		else {
			ShowModelCommand comm = new ShowModelCommand(getNewId(vWorkspace), worksheet.getId(), vWorksheetId);
			OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
			// Add the semantic types that have saved into the history
			for (int i=2; i<inputJson.length(); i++) {
				JSONObject hnodeObj = (JSONObject) inputJson.get(i);
				String hNodeId = (String) hnodeObj.get(ClientJsonKeys.value.name());
				
				JSONObject typeObj = (JSONObject) inputJson.get(++i);
				JSONObject value = (JSONObject) typeObj.get(ClientJsonKeys.value.name());
				
				SemanticType type = null;
				String domain = (String) value.get(SemanticType.ClientJsonKeys.Domain.name());
				String fullType = (String) value.get(SemanticType.ClientJsonKeys.FullType.name());
				boolean isPrimary = (Boolean) value.get(SemanticType.ClientJsonKeys.isPrimary.name());
				
				URI typeName = ontMgr.getURIFromString(fullType);
				URI domainName = null;
				if (domain != null && !domain.trim().equals(""))
					domainName = ontMgr.getURIFromString(domain);
				
				if(typeName != null) {
					type = new SemanticType(hNodeId, typeName, domainName, Origin.User, 1.00, isPrimary);
					worksheet.getSemanticTypes().addType(type);
				}
			}
			return comm;
		}
	}

}
