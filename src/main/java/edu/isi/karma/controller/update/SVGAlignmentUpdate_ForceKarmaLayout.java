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
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class SVGAlignmentUpdate_ForceKarmaLayout extends AbstractUpdate {
	private final VWorksheet vWorksheet;
	private final DirectedWeightedMultigraph<Node, Link> tree;
	private final Node root;
	
	private static Logger logger = LoggerFactory.getLogger(SVGAlignmentUpdate_ForceKarmaLayout.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, id, hNodeId, nodeType, source,
		target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered,
		nodes, links, maxTreeHeight, linkStatus
	}

	private enum JsonValues {
		key, holderLink, objPropertyLink, Unassigned, FakeRoot, FakeRootLink, 
		Add_Parent, DataPropertyOfColumnHolder, horizontalDataPropertyLink
	}

	public SVGAlignmentUpdate_ForceKarmaLayout(VWorksheet vWorksheet, Alignment alignment) {
		super();
		this.vWorksheet = vWorksheet;
		this.tree = alignment.getSteinerTree();
		this.root = alignment.GetTreeRoot();
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		List<String> hNodeIdList = new ArrayList<String>();
		List<HNodePath> columns = vWorksheet.getColumns();
		for(HNodePath path:columns)
			hNodeIdList.add(path.getLeaf().getId());
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), 
				vWorksheet.getId());
		
		JSONObject topObj = new JSONObject();
		try {
			topObj.put(GenericJsonKeys.updateType.name(),
					SVGAlignmentUpdate_ForceKarmaLayout.class.getSimpleName());
			topObj.put(JsonKeys.alignmentId.name(), alignmentId);
			topObj.put(JsonKeys.worksheetId.name(), vWorksheet.getId());
			
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
					/** Get info about the nodes that this node covers or sits above **/
					List<Node> nodesWithSemTypesCovered = new ArrayList<Node>();
					int height = getHeight(node, nodesWithSemTypesCovered, rootedTree);
					if(height >= maxTreeHeight) {
						maxTreeHeight = height;
					}
					
					/** Add the hnode ids of the columns that this vertex covers **/
					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
					for(Node v : nodesWithSemTypesCovered) {
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
							, height, hNodeIdsCoveredByVertex, hNodeId);
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
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(cNode.getHNodeId());
						
						JSONObject vertObj_holder = getNodeJsonObject(JsonValues.key.name(), source.getId()+"_holder"
								, NodeType.ColumnNode.name(), 0, hNodeIdsCoveredByVertex_holder, cNode.getHNodeId());
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
						String endHNodeId = ((ColumnNode)link.getTarget()).getHNodeId();
						// Get height of the class instance node
						List<Node> nodesWithSemTypesCovered = new ArrayList<Node>();
						int height = getHeight(link.getSource(), nodesWithSemTypesCovered, rootedTree);
						
						// Add 2 more holder nodes
						// Start node
						JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
						hNodeIdsCoveredByVertex_holder.put(startHNodeId);
						JSONObject startNode = getNodeJsonObject("", source.getId()+"_holder"
								, JsonValues.DataPropertyOfColumnHolder.name()
								, height-0.35, hNodeIdsCoveredByVertex_holder, startHNodeId);
						nodesArr.put(startNode);
						
						nodesIndexcounter++;
						
						// End node
						JSONArray hNodeIdsCoveredByVertex_holder_2 = new JSONArray();
						hNodeIdsCoveredByVertex_holder_2.put(endHNodeId);
						JSONObject endNode = getNodeJsonObject("", target.getId()+"_holder"
								, JsonValues.DataPropertyOfColumnHolder.name(), height-0.35
								, hNodeIdsCoveredByVertex_holder_2, endHNodeId);
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
						, 0, hNodeIdsCoveredByVertex, hNodeId);
				nodesArr.put(vertObj);
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
	
	private JSONObject getNodeJsonObject(String label, String id, String nodeType
			, double height, JSONArray hNodeIdsCoveredByVertex, String hNodeId) throws JSONException {
		JSONObject nodeObj = new JSONObject();
		nodeObj.put(JsonKeys.label.name(), label);
		nodeObj.put(JsonKeys.id.name(), id);
		nodeObj.put(JsonKeys.nodeType.name(), nodeType);
		nodeObj.put(JsonKeys.height.name(), height);
		nodeObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
		if (!hNodeId.equals(""))
			nodeObj.put(JsonKeys.hNodeId.name(), hNodeId);
		return nodeObj;
	}
	
	private JSONObject getLinkJsonObject(String label, String id, int sourceIndex, int targetIndex
			, String linkType, String sourceNodeId, String targetNodeId, String linkStatus) throws JSONException {
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
	
}
