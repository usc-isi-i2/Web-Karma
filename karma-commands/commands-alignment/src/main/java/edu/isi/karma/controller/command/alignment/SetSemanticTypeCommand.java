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

import java.util.*;

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
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommandFactory.Arguments;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;

public class SetSemanticTypeCommand extends WorksheetSelectionCommand {

	private final String hNodeId;
	private boolean trainAndShowUpdates;
	private String rdfLiteralType;
	private String language;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private SynonymSemanticTypes oldSynonymTypes;
	private JSONArray typesArr;
	private SynonymSemanticTypes newSynonymTypes;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
	private String labelName = "";
	private ArrayList<SemanticType> oldType;
	private boolean hasProvenanceType;
	
	protected SetSemanticTypeCommand(String id, String model, String worksheetId, String hNodeId,
									 JSONArray typesArr, boolean trainAndShowUpdates,
									 String rdfLiteralType, String language,
									 String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.typesArr = typesArr;
		this.rdfLiteralType = rdfLiteralType;
		this.language = language;
		this.hasProvenanceType = false;
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
		/*** Get the Alignment for this worksheet ***/
		inputColumns.clear();
		outputColumns.clear();
		inputColumns.add(hNodeId);
		outputColumns.add(hNodeId);
		try {
			HNode hn = workspace.getFactory().getHNode(hNodeId);
			labelName = hn.getAbsoluteColumnName(workspace.getFactory()); //hn.getColumnName();
		}catch(Exception e) {

		}
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		OntologyManager ontMgr = workspace.getOntologyManager();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontMgr.getContextId());
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(ontMgr);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>)alignment.getGraph().clone();

		ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
		columnNode.setRdfLiteralType(rdfLiteralType);
		columnNode.setLanguage(language);
		
		//Delete Old Semantic Types
		worksheet.getSemanticTypes().unassignColumnSemanticType(hNodeId);
		columnNode.unassignUserTypes();
		
		// Remove links from alignment
		List<LabeledLink> columnNodeIncomingLinks = alignment.getGraphBuilder().getIncomingLinks(columnNode.getId());
		if (columnNodeIncomingLinks != null) { 
			for(LabeledLink oldIncomingLinkToColumnNode : columnNodeIncomingLinks) {
				Node oldDomainNode = oldIncomingLinkToColumnNode.getSource();
				alignment.removeLink(oldIncomingLinkToColumnNode.getId());
				if (alignment.isNodeIsolatedInTree(oldDomainNode.getId()))
					alignment.removeNode(oldDomainNode.getId());
			}
		}
		
		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(hNodeId);
		oldSynonymTypes = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(hNodeId);
		
		//Remove duplicates from typesSet. Keep the last ones
		HashSet<String> typesSet = new HashSet<>();
		JSONArray newTypesArr = new JSONArray();
		for (int j = typesArr.length()-1; j >=0; j--) {
			JSONObject type = typesArr.getJSONObject(j);
			String typeKey = type.getString(ClientJsonKeys.FullType.name()) + "-" + type.getString(ClientJsonKeys.DomainId.name());
			if(!typesSet.contains(typeKey)) {
				typesSet.add(typeKey);
				newTypesArr.put(type);
			}
		}
		this.typesArr = newTypesArr;
		
		/*** Preprocess provenance types ***/
		JSONArray provTypes = new JSONArray();
		for (int i = 0; i < typesArr.length(); i++) {
			try {
				JSONObject type = typesArr.getJSONObject(i);
				boolean isProvenance = false;
				if(type.has(ClientJsonKeys.isProvenance.name()))
					isProvenance = type.getBoolean(ClientJsonKeys.isProvenance.name());
				
				if(isProvenance) {
					this.hasProvenanceType = true;
					Set<Node> internalNodes = alignment.getNodesByType(NodeType.InternalNode);
					if(internalNodes != null) {
						for(Node internalNode : internalNodes) {
							String nodeId = internalNode.getId();
							Set<LabeledLink> inLinks = alignment.getIncomingLinksInTree(nodeId);
							Set<LabeledLink> outLinks = alignment.getOutgoingLinksInTree(nodeId);
							if((inLinks != null && inLinks.size() > 0) 
									|| (outLinks != null && outLinks.size() > 0)) {
								String[] names = (String[]) type.keySet().toArray(new String[0]);
								JSONObject newType = new JSONObject(type, names);
								newType.put(ClientJsonKeys.DomainId.name(), internalNode.getId());
								newType.put(ClientJsonKeys.DomainUri.name(), internalNode.getLabel().getUri());
								String newTypeStr = newType.getString(ClientJsonKeys.FullType.name()) + "-" + newType.getString(ClientJsonKeys.DomainId.name());
								if(!typesSet.contains(newTypeStr)) {
									typesSet.add(newTypeStr);
									provTypes.put(newType);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				logger.error("JSON Exception occured", e);
			}
		}
		//Add the new provenance types to the typesArr 
		for(int i=0; i<provTypes.length(); i++)
			typesArr.put(provTypes.getJSONObject(i));
		
		//Add the new types back into the inputParameters
		JSONArray inputParams = new JSONArray(this.getInputParameterJson());
		HistoryJsonUtil.setArgumentValue(Arguments.SemanticTypesArray.name(), typesArr, inputParams);
		this.setInputParameterJson(inputParams.toString());
		
		/*** Add the appropriate nodes and links in alignment graph ***/
		ArrayList<SemanticType> typesList = new ArrayList<>();
		ArrayList<SemanticType> synonymTypesList = new ArrayList<>();
		for (int i = 0; i < typesArr.length(); i++) {
			try {
				LabeledLink newLink;
				JSONObject type = typesArr.getJSONObject(i);
				
				String sourceId = "";
				if(type.has(ClientJsonKeys.DomainId.name()))
					sourceId = type.getString(ClientJsonKeys.DomainId.name());
				
				String edgeUri = "";
				if(type.has(ClientJsonKeys.FullType.name()))
					edgeUri = type.getString(ClientJsonKeys.FullType.name());
				
				if (sourceId.trim().isEmpty()) {
					logger.error("domain id is emty");
					return new UpdateContainer(new ErrorUpdate("" +
							"Error occured while setting semantic type!"));
				}
				if(sourceId.endsWith(" (add)")) // for backward compatibility with previous models 
					sourceId = sourceId.substring(0, sourceId.length()-5).trim();


				if (edgeUri.trim().isEmpty()) {
					logger.error("fulltype is emty");
					return new UpdateContainer(new ErrorUpdate("" +
							"Error occured while setting semantic type!"));
				}
				
				Label linkLabel = ontMgr.getUriLabel(edgeUri);
				if (linkLabel == null) {
					logger.error("link label cannot be found in the ontology.");
					return new UpdateContainer(new ErrorUpdate("" +
							"Error occured while setting semantic type!"));					
				}

				boolean isProvenance = false;
				if(type.has(ClientJsonKeys.isProvenance.name()))
					isProvenance = type.getBoolean(ClientJsonKeys.isProvenance.name());
				
				Node source = alignment.getNodeById(sourceId);
				if (source == null) {
					
					String sourceUri = "";
					if(type.has(ClientJsonKeys.DomainUri.name())) 
						sourceUri = type.getString(ClientJsonKeys.DomainUri.name());
						
					if (sourceUri.trim().isEmpty()) {
						logger.error("source uri is emty");
						return new UpdateContainer(new ErrorUpdate("" +
								"Error occured while setting semantic type!"));
					}

					Label edgelabel = ontMgr.getUriLabel(sourceUri);
					source = new InternalNode(sourceId, edgelabel);
					source = alignment.addInternalNode((InternalNode)source);
					if (source == null) {
						logger.error("could not add the source " + sourceId + " to the graph.");
						return new UpdateContainer(new ErrorUpdate("" +
								"Error occured while setting semantic type!"));						
					}
				}
				newLink = alignment.addDataPropertyLink(source, columnNode, linkLabel, isProvenance);
				SemanticType newType = new SemanticType(hNodeId, linkLabel, source.getLabel(), source.getId(),
											isProvenance,
											SemanticType.Origin.User, 1.0);
				
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
				if (!duplicateSemanticType)
					columnNode.assignUserType(newType);

				if(newLink != null) {
					alignment.changeLinkStatus(newLink.getId(),
							LinkStatus.ForcedByUser);
				}
				
				worksheet.getSemanticTypes().addType(newType);
				typesList.add(newType);

			} catch (JSONException e) {
				logger.error("JSON Exception occured", e);
			}
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
		if (synonymTypesList.size() > 0) {
			// Update the synonym semanticTypes
			newSynonymTypes = new SynonymSemanticTypes(synonymTypesList);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(hNodeId, newSynonymTypes);
		}

		if ((!this.isExecutedInBatch() && trainAndShowUpdates) ||
				(this.isExecutedInBatch() && modelingConfiguration.getTrainOnApplyHistory())) {
			new SemanticTypeUtil().trainOnColumn(workspace, worksheet, typesList, selection);
		}


		c.append(this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
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
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(hNodeId, oldSynonymTypes);
		}

		// Replace the current alignment with the old alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
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
	public Set<String> getInputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}

	@Override
	public Set<String> getOutputColumns() {
		return new HashSet<>(Arrays.asList(hNodeId));
	}

	public boolean hasProvenanceType() {
		return this.hasProvenanceType;
	}
}
