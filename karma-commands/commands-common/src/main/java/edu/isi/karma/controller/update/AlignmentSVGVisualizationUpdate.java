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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DisplayModel;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AlignmentSVGVisualizationUpdate extends AbstractUpdate {
	private final String worksheetId;
	private final DirectedWeightedMultigraph<Node, LabeledLink> alignmentGraph;
	private final Alignment alignment;
	
	private static Logger logger = LoggerFactory.getLogger(AlignmentSVGVisualizationUpdate.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, id, hNodeId, nodeType, source,
		target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered,
		nodes, links, maxTreeHeight, linkStatus, linkUri, nodeDomain, isForcedByUser
	}

	private enum JsonValues {
		key, holderLink, objPropertyLink, Unassigned, FakeRoot, FakeRootLink, 
		Add_Parent, DataPropertyOfColumnHolder, horizontalDataPropertyLink
	}

	public AlignmentSVGVisualizationUpdate(String worksheetId, Alignment alignment) {
		super();
		this.worksheetId = worksheetId;
		this.alignment = alignment;
		this.alignmentGraph = alignment.getSteinerTree();
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		List<String> hNodeIdList = vWorksheet.getHeaderVisibleLeafNodes();
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				vWorkspace.getWorkspace().getId(), vWorksheet.getWorksheetId());
		
		JSONObject topObj = new JSONObject();
		try {
			topObj.put(GenericJsonKeys.updateType.name(),
					AlignmentSVGVisualizationUpdate.class.getSimpleName());
			topObj.put(JsonKeys.alignmentId.name(), alignmentId);
			topObj.put(JsonKeys.worksheetId.name(), worksheetId);
			
			// Using Mohsen's GraphUtils method for graph traversal
			DisplayModel dm = new DisplayModel(alignmentGraph, vWorksheet.getWorksheet().getHeaders());
			HashMap<Node, Integer> nodeHeightsMap = dm.getNodesLevel();
			HashMap<Node, Set<ColumnNode>> nodeCoverage = dm.getNodesSpan();
			
			/** Identify the max height **/
			int maxTreeHeight = 0;
			for (Node node:nodeHeightsMap.keySet()) {
				if(nodeHeightsMap.get(node) >= maxTreeHeight) {
					maxTreeHeight = nodeHeightsMap.get(node);
				}
			}

			/*** Add the nodes and the links from the Steiner tree ***/
			List<String> hNodeIdsAdded = new ArrayList<String>();
			JSONArray nodesArr = new JSONArray();
			JSONArray linksArr = new JSONArray();
			
			
			if (alignmentGraph != null && alignmentGraph.vertexSet().size() != 0) {
				/** Add the nodes **/
				Set<Node> nodes = alignmentGraph.vertexSet();
				HashMap<Node, Integer> verticesIndex = new HashMap<Node, Integer>();
				int nodesIndexcounter = 0;
				for (Node node : nodes) {
					/** Get info about the nodes that this node covers or sits above **/
					int height = nodeHeightsMap.get(node);
					
					/** Add the hnode ids of the columns that this vertex covers **/
					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
					for(Node v : nodeCoverage.get(node)) {
						if (v instanceof ColumnNode) {
							ColumnNode cNode = (ColumnNode) v;
							hNodeIdsCoveredByVertex.put(cNode.getHNodeId());
						}
					}
					
					String hNodeId = "";
					
					/** Add the semantic type information **/
					if (node instanceof ColumnNode) {
						ColumnNode cNode = (ColumnNode) node;
						hNodeId = cNode.getHNodeId();
						hNodeIdsAdded.add(cNode.getHNodeId());
					}
					JSONObject nodeObj = getNodeJsonObject(node.getLocalId(), node.getId(), node.getType().name()
							, height, node.isForceAddedByUser(), hNodeIdsCoveredByVertex, hNodeId, node.getUri());
					nodesArr.put(nodeObj);
					verticesIndex.put(node, nodesIndexcounter++);
				}
				
				/*** Add the links ***/
				Set<LabeledLink> links = alignmentGraph.edgeSet();
				for (LabeledLink link : links) {
					
					Node source = link.getSource();
					Integer sourceIndex = verticesIndex.get(source);
					Node target = link.getTarget();
					Integer targetIndex = verticesIndex.get(target);
					Set<LabeledLink> outEdges = alignmentGraph.outgoingEdgesOf(target);
					
					if(sourceIndex == null || targetIndex == null) {
						logger.error("Edge vertex index not found!");
						continue;
					}

					JSONObject linkObj = new JSONObject();
					linkObj.put(JsonKeys.source.name(), sourceIndex);
					linkObj.put(JsonKeys.target.name(), targetIndex);
					linkObj.put(JsonKeys.sourceNodeId.name(), source.getId());
					linkObj.put(JsonKeys.targetNodeId.name(), target.getId());
					
					linkObj.put(JsonKeys.label.name(), link.getLabel().getLocalName());
					linkObj.put(JsonKeys.id.name(), link.getId()+"");
					linkObj.put(JsonKeys.linkStatus.name(), link.getStatus().name());
					linkObj.put(JsonKeys.linkUri.name(), link.getLabel().getUri());

					if(target.getType() == NodeType.ColumnNode && outEdges.isEmpty()) {
						linkObj.put(JsonKeys.linkType.name(), JsonValues.holderLink.name());
						if(link.getKeyType() == LinkKeyInfo.PartOfKey)
							linkObj.put(JsonKeys.label.name(), link.getLabel().getLocalName()+"*");
					}

					linksArr.put(linkObj);
					
					if (link.getType() == LinkType.ClassInstanceLink 
							&& link.getKeyType() == LinkKeyInfo.PartOfKey 
							&& target instanceof ColumnNode) {
						ColumnNode cNode = (ColumnNode) target;
						// Add the holder vertex object and the link that attaches nodes to the columns
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(cNode.getHNodeId());
						
						JSONObject vertObj_holder = getNodeJsonObject(JsonValues.key.name(), source.getId()+"_holder"
								, NodeType.ColumnNode.name(), 0, false, hNodeIdsCoveredByVertex_holder, cNode.getHNodeId(),
								cNode.getLabel().getUri());
						nodesArr.put(vertObj_holder);
						nodesIndexcounter++;

						// Add the holder link
						JSONObject linkObj_holder = getLinkJsonObject(JsonValues.key.name(), ""
								, nodesIndexcounter, nodesIndexcounter-1, "", "", "", "");
						linksArr.put(linkObj_holder);
					}
					
					if (link.getType() == LinkType.DataPropertyOfColumnLink) {
						DataPropertyOfColumnLink dpLink = (DataPropertyOfColumnLink)link;
						String startHNodeId = dpLink.getSpecializedColumnHNodeId();

						// Get height of the class instance node
						int height = maxTreeHeight - nodeHeightsMap.get(link.getSource());
						
						// Add 2 more holder nodes
						// Start node
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(startHNodeId);
						JSONObject startNode = getNodeJsonObject("", source.getId()+"_holder"
								, JsonValues.DataPropertyOfColumnHolder.name()
								, height-0.35 
								, false
								, hNodeIdsCoveredByVertex_holder, startHNodeId, source.getLabel().getUri());
						nodesArr.put(startNode);
						
						nodesIndexcounter++;
						
						// End node
						String endHNodeId = ((ColumnNode)link.getTarget()).getHNodeId();
						JSONArray hNodeIdsCoveredByVertex_holder_2 = new JSONArray();
						hNodeIdsCoveredByVertex_holder_2.put(endHNodeId);
						JSONObject endNode = getNodeJsonObject("", target.getId()+"_holder", 
								JsonValues.DataPropertyOfColumnHolder.name(), height-0.35, false, 
								hNodeIdsCoveredByVertex_holder_2, endHNodeId, target.getLabel().getUri());
						nodesArr.put(endNode);

						nodesIndexcounter++;
						
						// Add the horizontal link
						JSONObject linkObj_holder = getLinkJsonObject("", "", nodesIndexcounter-2, 
								nodesIndexcounter-1, JsonValues.horizontalDataPropertyLink.name(), "", "", "");
						linksArr.put(linkObj_holder);
					} else if (link.getType() == LinkType.ObjectPropertySpecializationLink) {
						ObjectPropertySpecializationLink opLink = (ObjectPropertySpecializationLink)link;
						String specializedLinkId = opLink.getSpecializedLinkId();
						
						// Get height of the class instance node
						Node specializedLinkTarget = this.alignment.getNodeById(LinkIdFactory.getLinkTargetId(specializedLinkId));
						int height = nodeHeightsMap.get(specializedLinkTarget);
						
						// Add 2 more holder nodes
						// Start node
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						for(Node v : nodeCoverage.get(specializedLinkTarget)) {
							if (v instanceof ColumnNode) {
								ColumnNode cNode = (ColumnNode) v;
								hNodeIdsCoveredByVertex_holder.put(cNode.getHNodeId());
							}
						}
						JSONObject startNode = getNodeJsonObject("", source.getId()+"_holder", 
								JsonValues.DataPropertyOfColumnHolder.name(), 
								height+0.65, false, hNodeIdsCoveredByVertex_holder, "", source.getLabel().getUri());
						nodesArr.put(startNode);
						
						nodesIndexcounter++;
						
						// End node
						String endHNodeId = ((ColumnNode)link.getTarget()).getHNodeId();
						JSONArray hNodeIdsCoveredByVertex_holder_2 = new JSONArray();
						hNodeIdsCoveredByVertex_holder_2.put(endHNodeId);
						JSONObject endNode = getNodeJsonObject("", target.getId()+"_holder", 
								JsonValues.DataPropertyOfColumnHolder.name(), height+0.65, false,
								hNodeIdsCoveredByVertex_holder_2, endHNodeId, target.getLabel().getUri());
						nodesArr.put(endNode);

						nodesIndexcounter++;
						
						// Add the horizontal link
						JSONObject linkObj_holder = getLinkJsonObject("", "", nodesIndexcounter-2, 
								nodesIndexcounter-1, JsonValues.horizontalDataPropertyLink.name(), "", "", "");
						linksArr.put(linkObj_holder);
					}
					
					linkObj.put(JsonKeys.linkType.name(), link.getType());
				}
			} 

			// Add the vertices for the columns that were not in Steiner tree
			hNodeIdList.removeAll(hNodeIdsAdded);
			for(String hNodeId : hNodeIdList) {
				JSONArray hNodeIdsCoveredByVertex = new JSONArray();
				hNodeIdsCoveredByVertex.put(hNodeId);
				JSONObject vertObj = getNodeJsonObject("", hNodeId, JsonValues.Unassigned.name()
						, 0, false, hNodeIdsCoveredByVertex, hNodeId, "");
				nodesArr.put(vertObj);
			}
			
			topObj.put(JsonKeys.maxTreeHeight.name(), maxTreeHeight);
			topObj.put(JsonKeys.nodes.name(), nodesArr);
			topObj.put(JsonKeys.links.name(), linksArr);
			
			pw.write(topObj.toString());
		} catch (JSONException e) {
			logger.error("Error occured while writing JSON!", e);
		}
	}

	private JSONObject getNodeJsonObject(String label, String id, String nodeType
			, double height, boolean isForcedByUser, JSONArray hNodeIdsCoveredByVertex, String hNodeId, String nodeDomain) throws JSONException {
		JSONObject nodeObj = new JSONObject();
		nodeObj.put(JsonKeys.label.name(), label);
		nodeObj.put(JsonKeys.id.name(), id);
		nodeObj.put(JsonKeys.nodeType.name(), nodeType);
		nodeObj.put(JsonKeys.height.name(), height);
		nodeObj.put(JsonKeys.isForcedByUser.name(), isForcedByUser);
		nodeObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
		if (!hNodeId.equals(""))
			nodeObj.put(JsonKeys.hNodeId.name(), hNodeId);
		nodeObj.put(JsonKeys.nodeDomain.name(), nodeDomain);
		return nodeObj;
	}
	
	private JSONObject getLinkJsonObject(String label, String id, int sourceIndex, int targetIndex
			, String linkType, String sourceNodeId, String targetNodeId, String linkStatus) 
					throws JSONException {
		JSONObject linkObj = new JSONObject();
		linkObj.put(JsonKeys.label.name(), label);
		linkObj.put(JsonKeys.id.name(), id);
		linkObj.put(JsonKeys.source.name(), sourceIndex);
		linkObj.put(JsonKeys.target.name(), targetIndex);
		linkObj.put(JsonKeys.linkType.name(), linkType);
		if (!sourceNodeId.equals(""))
			linkObj.put(JsonKeys.sourceNodeId.name(), sourceNodeId);
		if (!targetNodeId.equals(""))
			linkObj.put(JsonKeys.targetNodeId.name(), targetNodeId);
		if (!linkStatus.equals(""))
			linkObj.put(JsonKeys.linkStatus.name(), linkStatus);
		return linkObj;
	}
	
	public boolean equals(Object o) {
		if (o instanceof AlignmentSVGVisualizationUpdate) {
			AlignmentSVGVisualizationUpdate t = (AlignmentSVGVisualizationUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}
}
