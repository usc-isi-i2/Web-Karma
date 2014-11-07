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

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory.Arguments;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;

public class ChangeInternalNodeLinksCommand extends WorksheetCommand {
	
	private final String alignmentId;
	private JSONArray initialEdges;
	private JSONArray newEdges;

	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;

	private StringBuilder descStr = new StringBuilder();

	public enum JsonKeys {
		edgeSourceId, edgeId, edgeTargetId, edgeSourceUri, edgeTargetUri
	}

	public ChangeInternalNodeLinksCommand(String id, String worksheetId,
			String alignmentId, JSONArray initialEdges, JSONArray newEdges) {
		super(id, worksheetId);
		this.alignmentId = alignmentId;
		this.initialEdges = initialEdges;
		this.newEdges = newEdges;

		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Change Links";
	}

	@Override
	public String getDescription() {
		return descStr.toString();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
//		logCommand(logger, workspace);
		// String alignmentId =
		// AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
		// worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		OntologyManager ontMgr = workspace.getOntologyManager();

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		// First delete the links that are not present in newEdges and present
		// in intialEdges
		try {
			deleteLinks(worksheet, alignment);
			addNewLinks(alignment, ontMgr);
			
			if(!this.isExecutedInBatch())
				alignment.align();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace);
	}

	private void addNewLinks(Alignment alignment, OntologyManager ontMgr)
			throws JSONException {
		for (int j = 0; j < newEdges.length(); j++) {
			JSONObject newEdge = newEdges.getJSONObject(j);

			String sourceId = newEdge.has(JsonKeys.edgeSourceId.name()) ? newEdge.getString(JsonKeys.edgeSourceId.name()) : null;
			
			Node sourceNode = null;
			Node targetNode = null;
			if(sourceId != null) {
				if(sourceId.endsWith(" (add)"))
					sourceId = sourceId.substring(0, sourceId.length()-5).trim();
				sourceNode = alignment.getNodeById(sourceId);
			} 
			String targetId = newEdge.has(JsonKeys.edgeTargetId.name()) ? newEdge.getString(JsonKeys.edgeTargetId.name()) : null;
			if(targetId != null) {
				if(targetId.endsWith(" (add)"))
					targetId = targetId.substring(0, targetId.length()-5).trim();
				targetNode = alignment.getNodeById(targetId);;
			}
			String edgeUri = newEdge.getString(JsonKeys.edgeId.name());
			String sourceUri = newEdge.has(JsonKeys.edgeSourceUri.name()) ? newEdge.getString(JsonKeys.edgeSourceUri.name()) : null;
			String targetUri = newEdge.has(JsonKeys.edgeTargetUri.name()) ? newEdge.getString(JsonKeys.edgeTargetUri.name()) : null;
			
			
			if (sourceNode == null && sourceUri != null) {
				sourceNode = alignment.addInternalNode(new Label(sourceUri));
			} 
			
			if (targetNode == null && targetUri != null) {
				targetNode = alignment.addInternalNode(new Label(targetUri));
			} 

			Label linkLabel = ontMgr.getUriLabel(edgeUri);

			LabeledLink newLink = alignment.addObjectPropertyLink(sourceNode,
					targetNode, linkLabel);
			alignment.changeLinkStatus(newLink.getId(),
					LinkStatus.ForcedByUser);


			// Add info to description string
			if (j == newEdges.length() - 1) {
				descStr.append(newLink.getLabel().getDisplayName());
			} else {
				descStr.append(newLink.getLabel().getDisplayName() + ",");
			}
		}
	}

	private void deleteLinks(Worksheet worksheet, Alignment alignment) throws JSONException {
		for (int i = 0; i < initialEdges.length(); i++) {
			JSONObject initialEdge = initialEdges.getJSONObject(i);
//			boolean exists = false;
//
//			for (int j = 0; j < newEdges.length(); j++) {
//				JSONObject newEdge = newEdges.getJSONObject(j);
//				if (newEdge.has(JsonKeys.edgeSourceId.name()) && (initialEdge.getString(JsonKeys.edgeSourceId.name())
//						.equals(newEdge.getString(JsonKeys.edgeSourceId.name())))
//
//						&& (newEdge.has(JsonKeys.edgeTargetId.name()) && initialEdge.getString(JsonKeys.edgeTargetId.name())
//								.equals(newEdge.getString(JsonKeys.edgeTargetId
//										.name())))
//
//						&& initialEdge.getString(JsonKeys.edgeId.name())
//								.equals(newEdge.getString(JsonKeys.edgeId
//										.name()))) {
//					exists = true;
//				}
//			}

//			if (!exists) {
				String targetId = initialEdge.getString(JsonKeys.edgeTargetId.name());
				String linkId = LinkIdFactory.getLinkId(
						initialEdge.getString(JsonKeys.edgeId.name()),
						initialEdge.getString(JsonKeys.edgeSourceId.name()),
						targetId);

				// alignment.changeLinkStatus(linkId, LinkStatus.Normal);
				alignment.removeLink(linkId);
				
				Node node = alignment.getNodeById(targetId);
				if(node instanceof ColumnNode) {
					ColumnNode cNode = (ColumnNode)node;
					worksheet.getSemanticTypes().unassignColumnSemanticType(cNode.getHNodeId());
				}
//			}
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Revert to the old alignment
		AlignmentManager.Instance()
				.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);

		return this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace);
	}

	@Override
	protected JSONObject getArgsJSON(Workspace workspace) {
		JSONObject args = new JSONObject();
		try {
			args.put("command", getTitle())
					.put(Arguments.alignmentId.name(), alignmentId)
					.put(Arguments.initialEdges.name(), initialEdges)
					.put(Arguments.worksheetId.name(),
							formatWorsheetId(workspace, worksheetId))
					.put(Arguments.newEdges.name(), newEdges);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return args;
	}
}
