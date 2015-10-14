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
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
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
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;

public class ChangeNodeCommand extends WorksheetSelectionCommand {
	
	private final String alignmentId;
	private final String oldNodeId;
	private final String newNodeId;
	private final String newNodeUri;
	
	private String description;
	private static Logger logger = LoggerFactory.getLogger(ChangeNodeCommand.class);
	
	public enum JsonKeys {
		oldNodeId, newNodeUri, newNodeId
	}

	enum Arguments {
		alignmentId, worksheetId, oldNodeId, newNodeUri, newNodeId
	}
	
	public ChangeNodeCommand(String id, String model, String worksheetId,
			String alignmentId, String oldNodeId, String newNodeId, String newNodeUri, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.alignmentId = alignmentId;
		this.oldNodeId = oldNodeId;
		this.newNodeId = newNodeId;
		this.newNodeUri = newNodeUri;
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Change Node";
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		OntologyManager ontMgr = workspace.getOntologyManager();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontMgr.getContextId());
		
		// First delete the links that are not present in newEdges and present
		// in intialEdges
		try {
			Set<LabeledLink> incomingLinks = new HashSet<>();
			Set<LabeledLink> outgoingLinks = new HashSet<>();
			Node newNode = alignment.getNodeById(newNodeId);
			Node oldNode = alignment.getNodeById(oldNodeId);
			
			if(newNode == null)
				newNode = alignment.addInternalNode(new InternalNode(newNodeId, new Label(newNodeUri)));
			if(this.isExecutedInBatch()) {
				incomingLinks.addAll(alignment.getCurrentIncomingLinksToNode(oldNodeId));
				outgoingLinks.addAll(alignment.getCurrentOutgoingLinksToNode(oldNodeId));
			} else {
				incomingLinks.addAll(alignment.getIncomingLinksInTree(oldNodeId));
				outgoingLinks.addAll(alignment.getOutgoingLinksInTree(oldNodeId));
			}
			
			//Change all incoming links to point to newNode
			for(LabeledLink link : incomingLinks) {
				logger.info("CHange incoming link: " + link.getId());
				alignment.removeLink(link.getId());
				
				String linkId = LinkIdFactory.getLinkId(link.getUri(), link.getSource().getId(), newNodeId);
				LabeledLink newLink = alignment.getLinkById(linkId);
				if (newLink != null) {
					alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
				} else {
					Label linkLabel = ontMgr.getUriLabel(link.getUri());
					newLink = alignment.addObjectPropertyLink(link.getSource(),
							newNode, linkLabel);
					linkId = newLink.getId();
				}	
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
				logger.info("Added link: " + linkId);
			}
			
			//Change all outgoing links to point to newNode
			for(LabeledLink link : outgoingLinks) {
				logger.info("CHange outgoing link: " + link.getId());
				alignment.removeLink(link.getId());
				
				String linkId = LinkIdFactory.getLinkId(link.getUri(), newNodeId, link.getTarget().getId());
				LabeledLink newLink = alignment.getLinkById(linkId);
				if (newLink != null) {
					alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
				} else {
					Label linkLabel = ontMgr.getUriLabel(link.getUri());
					if(link.getTarget() instanceof ColumnNode) {
						ColumnNode columnNode = (ColumnNode)link.getTarget();
						
						newLink = alignment.addDataPropertyLink(newNode,
							columnNode, linkLabel);
						
						SemanticType newType = new SemanticType(columnNode.getId(), 
													linkLabel, newNode.getLabel(), SemanticType.Origin.User, 1.0);
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
						
						if(this.isExecutedInBatch() && modelingConfiguration.getPredictOnApplyHistory()) {
							if (columnNode.getLearnedSemanticTypes() == null) {
								// do this only one time: if user assigns a semantic type to the column, 
								// and later clicks on Set Semantic Type button, we should not change the initially learned types 
								logger.debug("adding learned semantic types to the column " + columnNode.getId());
								columnNode.setLearnedSemanticTypes(
										new SemanticTypeUtil().getColumnSemanticSuggestions(workspace, worksheet, columnNode, 4, selection));
								if (columnNode.getLearnedSemanticTypes().isEmpty()) {
									logger.info("no semantic type learned for the column " + columnNode.getId());
								}
							}
						}
						
						if (newType != null) {
							// Update the SemanticTypes data structure for the worksheet
							worksheet.getSemanticTypes().addType(newType);

							// Update the synonym semanticTypes
							SynonymSemanticTypes newSynonymTypes = new SynonymSemanticTypes(new ArrayList<SemanticType>());
							worksheet.getSemanticTypes().addSynonymTypesForHNodeId(newType.getHNodeId(), newSynonymTypes);
						}

						if ((!this.isExecutedInBatch()) ||
								(this.isExecutedInBatch() && modelingConfiguration.getTrainOnApplyHistory())) {
							new SemanticTypeUtil().trainOnColumn(workspace, worksheet, newType, selection);
						}
					} else {
						newLink = alignment.addObjectPropertyLink(newNode,
							link.getTarget(), linkLabel);
					}
					linkId = newLink.getId();
				}	
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser); 
				logger.info("Added link: " + linkId);
			}
			
			this.description = oldNode.getDisplayId() + " to " + newNode.getDisplayId();
			
			if(!this.isExecutedInBatch())
				alignment.align();
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace);
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		//SInce undo is no longer allowed in UI, no logic required here/
		UpdateContainer c = new UpdateContainer();
		return c;
	}

	@Override
	protected JSONObject getArgsJSON(Workspace workspace) {
		JSONObject args = new JSONObject();
		try {
			args.put("command", getTitle())
					.put(Arguments.alignmentId.name(), alignmentId)
					.put(Arguments.oldNodeId.name(), oldNodeId)
					.put(Arguments.worksheetId.name(),
							formatWorsheetId(workspace, worksheetId))
					.put(Arguments.newNodeId.name(), newNodeId)
					.put(Arguments.newNodeUri.name(), newNodeUri);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return args;
	}
}
