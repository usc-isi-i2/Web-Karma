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
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class SuggestModelCommandFactory extends JSONInputCommandFactory {
	

	private enum Arguments {
		worksheetId, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new SuggestModelCommand(getNewId(workspace), 
				Command.NEW_MODEL,
				getWorksheetId(request, workspace), false, 
				selectionName);
	}

	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		SuggestModelCommand comm = new SuggestModelCommand(getNewId(workspace), model,
				worksheet.getId(), false, selectionName);
		OntologyManager ontMgr = workspace.getOntologyManager();
		// Add the semantic types that have saved into the history
		for (int i=2; i<inputJson.length(); i++) {
			JSONObject hnodeObj = (JSONObject) inputJson.get(i);
			String hNodeId = (String) hnodeObj.get(ClientJsonKeys.value.name());
			
			JSONObject typeObj = (JSONObject) inputJson.get(++i);
			JSONObject value = (JSONObject) typeObj.get(ClientJsonKeys.value.name());
			
			SemanticType type;
			String domain = (String) value.get(SemanticType.ClientJsonKeys.DomainUri.name());
			String fullType = (String) value.get(SemanticType.ClientJsonKeys.FullType.name());
			String domainId = (String) value.get(SemanticType.ClientJsonKeys.DomainId.name());
			
			Label typeName = ontMgr.getUriLabel(fullType);
			Label domainName = null;
			if (domain != null && !domain.trim().equals(""))
				domainName = ontMgr.getUriLabel(domain);
			
			if(typeName != null) {
				type = new SemanticType(hNodeId, typeName, domainName, domainId, false, Origin.User, 1.00);
				worksheet.getSemanticTypes().addType(type);
			}
		}
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SuggestModelCommand.class;
	}
}
