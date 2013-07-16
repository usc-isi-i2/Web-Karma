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
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class GetInternalNodesListOfAlignmentCommand extends Command {
	final private String alignmentId;
	final private INTERNAL_NODES_RANGE range;
	@SuppressWarnings("unused")
	private String propertyName;
	
	private enum JsonKeys {
		updateType, nodeLabel, nodeId, nodes
	}
	
	public enum INTERNAL_NODES_RANGE {
		existingTreeNodes, domainNodesOfProperty, allGraphNodes
	}

	protected GetInternalNodesListOfAlignmentCommand(String id, String alignmentId, 
			INTERNAL_NODES_RANGE range, String propertyName) {
		super(id);
		this.alignmentId = alignmentId;
		this.range = range;
		this.propertyName = propertyName;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Get Internal Nodes List";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		final Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		Set<Node> nodeSet = null;
		if (range == INTERNAL_NODES_RANGE.existingTreeNodes) {
			nodeSet = alignment.getSteinerTree().vertexSet();
		} else if (range == INTERNAL_NODES_RANGE.allGraphNodes) {
			nodeSet = alignment.getGraphNodes();
		}
		
		if (nodeSet == null) {
			nodeSet = new HashSet<Node>();
		}
		final Set<Node> finalNodeSet = nodeSet;
		
		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray nodesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "InternalNodesList");
					for (Node node:finalNodeSet) {
						if (!(node instanceof InternalNode)) {
							continue;
						}
						
						JSONObject nodeObj = new JSONObject();
						String nodeLabelStr = node.getDisplayId();
						
						Label nodeLabel = node.getLabel();
						if (nodeLabel.getUri() !=null && nodeLabel.getNs() != null 
								&& nodeLabel.getUri().equalsIgnoreCase(nodeLabel.getNs())) {
							nodeLabelStr = node.getId();
						}
						nodeObj.put(JsonKeys.nodeLabel.name(), nodeLabelStr);
						nodeObj.put(JsonKeys.nodeId.name(), node.getId());
						nodesArray.put(nodeObj);
					}
					
					obj.put(JsonKeys.nodes.name(), nodesArray);
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
		// Not required
		return null;
	}

}
