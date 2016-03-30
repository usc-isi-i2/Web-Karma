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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearner;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.VWorkspaceRegistry;


public class SuggestModelCommand extends WorksheetSelectionCommand {

	private String worksheetName;
	private Alignment initialAlignment = null;
	private DirectedWeightedMultigraph<Node, DefaultLink> initialGraph = null;
	private List<Node> steinerNodes;
//	private final boolean addVWorksheetUpdate;

	private static Logger logger = LoggerFactory
			.getLogger(SuggestModelCommand.class);

	protected SuggestModelCommand(String id, String model, String worksheetId, boolean addVWorksheetUpdate, String selectionId) {
		super(id, model, worksheetId, selectionId);
//		this.addVWorksheetUpdate = addVWorksheetUpdate;
		
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
		return "Suggest Model";
	}

	@Override
	public String getDescription() {
		return worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(WorkspaceKarmaHomeRegistry.getInstance().getKarmaHome(workspace.getId()));
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		OntologyManager ontologyManager = workspace.getOntologyManager();
		if(ontologyManager.isEmpty())
			return new UpdateContainer(new ErrorUpdate("No ontology loaded."));
		
		worksheetName = worksheet.getTitle();
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Alignment is NULL for " + worksheetId));
		}

		if (initialAlignment == null)
		{
			initialAlignment = alignment.getAlignmentClone();

			initialGraph = (DirectedWeightedMultigraph<Node, DefaultLink>)alignment.getGraph().clone();
			
			List<HNode> orderedNodeIds = new ArrayList<>();
			worksheet.getHeaders().getSortedLeafHNodes(orderedNodeIds);
			
			List<String> visibleHNodeIds = null;
			VWorkspace vWorkspace = VWorkspaceRegistry.getInstance().getVWorkspace(workspace.getId());
			if (vWorkspace != null) {
				VWorksheet viewWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
				visibleHNodeIds = viewWorksheet.getHeaderVisibleLeafNodes();
			}
						
			if (orderedNodeIds != null) {
				for (int i = 0; i < orderedNodeIds.size(); i++)
				{
					String hNodeId = orderedNodeIds.get(i).getId();

					if (visibleHNodeIds != null && !visibleHNodeIds.contains(hNodeId))
						continue;

					ColumnNode cn = alignment.getColumnNodeByHNodeId(hNodeId);
					
					if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.NotAssigned)
					{
						worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
						List<SemanticType> suggestedSemanticTypes = 
								new SemanticTypeUtil().getColumnSemanticSuggestions(workspace, worksheet, cn, 4, selection);
						cn.setLearnedSemanticTypes(suggestedSemanticTypes);
						cn.includeInAutoModel();
					} 
				}
			}
		} else {
		// Replace the current alignment with the old alignment
			alignment = initialAlignment;
			alignment.setGraph(initialGraph);
			if(!this.isExecutedInBatch())
				alignment.align();
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}

		steinerNodes = alignment.computeSteinerNodes();
		ModelLearner modelLearner;

		if (modelingConfiguration.getKnownModelsAlignment()) 
			modelLearner = new ModelLearner(alignment.getGraphBuilder(), steinerNodes);
		else
			modelLearner = new ModelLearner(ontologyManager, alignment.getLinksByStatus(LinkStatus.ForcedByUser), steinerNodes);

//		logger.info(GraphUtil.defaultGraphToString(ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact).getGraphBuilder().getGraph()));

		SemanticModel model = modelLearner.getModel();
		if (model == null) {
			logger.error("could not learn any model for this source!");
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating a semantic model for the source."));
		}
		
//		logger.info(GraphUtil.labeledGraphToString(model.getGraph()));
		
		List<SemanticType> semanticTypes = new LinkedList<>();
		alignment.updateAlignment(model, semanticTypes);
		logger.info(GraphUtil.labeledGraphToString(alignment.getSteinerTree()));

//		Set<ColumnNode> alignmentColumnNodes = alignment.getSourceColumnNodes();
//		if (alignmentColumnNodes != null) {
//			for (ColumnNode cn : alignmentColumnNodes) {
//				worksheet.getSemanticTypes().unassignColumnSemanticType(cn.getHNodeId());
//			}
//		}
//		if (semanticTypes != null) {
//			for (SemanticType st : semanticTypes)
//				worksheet.getSemanticTypes().addType(st);
//		}
		
		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, workspace, worksheet.getSemanticTypes().getListOfTypes());
			
			// Add the visualization update
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));

		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}
		c.add(new TagsUpdate());
		
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
		
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		OntologyManager ontologyManager = workspace.getOntologyManager();
		if(ontologyManager.isEmpty())
			return new UpdateContainer(new ErrorUpdate("No ontology loaded."));
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating R2RML Model!"));
		}

//		Set<ColumnNode> alignmentColumnNodes = alignment.getSourceColumnNodes();
//		if (alignmentColumnNodes != null) {
//			for (ColumnNode cn : alignmentColumnNodes) {
//				if (!cn.hasUserType())
//					worksheet.getSemanticTypes().unassignColumnSemanticType(cn.getHNodeId());
//			}
//		}

		alignment = initialAlignment;
		alignment.setGraph(initialGraph);
		if(!this.isExecutedInBatch())
			alignment.align();
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		

		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, workspace, worksheet.getSemanticTypes().getListOfTypes());
			c.append(this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			// Add the visualization update
		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}
		c.add(new TagsUpdate());
		
		return c;
	}
}
