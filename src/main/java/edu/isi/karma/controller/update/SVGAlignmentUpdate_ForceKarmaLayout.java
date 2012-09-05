package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.view.VWorkspace;

public class SVGAlignmentUpdate_ForceKarmaLayout extends AbstractUpdate {
	private final String vWorksheetId;
	private final String alignmentId;
//	private Alignment alignment;
	private final DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree;
//	private final Vertex root;
	private final List<String> hNodeIdList;
	
	private static Logger logger = LoggerFactory
			.getLogger(SVGAlignmentUpdate_ForceKarmaLayout.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, id, hNodeId, nodeType, source, target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered, nodes, links, maxTreeHeight, linkStatus
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
//		this.root = alignment.GetTreeRoot();
		this.hNodeIdList = hNodeIdList;
//		this.alignment=alignment;
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
			
//			@SuppressWarnings("unchecked")
//			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>) tree.clone();
//			// Reversing the inverse links
//			alignment.updateLinksDirections(this.root, null, treeClone);

			/*** Add the nodes and the links from the Steiner tree ***/
			List<String> hNodeIdsAdded = new ArrayList<String>();
			JSONArray vertArr = new JSONArray();
			JSONArray linksArr = new JSONArray();
			int maxTreeHeight = 0;
			
			if (tree != null && tree.vertexSet().size() != 0) {
				
				/*** Add the nodes ***/
				Set<Vertex> vertices = tree.vertexSet();
				HashMap<Vertex, Integer> verticesIndex = new HashMap<Vertex, Integer>();
				int nodesIndexcounter = 0;
				
				for (Vertex vertex : vertices) {
					JSONObject vertObj = new JSONObject();
					vertObj.put(JsonKeys.label.name(), vertex.getLocalID());
					vertObj.put(JsonKeys.id.name(), vertex.getID());
					vertObj.put(JsonKeys.nodeType.name(), vertex.getNodeType().name());
					
					List<Vertex> nodesWithSemTypesCovered = new ArrayList<Vertex>();
//					int height = getHeight(vertex, nodesWithSemTypesCovered, treeClone);
					int height = getHeight(vertex, nodesWithSemTypesCovered, tree);
					if(height >= maxTreeHeight) {
						maxTreeHeight = height;
					}
						
					vertObj.put(JsonKeys.height.name(), height);
					JSONArray hNodeIdsCoveredByVertex = new JSONArray();
					for(Vertex v : nodesWithSemTypesCovered)
						hNodeIdsCoveredByVertex.put(v.getSemanticType().getHNodeId());
					vertObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
					
					// Add the semantic type information if required.
					if(vertex.getSemanticType() != null) {
						SemanticType type = vertex.getSemanticType();
						vertObj.put(JsonKeys.hNodeId.name(), type.getHNodeId());
						hNodeIdsAdded.add(type.getHNodeId());
						
						if (vertex.getNodeType() == NodeType.Class) {
							if(type.isPartOfKey()) {
								vertObj.put(JsonKeys.label.name(), vertex.getLocalID() + "*");
							}
							
							// Add the holder vertex object and the link that attaches nodes to the columns
							JSONObject vertObj_holder = new JSONObject();
							vertObj_holder.put(JsonKeys.label.name(), JsonValues.key.name());
							vertObj_holder.put(JsonKeys.id.name(), vertex.getID()+"_holder");
							vertObj_holder.put(JsonKeys.nodeType.name(), NodeType.DataProperty.name());
							vertObj_holder.put(JsonKeys.hNodeId.name(), type.getHNodeId());
							
							JSONArray hNodeIdsCoveredByVertex_holder = new JSONArray();
							hNodeIdsCoveredByVertex_holder.put(vertex.getSemanticType().getHNodeId());
							vertObj_holder.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex_holder);
							vertObj_holder.put(JsonKeys.height.name(), 0);
							vertArr.put(vertObj_holder);
							
							nodesIndexcounter++;
							
							// Add the holder link
							JSONObject linkObj = new JSONObject();
							linkObj.put(JsonKeys.source.name(), nodesIndexcounter);
							linkObj.put(JsonKeys.target.name(), nodesIndexcounter-1);
							linkObj.put(JsonKeys.label.name(), JsonValues.key.name());
							linkObj.put(JsonKeys.id.name(), "");
							linksArr.put(linkObj);
						}
					}
					
					vertArr.put(vertObj);
					verticesIndex.put(vertex, nodesIndexcounter++);
				}
				
				/*** Add the links ***/
				Set<LabeledWeightedEdge> edges = tree.edgeSet();
				for (LabeledWeightedEdge edge : edges) {
					Vertex source = edge.getSource();
					Integer sourceIndex = verticesIndex.get(source);
					Vertex target = edge.getTarget();
					Integer targetIndex = verticesIndex.get(target);
					Set<LabeledWeightedEdge> outEdges = tree.outgoingEdgesOf(target);
					
					if(sourceIndex == null || targetIndex == null) {
						logger.error("Edge vertex index not found!");
						continue;
					}
					
					JSONObject linkObj = new JSONObject();
					linkObj.put(JsonKeys.source.name(), sourceIndex);
					linkObj.put(JsonKeys.target.name(), targetIndex);
					linkObj.put(JsonKeys.sourceNodeId.name(), source.getID());
					linkObj.put(JsonKeys.targetNodeId.name(), target.getID());
					linkObj.put(JsonKeys.label.name(), edge.getLocalLabel());
					linkObj.put(JsonKeys.id.name(), edge.getID()+"");
					linkObj.put(JsonKeys.linkStatus.name(), edge.getLinkStatus().name());
					
					if(target.getSemanticType() != null && outEdges.isEmpty()) {
						linkObj.put(JsonKeys.linkType.name(), JsonValues.holderLink.name());
						if(target.getSemanticType().isPartOfKey())
							linkObj.put(JsonKeys.label.name(), edge.getLocalLabel()+"*");
					}
						
					linksArr.put(linkObj);
					
					if(source.getNodeType() == NodeType.Class && target.getNodeType() == NodeType.Class) {
						linkObj.put(JsonKeys.linkType.name(), JsonValues.objPropertyLink.name());
					}
				}
			} 

