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
import java.util.Map;

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
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ShowModelCommand extends WorksheetCommand {

	private final String vWorksheetId;
	private String worksheetName;

	private static Logger logger = LoggerFactory
			.getLogger(ShowModelCommand.class);

	protected ShowModelCommand(String id, String worksheetId,
			String vWorksheetId) {
		super(id, worksheetId);
		this.vWorksheetId = vWorksheetId;
		
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();

		worksheetName = worksheet.getTitle();

		// Get the Outlier Tag
//		Tag outlierTag = vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier);

		// Generate the semantic types for the worksheet
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		if(ontMgr.isEmpty())
			return new UpdateContainer(new ErrorUpdate("No ontology loaded."));
//		SemanticTypeUtil.populateSemanticTypesUsingCRF(worksheet, outlierTag, vWorkspace.getWorkspace().getCrfModelHandler(), ontMgr);
		SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace().getCrfModelHandler(), ontMgr);
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(ontMgr);
		}

		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, vWorkspace);
			
			// Add the visualization update
			List<String> hNodeIdList = new ArrayList<String>();
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
			List<HNodePath> columns = vw.getColumns();
			for(HNodePath path:columns)
				hNodeIdList.add(path.getLeaf().getId());

			SVGAlignmentUpdate_ForceKarmaLayout svgUpdate = new SVGAlignmentUpdate_ForceKarmaLayout(vWorksheetId, alignmentId, alignment, hNodeIdList);
			c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
			c.add(svgUpdate);
			c.add(new TagsUpdate());
		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}
		return c;
	}

	private void saveSemanticTypesInformation(Worksheet worksheet, VWorkspace vWorkspace) throws JSONException {
		SemanticTypes types = worksheet.getSemanticTypes();
		JSONArray typesArray = new JSONArray();
		Map<String, SemanticType> typeMap = types.getTypes();
		
		// Add the vworksheet information
		JSONObject vwIDJObj = new JSONObject();
		vwIDJObj.put(ClientJsonKeys.name.name(), ParameterType.vWorksheetId.name());
		vwIDJObj.put(ClientJsonKeys.type.name(), ParameterType.vWorksheetId.name());
		vwIDJObj.put(ClientJsonKeys.value.name(), vWorksheetId);
		typesArray.put(vwIDJObj);
		
		// Add the check history information
		JSONObject chIDJObj = new JSONObject();
		chIDJObj.put(ClientJsonKeys.name.name(), ParameterType.checkHistory.name());
		chIDJObj.put(ClientJsonKeys.type.name(), ParameterType.other.name());
		chIDJObj.put(ClientJsonKeys.value.name(), false);
		typesArray.put(chIDJObj);
		
		for (String hNodeId : typeMap.keySet()) {
			// Add the hNode information
			JSONObject hNodeJObj = new JSONObject();
			hNodeJObj.put(ClientJsonKeys.name.name(), ParameterType.hNodeId.name());
			hNodeJObj.put(ClientJsonKeys.type.name(), ParameterType.hNodeId.name());
			hNodeJObj.put(ClientJsonKeys.value.name(), hNodeId);
			typesArray.put(hNodeJObj);
			
			// Add the semantic type information
			JSONObject typeJObj = new JSONObject();
			typeJObj.put(ClientJsonKeys.name.name(), ClientJsonKeys.SemanticType.name());
			typeJObj.put(ClientJsonKeys.type.name(), ParameterType.other.name());
			typeJObj.put(ClientJsonKeys.value.name(), typeMap.get(hNodeId).getJSONArrayRepresentation());
			typesArray.put(typeJObj);
		}
		
		setInputParameterJson(typesArray.toString(4));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}
}
