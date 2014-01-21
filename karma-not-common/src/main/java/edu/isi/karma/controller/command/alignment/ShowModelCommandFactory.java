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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.webserver.KarmaException;

public class ShowModelCommandFactory extends CommandFactory implements JSONInputCommandFactory {
	

	private enum Arguments {
		worksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		return new ShowModelCommand(getNewId(workspace), getWorksheetId(request, workspace),false);
	}

	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		
		ShowModelCommand comm = new ShowModelCommand(getNewId(workspace), worksheet.getId(), false);
		OntologyManager ontMgr = workspace.getOntologyManager();
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
			
			Label typeName = ontMgr.getUriLabel(fullType);
			Label domainName = null;
			if (domain != null && !domain.trim().equals(""))
				domainName = ontMgr.getUriLabel(domain);
			
			if(typeName != null) {
				type = new SemanticType(hNodeId, typeName, domainName, Origin.User, 1.00, isPrimary);
				worksheet.getSemanticTypes().addType(type);
			}
		}
		return comm;
	}

}
