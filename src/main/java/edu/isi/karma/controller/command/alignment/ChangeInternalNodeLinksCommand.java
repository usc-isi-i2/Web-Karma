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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory.Arguments;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;

public class ChangeInternalNodeLinksCommand extends Command {
	private final String worksheetId;
	private final String alignmentId;
	private JSONArray initialEdges;
	private JSONArray newEdges;

	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;

	private StringBuilder descStr = new StringBuilder();
	private static Logger logger = LoggerFactory
			.getLogger(ChangeInternalNodeLinksCommand.class);

	private enum JsonKeys {
		edgeSourceId, edgeId, edgeTargetId
	}

	public ChangeInternalNodeLinksCommand(String id, String worksheetId,
			String alignmentId, JSONArray initialEdges, JSONArray newEdges) {
		super(id);
		this.worksheetId = worksheetId;
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
		logCommand(logger, workspace);
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
			deleteLinks(alignment);
			addNewLinks(alignment, ontMgr);
			alignment.align();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return getAlignmentUpdateContainer(alignment, worksheet, workspace);
	}

	private void addNewLinks(Alignment alignment, OntologyManager ontMgr)
			throws JSONException {
		for (int j = 0; j < newEdges.length(); j++) {
			JSONObject newEdge = newEdges.getJSONObject(j);

			String sourceId = newEdge.getString(JsonKeys.edgeSourceId.name());
			String targetId = newEdge.getString(JsonKeys.edgeTargetId.name());
			String edgeUri = newEdge.getString(JsonKeys.edgeId.name());

			String linkId = LinkIdFactory
					.getLinkId(edgeUri, sourceId, targetId);
			LabeledLink newLink = alignment.getLinkById(linkId);
			if (newLink == null) {
				Node sourceNode = alignment.getNodeById(sourceId);
				if (sourceNode == null) {
					String errorMessage = "Error while adding new links: the new link goes FROM node '"
							+ sourceId
							+ "', but this node is NOT in the alignment.";
					logger.error(errorMessage);
				}
				Node targetNode = alignment.getNodeById(targetId);
				if (targetNode == null) {
					String errorMessage = "Error while adding new links: the new link goes TO node '"
							+ targetId
							+ "', but this node is NOT in the alignment.";
					logger.error(errorMessage);
				}
				Label linkLabel = ontMgr.getUriLabel(edgeUri);

				newLink = alignment.addObjectPropertyLink(sourceNode,
						targetNode, linkLabel);
				alignment.changeLinkStatus(newLink.getId(),
						LinkStatus.ForcedByUser);
			} else {
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
			}
			// Add info to description string
			if (j == newEdges.length() - 1) {
				descStr.append(newLink.getLabel().getDisplayName());
			} else {
				descStr.append(newLink.getLabel().getDisplayName() + ",");
			}
		}
	}

	private void deleteLinks(Alignment alignment) throws JSONException {
		for (int i = 0; i < initialEdges.length(); i++) {
			JSONObject initialEdge = initialEdges.getJSONObject(i);
			boolean exists = false;

			for (int j = 0; j < newEdges.length(); j++) {
				JSONObject newEdge = newEdges.getJSONObject(j);
				if ((initialEdge.getString(JsonKeys.edgeSourceId.name())
						.equals(newEdge.getString(JsonKeys.edgeSourceId.name())))

						&& (initialEdge.getString(JsonKeys.edgeTargetId.name())
								.equals(newEdge.getString(JsonKeys.edgeTargetId
										.name())))

						&& initialEdge.getString(JsonKeys.edgeId.name())
								.equals(newEdge.getString(JsonKeys.edgeId
										.name()))) {
					exists = true;
				}
			}

			if (!exists) {
				String linkId = LinkIdFactory.getLinkId(
						initialEdge.getString(JsonKeys.edgeId.name()),
						initialEdge.getString(JsonKeys.edgeSourceId.name()),
						initialEdge.getString(JsonKeys.edgeTargetId.name()));

				// alignment.changeLinkStatus(linkId, LinkStatus.Normal);
				alignment.removeLink(linkId);
			}
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		// Revert to the old alignment
		AlignmentManager.Instance()
				.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);

		// Get the alignment update
		return getAlignmentUpdateContainer(oldAlignment, worksheet, workspace);
	}

	// TODO this is in worksheetcommand
	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, Workspace workspace) {
		// Add the visualization update
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
		c.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));
		return c;
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
