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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.OntologyHierarchyUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class GetDomainsForDataPropertyCommand extends Command {

	private final String dataPropertyURI;
	private final String worksheetId;

	private static Logger logger = LoggerFactory
			.getLogger(GetDomainsForDataPropertyCommand.class.getSimpleName());

	public enum JsonKeys {
		updateType, URI, metadata, data, URIorId, newIndex
	}

	public GetDomainsForDataPropertyCommand(String id, String uri, String worksheetId) {
		super(id);
		this.dataPropertyURI = uri;
		this.worksheetId = worksheetId;
	}

	@Override
	public String getCommandName() {
		return "Get Domains for Data Property";
	}

	@Override
	public String getTitle() {
		return this.getClass().getSimpleName();
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
		final OntologyManager ontMgr = workspace.getOntologyManager();
		final HashSet<String> domains = ontMgr.getDomainsOfProperty(
				dataPropertyURI, true);
		final Alignment alignment = AlignmentManager.Instance().getAlignment(
				workspace.getId(), worksheetId);

		// Show all the classes when none are present
		if (domains == null || domains.size() == 0) {
			return new UpdateContainer(new OntologyHierarchyUpdate(ontMgr.getClassHierarchy(), 
					"OntologyClassHierarchyUpdate", true, alignment));
		}
		
		final Set<String> steinerTreeNodeIds = new HashSet<String>();
		if (alignment != null && !alignment.isEmpty()) {
			for (Node node: alignment.getSteinerTree().vertexSet()) {
				if (node.getType() == NodeType.InternalNode) {
					steinerTreeNodeIds.add(node.getId());
				}
			}
		}

		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "DomainsForDataPropertyUpdate");

					JSONArray dataArray = new JSONArray();
					for (String domain : domains) {
						JSONObject classObject = new JSONObject();

						Label domainURI = ontMgr.getUriLabel(domain);
						if(domainURI == null)
							continue;
						classObject.put(JsonKeys.data.name(), domainURI.getDisplayName());

						JSONObject metadataObject = new JSONObject();
						metadataObject.put(JsonKeys.URIorId.name(), domain);
						classObject.put(JsonKeys.metadata.name(), metadataObject);

						int graphLastIndex = -1;
						if (alignment != null) {
							graphLastIndex = alignment.getLastIndexOfNodeUri(domainURI.getUri());
						}
						// If the node exists in graph but not in tree then use the graph node id
						if (graphLastIndex != -1) {
							if (!steinerTreeNodeIds.contains(domainURI.getUri() + graphLastIndex)) {
								metadataObject.put(JsonKeys.newIndex.name(), 1);
							} else {
								metadataObject.put(JsonKeys.newIndex.name(), graphLastIndex+1);
							}
						} else {
							metadataObject.put(JsonKeys.newIndex.name(), 1);
						}
						
						dataArray.put(classObject);
						
						// Populate the graph nodes also
						if (alignment != null) {
							Set<Node> graphNodes = alignment.getNodesByUri(domain);
							if (graphNodes != null && graphNodes.size() != 0) {
								for (Node graphNode: graphNodes) {
									if (steinerTreeNodeIds.contains(graphNode.getId())) {
										JSONObject graphNodeObj = new JSONObject();
										graphNodeObj.put(JsonKeys.data.name(), graphNode.getDisplayId());
										JSONObject metadataObject_gNode = new JSONObject();
										metadataObject_gNode.put(JsonKeys.URIorId.name(), graphNode.getId());
										graphNodeObj.put(JsonKeys.metadata.name(), metadataObject_gNode);
										dataArray.put(graphNodeObj);
									}
								}
							}
						}
						
					}
					outputObject.put(JsonKeys.data.name(), dataArray);

					pw.println(outputObject.toString());
				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}

}
