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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class GetAlternativeLinksCommand extends Command {
	private final String nodeId;
	private final String alignmentId;

	private enum JsonKeys {
		updateType, edgeLabel, edgeId, edgeSource, Edges, selected
	}

	public String getNodeId() {
		return nodeId;
	}

	protected GetAlternativeLinksCommand(String id, String nodeId,
			String alignmentId) {
		super(id);
		this.nodeId = nodeId;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Alternative Links";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		final List<Link> links = alignment.getAllPossibleLinksToNode(nodeId);
		final Link currentLink = alignment.getCurrentLinkToNode(nodeId); 

		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray edgesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "GetAlternativeLinks");
					for (Link link : links) {
						
						String linkLabel = link.getLabel().getLocalNameWithPrefix();
						String edgeSourceLabel = "";
						
						Node edgeSource = link.getSource();
//						if(edgeSource.getPrefix() != null && !edgeSource.getPrefix().equals(""))
//							edgeSourceLabel = edgeSource.getPrefix() + ":" + edgeSource.getLocalLabel();
//						else
							edgeSourceLabel = edgeSource.getLocalId();
						
						JSONObject edgeObj = new JSONObject();
						edgeObj.put(JsonKeys.edgeId.name(), link.getId());
						edgeObj.put(JsonKeys.edgeLabel.name(), linkLabel);
						edgeObj.put(JsonKeys.edgeSource.name(),edgeSourceLabel);
//						if (currentLink != null && link.getLabel().getUri().equals(currentLink.getLabel().getUri())
//								&& edgeSource.getLabel().getUri().equals(currentLink.getLabel().getUri())) {
						if (link == currentLink) {
							edgeObj.put(JsonKeys.selected.name(), true);
						} else {
							edgeObj.put(JsonKeys.selected.name(), false);
						}
						edgesArray.put(edgeObj);
					}
					obj.put(JsonKeys.Edges.name(), edgesArray);
					pw.println(obj.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		return upd;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required!
		return null;
	}

}
