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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Workspace;

public class ApplyModelFromTripleStoreCommand extends WorksheetCommand {
	
	private final String modelName;
//	private final String sourceName;
	
	private enum sparqlKeys {
		results, bindings, history, value
	}
	
	private static Logger logger = LoggerFactory.getLogger(ApplyModelFromTripleStoreCommand.class);
	
	public ApplyModelFromTripleStoreCommand(String id, String model, String worksheetId, String modelName, 
			String sourceName) {
		super(id, model, worksheetId);
		this.modelName = modelName;
//		this.sourceName = sourceName;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Apply Model";
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
		
		// Get the command history from model by doing a sparql query on the Triple store
		String query = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> " +
				"SELECT ?history WHERE " +
				"{ ?x km-dev:modelName \"" + modelName + "\" ." +
				"  ?x km-dev:hasWorksheetHistory ?history . }";
		try {
//			JSONObject res = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultModelsRepoUrl);
			String sData = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultModelsRepoUrl, "application/sparql-results+json", null);
			if (sData == null | sData.isEmpty()) {
				logger.error("Empty response object from query : " + query);
			}
			JSONObject res = new JSONObject(sData);
			
			JSONArray bindings = res.getJSONObject(sparqlKeys.results.name())
					.getJSONArray(sparqlKeys.bindings.name());
			
			if (bindings.length() == 0) {
				return new UpdateContainer(new ErrorUpdate("No history found in the " +
						"selected model!"));
			}
			
			String history = bindings.getJSONObject(0).getJSONObject(
					sparqlKeys.history.name()).getString(sparqlKeys.value.name());
			
	
			// Execute the history
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);


			if (history.isEmpty()) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			
			JSONArray historyJson = new JSONArray(history);
			histExecutor.executeAllCommands(historyJson);
			
			// Add worksheet updates that could have resulted out of the transformation commands
			UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId());
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			c.add(new InfoUpdate("Model successfully applied!"));
			return c;
			
		} catch (Exception e) {
			logger.error("Error applying model from triple store!", e);
			return new UpdateContainer(new ErrorUpdate("Error applying model from triple store!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}

}
