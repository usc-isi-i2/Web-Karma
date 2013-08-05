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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ApplyModelFromTripleStoreCommand extends Command {
	
	private final String vWorksheetId;
	private final String modelName;
//	private final String sourceName;
	
	private enum sparqlKeys {
		results, bindings, history, value
	}
	
	private static Logger logger = LoggerFactory.getLogger(ApplyModelFromTripleStoreCommand.class);
	
	public ApplyModelFromTripleStoreCommand(String id, String vWorksheetId, String modelName, 
			String sourceName) {
		super(id);
		this.vWorksheetId = vWorksheetId;
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		
		// Get the command history from model by doing a sparql query on the Triple store
		String query = "PREFIX km-dev:<http://isi.edu/integration/karma/dev#> " +
				"SELECT ?history WHERE " +
				"{ ?x km-dev:modelName \"" + modelName + "\" ." +
				"  ?x km-dev:hasWorksheetHistory ?history . }";
		try {
			JSONObject res = TripleStoreUtil.invokeSparqlQuery(query, TripleStoreUtil.defaultModelsRepoUrl);
			
			JSONArray bindings = res.getJSONObject(sparqlKeys.results.name())
					.getJSONArray(sparqlKeys.bindings.name());
			
			if (bindings.length() == 0) {
				return new UpdateContainer(new ErrorUpdate("No history found in the " +
						"selected model!"));
			}
			
			String history = bindings.getJSONObject(0).getJSONObject(
					sparqlKeys.history.name()).getString(sparqlKeys.value.name());
			
	
			// Execute the history
			WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(
					vWorksheetId, vWorkspace);


			if (history.isEmpty()) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			
			JSONArray historyJson = new JSONArray(history);
			histReader.readAndExecuteAllCommands(historyJson);
			
			// Add worksheet updates that could have resulted out of the transformation commands
			UpdateContainer c =  new UpdateContainer();
			vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,
					worksheet.getHeaders().getAllPaths(), vWorkspace);
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
			vw.update(c);

			String alignmentId = AlignmentManager.Instance().constructAlignmentId(
					vWorkspace.getWorkspace().getId(), vWorksheetId);
			Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
			if (alignment == null) {
				alignment = new Alignment(vWorkspace.getWorkspace().getOntologyManager());
				AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
			}

			// Compute the semantic type suggestions
			SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace().getCrfModelHandler(), 
					vWorkspace.getWorkspace().getOntologyManager(), alignment);
			
			// Add the alignment update
			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
			c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), alignment));

			c.add(new InfoUpdate("Model successfully applied!"));
			return c;
			
		} catch (Exception e) {
			logger.error("Error applying model from triple store!", e);
			return new UpdateContainer(new ErrorUpdate("Error applying model from triple store!"));
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}

}
