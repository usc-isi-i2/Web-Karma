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
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.UIConfiguration;
import edu.isi.karma.config.UIConfigurationRegistry;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.ColumnMetadata;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DisplayModel;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AlignmentSVGVisualizationUpdate extends AbstractUpdate {
	private final String worksheetId;
	private DirectedWeightedMultigraph<Node, LabeledLink> alignmentGraph;
	private Alignment alignment;
	private static Logger logger = LoggerFactory
			.getLogger(AlignmentSVGVisualizationUpdate.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, rdfsLabel, rdfsComment, id, hNodeId, nodeType, source,
		target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered,
		nodes, links, maxTreeHeight, linkStatus, linkUri, nodeDomain, isForcedByUser, 
		isUri, nodeId, column, anchors, edgeLinks, alignObject, tableLayout, columnName, hasNestedTable, columns, isProvenance,
	}

	private enum JsonValues {
		key, holderLink, objPropertyLink, Unassigned, FakeRoot, FakeRootLink, Add_Parent, DataPropertyOfColumnHolder, horizontalDataPropertyLink
	}

	public AlignmentSVGVisualizationUpdate(String worksheetId) {
		super();
		this.worksheetId = worksheetId;
		
	}

	private JSONObject getForceLayoutNodeJsonObject(int id, String label, String rdfsLabel, String rdfsComment,
			String nodeId, String nodeType, boolean isForcedByUser,
			String nodeDomain, boolean isUri) throws JSONException {
		JSONObject nodeObj = new JSONObject();
		nodeObj.put(JsonKeys.label.name(), label);
		nodeObj.put(JsonKeys.rdfsLabel.name(), rdfsLabel);
		nodeObj.put(JsonKeys.rdfsComment.name(), rdfsComment);
		nodeObj.put(JsonKeys.id.name(), id);
		nodeObj.put(JsonKeys.nodeId.name(), nodeId);
		nodeObj.put(JsonKeys.nodeType.name(), nodeType);
		nodeObj.put(JsonKeys.isForcedByUser.name(), isForcedByUser);
		nodeObj.put(JsonKeys.nodeDomain.name(), nodeDomain);
		nodeObj.put(JsonKeys.isUri.name(), isUri);
		return nodeObj;
	}

	private JSONObject getForceLayoutColumnJsonObject(int id, String label, String rdfsLabel, String rdfsComment,
			String nodeId, String nodeType, boolean isForcedByUser,
			String hNodeId, String nodeDomain, int columnIndex)
			throws JSONException {
		JSONObject nodeObj = getForceLayoutNodeJsonObject(id, label, rdfsLabel, rdfsComment, nodeId,
				nodeType, isForcedByUser, nodeDomain, false);
		nodeObj.put(JsonKeys.hNodeId.name(), hNodeId);
		nodeObj.put(JsonKeys.column.name(), columnIndex);
		return nodeObj;
	}

	public boolean equals(Object o) {
		if (o instanceof AlignmentSVGVisualizationUpdate) {
			AlignmentSVGVisualizationUpdate t = (AlignmentSVGVisualizationUpdate) o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = this.worksheetId != null ? this.worksheetId.hashCode() : 0;
		result = 31 * result + (this.alignmentGraph != null ? this.alignmentGraph.hashCode() : 0);
		result = 31 * result + (this.alignment != null ? this.alignment.hashCode() : 0);
		return result;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Workspace workspace = vWorkspace.getWorkspace();
		alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		this.alignmentGraph = alignment.getSteinerTree();
		UIConfiguration uiConfiguration = UIConfigurationRegistry.getInstance().getUIConfiguration(vWorkspace.getWorkspace().getContextId());
		if (uiConfiguration.isForceModelLayoutEnabled())
			generateJsonForForceLayout(prefix, pw, vWorkspace);
		else
			generateJsonForNormalLayout(prefix, pw, vWorkspace);
	}

	public void generateJsonForForceLayout(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {

		VWorksheet vWorksheet = vWorkspace.getViewFactory()
				.getVWorksheetByWorksheetId(worksheetId);
		List<String> hNodeIdList = vWorksheet.getHeaderVisibleLeafNodes();

		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				vWorkspace.getWorkspace().getId(), vWorksheet.getWorksheetId());

		JSONObject topObj = new JSONObject();
		try {
			topObj.put(GenericJsonKeys.updateType.name(),
					AlignmentSVGVisualizationUpdate.class.getSimpleName());
			topObj.put(JsonKeys.alignmentId.name(), alignmentId);
			topObj.put(JsonKeys.worksheetId.name(), worksheetId);

			/*** Add the nodes and the links from the Steiner tree ***/
			JSONArray nodesArr = new JSONArray();
			JSONArray anchorsArr = new JSONArray();
			JSONArray linksArr = new JSONArray();
			JSONArray edgeLinksArr = new JSONArray();

			Map<Node, Integer> verticesIndex = new HashMap<>();
			Map<String, ColumnNode> columnNodes = new HashMap<>();

			if (alignmentGraph != null
					&& !alignmentGraph.vertexSet().isEmpty()) {
				Set<Node> nodes = alignmentGraph.vertexSet();
				for (Node node : nodes) {
					if (node instanceof ColumnNode) {
						columnNodes.put(((ColumnNode) node).getHNodeId(),
								(ColumnNode) node);
					}
				}
			}
			HTable headers = vWorksheet.getWorksheet().getHeaders();
			for (int columnNum = 0; columnNum < hNodeIdList.size(); columnNum++) {
				String hNodeId = hNodeIdList.get(columnNum);
				ColumnNode node = columnNodes.get(hNodeId);
				JSONObject anchorObj;
				HNode hNode = headers.getHNode(hNodeId, true);
				if (node != null) {
					anchorObj = getForceLayoutColumnJsonObject(columnNum,
							hNode.getColumnName(), 
							node.getLabel().getRdfsLabel(),
							node.getLabel().getRdfsComment(),
							node.getId(), 
							node.getType().name(), 
							node.isForced(), 
							hNodeId,
							node.getUri(), 
							columnNum);
				} else {
					
					anchorObj = getForceLayoutColumnJsonObject(columnNum,
							hNode.getColumnName(), 
							"", "",
							hNode.getId(), 
							"ColumnNode",
							false, 
							hNodeId, 
							"", 
							columnNum);
				}
				anchorsArr.put(anchorObj);
				verticesIndex.put(node, columnNum);
			}

			int nodesIndexcounter = hNodeIdList.size();

			if (alignmentGraph != null
					&& !alignmentGraph.vertexSet().isEmpty()) {
				/** Add the nodes **/
				Set<Node> nodes = alignmentGraph.vertexSet();
				for (Node node : nodes) {
					/** Add the semantic type information **/
					if (node instanceof ColumnNode) {
						// Already handled
					} else {
						boolean isUri = false;
						if(node instanceof LiteralNode && ((LiteralNode)node).isUri())
							isUri = true;
						JSONObject nodeObj = getForceLayoutNodeJsonObject(
								nodesIndexcounter, 
								node.getLocalId(), 
								node.getLabel().getRdfsLabel(),
								node.getLabel().getRdfsComment(),
								node.getId(), 
								node.getType().name(),
								node.isForced(), 
								node.getUri(), 
								isUri);
						nodesArr.put(nodeObj);
						verticesIndex.put(node, nodesIndexcounter++);
					}
				}

				/*** Add the links ***/
				Set<LabeledLink> links = alignmentGraph.edgeSet();
				for (LabeledLink link : links) {

					Node source = link.getSource();
					Integer sourceIndex = verticesIndex.get(source);
					Node target = link.getTarget();
					Integer targetIndex = verticesIndex.get(target);
					Set<LabeledLink> outEdges = alignmentGraph
							.outgoingEdgesOf(target);

					if (sourceIndex == null || targetIndex == null) {
						logger.error("Edge vertex index not found!");
						continue;
					}

					JSONObject linkObj = new JSONObject();
					linkObj.put(JsonKeys.source.name(), sourceIndex);
					linkObj.put(JsonKeys.target.name(), targetIndex);
					linkObj.put(JsonKeys.sourceNodeId.name(), source.getId());
					linkObj.put(JsonKeys.targetNodeId.name(), target.getId());

					linkObj.put(JsonKeys.label.name(), link.getLabel().getLocalName());
					linkObj.put(JsonKeys.rdfsLabel.name(), link.getLabel().getRdfsLabel());
					linkObj.put(JsonKeys.rdfsComment.name(), link.getLabel().getRdfsComment());
					linkObj.put(JsonKeys.isProvenance.name(), link.isProvenance());
					
					linkObj.put(JsonKeys.id.name(), link.getId() + "");
					linkObj.put(JsonKeys.linkStatus.name(), link.getStatus()
							.name());
					linkObj.put(JsonKeys.linkUri.name(), link.getLabel()
							.getUri());

					if (target.getType() == NodeType.ColumnNode
							&& outEdges.isEmpty()) {
						linkObj.put(JsonKeys.linkType.name(),
								JsonValues.holderLink.name());
					}

					linkObj.put(JsonKeys.linkType.name(), link.getType());
					if (link.getType() == LinkType.ObjectPropertySpecializationLink) {
						ObjectPropertySpecializationLink spLink = (ObjectPropertySpecializationLink) link;
						String linkId = spLink.getSpecializedLinkId();
						linkObj.put(JsonKeys.source.name(), linkId);
						edgeLinksArr.put(linkObj);
					} else if (link.getType() == LinkType.DataPropertyOfColumnLink) {
						DataPropertyOfColumnLink spLink = (DataPropertyOfColumnLink) link;
						String linkId = spLink.getSpecializedLinkId();
						linkObj.put(JsonKeys.source.name(), linkId);
						edgeLinksArr.put(linkObj);
					} else {
						linksArr.put(linkObj);
					}
				}
			}

			JSONObject alignObject = new JSONObject();
			alignObject.put(JsonKeys.anchors.name(), anchorsArr);
			alignObject.put(JsonKeys.tableLayout.name(), getTableLayout(vWorkspace));
			alignObject.put(JsonKeys.nodes.name(), nodesArr);
			alignObject.put(JsonKeys.links.name(), linksArr);
			alignObject.put(JsonKeys.edgeLinks.name(), edgeLinksArr);

			topObj.put(JsonKeys.alignObject.name(), alignObject);

			pw.write(topObj.toString());
		} catch (JSONException e) {
			logger.error("Error occured while writing JSON!", e);
		}

	}

	public JSONArray getTableLayout(VWorkspace vWorkspace) {
		VWorksheet vWorksheet = vWorkspace.getViewFactory()
				.getVWorksheetByWorksheetId(worksheetId);
		Worksheet wk = vWorksheet.getWorksheet();
		ColumnMetadata colMeta = wk.getMetadataContainer().getColumnMetadata();
		List<VHNode> viewHeaders = vWorksheet.getHeaderViewNodes();
			
		return getColumnsJsonArray(viewHeaders, colMeta);
	}
	
	private JSONArray getColumnsJsonArray(List<VHNode> viewHeaders, ColumnMetadata colMeta) throws JSONException {
		JSONArray colArr = new JSONArray();
		
		for (VHNode hNode:viewHeaders) {
			if(hNode.isVisible()) {
				JSONObject hNodeObj = new JSONObject();
				String columnName = hNode.getColumnName();
				
				hNodeObj.put(JsonKeys.columnName.name(), columnName);
				hNodeObj.put(JsonKeys.hNodeId.name(), hNode.getId());
				
				if (hNode.hasNestedTable()) {
					hNodeObj.put(JsonKeys.hasNestedTable.name(), true);
					
					List<VHNode> nestedHeaders = hNode.getNestedNodes();
					hNodeObj.put(JsonKeys.columns.name(), getColumnsJsonArray(nestedHeaders, colMeta));
				} else {
					hNodeObj.put(JsonKeys.hasNestedTable.name(), false);
				}
				
				colArr.put(hNodeObj);
			}
		}
		
		return colArr;
	}
	
	public void generateJsonForNormalLayout(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet = vWorkspace.getViewFactory()
				.getVWorksheetByWorksheetId(worksheetId);
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
			DisplayModel dm = new DisplayModel(alignmentGraph, vWorksheet
					.getWorksheet().getHeaders());
			HashMap<Node, Integer> nodeHeightsMap = dm.getNodesLevel();
			Map<Node, Set<ColumnNode>> nodeCoverage = dm.getNodesSpan();
			/** Identify the max height **/
			int maxTreeHeight = 0;
			for (Map.Entry<Node, Integer> nodeIntegerEntry : nodeHeightsMap.entrySet()) {
				if (nodeIntegerEntry.getValue() >= maxTreeHeight) {
					maxTreeHeight = nodeIntegerEntry.getValue();
				}
			}
			/*** Add the nodes and the links from the Steiner tree ***/
			List<String> hNodeIdsAdded = new ArrayList<>();
			JSONArray nodesArr = new JSONArray();
			JSONArray linksArr = new JSONArray();
			if (alignmentGraph != null
					&& !alignmentGraph.vertexSet().isEmpty()) {
				/** Add the nodes **/
				Set<Node> nodes = alignmentGraph.vertexSet();
				Map<Node, Integer> verticesIndex = new HashMap<>();
				int nodesIndexcounter = 0;
				for (Node node : nodes) {
					/**
					 * Get info about the nodes that this node covers or sits
					 * above
					 **/
					int height = nodeHeightsMap.get(node);
					/** Add the hnode ids of the columns that this vertex covers **/
					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
					for (Node v : nodeCoverage.get(node)) {
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

					boolean isUri = false;
					if(node instanceof LiteralNode)
						isUri = ((LiteralNode)node).isUri();

					JSONObject nodeObj = getNormalLayoutNodeJsonObject(node.getLocalId(),
							node.getLabel().getRdfsLabel(), node.getLabel().getRdfsComment(),
							node.getId(), node.getType().name(), height,
							node.isForced(), hNodeIdsCoveredByVertex,
							hNodeId, node.getUri(), isUri);

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
					Set<LabeledLink> outEdges = alignmentGraph
							.outgoingEdgesOf(target);
					if (sourceIndex == null || targetIndex == null) {
						logger.error("Edge vertex index not found!");
						continue;
					}
					JSONObject linkObj = new JSONObject();
					linkObj.put(JsonKeys.source.name(), sourceIndex);
					linkObj.put(JsonKeys.target.name(), targetIndex);
					linkObj.put(JsonKeys.sourceNodeId.name(), source.getId());
					linkObj.put(JsonKeys.targetNodeId.name(), target.getId());
					linkObj.put(JsonKeys.label.name(), link.getLabel().getLocalName());
					linkObj.put(JsonKeys.rdfsLabel.name(), link.getLabel().getRdfsLabel());
					linkObj.put(JsonKeys.rdfsComment.name(), link.getLabel().getRdfsComment());
					linkObj.put(JsonKeys.id.name(), link.getId() + "");
					linkObj.put(JsonKeys.linkStatus.name(), link.getStatus()
							.name());
					linkObj.put(JsonKeys.linkUri.name(), link.getLabel()
							.getUri());
					if (target.getType() == NodeType.ColumnNode
							&& outEdges.isEmpty()) {
						linkObj.put(JsonKeys.linkType.name(),
								JsonValues.holderLink.name());
					}
					linksArr.put(linkObj);
					if (link.getType() == LinkType.ClassInstanceLink
							&& target instanceof ColumnNode) {
						ColumnNode cNode = (ColumnNode) target;
						// Add the holder vertex object and the link that
						// attaches nodes to the columns
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(cNode.getHNodeId());

						JSONObject vertObj_holder = getNormalLayoutNodeJsonObject(
								JsonValues.key.name(), source.getLabel().getRdfsLabel(),
									source.getLabel().getRdfsComment(),
									source.getId() + "_holder",
									NodeType.ColumnNode.name(), 0, false,
									hNodeIdsCoveredByVertex_holder,
									cNode.getHNodeId(), cNode.getLabel().getUri(), false);

						nodesArr.put(vertObj_holder);
						nodesIndexcounter++;
						// Add the holder link
						JSONObject linkObj_holder = getNormalLayoutLinkJsonObject(
								JsonValues.key.name(), "", "", "", nodesIndexcounter,
								nodesIndexcounter - 1, "", "", "", "");
						linksArr.put(linkObj_holder);
					}
					if (link.getType() == LinkType.DataPropertyOfColumnLink) {
						DataPropertyOfColumnLink dpLink = (DataPropertyOfColumnLink) link;
						String startHNodeId = dpLink
								.getSpecializedColumnHNodeId();
						// Get height of the class instance node
						int height = maxTreeHeight
								- nodeHeightsMap.get(link.getSource());
						// Add 2 more holder nodes
						// Start node
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						boolean isSUri = false;
						if(source instanceof LiteralNode) {
							isSUri = ((LiteralNode)source).isUri();
						}
							
						hNodeIdsCoveredByVertex_holder.put(startHNodeId);

						JSONObject startNode = getNormalLayoutNodeJsonObject("", "", "",
								source.getId() + "_holder",
								JsonValues.DataPropertyOfColumnHolder.name(),
								height - 0.35, false,
								hNodeIdsCoveredByVertex_holder, startHNodeId,
								source.getLabel().getUri(), isSUri);

						nodesArr.put(startNode);
						nodesIndexcounter++;
						// End node
						String endHNodeId = ((ColumnNode) link.getTarget())
								.getHNodeId();
						JSONArray hNodeIdsCoveredByVertex_holder_2 = new JSONArray();
						hNodeIdsCoveredByVertex_holder_2.put(endHNodeId);

						boolean isTUri = false;
						if(target instanceof LiteralNode) {
							isTUri = ((LiteralNode)target).isUri();
						}
						
						JSONObject endNode = getNormalLayoutNodeJsonObject("", "", "",
								target.getId() + "_holder",
								JsonValues.DataPropertyOfColumnHolder.name(),
								height - 0.35, false,
								hNodeIdsCoveredByVertex_holder_2, endHNodeId,
								target.getLabel().getUri(), isTUri);

						nodesArr.put(endNode);
						nodesIndexcounter++;
						// Add the horizontal link
						JSONObject linkObj_holder = getNormalLayoutLinkJsonObject("", "", "", "",
								nodesIndexcounter - 2, nodesIndexcounter - 1,
								JsonValues.horizontalDataPropertyLink.name(),
								"", "", "");
						linksArr.put(linkObj_holder);
					} else if (link.getType() == LinkType.ObjectPropertySpecializationLink) {
						ObjectPropertySpecializationLink opLink = (ObjectPropertySpecializationLink) link;
						String specializedLinkId = opLink
								.getSpecializedLinkId();
						// Get height of the class instance node
						Node specializedLinkTarget = this.alignment
								.getNodeById(LinkIdFactory
										.getLinkTargetId(specializedLinkId));
						int height = nodeHeightsMap.get(specializedLinkTarget);
						// Add 2 more holder nodes
						// Start node
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						for (Node v : nodeCoverage.get(specializedLinkTarget)) {
							if (v instanceof ColumnNode) {
								ColumnNode cNode = (ColumnNode) v;
								hNodeIdsCoveredByVertex_holder.put(cNode
										.getHNodeId());
							}
						}

						boolean isSUri = false;
						if(source instanceof LiteralNode) {
							isSUri = ((LiteralNode)source).isUri();
						}
						JSONObject startNode = getNormalLayoutNodeJsonObject("", "", "",
								source.getId() + "_holder",
								JsonValues.DataPropertyOfColumnHolder.name(),
								height + 0.65, false,
								hNodeIdsCoveredByVertex_holder, "", source
										.getLabel().getUri(), isSUri);

						nodesArr.put(startNode);
						nodesIndexcounter++;
						// End node
						String endHNodeId = ((ColumnNode) link.getTarget())
								.getHNodeId();
						JSONArray hNodeIdsCoveredByVertex_holder_2 = new JSONArray();
						hNodeIdsCoveredByVertex_holder_2.put(endHNodeId);

						boolean isTUri = false;
						if(source instanceof LiteralNode) {
							isTUri = ((LiteralNode)target).isUri();
						}
						
						JSONObject endNode = getNormalLayoutNodeJsonObject("", "", "",
								target.getId() + "_holder",
								JsonValues.DataPropertyOfColumnHolder.name(),
								height + 0.65, false,
								hNodeIdsCoveredByVertex_holder_2, endHNodeId,
								target.getLabel().getUri(), isTUri);

						nodesArr.put(endNode);
						nodesIndexcounter++;
						// Add the horizontal link
						JSONObject linkObj_holder = getNormalLayoutLinkJsonObject("", "", "", "",
								nodesIndexcounter - 2, nodesIndexcounter - 1,
								JsonValues.horizontalDataPropertyLink.name(),
								"", "", "");
						linksArr.put(linkObj_holder);
					}
					linkObj.put(JsonKeys.linkType.name(), link.getType());
				}
			}
			// Add the vertices for the columns that were not in Steiner tree
			hNodeIdList.removeAll(hNodeIdsAdded);
			for (String hNodeId : hNodeIdList) {
				JSONArray hNodeIdsCoveredByVertex = new JSONArray();
				hNodeIdsCoveredByVertex.put(hNodeId);

				JSONObject vertObj = getNormalLayoutNodeJsonObject("", "", "", hNodeId,
						JsonValues.Unassigned.name(), 0, false,
						hNodeIdsCoveredByVertex, hNodeId, "", false);

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

	private JSONObject getNormalLayoutNodeJsonObject(String label, String rdfsLabel, String rdfsComment,
			String id,
			String nodeType, double height, boolean isForcedByUser,
			JSONArray hNodeIdsCoveredByVertex, String hNodeId, String nodeDomain, 
			boolean isUri) throws JSONException {
		JSONObject nodeObj = new JSONObject();
		nodeObj.put(JsonKeys.label.name(), label);
		nodeObj.put(JsonKeys.rdfsLabel.name(), rdfsLabel);
		nodeObj.put(JsonKeys.rdfsComment.name(), rdfsComment);
		nodeObj.put(JsonKeys.id.name(), id);
		nodeObj.put(JsonKeys.nodeType.name(), nodeType);
		nodeObj.put(JsonKeys.height.name(), height);
		nodeObj.put(JsonKeys.isForcedByUser.name(), isForcedByUser);
		nodeObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
		if (!hNodeId.equals(""))
			nodeObj.put(JsonKeys.hNodeId.name(), hNodeId);
		nodeObj.put(JsonKeys.nodeDomain.name(), nodeDomain);
		nodeObj.put(JsonKeys.isUri.name(), isUri);
		return nodeObj;
	}
	
	private JSONObject getNormalLayoutLinkJsonObject(String label, String rdfsLabel, String rdfsComment,
			String id,
			int sourceIndex, int targetIndex, String linkType,
			String sourceNodeId, String targetNodeId, String linkStatus)
			throws JSONException {
		JSONObject linkObj = new JSONObject();
		linkObj.put(JsonKeys.label.name(), label);
		linkObj.put(JsonKeys.rdfsLabel.name(), rdfsLabel);
		linkObj.put(JsonKeys.rdfsComment.name(), rdfsComment);
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
}