			// Add the vertices for the columns that were not in Steiner tree
			hNodeIdList.removeAll(hNodeIdsAdded);
			for(String hNodeId : hNodeIdList) {
				JSONObject vertObj = new JSONObject();
				vertObj.put(JsonKeys.id.name(), hNodeId);
				vertObj.put(JsonKeys.hNodeId.name(), hNodeId);
				vertObj.put(JsonKeys.nodeType.name(), JsonValues.Unassigned.name());
				vertArr.put(vertObj);
				vertObj.put(JsonKeys.height.name(), 0);
				JSONArray hNodeIdsCoveredByVertex = new JSONArray();
				hNodeIdsCoveredByVertex.put(hNodeId);
				vertObj.put(JsonKeys.hNodesCovered.name(), hNodeIdsCoveredByVertex);
			}
			
			topObj.put(JsonKeys.maxTreeHeight.name(), maxTreeHeight);
			topObj.put(JsonKeys.nodes.name(), vertArr);
			topObj.put(JsonKeys.links.name(), linksArr);
			
			pw.write(topObj.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while writing JSON!", e);
		}
	}

	private int getHeight(Vertex vertex, List<Vertex> nodesWithSemTypesCovered, DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone) {
		BreadthFirstIterator<Vertex, LabeledWeightedEdge> itr = new BreadthFirstIterator<Vertex, LabeledWeightedEdge>(treeClone, vertex);
		Vertex lastNodeWithSemanticType = null;
		int height = 0;
		while(itr.hasNext()) {
			Vertex v = itr.next();
			if(v.getSemanticType() != null) {
				lastNodeWithSemanticType = v;
				nodesWithSemTypesCovered.add(v);
			}
		}
		
		if(lastNodeWithSemanticType != null) {
			height = new DijkstraShortestPath<Vertex, LabeledWeightedEdge>(treeClone, vertex, lastNodeWithSemanticType).getPathEdgeList().size();
			if(lastNodeWithSemanticType.getNodeType() == NodeType.Class && lastNodeWithSemanticType.getSemanticType() != null && vertex != lastNodeWithSemanticType) {
				height += getHeight(lastNodeWithSemanticType, new ArrayList<Vertex>(), treeClone);  
			}
			
			if(vertex == lastNodeWithSemanticType && vertex.getNodeType() == NodeType.Class && vertex.getSemanticType() != null) {
				height = 1;
			}
				
		}
		return height;
	}
	
}
