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
import edu.isi.karma.rep.alignment.InternalNode;
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

	private StringBuilder addDescStr = new StringBuilder();
	private StringBuilder delDescStr = new StringBuilder();

	public enum JsonKeys {
		edgeSourceId, edgeId, edgeTargetId, edgeSourceUri, edgeTargetUri
	}

	public ChangeInternalNodeLinksCommand(String id, String model, String worksheetId,
			String alignmentId, JSONArray initialEdges, JSONArray newEdges) {
		super(id, model, worksheetId);
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
		String addStr = addDescStr.toString();
		String delStr = delDescStr.toString();
		String res = "";
		if(addStr.length() > 0)
			res += "Add: " + addStr;
		if(delStr.length() > 0) {
			if(res.length() > 0)
				res += ", ";
			res += "Delete: " + delStr;
		}
		return res;
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
			refineInitialEdges(alignment);
			deleteLinks(worksheet, alignment);
			addNewLinks(alignment, ontMgr);
			
			if(!this.isExecutedInBatch())
				alignment.align();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace);
	}

	private void refineInitialEdges(Alignment alignment) {
		int j = initialEdges.length() - 1;
		while (j >= 0) {
			JSONObject initialEdge = initialEdges.getJSONObject(j);
			String edgeUri = initialEdge.getString(JsonKeys.edgeId.name());
			String sourceId = initialEdge.getString(JsonKeys.edgeSourceId.name());
			String targetId = initialEdge.getString(JsonKeys.edgeTargetId.name());
			String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
			if (alignment.getLinkById(linkId) == null) { // the link is not even in the graph
				initialEdges.remove(j);
			}
			j--;
		}

	}

	private void addNewLinks(Alignment alignment, OntologyManager ontMgr)
			throws JSONException {
		for (int i = 0; i < newEdges.length(); i++) {
			JSONObject newEdge = newEdges.getJSONObject(i);

			boolean exists = false;

			for (int j = 0; j < initialEdges.length(); j++) {
				JSONObject initialEdge = initialEdges.getJSONObject(j);
				if 	(
						initialEdge.has(JsonKeys.edgeId.name()) && newEdge.has(JsonKeys.edgeId.name()) && 
						initialEdge.getString(JsonKeys.edgeId.name()).equals(newEdge.getString(JsonKeys.edgeId.name()))
							
						&& initialEdge.has(JsonKeys.edgeSourceId.name()) && newEdge.has(JsonKeys.edgeSourceId.name()) && 
						initialEdge.getString(JsonKeys.edgeSourceId.name()).equals(newEdge.getString(JsonKeys.edgeSourceId.name()))

						&& initialEdge.has(JsonKeys.edgeTargetId.name()) && (newEdge.has(JsonKeys.edgeTargetId.name()) && 
						initialEdge.getString(JsonKeys.edgeTargetId.name()).equals(newEdge.getString(JsonKeys.edgeTargetId.name())))
					)
					exists = true;
			}
			
			if (exists) {
				String edgeUri = newEdge.getString(JsonKeys.edgeId.name());
				String sourceId = newEdge.getString(JsonKeys.edgeSourceId.name());
				String targetId = newEdge.getString(JsonKeys.edgeTargetId.name());
				String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
				continue;
			}
			
			String edgeUri = newEdge.getString(JsonKeys.edgeId.name());
			
			String sourceUri = newEdge.has(JsonKeys.edgeSourceUri.name()) ? newEdge.getString(JsonKeys.edgeSourceUri.name()) : null;
			String sourceId = newEdge.has(JsonKeys.edgeSourceId.name()) ? newEdge.getString(JsonKeys.edgeSourceId.name()) : null;
			Node sourceNode = null;
			Label sourceLabel = null;
			if(sourceUri != null)
				sourceLabel = ontMgr.getUriLabel(sourceUri);
			if(sourceId != null) {
				if(sourceId.endsWith(" (add)"))
					sourceId = sourceId.substring(0, sourceId.length()-5).trim();
				sourceNode = alignment.getNodeById(sourceId);
				
				if(sourceNode == null) {
					sourceNode = alignment.addInternalNode(new InternalNode(sourceId, sourceLabel));
				}
			} else if(sourceUri != null){
				sourceNode = alignment.addInternalNode(sourceLabel);
				sourceId = sourceNode.getId();
			}
			
			String targetUri = newEdge.has(JsonKeys.edgeTargetUri.name()) ? newEdge.getString(JsonKeys.edgeTargetUri.name()) : null;
			String targetId = newEdge.has(JsonKeys.edgeTargetId.name()) ? newEdge.getString(JsonKeys.edgeTargetId.name()) : null;
			Label targetLabel = null;
			if(targetUri != null)
				targetLabel = ontMgr.getUriLabel(targetUri);
			Node targetNode = null;
			if(targetId != null) {
				if(targetId.endsWith(" (add)"))
					targetId = targetId.substring(0, targetId.length()-5).trim();
				targetNode = alignment.getNodeById(targetId);
				
				if(targetNode == null) {
					targetNode = alignment.addInternalNode(new InternalNode(targetId, targetLabel));
				}
			} else if(targetUri != null) {
				targetNode = alignment.addInternalNode(targetLabel);
				targetId = targetNode.getId();
			}
			
			String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
			LabeledLink newLink = alignment.getLinkById(linkId);
			if (newLink != null) {
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
			} else {
				Label linkLabel = ontMgr.getUriLabel(edgeUri);
				newLink = alignment.addObjectPropertyLink(sourceNode,
						targetNode, linkLabel);
				linkId = newLink.getId();
			}
				
			alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);


			// Add info to description string
			if (i == newEdges.length() - 1) {
				addDescStr.append(newLink.getLabel().getDisplayName());
			} else {
				addDescStr.append(newLink.getLabel().getDisplayName() + ",");
			}
		}
	}

	private void deleteLinks(Worksheet worksheet, Alignment alignment) throws JSONException {
		String sep = "";
		for (int i = 0; i < initialEdges.length(); i++) {
			JSONObject initialEdge = initialEdges.getJSONObject(i);

			boolean exists = false;

			for (int j = 0; j < newEdges.length(); j++) {
				JSONObject newEdge = newEdges.getJSONObject(j);
				if 	(
						initialEdge.has(JsonKeys.edgeId.name()) && newEdge.has(JsonKeys.edgeId.name()) && 
						initialEdge.getString(JsonKeys.edgeId.name()).equals(newEdge.getString(JsonKeys.edgeId.name()))
							
						&& initialEdge.has(JsonKeys.edgeSourceId.name()) && newEdge.has(JsonKeys.edgeSourceId.name()) && 
						initialEdge.getString(JsonKeys.edgeSourceId.name()).equals(newEdge.getString(JsonKeys.edgeSourceId.name()))

						&& initialEdge.has(JsonKeys.edgeTargetId.name()) && (newEdge.has(JsonKeys.edgeTargetId.name()) && 
						initialEdge.getString(JsonKeys.edgeTargetId.name()).equals(newEdge.getString(JsonKeys.edgeTargetId.name())))
					)
					exists = true;
			}
						
			if (!exists) {
				String targetId = initialEdge.getString(JsonKeys.edgeTargetId.name());
				String linkId = LinkIdFactory.getLinkId(
						initialEdge.getString(JsonKeys.edgeId.name()),
						initialEdge.getString(JsonKeys.edgeSourceId.name()),
						targetId);

				// Add info to description string
				LabeledLink delLink = alignment.getLinkById(linkId);
				if(delLink != null) {
					delDescStr.append(sep + delLink.getLabel().getDisplayName());
					sep = ", ";
				}
				
//				alignment.changeLinkStatus(linkId, LinkStatus.Normal);
				alignment.removeLink(linkId);
				
				Node node = alignment.getNodeById(targetId);
				if(node instanceof ColumnNode) {
					ColumnNode cNode = (ColumnNode)node;
					worksheet.getSemanticTypes().unassignColumnSemanticType(cNode.getHNodeId());
				}
			}
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
