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

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.modeling.ontology.AutoOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class SuggestAutoModelCommandFactory extends JSONInputCommandFactory {

	private static Logger logger = LoggerFactory
			.getLogger(SuggestAutoModelCommandFactory.class);

	private enum Arguments {
		worksheetId, selectionName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
//		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new SuggestAutoModelCommand(getNewId(workspace), Command.NEW_MODEL, getWorksheetId(
				request, workspace));
	}

	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		AutoOntology autoOntology = new AutoOntology(worksheet);
		String path = contextParameters
				.getParameterValue(ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY)
				 + worksheet.getTitle() + ".owl";
		try {
			autoOntology.Build(path);
		} catch (IOException e) {
			logger.error("Error occured while creating auto model!", e);
		}

		OntologyManager ontMgr = workspace.getOntologyManager();
		File autoOtologyFile = new File(path);
		logger.info("Loading ontology: " + autoOtologyFile.getAbsolutePath());
		String encoding = EncodingDetector.detect(autoOtologyFile);
		try {
			ontMgr.doImportAndUpdateCache(autoOtologyFile, encoding);
			logger.info("Done loading ontology: "
					+ autoOtologyFile.getAbsolutePath());
		} catch(Exception e) {
			logger.error("Error importing auto ontology file: " + autoOtologyFile.getAbsolutePath());
		}
		
		SuggestAutoModelCommand comm = new SuggestAutoModelCommand(
				getNewId(workspace), model, worksheet.getId());
		// Add the semantic types that have saved into the history
		for (int i = 2; i < inputJson.length(); i++) {
			JSONObject hnodeObj = (JSONObject) inputJson.get(i);
			String hNodeId = (String) hnodeObj.get(ClientJsonKeys.value
					.name());

			JSONObject typeObj = (JSONObject) inputJson.get(++i);
			JSONObject value = (JSONObject) typeObj
					.get(ClientJsonKeys.value.name());

			SemanticType type;
			String domain = (String) value
					.get(SemanticType.ClientJsonKeys.DomainUri.name());
			String domainId = (String) value
					.get(SemanticType.ClientJsonKeys.DomainId.name());
			String fullType = (String) value
					.get(SemanticType.ClientJsonKeys.FullType.name());
			
			Label typeName = ontMgr.getUriLabel(fullType);
			Label domainName = null;
			if (domain != null && !domain.trim().equals(""))
				domainName = ontMgr.getUriLabel(domain);

			if (typeName != null) {
				type = new SemanticType(hNodeId, typeName, domainName, domainId, 
						false, Origin.User, 1.00);
				worksheet.getSemanticTypes().addType(type);
			}
		}
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SuggestAutoModelCommand.class;
	}
}
