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
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory.Arguments;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
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
	public enum LinkJsonKeys {
		edgeSourceId, edgeId, edgeTargetId, edgeSourceUri, edgeTargetUri, isProvenance
	}

	private static Logger logger = LoggerFactory.getLogger(ChangeInternalNodeLinksCommand.class);
	
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
		return CommandType.notInHistory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		OntologyManager ontMgr = workspace.getOntologyManager();

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		UpdateContainer uc = new UpdateContainer();
		WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
				worksheetId, workspace);
		// First delete the links that are not present in newEdges and present
		// in intialEdges
		try {
			refineInitialEdges(alignment);
			uc.append(deleteLinks(histExecutor, workspace, worksheet, alignment));
			uc.append(addNewLinks(histExecutor, workspace, alignment, ontMgr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return uc;
	}

	private void refineInitialEdges(Alignment alignment) {
		int j = initialEdges.length() - 1;
		while (j >= 0) {
			JSONObject initialEdge = initialEdges.getJSONObject(j);
			String edgeUri = initialEdge.getString(LinkJsonKeys.edgeId.name());
			String sourceId = initialEdge.getString(LinkJsonKeys.edgeSourceId.name());
			String targetId = initialEdge.getString(LinkJsonKeys.edgeTargetId.name());
			String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
			if (alignment.getLinkById(linkId) == null) { // the link is not even in the graph
				initialEdges.remove(j);
			}
			j--;
		}

	}

	private UpdateContainer addNewLinks(WorksheetCommandHistoryExecutor histExecutor, Workspace workspace, Alignment alignment, OntologyManager ontMgr)
			throws JSONException {
		UpdateContainer uc = new UpdateContainer();
		boolean saveToHistory = !this.isExecutedInBatch();
		for (int i = 0; i < newEdges.length(); i++) {
			JSONObject newEdge = newEdges.getJSONObject(i);
			try {
				Command cmd = (new AddLinkCommandFactory()).createCommand(worksheetId, alignmentId, newEdge, model, workspace);
				cmd.setExecutedInBatch(this.isExecutedInBatch());
				uc.append(workspace.getCommandHistory().doCommand(cmd, workspace, saveToHistory && i==newEdges.length()-1));
			} catch(CommandException e) {
				logger.error("Error adding a new link: " + newEdge.toString(), e);
			}
		}
		return uc;
	}

	private UpdateContainer deleteLinks(WorksheetCommandHistoryExecutor histExecutor, Workspace workspace, Worksheet worksheet, Alignment alignment) throws JSONException {
		UpdateContainer uc = new UpdateContainer();

		boolean saveToHistory = !this.isExecutedInBatch();
		for (int i = 0; i < initialEdges.length(); i++) {
			JSONObject initialEdge = initialEdges.getJSONObject(i);
			int newEdgeIdx = -1;
			for (int j = 0; j < newEdges.length(); j++) {
				JSONObject newEdge = newEdges.getJSONObject(j);
				if 	(
						initialEdge.has(LinkJsonKeys.edgeId.name()) && newEdge.has(LinkJsonKeys.edgeId.name()) && 
						initialEdge.getString(LinkJsonKeys.edgeId.name()).equals(newEdge.getString(LinkJsonKeys.edgeId.name()))
							
						&& initialEdge.has(LinkJsonKeys.edgeSourceId.name()) && newEdge.has(LinkJsonKeys.edgeSourceId.name()) && 
						initialEdge.getString(LinkJsonKeys.edgeSourceId.name()).equals(newEdge.getString(LinkJsonKeys.edgeSourceId.name()))

						&& initialEdge.has(LinkJsonKeys.edgeTargetId.name()) && (newEdge.has(LinkJsonKeys.edgeTargetId.name()) && 
						initialEdge.getString(LinkJsonKeys.edgeTargetId.name()).equals(newEdge.getString(LinkJsonKeys.edgeTargetId.name())))
					)
					newEdgeIdx = j;
			}
			
			
			if(newEdgeIdx != -1) {
				JSONObject newEdge = newEdges.getJSONObject(newEdgeIdx);
				boolean newEdgeProv = (newEdge.has(LinkJsonKeys.isProvenance.name())) ? newEdge.getBoolean(LinkJsonKeys.isProvenance.name()): false;
				boolean initialEdgeProv = (initialEdge.has(LinkJsonKeys.isProvenance.name())) ? initialEdge.getBoolean(LinkJsonKeys.isProvenance.name()): false;
				if(initialEdgeProv && !newEdgeProv) {
					//Are removing the provenance, then remove from the "Main" link instead
					String linkId = LinkIdFactory.getLinkId(
							newEdge.getString(LinkJsonKeys.edgeId.name()),
							newEdge.getString(LinkJsonKeys.edgeSourceId.name()),
							newEdge.getString(LinkJsonKeys.edgeTargetId.name()));
					LabeledLink link = alignment.getLinkById(linkId);
					if(!link.isMainProvenanceLink()) {
						link = getMainProvenanceLink(alignment, link);
						if(link != null) {
							initialEdge.put(LinkJsonKeys.edgeSourceId.name(), link.getSource().getId());
							initialEdge.put(LinkJsonKeys.edgeSourceUri.name(), link.getSource().getUri());
							newEdge.put(LinkJsonKeys.edgeSourceId.name(), link.getSource().getId());
							newEdge.put(LinkJsonKeys.edgeSourceUri.name(), link.getSource().getUri());
						}
					}
				}
			}
			
			
			try {
				Command cmd = (new DeleteLinkCommandFactory()).createCommand(worksheetId, alignmentId, initialEdge, model, workspace);
				cmd.setExecutedInBatch(this.isExecutedInBatch());
				uc.append(workspace.getCommandHistory().doCommand(cmd, workspace, saveToHistory && i==initialEdges.length()-1));
				
			} catch(Exception e) {
				logger.error("Error removing a link: " + initialEdge.toString(), e);
			}
			
		}
		return uc;
	}

	private LabeledLink getMainProvenanceLink(Alignment alignment, LabeledLink link) {
		String edgeUri = link.getUri();
		for(LabeledLink otherLink : alignment.getCurrentIncomingLinksToNode(link.getTarget().getId())) {
			if(otherLink.getUri().equals(edgeUri) && otherLink.isMainProvenanceLink())
				return otherLink;
		}
		return null;
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
