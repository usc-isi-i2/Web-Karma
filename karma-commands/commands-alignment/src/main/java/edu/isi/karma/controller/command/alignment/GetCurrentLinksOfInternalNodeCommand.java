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

import java.io.PrintWriter;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class GetCurrentLinksOfInternalNodeCommand extends Command {
	private final String nodeId;
	private final String alignmentId;

	private enum JsonKeys {
		updateType, edgeLabel, edgeId, edgeSource, edgeTarget, 
		edges, direction, edgeSourceId, edgeTargetId, edgeSourceUri, edgeTargetUri,
		edgeSourceRDFSLabel, edgeTargetRDFSLabel
	}
	
	private enum LINK_DIRECTION {
		incoming, outgoing
	}
	
	protected GetCurrentLinksOfInternalNodeCommand(String id, String model, String nodeId, String alignmentId) {
		super(id, model);
		this.nodeId = nodeId;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Get Current Links";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		final Set<LabeledLink> incomingLinks;
		final Set<LabeledLink> outgoingLinks;
		if(this.isExecutedInBatch()) {
			incomingLinks = alignment.getCurrentIncomingLinksToNode(nodeId);
			outgoingLinks = alignment.getCurrentOutgoingLinksToNode(nodeId);
		} else {
			incomingLinks = alignment.getIncomingLinksInTree(nodeId);
			outgoingLinks = alignment.getOutgoingLinksInTree(nodeId);
		}
		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray edgesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "GetCurrentLinks");
					
					/** Add the incoming links **/
					if (incomingLinks != null && !incomingLinks.isEmpty()) {
						for (LabeledLink inLink:incomingLinks) {
								addLink(inLink, LINK_DIRECTION.incoming, edgesArray);
						}
					}
					
					/** Add the outgoing links **/
					if (outgoingLinks != null && !outgoingLinks.isEmpty()) {
						for (LabeledLink outLink:outgoingLinks) {
							if (!(outLink.getTarget() instanceof ColumnNode)) {
								addLink(outLink, LINK_DIRECTION.outgoing, edgesArray);
							}
						}
					}
					
					obj.put(JsonKeys.edges.name(), edgesArray);
					pw.println(obj.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			private void addLink(LabeledLink link, LINK_DIRECTION direction, JSONArray edgesArray) 
					throws JSONException {
				String linkLabel = link.getLabel().getDisplayName();
				
				Node edgeSource = alignment.getNodeById(LinkIdFactory.
						getLinkSourceId(link.getId()));
				Node edgeTarget = alignment.getNodeById(LinkIdFactory.
						getLinkTargetId(link.getId()));
				
				String edgeSourceLabel = edgeSource.getDisplayId();
				String edgeSourceId = edgeSource.getId();
				String edgeSourceUri = edgeSource.getUri();
				String edgeTargetLabel = edgeTarget.getDisplayId();
				String edgeTargetId = edgeTarget.getId();
				String edgeTargetUri = edgeTarget.getUri();
				
				Label srcNodeLabel = edgeSource.getLabel();
				if (srcNodeLabel.getUri() !=null && srcNodeLabel.getNs() != null 
						&& srcNodeLabel.getUri().equalsIgnoreCase(srcNodeLabel.getNs())) {
					edgeSourceLabel = edgeSource.getId();
				}
				Label trgNodeLabel = edgeTarget.getLabel();
				if (trgNodeLabel.getUri() !=null && trgNodeLabel.getNs() != null 
						&& trgNodeLabel.getUri().equalsIgnoreCase(trgNodeLabel.getNs())) {
					edgeTargetLabel = edgeTarget.getId();
				}
					
				JSONObject edgeObj = new JSONObject();
				edgeObj.put(JsonKeys.edgeId.name(), link.getLabel().getUri());
				edgeObj.put(JsonKeys.edgeLabel.name(), linkLabel);
				edgeObj.put(JsonKeys.edgeSource.name(), edgeSourceLabel);
				edgeObj.put(JsonKeys.edgeSourceRDFSLabel.name(), srcNodeLabel.getRdfsLabel());
				edgeObj.put(JsonKeys.edgeSourceId.name(), edgeSourceId);
				edgeObj.put(JsonKeys.edgeSourceUri.name(), edgeSourceUri);
				edgeObj.put(JsonKeys.direction.name(), direction.name());
				edgeObj.put(JsonKeys.edgeTargetRDFSLabel.name(), trgNodeLabel.getRdfsLabel());
				edgeObj.put(JsonKeys.edgeTarget.name(), edgeTargetLabel);
				edgeObj.put(JsonKeys.edgeTargetId.name(), edgeTargetId);
				edgeObj.put(JsonKeys.edgeTargetUri.name(), edgeTargetUri);
				edgesArray.put(edgeObj);
			}
		});
		return upd;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
