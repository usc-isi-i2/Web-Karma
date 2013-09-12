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

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class ChangeInternalNodeLinksCommand extends Command {
	private final String vWorksheetId;
	private final String alignmentId;
	private JSONArray initialEdges;
	private JSONArray newEdges;
	
	// Required for undo
	private Alignment 	 oldAlignment;
	private DirectedWeightedMultigraph<Node, Link> oldGraph;
	
	private StringBuilder descStr = new StringBuilder();
	private static Logger logger = Logger.getLogger(ChangeInternalNodeLinksCommand.class);
	
	private enum JsonKeys {
		edgeSourceId, edgeId, edgeTargetId
	}
	
	public ChangeInternalNodeLinksCommand(String id, String vWorksheetId,
			String alignmentId, JSONArray initialEdges, JSONArray newEdges) {
		super(id);
		this.vWorksheetId = vWorksheetId;
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		
		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, Link>)alignment.getGraph().clone();
		
		// First delete the links that are not present in newEdges and present in intialEdges
		try {
			deleteLinks(alignment);
			addNewLinks(alignment, ontMgr);
			alignment.align();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return getAlignmentUpdateContainer(alignment, worksheet, vWorkspace);
	}

	private void addNewLinks(Alignment alignment, OntologyManager ontMgr) throws JSONException {
		for (int j=0; j<newEdges.length(); j++) {
			JSONObject newEdge = newEdges.getJSONObject(j);
			
			String sourceId = newEdge.getString(JsonKeys.edgeSourceId.name());
			String targetId = newEdge.getString(JsonKeys.edgeTargetId.name());
			String edgeUri = newEdge.getString(JsonKeys.edgeId.name());
			
			String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
			Link newLink = alignment.getLinkById(linkId); 
			if (newLink == null) {
				Node sourceNode = alignment.getNodeById(sourceId);
				if (sourceNode == null) {
					logger.error("NULL source node! Please notify this error.");
				}
				Node targetNode = alignment.getNodeById(targetId);
				if (targetNode == null) {
					logger.error("NULL target node! Please notify this error.");
				}
				Label linkLabel = ontMgr.getUriLabel(edgeUri);
				
				newLink = alignment.addObjectPropertyLink(sourceNode, targetNode, linkLabel);
				alignment.changeLinkStatus(newLink.getId(), LinkStatus.ForcedByUser);
			} else {
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
			}
			// Add info to description string
			if (j == newEdges.length()-1) {
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
			
			for (int j=0; j<newEdges.length(); j++) {
				JSONObject newEdge = newEdges.getJSONObject(j);
				if ((initialEdge.getString(JsonKeys.edgeSourceId.name())
						.equals(newEdge.getString(JsonKeys.edgeSourceId.name())))
						
					&& (initialEdge.getString(JsonKeys.edgeTargetId.name())
						.equals(newEdge.getString(JsonKeys.edgeTargetId.name())))
						
					&& initialEdge.getString(JsonKeys.edgeId.name())
						.equals(newEdge.getString(JsonKeys.edgeId.name()))) {
					exists = true;
				}
			}
			
			if (!exists) {
				String linkId = LinkIdFactory.getLinkId(initialEdge.getString(JsonKeys.edgeId.name()),
						initialEdge.getString(JsonKeys.edgeSourceId.name()), 
						initialEdge.getString(JsonKeys.edgeTargetId.name()));
				
				alignment.changeLinkStatus(linkId, LinkStatus.Normal);
//				alignment.removeLink(linkId);
			}
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Revert to the old alignment
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);
		
		// Get the alignment update
		return getAlignmentUpdateContainer(oldAlignment, worksheet, vWorkspace);
	}
	
	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, VWorkspace vWorkspace) {
		// Add the visualization update
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(
				vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), alignment));
		return c;
	}

}
