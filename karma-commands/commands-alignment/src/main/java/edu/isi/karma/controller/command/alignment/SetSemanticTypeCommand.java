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
import edu.isi.karma.controller.command.selection.SuperSelection;
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
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;

public class SetSemanticTypeCommand extends WorksheetSelectionCommand {

	private final String hNodeId;
	private boolean trainAndShowUpdates;
	private String rdfLiteralType;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private SynonymSemanticTypes oldSynonymTypes;
	private JSONArray typesArr;
	private SynonymSemanticTypes newSynonymTypes;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
	private String labelName = "";
	private SemanticType oldType;
	private SemanticType newType;

	protected SetSemanticTypeCommand(String id, String model, String worksheetId, String hNodeId,
									 JSONArray typesArr, boolean trainAndShowUpdates,
									 String rdfLiteralType, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.typesArr = typesArr;
		this.rdfLiteralType = rdfLiteralType;

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

		/*** Add the appropriate nodes and links in alignment graph ***/
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		for (int i = 0; i < typesArr.length(); i++) {
			try {
				LabeledLink newLink = null;
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
				
				boolean semanticTypeAlreadyExists = false;

				// Check if a semantic type already exists for the column
				ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
				columnNode.setRdfLiteralType(rdfLiteralType);
				List<LabeledLink> columnNodeIncomingLinks = alignment.getGraphBuilder().getIncomingLinks(columnNode.getId());
				LabeledLink oldIncomingLinkToColumnNode = null;
				Node oldDomainNode = null;
				if (columnNodeIncomingLinks != null && !columnNodeIncomingLinks.isEmpty()) { // SemanticType already assigned
					semanticTypeAlreadyExists = true;
					oldIncomingLinkToColumnNode = columnNodeIncomingLinks.get(0);
					oldDomainNode = oldIncomingLinkToColumnNode.getSource();
				}

				// When only the link changes between the class node and the internal node (domain)
				if (semanticTypeAlreadyExists && oldDomainNode == source) {
					alignment.removeLink(oldIncomingLinkToColumnNode.getId());
					newLink = alignment.addDataPropertyLink(source, columnNode, linkLabel);
				}
				// When there was an existing semantic type and the new domain is a new node in the graph and semantic type already existed 
				else if (semanticTypeAlreadyExists) {
					alignment.removeLink(oldIncomingLinkToColumnNode.getId());
					if (alignment.isNodeIsolatedInTree(oldDomainNode.getId()))
						alignment.removeNode(oldDomainNode.getId());
					newLink = alignment.addDataPropertyLink(source, columnNode, linkLabel);
				} else {
					newLink = alignment.addDataPropertyLink(source, columnNode, linkLabel);
				}		
				
				newType = new SemanticType(hNodeId, linkLabel, source.getLabel(), SemanticType.Origin.User, 1.0);

//				String domainValue;
//				if(type.has(ClientJsonKeys.DomainId.name()))
//					domainValue = type.getString(ClientJsonKeys.DomainId.name());
//				else
//					domainValue = type.getString("Domain"); //For backward compatibility to older models
//				String fullTypeValue = type.getString(ClientJsonKeys.FullType.name());
//
//				boolean isClassSemanticType = false;
//				boolean semanticTypeAlreadyExists = false;
//				Node domain = null;
//				String domainUriOrId;
//				Label linkLabel;
//				
//				// if domain value is empty, semantic type is a class semantic type
//				if (domainValue.equals("")) {
//					isClassSemanticType = true;
//					domainUriOrId = fullTypeValue;
//					linkLabel = ClassInstanceLink.getFixedLabel();
//				} else {
//					isClassSemanticType = false;
//					domainUriOrId = domainValue;
//					linkLabel = ontMgr.getUriLabel(fullTypeValue);
//					if (linkLabel == null) {
//						logger.error("URI/ID does not exist in the ontology or model: " + fullTypeValue);
//						continue;
//					}
//				}
//				
//				if(domainUriOrId.endsWith(" (add)"))
//					domainUriOrId = domainUriOrId.substring(0, domainUriOrId.length()-5).trim();
//				
//				domain = alignment.getNodeById(domainUriOrId);
//				logger.info("Got domain for domainUriOrId:" + domainUriOrId + " ::" + domain);
//				if (domain == null) {
//					Label label = ontMgr.getUriLabel(domainUriOrId);
//					if (label == null) {
//						if(type.has(ClientJsonKeys.DomainUri.name())) {
//							label = new Label(type.getString(ClientJsonKeys.DomainUri.name()));
//						} else {
//							//This part of the code is for backward compatibility. Newer models should have domainUri
//							int len = domainValue.length();
//							if ((len > 1) && Character.isDigit(domainValue.charAt(len-1))) {
//								String newDomainValue = domainValue.substring(0, len-1);
//								label = ontMgr.getUriLabel(newDomainValue);
//							}
//							if (label == null) {
//								logger.error("No graph node found for the node: " + domainValue);
//								return new UpdateContainer(new ErrorUpdate("" +
//								"Error occured while setting semantic type!"));
//							}
//						}
//					}
//					domain = alignment.addInternalNode(label);
//				}
//					
//				// Check if a semantic type already exists for the column
//				ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
//				columnNode.setRdfLiteralType(rdfLiteralType);
//				List<LabeledLink> columnNodeIncomingLinks = alignment.getIncomingLinksInGraph(columnNode.getId());
//				LabeledLink oldIncomingLinkToColumnNode = null;
//				Node oldDomainNode = null;
//				if (columnNodeIncomingLinks != null && !columnNodeIncomingLinks.isEmpty()) { // SemanticType already assigned
//					semanticTypeAlreadyExists = true;
//					oldIncomingLinkToColumnNode = columnNodeIncomingLinks.get(0);
//					oldDomainNode = oldIncomingLinkToColumnNode.getSource();
//				}
//
//				if (true) { //type.getBoolean(ClientJsonKeys.isPrimary.name())) {
//					
//					if (isClassSemanticType) {
//						if (semanticTypeAlreadyExists && oldDomainNode == domain) {
//							newLink = oldIncomingLinkToColumnNode;
//							// do nothing;
//						} else if (semanticTypeAlreadyExists) {
//							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
////							alignment.removeNode(oldDomainNode.getId());
//							newLink = alignment.addClassInstanceLink(domain, columnNode, LinkKeyInfo.None);
//						} else {
//							newLink = alignment.addClassInstanceLink(domain, columnNode, LinkKeyInfo.None);
//						}
//					} 
//					// Property semantic type
//					else {
//
//						// When only the link changes between the class node and the internal node (domain)
//						if (semanticTypeAlreadyExists && oldDomainNode == domain) {
//							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
//							newLink = alignment.addDataPropertyLink(domain, columnNode, linkLabel);
//						}
//						// When there was an existing semantic type and the new domain is a new node in the graph and semantic type already existed 
//						else if (semanticTypeAlreadyExists) {
//							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
////							alignment.removeNode(oldDomainNode.getId());
//							newLink = alignment.addDataPropertyLink(domain, columnNode, linkLabel);
//						} else {
//							newLink = alignment.addDataPropertyLink(domain, columnNode, linkLabel);
//						}						
//					}
//				} 
//				else { // Synonym semantic type
//					SemanticType synType = new SemanticType(hNodeId, linkLabel, domain.getLabel(), SemanticType.Origin.User, 1.0);
//					typesList.add(synType);
//				}
//				
//				// Create the semantic type object
//				newType = new SemanticType(hNodeId, linkLabel, domain.getLabel(), SemanticType.Origin.User, 1.0);
////			newType = new SemanticType(hNodeId, classNode.getLabel(), null, SemanticType.Origin.User, 1.0,isPartOfKey);
				
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

			} catch (JSONException e) {
				logger.error("JSON Exception occured", e);
			}
		}

		UpdateContainer c = new UpdateContainer();

		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(hNodeId);
		oldSynonymTypes = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(hNodeId);

		if (newType != null) {
			// Update the SemanticTypes data structure for the worksheet
			worksheet.getSemanticTypes().addType(newType);

			// Update the synonym semanticTypes
			newSynonymTypes = new SynonymSemanticTypes(typesList);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), newSynonymTypes);
		}

		if ((!this.isExecutedInBatch() && trainAndShowUpdates) ||
				(this.isExecutedInBatch() && modelingConfiguration.getTrainOnApplyHistory())) {
			new SemanticTypeUtil().trainOnColumn(workspace, worksheet, newType, selection);
		}


		c.append(this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (oldType == null) {
			worksheet.getSemanticTypes().unassignColumnSemanticType(newType.getHNodeId());
		} else {
			worksheet.getSemanticTypes().addType(oldType);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), oldSynonymTypes);
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

	public void updateField(SetSemanticTypeCommand command) {
		this.typesArr = new JSONArray(command.typesArr.toString());
		this.trainAndShowUpdates = command.trainAndShowUpdates;
		this.rdfLiteralType = command.rdfLiteralType;

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
