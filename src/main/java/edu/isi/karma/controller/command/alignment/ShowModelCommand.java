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

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.SemanticType;

public class ShowModelCommand extends WorksheetCommand {

	private String worksheetName;
	private final boolean addVWorksheetUpdate;

	private static Logger logger = LoggerFactory
			.getLogger(ShowModelCommand.class);

	protected ShowModelCommand(String id, String worksheetId, boolean addVWorksheetUpdate) {
		super(id, worksheetId);
		this.addVWorksheetUpdate = addVWorksheetUpdate;
		
		/** NOTE Not saving this command in history for now since we are 
		 * not letting CRF model assign semantic types automatically. This command 
		 * was being saved in history to keep track of the semantic types 
		 * that were assigned by the CRF Model **/ 
		// addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Show Model";
	}

	@Override
	public String getDescription() {
		return worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		
		worksheetName = worksheet.getTitle();

		// Get the Outlier Tag
//		Tag outlierTag = vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier);

		// Generate the semantic types for the worksheet
		OntologyManager ontMgr = workspace.getOntologyManager();
		if(ontMgr.isEmpty())
			return new UpdateContainer(new ErrorUpdate("No ontology loaded."));
		
		
		if (addVWorksheetUpdate) {
			c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId));
		}
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		c.add(new TagsUpdate());
		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, workspace, worksheet.getSemanticTypes().getListOfTypes());
		
		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}

		// Create column nodes for the alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
			HNode node = path.getLeaf();
			// TODO: adding list of CRF semantic types
			alignment.addColumnNode(node.getId(), node.getColumnName(), "", null);
		}

		return c;
	}

	private void saveSemanticTypesInformation(Worksheet worksheet, Workspace workspace
			, Collection<SemanticType> semanticTypes) throws JSONException {
		JSONArray typesArray = new JSONArray();
		
		// Add the vworksheet information
		JSONObject vwIDJObj = new JSONObject();
		vwIDJObj.put(ClientJsonKeys.name.name(), ParameterType.worksheetId.name());
		vwIDJObj.put(ClientJsonKeys.type.name(), ParameterType.worksheetId.name());
		vwIDJObj.put(ClientJsonKeys.value.name(), worksheetId);
		typesArray.put(vwIDJObj);
		
		// Add the check history information
		JSONObject chIDJObj = new JSONObject();
		chIDJObj.put(ClientJsonKeys.name.name(), ParameterType.checkHistory.name());
		chIDJObj.put(ClientJsonKeys.type.name(), ParameterType.other.name());
		chIDJObj.put(ClientJsonKeys.value.name(), false);
		typesArray.put(chIDJObj);
		
		for (SemanticType type: semanticTypes) {
			// Add the hNode information
			JSONObject hNodeJObj = new JSONObject();
			hNodeJObj.put(ClientJsonKeys.name.name(), ParameterType.hNodeId.name());
			hNodeJObj.put(ClientJsonKeys.type.name(), ParameterType.hNodeId.name());
			hNodeJObj.put(ClientJsonKeys.value.name(), type.getHNodeId());
			typesArray.put(hNodeJObj);
			
			// Add the semantic type information
			JSONObject typeJObj = new JSONObject();
			typeJObj.put(ClientJsonKeys.name.name(), ClientJsonKeys.SemanticType.name());
			typeJObj.put(ClientJsonKeys.type.name(), ParameterType.other.name());
			typeJObj.put(ClientJsonKeys.value.name(), type.getJSONArrayRepresentation());
			typesArray.put(typeJObj);
		}
		setInputParameterJson(typesArray.toString());
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
}
