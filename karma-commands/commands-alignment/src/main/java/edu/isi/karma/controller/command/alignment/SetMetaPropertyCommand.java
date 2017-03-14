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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.alignment.SetMetaPropertyCommandFactory.Arguments;
import edu.isi.karma.controller.command.alignment.SetMetaPropertyCommandFactory.METAPROPERTY_NAME;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
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
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;


public class SetMetaPropertyCommand extends WorksheetSelectionCommand {

	private final String hNodeId;
	private final boolean trainAndShowUpdates;
	private METAPROPERTY_NAME metaPropertyName;
	private final String metaPropertyUri;
	private String metaPropertyId;
	private final String rdfLiteralType;
	private final String language;
	private String labelName = "";
	private SynonymSemanticTypes oldSynonymTypes;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
	private ArrayList<SemanticType> oldType;


	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getSimpleName());

	protected SetMetaPropertyCommand(String id, String model, String worksheetId,
			String hNodeId, METAPROPERTY_NAME metaPropertyName,
			String metaPropertyUri, String metaPropertyId, 
			boolean trainAndShowUpdates,
			String rdfLiteralType, String language, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.metaPropertyName = metaPropertyName;
		this.metaPropertyUri = metaPropertyUri;
		this.metaPropertyId = metaPropertyId;
		this.rdfLiteralType = rdfLiteralType;
		this.language = language;

		addTag(CommandTag.SemanticType);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set Semantic Type";
	}

	@Override
	public String getDescription() {
		return labelName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		logCommand(logger, workspace);
		try {
			HNode hn = workspace.getFactory().getHNode(hNodeId);
			labelName = hn.getAbsoluteColumnName(workspace.getFactory());
		}catch(Exception e) {

		}
		/*** Get the Alignment for this worksheet ***/
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		OntologyManager ontMgr = workspace.getOntologyManager();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontMgr.getContextId());
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		if (alignment == null) {
			alignment = new Alignment(ontMgr);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		/** Check if a semantic type already exists for the column **/
		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		columnNode.setRdfLiteralType(rdfLiteralType);
		columnNode.setLanguage(language);
		List<LabeledLink> columnNodeIncomingLinks = alignment.getGraphBuilder().getIncomingLinks(columnNode.getId());
		if (columnNodeIncomingLinks != null) { 
			for(LabeledLink oldIncomingLinkToColumnNode : columnNodeIncomingLinks) {
				Node oldDomainNode = oldIncomingLinkToColumnNode.getSource();
				alignment.removeLink(oldIncomingLinkToColumnNode.getId());
				if (alignment.isNodeIsolatedInTree(oldDomainNode.getId()))
					alignment.removeNode(oldDomainNode.getId());
			}
		}

		SemanticType newType = null;
		if(metaPropertyId.endsWith(" (add)"))
			metaPropertyId = metaPropertyId.substring(0, metaPropertyId.length()-5).trim();

		if (metaPropertyName.equals(METAPROPERTY_NAME.isUriOfClass)) {
			Node classNode = alignment.getNodeById(metaPropertyId);

			if (classNode == null) {
				Label classNodeLabel = ontMgr.getUriLabel(metaPropertyUri);
				if (classNodeLabel == null) {
					String errorMessage = "Error while setting a classLink. MetaPropertyUri '"
							+ metaPropertyUri
							+ "' should be in the Ontology Manager, but it is not.";
					logger.error(errorMessage);
					return new UpdateContainer(new ErrorUpdate(errorMessage));
				}
				classNode = new InternalNode(metaPropertyId, classNodeLabel);
				classNode = alignment.addInternalNode((InternalNode)classNode);
			}

			LabeledLink newLink = alignment.addClassInstanceLink(classNode, columnNode,
					LinkKeyInfo.UriOfInstance);
			alignment.changeLinkStatus(newLink.getId(),
					LinkStatus.ForcedByUser);

			// Create the semantic type object
			newType = new SemanticType(hNodeId,
					ClassInstanceLink.getFixedLabel(), classNode.getLabel(),
					classNode.getId(),
					false,
					SemanticType.Origin.User, 1.0);
		} else if (metaPropertyName
				.equals(METAPROPERTY_NAME.isSpecializationForEdge)) {

			LabeledLink propertyLink = alignment.getLinkById(metaPropertyId);
			Node classInstanceNode = alignment.getNodeById(LinkIdFactory
					.getLinkSourceId(metaPropertyId));

			if(propertyLink == null && this.isExecutedInBatch()) {
				//Try to add the link, it might not exist beacuse of the batch
				Node targetNode = alignment.getNodeById(LinkIdFactory.getLinkTargetId(metaPropertyId));
				Label linkLabel = new Label(LinkIdFactory.getLinkUri(metaPropertyId));
				LabeledLink newLink = alignment.addObjectPropertyLink(classInstanceNode,
						targetNode, linkLabel);
				alignment.changeLinkStatus(newLink.getId(),
						LinkStatus.ForcedByUser);
				propertyLink = alignment.getLinkById(metaPropertyId);
			}

			if (propertyLink == null) {
				String errorMessage = "Error while specializing a link. The DefaultLink '"
						+ metaPropertyId
						+ "' should already be in the alignment, but it is not.";
				logger.error(errorMessage);
				return new UpdateContainer(new ErrorUpdate(errorMessage));
			}

			if (propertyLink instanceof DataPropertyLink) {
				String targetHNodeId = ((ColumnNode) propertyLink.getTarget())
						.getHNodeId();
				LabeledLink newLink = alignment.addDataPropertyOfColumnLink(classInstanceNode,
						columnNode, targetHNodeId, propertyLink.getId());
				alignment.changeLinkStatus(newLink.getId(),
						LinkStatus.ForcedByUser);

				// Create the semantic type object
				newType = new SemanticType(hNodeId,
						DataPropertyOfColumnLink.getFixedLabel(),
						classInstanceNode.getLabel(), 
						classInstanceNode.getId(),
						false,
						SemanticType.Origin.User,
						1.0);
			} else if (propertyLink instanceof ObjectPropertyLink) {
				LabeledLink newLink = alignment.addObjectPropertySpecializationLink(
						classInstanceNode, columnNode, propertyLink.getId());
				alignment.changeLinkStatus(newLink.getId(),
						LinkStatus.ForcedByUser);

				// Create the semantic type object
				newType = new SemanticType(hNodeId,
						ObjectPropertySpecializationLink.getFixedLabel(),
						classInstanceNode.getLabel(), 
						classInstanceNode.getId(),
						false,
						SemanticType.Origin.User,
						1.0);
			}

		} else if (metaPropertyName.equals(METAPROPERTY_NAME.isSubclassOfClass)) {
			Node classNode = alignment.getNodeById(metaPropertyId);

			if (classNode == null) {
				Label classNodeLabel = ontMgr.getUriLabel(metaPropertyUri);
				if (classNodeLabel == null) {
					String errorMessage = "Error while setting an advances subclass. MetaPropertyValue '"
							+ metaPropertyUri
							+ "' should be in the Ontology Manager, but it is not.";
					logger.error(errorMessage);
					return new UpdateContainer(new ErrorUpdate(errorMessage));
				}
				classNode = new InternalNode(metaPropertyId, classNodeLabel);
				classNode = alignment.addInternalNode((InternalNode)classNode);
			}
			LabeledLink newLink = alignment.addColumnSubClassOfLink(classNode, columnNode);
			alignment.changeLinkStatus(newLink.getId(),
					LinkStatus.ForcedByUser);

			// Create the semantic type object
			newType = new SemanticType(hNodeId,
					ColumnSubClassLink.getFixedLabel(), classNode.getLabel(), classNode.getId(), false,
					SemanticType.Origin.User, 1.0);
		}

		List<SemanticType> userSemanticTypes = columnNode.getUserSemanticTypes();
		boolean duplicateSemanticType = false;
		if (userSemanticTypes != null) {
			for (SemanticType st : userSemanticTypes) {
				if (st.getModelLabelString().equalsIgnoreCase(newType.getModelLabelString())) {
					duplicateSemanticType = true;
					break;
				}
			}
		}
		if (!duplicateSemanticType) {
			columnNode.unassignUserTypes();
			columnNode.assignUserType(newType);
		}
		
		// Update the alignment
		if(!this.isExecutedInBatch())
			alignment.align();
		else if (modelingConfiguration.getPredictOnApplyHistory()) {
			if (columnNode.getLearnedSemanticTypes() == null) {
				// do this only one time: if user assigns a semantic type to the column, 
				// and later clicks on Set Semantic Type button, we should not change the initially learned types 
				logger.debug("adding learned semantic types to the column " + hNodeId);
				columnNode.setLearnedSemanticTypes(
						new SemanticTypeUtil().getColumnSemanticSuggestions(workspace, worksheet, columnNode, 4, selection));
				if (columnNode.getLearnedSemanticTypes().isEmpty()) {
					logger.info("no semantic type learned for the column " + hNodeId);
				}
			}
		}

		UpdateContainer c = new UpdateContainer();

		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(
				hNodeId);

		oldSynonymTypes = worksheet.getSemanticTypes()
				.getSynonymTypesForHNodeId(newType.getHNodeId());

		// Update the SemanticTypes data structure for the worksheet
		worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
		worksheet.getSemanticTypes().addType(newType);

		// Update the synonym semanticTypes
		// worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(),
		// newSynonymTypes);

		ArrayList<SemanticType> newTypes = new ArrayList<>();
		newTypes.add(newType);
		if ((!this.isExecutedInBatch() && trainAndShowUpdates) ||
				(this.isExecutedInBatch() && modelingConfiguration.getTrainOnApplyHistory())) {
			new SemanticTypeUtil().trainOnColumn(workspace, worksheet, newTypes, selection);
		}
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (oldType == null) {
			worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
		} else {
			worksheet.getSemanticTypes().setType(oldType);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(
					hNodeId, oldSynonymTypes);
		}

		// Replace the current alignment with the old alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		AlignmentManager.Instance()
		.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		// Get the alignment update if any
		try {
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		} catch (Exception e) {
			logger.error("Error occured while unsetting the semantic type!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while unsetting the semantic type!"));
		}
		return c;
	}

	@Override
	protected JSONObject getArgsJSON(Workspace workspace) {
		JSONObject args = new JSONObject();
		try {
			args.put("command", getTitle())
			.put(Arguments.metaPropertyName.name(), metaPropertyName)
			.put(Arguments.metaPropertyId.name(), metaPropertyId)
			.put(Arguments.metaPropertyUri.name(), metaPropertyUri)
			.put(Arguments.worksheetId.name(),
					formatWorsheetId(workspace, worksheetId))
					.put(Arguments.hNodeId.name(),
							formatHNodeId(workspace, hNodeId));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return args;
	}
	
	@Override
	public Set<String> getInputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}

	@Override
	public Set<String> getOutputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}
}
