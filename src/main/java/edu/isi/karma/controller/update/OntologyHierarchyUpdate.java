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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyTreeNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class OntologyHierarchyUpdate extends AbstractUpdate {
	private OntologyTreeNode rootNode;
	private String updateType;
	private boolean addSteinerTreeNodesAsChildren;
	private Alignment alignment;
	
	private static Logger logger = LoggerFactory.getLogger(OntologyHierarchyUpdate.class.getSimpleName());
	
	private enum JsonKeys {
		data, URIorId, metadata, children, isExistingGraphNode, newIndex, isExistingSteinerTreeNode
	}
	
	public OntologyHierarchyUpdate(OntologyTreeNode rootNode, String updateType, 
			boolean addGraphNodesAsChildren, Alignment alignment) {
		this.rootNode = rootNode;
		this.updateType = updateType;
		this.addSteinerTreeNodesAsChildren = addGraphNodesAsChildren;
		this.alignment = alignment;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		List<OntologyTreeNode> rootNodes = rootNode.getChildren();
		
		Set<String> steinerTreeNodeIds = new HashSet<String>();
		if (addSteinerTreeNodesAsChildren && alignment != null && !alignment.isEmpty()) {
			for (Node node: alignment.getSteinerTree().vertexSet()) {
				if (node.getType() == NodeType.InternalNode) {
					steinerTreeNodeIds.add(node.getId());
				}
			}
		}
		
		try {
			JSONArray dataArray = new JSONArray();
			for (OntologyTreeNode node: rootNodes) {
				JSONObject resourceObject = new JSONObject();
				JSONArray childrenArray = new JSONArray();
				
				// Add the children
				if (node.hasChildren()) {
					addChildren(node, childrenArray, steinerTreeNodeIds);
				}
				// Add the graph nodes
				if (addSteinerTreeNodesAsChildren && alignment!=null)
					addSteinerTreeNodes(node, childrenArray, steinerTreeNodeIds);

				resourceObject = getResourceObject(node, childrenArray, steinerTreeNodeIds);
				dataArray.put(resourceObject);
			}
			
			// Prepare the output JSON
			JSONObject outputObject = new JSONObject();
			outputObject.put(GenericJsonKeys.updateType.name(), updateType);
			outputObject.put(JsonKeys.data.name(), dataArray);
			System.out.println(outputObject.toString(4));
			pw.println(outputObject.toString());
		} catch (JSONException e) {
			logger.error("Error occured while creating ontology hierarchy JSON!", e);
		}
	}

	private JSONObject getResourceObject(OntologyTreeNode node,
			JSONArray childrenArray, Set<String> steinerTreeNodeIds) throws JSONException {
		JSONObject resourceObject = new JSONObject();
		Label resourceUri = node.getLabel();
		
		resourceObject.put(JsonKeys.children.name(), childrenArray);
		resourceObject.put(JsonKeys.data.name(), resourceUri.getLocalNameWithPrefix());
		JSONObject metadataObject = new JSONObject();
		int graphLastIndex = -1;
		if (addSteinerTreeNodesAsChildren && alignment != null) {
			graphLastIndex = alignment.getLastIndexOfNodeUri(node.getLabel().getUri());
		}
		// If the node exists in graph but not in tree then use the graph node id
		if (graphLastIndex != -1) {
			if (!steinerTreeNodeIds.contains(node.getLabel().getUri() + graphLastIndex)) {
				metadataObject.put(JsonKeys.URIorId.name(), node.getLabel().getUri() + graphLastIndex);
//				metadataObject.put(JsonKeys.isExistingGraphNode.name(), true);
				metadataObject.put(JsonKeys.isExistingSteinerTreeNode.name(), false);
				metadataObject.put(JsonKeys.newIndex.name(), 1);
			} else {
				metadataObject.put(JsonKeys.URIorId.name(), node.getLabel().getUri());
//				metadataObject.put(JsonKeys.isExistingGraphNode.name(), false);
				metadataObject.put(JsonKeys.isExistingSteinerTreeNode.name(), false);
				metadataObject.put(JsonKeys.newIndex.name(), graphLastIndex+1);
			}
		} else {
			metadataObject.put(JsonKeys.URIorId.name(), node.getLabel().getUri());
//			metadataObject.put(JsonKeys.isExistingGraphNode.name(), false);
			metadataObject.put(JsonKeys.newIndex.name(), 1);
			metadataObject.put(JsonKeys.isExistingSteinerTreeNode.name(), false);
		}
		resourceObject.put(JsonKeys.metadata.name(), metadataObject);
		
		return resourceObject;
	}

	private void addSteinerTreeNodes(OntologyTreeNode node, JSONArray childrenArray, Set<String> steinerTreeNodeIds) throws JSONException {
		String nodeUri = node.getLabel().getUri();
		List<Node> graphNodes = alignment.getNodesByUri(nodeUri);
		if (graphNodes != null && !graphNodes.isEmpty()) {
			for (Node graphNode : graphNodes) {
				if (steinerTreeNodeIds.contains(graphNode.getId())) {
					JSONObject graphNodeObj = new JSONObject();
					graphNodeObj.put(JsonKeys.data.name(), graphNode.getLocalIdWithPrefixIfAvailable());
					JSONObject metadataObject = new JSONObject();
					metadataObject.put(JsonKeys.URIorId.name(), graphNode.getId());
//					metadataObject.put(JsonKeys.isExistingGraphNode.name(), true);
					metadataObject.put(JsonKeys.isExistingSteinerTreeNode.name(), true);
					graphNodeObj.put(JsonKeys.metadata.name(), metadataObject);
					childrenArray.put(graphNodeObj);
				}
			}
		}
	}

	private void addChildren(OntologyTreeNode parentNode, JSONArray childrenArray, Set<String> steinerTreeNodeIds) throws JSONException {
		List<OntologyTreeNode> children = parentNode.getChildren();
		for (OntologyTreeNode childNode : children) {
			JSONObject resourceObject = new JSONObject();
			JSONArray childrenArraySubProp = new JSONArray();
			// Add the children
			if (childNode.hasChildren()) {
				addChildren(childNode, childrenArraySubProp, steinerTreeNodeIds);
			}
			// Add the graph nodes
			if (addSteinerTreeNodesAsChildren && alignment!=null) {
				addSteinerTreeNodes(childNode, childrenArraySubProp, steinerTreeNodeIds);
			}
			resourceObject = getResourceObject(childNode, childrenArraySubProp, steinerTreeNodeIds);
			childrenArray.put(resourceObject);
		}
	}
}
