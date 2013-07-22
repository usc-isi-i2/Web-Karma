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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class GetAlternativeLinksCommand extends Command {
	private final String sourceNodeId;
	private final String targetNodeId;
	private final String alignmentId;
	private final ALTERNATIVE_LINKS_RANGE linksRange;

	private enum JsonKeys {
		updateType, edgeLabel, edgeId, edgeSource, edges
	}
	
	protected enum ALTERNATIVE_LINKS_RANGE {
		compatibleLinks, allObjectProperties;
	}

	public GetAlternativeLinksCommand(String id, String sourceNodeId,
			String targetNodeId, String alignmentId, ALTERNATIVE_LINKS_RANGE range) {
		super(id);
		this.sourceNodeId = sourceNodeId;
		this.targetNodeId = targetNodeId;
		this.alignmentId = alignmentId;
		this.linksRange = range;
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
		final Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		
		Map<String, Label> linkList = new HashMap<String, Label>();
		if (linksRange == ALTERNATIVE_LINKS_RANGE.allObjectProperties) {
			linkList = vWorkspace.getWorkspace().
					getOntologyManager().getObjectProperties();
			
		} else if (linksRange == ALTERNATIVE_LINKS_RANGE.compatibleLinks) {
			Node sourceNode = alignment.getNodeById(sourceNodeId);
			Node targetNode = alignment.getNodeById(targetNodeId);
			
			if (sourceNode == null || targetNode == null) {
				return new UpdateContainer(new ErrorUpdate("Error occured while getting links!"));
			}
			
			Set<String> possibleLinks = ontMgr.getObjectPropertiesIndirect(
							sourceNode.getLabel().getUri(), targetNode.getLabel().getUri());
			if (possibleLinks != null) {
				for (String linkUri:possibleLinks) {
					Label linkLabel = ontMgr.getUriLabel(linkUri);
					linkList.put(linkUri, linkLabel);
				}
			}
		}
		
		final Map<String, Label> finalLinksSet = linkList;
		
		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray nodesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "LinksList");
					
					for (Label linkLabel:finalLinksSet.values()) {
						String edgeLabelStr = linkLabel.getDisplayName();
						JSONObject edgeObj = new JSONObject();
						if (linkLabel.getUri() !=null && linkLabel.getNs() != null 
								&& linkLabel.getUri().equalsIgnoreCase(linkLabel.getNs())) {
							edgeLabelStr = linkLabel.getUri();
						}
						
						edgeObj.put(JsonKeys.edgeLabel.name(), edgeLabelStr);
						edgeObj.put(JsonKeys.edgeId.name(), linkLabel.getUri());
						nodesArray.put(edgeObj);
					}
					
					obj.put(JsonKeys.edges.name(), nodesArray);
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
