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

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeTrainingThread;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.ClientJsonKeys;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;

public class SetSemanticTypeCommand extends Command {

	private final String hNodeId;
	private final String worksheetId;
	private final boolean trainAndShowUpdates;
	private final String rdfLiteralType;
	private CRFColumnModel oldColumnModel;
	private SynonymSemanticTypes oldSynonymTypes;
	private JSONArray typesArr;
	private SynonymSemanticTypes newSynonymTypes;
	private final boolean isPartOfKey;
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, Link> oldGraph;
//	private Link newLink;
	
	private SemanticType oldType;
	private SemanticType newType;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	protected SetSemanticTypeCommand(String id, String worksheetId, String hNodeId, 
			boolean isPartOfKey, JSONArray typesArr, boolean trainAndShowUpdates, String rdfLiteralType) {
		super(id);
		this.hNodeId = hNodeId;
		this.worksheetId = worksheetId;
		this.isPartOfKey = isPartOfKey;
		this.trainAndShowUpdates = trainAndShowUpdates;
		this.typesArr = typesArr;
		this.rdfLiteralType = rdfLiteralType;

		addTag(CommandTag.Modeling);
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
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		/*** Get the Alignment for this worksheet ***/
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		OntologyManager ontMgr = workspace.getOntologyManager();
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(ontMgr);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
		
		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, Link>)alignment.getGraph().clone();
		
		/*** Add the appropriate nodes and links in alignment graph ***/
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		for (int i = 0; i < typesArr.length(); i++) {
			try {
				JSONObject type = typesArr.getJSONObject(i);
				
				// For property semantic types, domain uri goes to "domainValue" and link uri goes to "fullTypeValue".
				// For class semantic type, class uri goes "fullTypeValue" and "domainValue" is empty.
				String domainValue = type.getString(ClientJsonKeys.Domain.name());
				String fullTypeValue = type.getString(ClientJsonKeys.FullType.name());
//				System.out.println("FULL TYPE:" + type.getString(ClientJsonKeys.FullType.name()));
//				System.out.println("Domain: " + type.getString(ClientJsonKeys.Domain.name()));
				
				// Look if the domain value exists. If it exists, then it is a domain of a data property. If not
				// then the value in FullType has the the value which indicates if a new class instance is needed
				// or an existing class instance should be used (this is the case when just the class is chosen as a sem type).
//				Label domainName = null;
				
				boolean isClassSemanticType = false;
				boolean semanticTypeAlreadyExists = false;
				Node domain = null;
				String domainUriOrId;
				Label linkLabel;
				
				// if domain value is empty, semantic type is a class semantic type
				if (domainValue.equals("")) {
					isClassSemanticType = true;
					domainUriOrId = fullTypeValue;
					linkLabel = ClassInstanceLink.getFixedLabel();
				} else {
					isClassSemanticType = false;
					domainUriOrId = domainValue;
					linkLabel = ontMgr.getUriLabel(fullTypeValue);
					if (linkLabel == null) {
						logger.error("URI/ID does not exist in the ontology or model: " + fullTypeValue);
						continue;
					}
				}
				
				domain = alignment.getNodeById(domainUriOrId);
				if (domain == null) {
					Label label = ontMgr.getUriLabel(domainUriOrId);
//					if (label == null) {
//						logger.error("URI/ID does not exist in the ontology or model: " + domainUriOrId);
//						continue;
//					}
					if (label == null) {
						int len = domainValue.length(); 
						if ((len > 1) && Character.isDigit(domainValue.charAt(len-1))) {
							String newDomainValue = domainValue.substring(0, len-1);
							label = ontMgr.getUriLabel(newDomainValue);
						}
						// If still node is not found
						if (label == null) {
							logger.error("No graph node found for the node: " + domainValue);
							return new UpdateContainer(new ErrorUpdate("" +
									"Error occured while setting semantic type!"));
						}
					}
					domain = alignment.addInternalNode(label);
				}
					
				// Check if a semantic type already exists for the column
				ColumnNode columnNode = alignment.getColumnNodeByHNodeId(hNodeId);
				columnNode.setRdfLiteralType(rdfLiteralType);
				List<Link> columnNodeIncomingLinks = alignment.getIncomingLinks(columnNode.getId());
				Link oldIncomingLinkToColumnNode = null;
				Node oldDomainNode = null;
				if (columnNodeIncomingLinks != null && !columnNodeIncomingLinks.isEmpty()) { // SemanticType already assigned
					semanticTypeAlreadyExists = true;
					oldIncomingLinkToColumnNode = columnNodeIncomingLinks.get(0);
					oldDomainNode = oldIncomingLinkToColumnNode.getSource();
				}

				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					
					if (isClassSemanticType) {
						LinkKeyInfo keyInfo = isPartOfKey ? LinkKeyInfo.PartOfKey : LinkKeyInfo.None;
						if (semanticTypeAlreadyExists && oldDomainNode == domain) {
							// do nothing;
						} else if (semanticTypeAlreadyExists) {
							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
//							alignment.removeNode(oldDomainNode.getId());
							alignment.addClassInstanceLink(domain, columnNode, keyInfo);
						} else {
							alignment.addClassInstanceLink(domain, columnNode, keyInfo);
						}
					} 
					// Property semantic type
					else {

						// When only the link changes between the class node and the internal node (domain)
						if (semanticTypeAlreadyExists && oldDomainNode == domain) {
							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
							alignment.addDataPropertyLink(domain, columnNode, linkLabel, isPartOfKey);
						}
						// When there was an existing semantic type and the new domain is a new node in the graph and semantic type already existed 
						else if (semanticTypeAlreadyExists) {
							alignment.removeLink(oldIncomingLinkToColumnNode.getId());
//							alignment.removeNode(oldDomainNode.getId());
							alignment.addDataPropertyLink(domain, columnNode, linkLabel, isPartOfKey);
						} else {
							alignment.addDataPropertyLink(domain, columnNode, linkLabel, isPartOfKey);
						}						
					}
				} else { // Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId, linkLabel, domain.getLabel(), SemanticType.Origin.User, 1.0,isPartOfKey);
					typesList.add(synType);
				}
				
				// Update the alignment
				alignment.align();
				// Create the semantic type object
				newType = new SemanticType(hNodeId, linkLabel, domain.getLabel(), SemanticType.Origin.User, 1.0, isPartOfKey);
//				newType = new SemanticType(hNodeId, classNode.getLabel(), null, SemanticType.Origin.User, 1.0,isPartOfKey);
				columnNode.setUserSelectedSemanticType(newType);

			} catch (JSONException e) {
				logger.error("JSON Exception occured", e);
			}
		}
		
		UpdateContainer c = new UpdateContainer();
		CRFModelHandler crfModelHandler = workspace.getCrfModelHandler();

		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeForHNodeId(hNodeId);
		oldColumnModel = worksheet.getCrfModel().getModelByHNodeId(hNodeId);
		oldSynonymTypes = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(hNodeId);

		if (newType != null) {
			// Update the SemanticTypes data structure for the worksheet
			worksheet.getSemanticTypes().addType(newType);

			// Update the synonym semanticTypes
			newSynonymTypes = new SynonymSemanticTypes(typesList);
			worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), newSynonymTypes);
		}

		// Identify the outliers if the semantic type exists in the crfmodel
