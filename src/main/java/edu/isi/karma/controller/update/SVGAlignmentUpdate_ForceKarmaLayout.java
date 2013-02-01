package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class SVGAlignmentUpdate_ForceKarmaLayout extends AbstractUpdate {
	private final String vWorksheetId;
	private final String alignmentId;
//	private Alignment alignment;
	private final DirectedWeightedMultigraph<Node, Link> tree;
	private final Node root;
	private final List<String> hNodeIdList;
	
	private static Logger logger = LoggerFactory.getLogger(SVGAlignmentUpdate_ForceKarmaLayout.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, id, hNodeId, nodeType, source,
		target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered,
		nodes, links, maxTreeHeight, linkStatus
	}

	private enum JsonValues {
		key, holderLink, objPropertyLink, Unassigned, FakeRoot, FakeRootLink, Add_Parent
	}

	public SVGAlignmentUpdate_ForceKarmaLayout(String vWorksheetId,
			String alignmentId,
			Alignment alignment, List<String> hNodeIdList) {
		super();
		this.vWorksheetId = vWorksheetId;
		this.alignmentId = alignmentId;
		this.tree = alignment.getSteinerTree();
		this.root = alignment.GetTreeRoot();
		this.hNodeIdList = hNodeIdList;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject topObj = new JSONObject();
		try {
			topObj.put(GenericJsonKeys.updateType.name(),
					SVGAlignmentUpdate_ForceKarmaLayout.class.getSimpleName());
			topObj.put(JsonKeys.alignmentId.name(), alignmentId);
			topObj.put(JsonKeys.worksheetId.name(), vWorksheetId);
			
			// Reversing the inverse links for easy traversal through graph
			Set<String> reversedLinks = new HashSet<String>();
			DirectedWeightedMultigraph<Node, Link> rootedTree = GraphUtil.treeToRootedTree(tree, this.root, reversedLinks);

			/*** Add the nodes and the links from the Steiner tree ***/
			List<String> hNodeIdsAdded = new ArrayList<String>();
			JSONArray nodesArr = new JSONArray();
			JSONArray linksArr = new JSONArray();
			int maxTreeHeight = 0;
			
			if (rootedTree != null && rootedTree.vertexSet().size() != 0) {
				/** Add the nodes **/
				Set<Node> nodes = rootedTree.vertexSet();
				HashMap<Node, Integer> verticesIndex = new HashMap<Node, Integer>();
				int nodesIndexcounter = 0;
				for (Node node : nodes) {
					/** Create the node JSON object **/
					JSONObject nodeObj = new JSONObject();
					nodeObj.put(JsonKeys.label.name(), node.getLocalId());
					nodeObj.put(JsonKeys.nodeType.name(), node.getType().name());
					
					/** Get info about the nodes that this node covers or sits above **/
					List<Node> nodesWithSemTypesCovered = new ArrayList<Node>();
					int height = getHeight(node, nodesWithSemTypesCovered, rootedTree);
					if(height >= maxTreeHeight) {
						maxTreeHeight = height;
					}
					
					/** Add the hnode ids of the columns that this vertex covers **/
					nodeObj.put(JsonKeys.height.name(), height);
					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
					for(Node v : nodesWithSemTypesCovered) {
						if (v instanceof ColumnNode) {
							ColumnNode cNode = (ColumnNode) v;
							hNodeIdsCoveredByVertex.put(cNode.getHNodeId());
						}
					}
					nodeObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
					
					/** Add the semantic type information **/
					if (node instanceof ColumnNode) {
						ColumnNode cNode = (ColumnNode) node;
						nodeObj.put(JsonKeys.hNodeId.name(), cNode.getHNodeId());
						hNodeIdsAdded.add(cNode.getHNodeId());
					}
					
					nodesArr.put(nodeObj);
					verticesIndex.put(node, nodesIndexcounter++);
				}
				
				/*** Add the links ***/
				Set<Link> links = rootedTree.edgeSet();
				for (Link link : links) {
					Node source = link.getSource();
					Integer sourceIndex = verticesIndex.get(source);
					Node target = link.getTarget();
					Integer targetIndex = verticesIndex.get(target);
					Set<Link> outEdges = rootedTree.outgoingEdgesOf(target);
					
					if(sourceIndex == null || targetIndex == null) {
						logger.error("Edge vertex index not found!");
						continue;
					}

					JSONObject linkObj = new JSONObject();
					if (reversedLinks.contains(link.getId())) {
						linkObj.put(JsonKeys.source.name(), targetIndex);
						linkObj.put(JsonKeys.target.name(), sourceIndex);
						linkObj.put(JsonKeys.sourceNodeId.name(), target.getId());
						linkObj.put(JsonKeys.targetNodeId.name(), source.getId());
					} else {
						linkObj.put(JsonKeys.source.name(), sourceIndex);
						linkObj.put(JsonKeys.target.name(), targetIndex);
						linkObj.put(JsonKeys.sourceNodeId.name(), source.getId());
						linkObj.put(JsonKeys.targetNodeId.name(), target.getId());
					}
					
					linkObj.put(JsonKeys.label.name(), link.getLabel().getLocalName());
					linkObj.put(JsonKeys.id.name(), link.getId()+"");
					linkObj.put(JsonKeys.linkStatus.name(), link.getStatus().name());

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
						JSONObject vertObj_holder = new JSONObject();
						vertObj_holder.put(JsonKeys.label.name(), JsonValues.key.name());
						vertObj_holder.put(JsonKeys.id.name(), source.getId()+"_holder");
						vertObj_holder.put(JsonKeys.nodeType.name(), NodeType.ColumnNode.name());
						vertObj_holder.put(JsonKeys.hNodeId.name(), cNode.getHNodeId());

						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(cNode.getHNodeId());
						vertObj_holder.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex_holder);
						vertObj_holder.put(JsonKeys.height.name(), 0);
						nodesArr.put(vertObj_holder);

						nodesIndexcounter++;

						// Add the holder link
						JSONObject linkObj_holder = new JSONObject();
						linkObj_holder.put(JsonKeys.source.name(), nodesIndexcounter);
						linkObj_holder.put(JsonKeys.target.name(), nodesIndexcounter-1);
						linkObj_holder.put(JsonKeys.label.name(), JsonValues.key.name());
						linkObj_holder.put(JsonKeys.id.name(), "");
						linksArr.put(linkObj_holder);
					}

					if(link.getType() == LinkType.ObjectPropertyLink) {
						linkObj.put(JsonKeys.linkType.name(), JsonValues.objPropertyLink.name());
					}
				}
				
				
//				/*** Add the nodes ***/
//				Set<Node> vertices = tree.vertexSet();
//				HashMap<Node, Integer> verticesIndex = new HashMap<Node, Integer>();
//				int nodesIndexcounter = 0;
//				
//				for (Node vertex : vertices) {
//					JSONObject vertObj = new JSONObject();
//					vertObj.put(JsonKeys.label.name(), vertex.getLocalID());
//					vertObj.put(JsonKeys.id.name(), vertex.getID());
//					vertObj.put(JsonKeys.nodeType.name(), vertex.getNodeType().name());
//					
//					List<Node> nodesWithSemTypesCovered = new ArrayList<Node>();
////					int height = getHeight(vertex, nodesWithSemTypesCovered, treeClone);
//					int height = getHeight(vertex, nodesWithSemTypesCovered, tree);
//					if(height >= maxTreeHeight) {
//						maxTreeHeight = height;
//					}
//						
//					vertObj.put(JsonKeys.height.name(), height);
//					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
//					for(Node v : nodesWithSemTypesCovered)
//						hNodeIdsCoveredByVertex.put(v.getSemanticType().getHNodeId());
//					vertObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
//					
//					// Add the semantic type information if required.
//					if(vertex.getSemanticType() != null) {
//						SemanticType type = vertex.getSemanticType();
//						vertObj.put(JsonKeys.hNodeId.name(), type.getHNodeId());
//						hNodeIdsAdded.add(type.getHNodeId());
//						
//						if (vertex.getNodeType() == NodeType.Class) {
//							if(type.isPartOfKey()) {
//								vertObj.put(JsonKeys.label.name(), vertex.getLocalID() + "*");
//							}
//							
//							// Add the holder vertex object and the link that attaches nodes to the columns
//							JSONObject vertObj_holder = new JSONObject();
//							vertObj_holder.put(JsonKeys.label.name(), JsonValues.key.name());
//							vertObj_holder.put(JsonKeys.id.name(), vertex.getID()+"_holder");
//							vertObj_holder.put(JsonKeys.nodeType.name(), NodeType.DataProperty.name());
//							vertObj_holder.put(JsonKeys.hNodeId.name(), type.getHNodeId());
//							
//							JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
//							hNodeIdsCoveredByVertex_holder.put(vertex.getSemanticType().getHNodeId());
//							vertObj_holder.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex_holder);
//							vertObj_holder.put(JsonKeys.height.name(), 0);
//							vertArr.put(vertObj_holder);
//							
//							nodesIndexcounter++;
//							
//							// Add the holder link
//							JSONObject linkObj = new JSONObject();
//							linkObj.put(JsonKeys.source.name(), nodesIndexcounter);
//							linkObj.put(JsonKeys.target.name(), nodesIndexcounter-1);
//							linkObj.put(JsonKeys.label.name(), JsonValues.key.name());
//							linkObj.put(JsonKeys.id.name(), "");
//							linksArr.put(linkObj);
//						}
//					}
//					
//					vertArr.put(vertObj);
//					verticesIndex.put(vertex, nodesIndexcounter++);
//				}
//				
//				/*** Add the links ***/
//				Set<Link> edges = tree.edgeSet();
//				for (Link edge : edges) {
//					Node source = edge.getSource();
//					Integer sourceIndex = verticesIndex.get(source);
//					Node target = edge.getTarget();
//					Integer targetIndex = verticesIndex.get(target);
//					Set<Link> outEdges = tree.outgoingEdgesOf(target);
//					
//					if(sourceIndex == null || targetIndex == null) {
//						logger.error("Edge vertex index not found!");
//						continue;
//					}
//					
//					JSONObject linkObj = new JSONObject();
//					linkObj.put(JsonKeys.source.name(), sourceIndex);
//					linkObj.put(JsonKeys.target.name(), targetIndex);
//					linkObj.put(JsonKeys.sourceNodeId.name(), source.getID());
//					linkObj.put(JsonKeys.targetNodeId.name(), target.getID());
//					linkObj.put(JsonKeys.label.name(), edge.getLocalLabel());
//					linkObj.put(JsonKeys.id.name(), edge.getID()+"");
//					linkObj.put(JsonKeys.linkStatus.name(), edge.getLinkStatus().name());
//					
//					if(target.getSemanticType() != null && outEdges.isEmpty()) {
//						linkObj.put(JsonKeys.linkType.name(), JsonValues.holderLink.name());
//						if(target.getSemanticType().isPartOfKey())
//							linkObj.put(JsonKeys.label.name(), edge.getLocalLabel()+"*");
//					}
//						
//					linksArr.put(linkObj);
//					
//					if(source.getNodeType() == NodeType.Class && target.getNodeType() == NodeType.Class) {
//						linkObj.put(JsonKeys.linkType.name(), JsonValues.objPropertyLink.name());
//					}
//				}
			} 

			// Add the vertices for the columns that were not in Steiner tree
			hNodeIdList.removeAll(hNodeIdsAdded);
			for(String hNodeId : hNodeIdList) {
				JSONObject vertObj = new JSONObject();
				vertObj.put(JsonKeys.id.name(), hNodeId);
				vertObj.put(JsonKeys.hNodeId.name(), hNodeId);
				vertObj.put(JsonKeys.nodeType.name(), JsonValues.Unassigned.name());
				nodesArr.put(vertObj);
				vertObj.put(JsonKeys.height.name(), 0);
				JSONArray hNodeIdsCoveredByVertex = new JSONArray();
				hNodeIdsCoveredByVertex.put(hNodeId);
				vertObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
			}
			
			topObj.put(JsonKeys.maxTreeHeight.name(), maxTreeHeight);
			topObj.put(JsonKeys.nodes.name(), nodesArr);
			topObj.put(JsonKeys.links.name(), linksArr);
			
			pw.write(topObj.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while writing JSON!", e);
		}
	}

	private int getHeight(Node vertex, List<Node> nodesWithSemTypesCovered, DirectedWeightedMultigraph<Node, Link> treeClone) {
		BreadthFirstIterator<Node, Link> itr = new BreadthFirstIterator<Node, Link>(treeClone, vertex);
		Node lastNodeWithSemanticType = null;
		int height = 0;
		while(itr.hasNext()) {
			Node v = itr.next();
			if(v.getType() == NodeType.ColumnNode) {
				lastNodeWithSemanticType = v;
				nodesWithSemTypesCovered.add(v);
			}
		}
		
		if(lastNodeWithSemanticType != null) {
			height = new DijkstraShortestPath<Node, Link>(treeClone, vertex, lastNodeWithSemanticType).getPathEdgeList().size();
			if(lastNodeWithSemanticType.getType() == NodeType.InternalNode && lastNodeWithSemanticType.getType() == NodeType.ColumnNode && vertex != lastNodeWithSemanticType) {
				height += getHeight(lastNodeWithSemanticType, new ArrayList<Node>(), treeClone);  
			}
			
			if(vertex == lastNodeWithSemanticType && vertex.getType() == NodeType.InternalNode && vertex.getType() == NodeType.ColumnNode) {
				height = 1;
			}
				
		}
		return height;
	}
	
}
