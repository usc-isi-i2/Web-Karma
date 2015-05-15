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

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class FetchExistingModelsForWorksheetCommand extends WorksheetCommand {
	
	private enum sparqlKeys {
		results, bindings, modelName, sourceName, value
	}
	
	private enum JsonKeys {
		updateType, existingModelNames, worksheetName, modelName, sourceName
	}
	
	private static Logger logger = LoggerFactory.getLogger(FetchExistingModelsForWorksheetCommand.class);

	public FetchExistingModelsForWorksheetCommand(String id, String model, String worksheetId) {
		super(id, model, worksheetId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fetch Existing Models";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		final String wkName = worksheet.getTitle();
		
		
		String sparqlQuery = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> " +
				"SELECT ?modelName ?sourceName WHERE " +
				"{ ?x km-dev:modelName ?modelName ." +
				"  ?x km-dev:sourceName ?sourceName . }";
		try {
			String sData = TripleStoreUtil.invokeSparqlQuery(sparqlQuery, TripleStoreUtil.defaultModelsRepoUrl, "application/sparql-results+json", null);
			if (sData == null | sData.isEmpty()) {
				logger.error("Empty response object from query : " + sparqlQuery);
			}
			final JSONObject sprqlOutput = new JSONObject(sData);
//			final JSONObject sprqlOutput = TripleStoreUtil.invokeSparqlQuery(sparqlQuery,
//					TripleStoreUtil.defaultModelsRepoUrl);
			
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject response = new JSONObject();
					JSONArray list = new JSONArray();
					
					try {
						JSONArray values = sprqlOutput.getJSONObject(sparqlKeys.results.name())
								.getJSONArray(sparqlKeys.bindings.name());
						
						// prepare the output
						for (int i=0; i<values.length(); i++) {
							JSONObject modelObj = new JSONObject();
							JSONObject binding = values.getJSONObject(i);
							String sourceName = binding.getJSONObject(sparqlKeys.sourceName.name())
									.getString(sparqlKeys.value.name());
							String modelName = binding.getJSONObject(sparqlKeys.modelName.name())
									.getString(sparqlKeys.value.name());
							
							modelObj.put(JsonKeys.modelName.name(), modelName);
							modelObj.put(JsonKeys.sourceName.name(), sourceName);
							list.put(modelObj);
						}
						
						response.put(JsonKeys.updateType.name(), "ExistingModelsList");
						response.put(JsonKeys.existingModelNames.name(), list);
						response.put(JsonKeys.worksheetName.name(), wkName);
						
						pw.print(response.toString());
					} catch (JSONException e) {
						logger.error("Error creating JSON response for model names!", e);
					}
				}
			});
		} catch (Exception e1) {
			logger.error("Error creating JSON response for model names!", e1);
			return new UpdateContainer(new ErrorUpdate("Error occured while getting models!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}

}