//		List<String> existingLabels = new ArrayList<String>();
//		crfModelHandler.getLabels(existingLabels);
//		if (existingLabels.contains(newType.getCrfModelLabelString())) {
//			identifyOutliers(worksheet, vWorkspace, crfModelHandler, newType);
//			c.add(new TagsUpdate());
//		}
		
		if(trainAndShowUpdates) {
			c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
			try {
				// Add the visualization update
				c.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));
			} catch (Exception e) {
				logger.error("Error occured while setting the semantic type!", e);
				return new UpdateContainer(new ErrorUpdate(
						"Error occured while setting the semantic type!"));
			}
			
			
			// Train the semantic type in a separate thread
			Thread t = new Thread(new SemanticTypeTrainingThread(crfModelHandler, worksheet, newType));
			t.start();
			
			return c;
			
		} 
		
		// Just do the alignment, no training and update JSON required.
//		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace, vWorksheetId);
//		align.align(false);
		
		return c;
	}

//	private void identifyOutliers(Worksheet worksheet,
//			VWorkspace vWorkspace, CRFModelHandler crfModelHandler, SemanticType type) {
//		Tag outlierTag = vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier);
//		Map<ColumnFeature, Collection<String>> features = new HashMap<ColumnFeature, Collection<String>>();
//		
//		// Get the HNodePath
//		List<HNodePath> allPaths = worksheet.getHeaders().getAllPaths();
//		for (HNodePath currentPath:allPaths) {
//			if (currentPath.getLeaf().getId().equals(hNodeId)) {
////				List<String> columnNamesList = new ArrayList<String>();
////				columnNamesList.add(currentPath.getLeaf().getColumnName());
////				features.put(ColumnFeature.ColumnHeaderName, columnNamesList);
//				String typeString = newType.isClass() ? newType.getType().getUri() : newType.getDomain().getUri() + "|" + newType.getType().getUri();
//				SemanticTypeUtil.identifyOutliers(worksheet, typeString, currentPath, outlierTag, features, crfModelHandler);
//				break;
//			}
//		}
//		
//	}

//	private ColumnNode getColumnNode(Alignment alignment, HNode hNode) {
//		String columnName = hNode.getColumnName();
//		return alignment.getColumnNodeByHNodeId(hNodeId);
//		
//		if (columnNode == null) {
//			columnNode = alignment.addColumnNode(hNodeId, columnName, rdfLiteralType);
//		} else {
//			// Remove old column node if it exists
//			alignment.removeNode(columnNode.getId());
//			columnNode = alignment.addColumnNode(hNodeId, columnName, rdfLiteralType);
//		}
//		return columnNode;
//	}

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

		worksheet.getCrfModel().addColumnModel(newType.getHNodeId(), oldColumnModel);

		// Replace the current alignment with the old alignment
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//		GraphUtil.printGraph(oldAlignment.getGraph());
//		GraphUtil.printGraph(oldAlignment.getSteinerTree());
		
		// Get the alignment update if any
		try {
			c.add(new SemanticTypesUpdate(worksheet, worksheetId, oldAlignment));
			c.add(new AlignmentSVGVisualizationUpdate(worksheetId, oldAlignment));
		} catch (Exception e) {
			logger.error("Error occured while unsetting the semantic type!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while unsetting the semantic type!"));
		}
		return c;
	}
}
