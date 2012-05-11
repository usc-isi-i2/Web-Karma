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

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Name;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.view.VWorkspace;

public class SVGAlignmentUpdate_ForceKarmaLayout extends AbstractUpdate {
	private final String vWorksheetId;
	private final String alignmentId;
	private final DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree;
	private final Vertex root;
	private final List<String> hNodeIdList;
	
	private static Logger logger = LoggerFactory
			.getLogger(SVGAlignmentUpdate_ForceKarmaLayout.class);

	private enum JsonKeys {
		worksheetId, alignmentId, label, id, hNodeId, nodeType, source, target, linkType, sourceNodeId, targetNodeId, height, hNodesCovered, nodes, links, maxTreeHeight
	}

	private enum JsonValues {
		key, holderLink, objPropertyLink, Unassigned
	}

	public SVGAlignmentUpdate_ForceKarmaLayout(String vWorksheetId,
			String alignmentId,
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree,
			Vertex root, List<String> hNodeIdList) {
		super();
		this.vWorksheetId = vWorksheetId;
		this.alignmentId = alignmentId;
		this.tree = tree;
		this.root = root;
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
			
			@SuppressWarnings("unchecked")
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>) tree.clone();
			// Reversing the inverse links
			updateLinksDirections(this.root, null, treeClone);

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
					vertObj.put(JsonKeys.label.name(), vertex.getLocalLabel());
					vertObj.put(JsonKeys.id.name(), vertex.getID());
					vertObj.put(JsonKeys.nodeType.name(), vertex.getNodeType().name());
					
					List<Vertex> nodesWithSemTypesCovered = new ArrayList<Vertex>();
					int height = getHeight(vertex, nodesWithSemTypesCovered, treeClone);
					if(height > maxTreeHeight)
						maxTreeHeight = height;
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
							// Add the holder vertex object and the link that attaches nodes to the columns
							JSONObject vertObj_holder = new JSONObject();
							vertObj_holder.put(JsonKeys.label.name(), JsonValues.key.name());
							vertObj_holder.put(JsonKeys.id.name(), vertex.getID()+"_holder");
							vertObj_holder.put(JsonKeys.nodeType.name(), NodeType.DataProperty.name());
							vertObj_holder.put(JsonKeys.hNodeId.name(), type.getHNodeId());
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
					
					if(target.getSemanticType() != null && outEdges.isEmpty())
						linkObj.put(JsonKeys.linkType.name(), JsonValues.holderLink.name());
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
		}
		return height;
	}
	
	private void updateLinksDirections(Vertex root, LabeledWeightedEdge e, DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone) {
		
		if (root == null)
			return;
		Vertex source, target;
		LabeledWeightedEdge inLink;
		
		LabeledWeightedEdge[] incomingLinks = treeClone.incomingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);
		if (incomingLinks != null && incomingLinks.length != 0) {
			for (int i = 0; i < incomingLinks.length; i++) {
				
				inLink = incomingLinks[i];
				source = inLink.getSource();
				target = inLink.getTarget();
				// don't remove the incoming link from parent to this node
				if (inLink.getID().equalsIgnoreCase(e.getID()))
					continue;
				
				LabeledWeightedEdge inverseLink = new LabeledWeightedEdge(inLink.getID(), new Name(inLink.getUri(), inLink.getNs(), inLink.getPrefix()), inLink.getLinkType(), true);
				treeClone.addEdge(target, source, inverseLink);
				treeClone.setEdgeWeight(inverseLink, inLink.getWeight());
				treeClone.removeEdge(inLink);
			}
		}

		LabeledWeightedEdge[] outgoingLinks = treeClone.outgoingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);

		if (outgoingLinks == null || outgoingLinks.length == 0)
			return;
		for (int i = 0; i < outgoingLinks.length; i++) {
			target = outgoingLinks[i].getTarget();
			updateLinksDirections(target, outgoingLinks[i], treeClone);
		}
	}	

}
