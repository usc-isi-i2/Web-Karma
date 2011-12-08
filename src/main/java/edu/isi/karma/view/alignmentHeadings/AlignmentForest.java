package edu.isi.karma.view.alignmentHeadings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.hierarchicalheadings.TForest;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class AlignmentForest implements TForest {
	private List<TNode> roots = new ArrayList<TNode>();
	private Map<TNode, HNode> TNodeToHNodeMap = new HashMap<TNode, HNode>();

	private static Logger logger = LoggerFactory
			.getLogger(AlignmentForest.class.getName());

	@Override
	public List<TNode> getRoots() {
		return roots;
	}

	public void addRoot(TNode root) {
		roots.add(root);
	}

	public static AlignmentForest constructFromSteinerTree(
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree,
			Vertex treeRoot, List<HNode> sortedHeaders) {

		AlignmentForest forest = new AlignmentForest();

		// Recursively add the vertices to the forest
		TNode root = forest.populateWithVertex(treeRoot, tree, null);
		forest.addRoot(root);

		forest.reorderTreeNodes(sortedHeaders);

		/*** Get the final order of the columns from the forest ***/
		// The same list sortedHeaders is used to communicate the final order
		List<HNode> finalOrder = forest.getFinalColumnOrder(sortedHeaders);
		// Need to preserve the unaligned columns
		ArrayList<HNode> unalignedColumns = new ArrayList<HNode>();
		unalignedColumns.addAll(sortedHeaders);
		unalignedColumns.removeAll(finalOrder);
		// Add the aligned columns
		sortedHeaders.clear();
		sortedHeaders.addAll(finalOrder);
		// Add the unaligned columns
		sortedHeaders.addAll(unalignedColumns);
		return forest;
	}

	private List<HNode> getFinalColumnOrder(List<HNode> sortedHeaders) {
		ArrayList<HNode> finalOrder = new ArrayList<HNode>();
		Collection<TNode> columnTnodes = TNodeToHNodeMap.keySet();
		addToFinalOrderFromChildren(finalOrder, roots, columnTnodes);
		return finalOrder;
	}

	private void addToFinalOrderFromChildren(ArrayList<HNode> finalOrder,
			List<TNode> nodes, Collection<TNode> columnTnodes) {
		for (TNode node : nodes) {
			AlignmentNode alNode = (AlignmentNode) node;
			if (columnTnodes.contains(node))
				finalOrder.add(TNodeToHNodeMap.get(node));

			if (alNode.hasChildren()) {
				addToFinalOrderFromChildren(finalOrder, alNode.getChildren(),
						columnTnodes);
			}
		}
	}

	private void reorderTreeNodes(List<HNode> sortedHeaders) {
		// Assign sequential numbers to all TNodes according to column order
		HashMap<TNode, Integer> nodeIndexMap = new HashMap<TNode, Integer>();
		int counter = 0;
		for (HNode hNode : sortedHeaders) {
			String id = hNode.getId();
			TNode node = getAlignmentNodeWithHNodeId(roots, id);

			// Check for the special case where the intermediate node is
			// attached to a column. We added the same semantic type object to
			// one of its child
			if (node.getChildren() != null && node.getChildren().size() != 0) {
				node = getAlignmentNodeWithHNodeId(node.getChildren(), id);
			}
			nodeIndexMap.put(node, counter++);

			// Store the node in a map as it helps in determining final order
			TNodeToHNodeMap.put(node, hNode);
		}

		logger.info("Node Map: " + nodeIndexMap);
		for (TNode node : nodeIndexMap.keySet()) {
			AlignmentNode nodeAl = (AlignmentNode) node;
			logger.info(nodeAl.getType().getType() + " of "
					+ nodeAl.getType().getDomain() + " : "
					+ nodeIndexMap.get(node));
		}

		// For each root, sort at each level
		for (TNode root : roots) {
			// Collect nodes at each depth
			HashMap<Integer, ArrayList<TNode>> depthMap = new HashMap<Integer, ArrayList<TNode>>();
			calculateDepth(root, 0, depthMap);

			// Calculate the max depth
			Set<Integer> depths = depthMap.keySet();
			Integer maxDepth = Collections.max(depths);

			// Sort the nodes at each level starting from second last to top
			int startingLevel = maxDepth - 1;
			for (int i = startingLevel; i >= 0; i--) {
				List<TNode> nodeList = depthMap.get(i);
				for (TNode node : nodeList) {
					AlignmentNode nodeAl = (AlignmentNode) node;
					if (nodeAl.hasChildren()) {
						int minColIndex = sortChildren(nodeAl, nodeIndexMap);
						nodeIndexMap.put(nodeAl, minColIndex);
					}
				}
			}
		}
	}

	private int sortChildren(AlignmentNode parent,
			HashMap<TNode, Integer> nodeIndexMap) {
		List<TNode> children = parent.getChildren();

		logger.info("Parent: " + parent.getId());
		// Case of 1 child
		if (children.size() == 1) {
			return nodeIndexMap.get(children.get(0));
		}

		logger.info("Initial Order:");
		for (int i = 0; i < children.size(); i++) {
			logger.info(children.get(i).getId() + "   ");
		}

		int minSeqIndex = Integer.MAX_VALUE;
		for (int i = 1; i < children.size(); i++) {
			TNode child = children.get(i);

			int seqIndex = nodeIndexMap.get(child);
			logger.info("Working on " + child.getId() + " Index:" + seqIndex);

			if (seqIndex < minSeqIndex)
				minSeqIndex = seqIndex;

			for (int j = i - 1; j >= 0; j--) {
				TNode currChild = children.get(j);

				int currSeqIndex = nodeIndexMap.get(currChild);
				logger.info("Current Child: " + currChild.getId() + " Index: "
						+ currSeqIndex);
				if (currSeqIndex > seqIndex) {
					logger.info("SWAPPING!!!" + child.getId() + " with "
							+ currChild.getId());

					Collections.swap(children, children.indexOf(child), j);
				}
				logger.info("Order:");
				for (int z = 0; z < children.size(); z++) {
					logger.info(children.get(z).getId() + "   ");
				}
			}
		}

		logger.info("Final Order:");
		for (int i = 0; i < children.size(); i++) {
			logger.info(children.get(i).getId() + "   ");
		}
		logger.info("Min Index: " + minSeqIndex);
		return minSeqIndex;
	}

	private void calculateDepth(TNode node, int depth,
			HashMap<Integer, ArrayList<TNode>> depthMap) {
		// Populate the node in depth map
		ArrayList<TNode> nodeList = depthMap.get(depth);
		if (nodeList == null) {
			nodeList = new ArrayList<TNode>();
			depthMap.put(depth, nodeList);
		}
		nodeList.add(node);

		// Recurse to children
		AlignmentNode nodeAl = (AlignmentNode) node;
		if (nodeAl.hasChildren()) {
			for (TNode child : nodeAl.getChildren()) {
				calculateDepth(child, depth + 1, depthMap);
			}
		}
	}

	private TNode getAlignmentNodeWithHNodeId(List<TNode> nodes, String hNodeId) {

		for (TNode node : nodes) {
			AlignmentNode alNode = (AlignmentNode) node;
			if (alNode.hasSemanticType()) {
				if (alNode.getSemanticTypeHNodeId().equals(hNodeId)) {
					return alNode;
				}
			}

			if (alNode.hasChildren()) {
				TNode nodeC = getAlignmentNodeWithHNodeId(alNode.getChildren(),
						hNodeId);
				if (nodeC != null)
					return nodeC;
			}
		}
		return null;
	}

	private TNode populateWithVertex(Vertex vertex,
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree,
			LabeledWeightedEdge parentEdge) {
		// Add the information about the parent link
		AlignmentLink parentLink = null;
		if (parentEdge != null) {
			parentLink = new AlignmentLink(parentEdge.getID(),
					parentEdge.getLabel());
		}

		// Add the children
		List<TNode> children = new ArrayList<TNode>();

		// Check if the vertex is an intermediate node that should be attached
		// to a column. In such case add a blank child node
		if (vertex.getSemanticType() != null
				&& tree.outgoingEdgesOf(vertex).size() != 0) {
			logger.info("Intermediate Node with column attached to it: "
					+ vertex.getLabel());
			AlignmentLink link = new AlignmentLink(vertex.getSemanticType()
					.getHNodeId() + "BlankLink", "BlankNode");
			TNode node = new AlignmentNode(vertex.getID() + "BlankNodeId",
					null, link, "BlankNode", vertex.getSemanticType());
			children.add(node);
		}

		//
		Set<LabeledWeightedEdge> edges = tree.outgoingEdgesOf(vertex);
		for (LabeledWeightedEdge edge : edges) {
			TNode child = populateWithVertex(edge.getTarget(), tree, edge);
			children.add(child);
		}

		AlignmentNode node = new AlignmentNode(vertex.getID(), children,
				parentLink, vertex.getLabel(), vertex.getSemanticType());
		return node;
	}
}
