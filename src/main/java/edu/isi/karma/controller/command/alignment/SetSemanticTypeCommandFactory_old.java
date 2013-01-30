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

import java.util.ArrayList;
import java.util.List;

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
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SetSemanticTypeCommandFactory_old extends CommandFactory implements JSONInputCommandFactory {

	private enum Arguments {
		vWorksheetId, hNodeId, isKey, SemanticTypesArray, trainAndShowUpdates
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getSimpleName());

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId.name());
		boolean isPartOfKey = Boolean.parseBoolean(request.getParameter(Arguments.isKey.name()));

		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		/*
		 * Parse the input JSON Array to get the sem types (including the
		 * synonym ones)
		 */
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		SemanticType primaryType = null;
		String arrStr = request.getParameter(SemanticTypesUpdate.JsonKeys.SemanticTypesArray.name());
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject type = arr.getJSONObject(i);
				// Look for the primary semantic type
				Label typeName = ontMgr.getUriLabel(type.getString(ClientJsonKeys.FullType.name()));
				if(typeName == null) {
					logger.error("Could not find the resource " + type.getString(ClientJsonKeys.FullType.name()) + " in ontology model!");
					return null;
				}
				Label domainName = null;
				if (type.getString(ClientJsonKeys.Domain.name()) != "")
					domainName = ontMgr.getUriLabel(type.getString(ClientJsonKeys.Domain.name()));

				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					primaryType = new SemanticType(hNodeId, typeName,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
				} else { // Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId, typeName,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					typesList.add(synType);
				}
			}
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}

		SynonymSemanticTypes synTypes = new SynonymSemanticTypes(typesList);

		return new SetSemanticTypeCommand_old(getNewId(vWorkspace), vWorksheetId,
				hNodeId, isPartOfKey, primaryType, synTypes, true);
	}

	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace) throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String vWorksheetId = HistoryJsonUtil.getStringValue(Arguments.vWorksheetId.name(), inputJson);
		String arrStr = HistoryJsonUtil.getStringValue(Arguments.SemanticTypesArray.name(), inputJson);
		boolean isPartOfKey = HistoryJsonUtil.getBooleanValue(Arguments.isKey.name(), inputJson);
		boolean train = HistoryJsonUtil.getBooleanValue(Arguments.trainAndShowUpdates.name(), inputJson);
		
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		/*
		 * Parse the input JSON Array to get the sem types (including the
		 * synonym ones)
		 */
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		SemanticType primaryType = null;
		
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject type = arr.getJSONObject(i);
				// Look for the primary semantic type
				Label typeName = ontMgr.getUriLabel(type.getString(SemanticTypesUpdate.JsonKeys.FullType.name()));
				if(typeName == null) {
					logger.error("Could not find the resource " + type.getString(SemanticTypesUpdate.JsonKeys.FullType.name()) + " in ontology model!");
					return null;
				}
				Label domainName = null;
				if (type.getString(SemanticTypesUpdate.JsonKeys.Domain.name()) != "")
					domainName = ontMgr.getUriLabel(type.getString(SemanticTypesUpdate.JsonKeys.Domain.name()));

				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					primaryType = new SemanticType(hNodeId, typeName,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
				} else { // Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId, typeName,domainName, SemanticType.Origin.User, 1.0,isPartOfKey);
					typesList.add(synType);
				}
			}
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}
		SynonymSemanticTypes synTypes = new SynonymSemanticTypes(typesList);
		
		SetSemanticTypeCommand_old comm = new SetSemanticTypeCommand_old(getNewId(vWorkspace), vWorksheetId,hNodeId, isPartOfKey, primaryType, synTypes, train);
		
		// Change the train flag, so that it does not train while reading from history
		HistoryJsonUtil.setArgumentValue(Arguments.trainAndShowUpdates.name(), false, inputJson);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
}
