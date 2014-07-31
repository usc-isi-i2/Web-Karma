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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearner;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubClassLink;


public class ShowModelCommand extends WorksheetCommand {

	private String worksheetName;
	private Alignment initialAlignment = null;
	private DirectedWeightedMultigraph<Node, DefaultLink> initialGraph = null;
	private List<ColumnNode> columnNodes;
	private Set<String> columnsWithoutSemanticType = null;
//	private final boolean addVWorksheetUpdate;

	private static Logger logger = LoggerFactory
			.getLogger(ShowModelCommand.class);

	protected ShowModelCommand(String id, String worksheetId, boolean addVWorksheetUpdate) {
		super(id, worksheetId);
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
		return "Show Model";
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
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		OntologyManager ontologyManager = workspace.getOntologyManager();
		if(ontologyManager.isEmpty())
			return new UpdateContainer(new ErrorUpdate("No ontology loaded."));
		
		worksheetName = worksheet.getTitle();
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), worksheetId, ontologyManager);
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Alignment is NULL for " + worksheetId));
		}

		if (initialAlignment == null)
		{
			initialAlignment = alignment.getAlignmentClone();

			initialGraph = (DirectedWeightedMultigraph<Node, DefaultLink>)alignment.getGraph().clone();
			
			columnNodes = new LinkedList<ColumnNode>();
			columnsWithoutSemanticType = new HashSet<String>();
			List<HNode> orderedNodeIds = new ArrayList<HNode>();
			worksheet.getHeaders().getSortedLeafHNodes(orderedNodeIds);
			if (orderedNodeIds != null)
				for (int i = 0; i < orderedNodeIds.size(); i++)
				{
					String hNodeId = orderedNodeIds.get(i).getId();
					ColumnNode cn = alignment.getColumnNodeByHNodeId(hNodeId);
					if (cn.getUserSelectedSemanticType() == null)
					{
						columnsWithoutSemanticType.add(hNodeId);
						worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
					}
					columnNodes.add(cn);
				}
		} else {
		// Replace the current alignment with the old alignment
			alignment = initialAlignment;
			alignment.setGraph(initialGraph);
			alignment.align();
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
		
		ModelLearner modelLearner = new ModelLearner(workspace, worksheet, ontologyManager, columnNodes);
		SemanticModel model = modelLearner.getModel();
		if (model == null) {
			logger.error("could not learn any model for this source!");
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating a semantic model for the source."));
		}
		
//		logger.info(GraphUtil.labeledGraphToString(model.getGraph()));
		
		HashSet<String> alignmentNodeUris = new HashSet<String>();
		HashMap<Node, Node> modelToAlignmentNode = new HashMap<Node, Node>();
		Set<Node> nodesWithSameUri;
		if (model != null) {
			String uri;
			for (Node n : model.getGraph().vertexSet()) {
				if (n instanceof InternalNode) {
					uri = n.getUri();
					InternalNode iNode;
					
					if (alignmentNodeUris.contains(uri)) {
						iNode = alignment.addInternalNode(n.getLabel());
					} else {
						nodesWithSameUri = alignment.getNodesByUri(uri);
						if (nodesWithSameUri != null && !nodesWithSameUri.isEmpty())
							iNode = (InternalNode)nodesWithSameUri.iterator().next();
						else
							iNode = alignment.addInternalNode(n.getLabel());
					}
					modelToAlignmentNode.put(n, iNode);
					alignmentNodeUris.add(uri);
				}
				if (n instanceof ColumnNode) {
					if (model.getMappingToSourceColumns() != null)
						modelToAlignmentNode.put(n, model.getMappingToSourceColumns().get(n));
				}
			}
			
			Node source, target;
			for (LabeledLink l : model.getGraph().edgeSet()) {
				
				if (!(l.getSource() instanceof InternalNode)) {
					logger.error("column node cannot have an outgoing link!");
					return new UpdateContainer(new ErrorUpdate(
							"Error occured while generating the model for the source. " +
							"A column node cannot have an outgoing link."));
				}

				
				source = modelToAlignmentNode.get(l.getSource());
				target = modelToAlignmentNode.get(l.getTarget());
				
				if (source == null || target == null)
					continue;

				LabeledLink newLink = null;
				if (l instanceof DataPropertyLink)
					newLink = alignment.addDataPropertyLink(source, target, l.getLabel(), l.getKeyType() == LinkKeyInfo.PartOfKey? true : false);
				else if (l instanceof ObjectPropertyLink)
					newLink = alignment.addObjectPropertyLink(source, target, l.getLabel());
				else if (l instanceof SubClassLink)
					newLink = alignment.addSubClassOfLink(source, target);
				else if (l instanceof ClassInstanceLink)
					newLink = alignment.addClassInstanceLink(source, target, l.getKeyType());
				else if (l instanceof ColumnSubClassLink)
					newLink = alignment.addColumnSubClassOfLink(source, target);
				else if (l instanceof DataPropertyOfColumnLink)
					newLink = alignment.addDataPropertyOfColumnLink(source, target, ((DataPropertyOfColumnLink)l).getSpecializedColumnHNodeId());
				else if (l instanceof ObjectPropertySpecializationLink)
					newLink = alignment.addObjectPropertySpecializationLink(source, target, ((ObjectPropertySpecializationLink)l).getSpecializedLinkId());
				else {
		    		logger.error("cannot instanciate a link from the type: " + l.getType().toString());
		    		continue;
				}
				
				if (newLink == null) // link already exist
					continue;
				
				if (target instanceof ColumnNode) {
					SemanticType st = new SemanticType(((ColumnNode)target).getHNodeId(), 
							newLink.getLabel(), source.getLabel(), SemanticType.Origin.User, 1.0, false);
					worksheet.getSemanticTypes().addType(st);
				}
				
				if (!(target instanceof ColumnNode) && newLink != null)
					alignment.changeLinkStatus(newLink.getId(), LinkStatus.ForcedByUser);

			}
		}
		
		alignment.align();
		
		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, workspace, worksheet.getSemanticTypes().getListOfTypes());
			
			// Add the visualization update
			c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
			c.add(new AlignmentSVGVisualizationUpdate(
					worksheetId, alignment));
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

		alignment = initialAlignment;
		alignment.setGraph(initialGraph);
		alignment.align();
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		
		if (this.columnsWithoutSemanticType != null) {
			for (String hNodeId : this.columnsWithoutSemanticType) {
				worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
			}
		}

		try {
			// Save the semantic types in the input parameter JSON
			saveSemanticTypesInformation(worksheet, workspace, worksheet.getSemanticTypes().getListOfTypes());
			
			// Add the visualization update
			c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
			c.add(new AlignmentSVGVisualizationUpdate(
					worksheetId, alignment));
		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}
		c.add(new TagsUpdate());
		
		return c;
	}
}
