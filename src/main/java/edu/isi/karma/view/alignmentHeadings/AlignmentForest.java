package edu.isi.karma.view.alignmentHeadings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.hierarchicalheadings.TForest;
import edu.isi.karma.rep.hierarchicalheadings.TNode;

public class AlignmentForest implements TForest {
	private List<TNode> roots = new ArrayList<TNode>();

	@Override
	public List<TNode> getRoots() {
		return roots;
	}

	public void addRoot(TNode root) {
		roots.add(root);
	}

	public void initializeTestForest() {
		// AlignmentNode b1 = new AlignmentNode("b1");
		// AlignmentNode b2 = new AlignmentNode("b2");
		// ArrayList<TNode> a1Nodes = new ArrayList<TNode>();
		// a1Nodes.add(b1);
		// a1Nodes.add(b2);
		// AlignmentNode a1 = new AlignmentNode("a1", a1Nodes);
		// roots.add(a1);
		//
		//
		// AlignmentNode c1 = new AlignmentNode("c1");
		// AlignmentNode c2 = new AlignmentNode("c2");
		// ArrayList<TNode> b4Nodes = new ArrayList<TNode>();
		// b4Nodes.add(c1);
		// b4Nodes.add(c2);
		// AlignmentNode b4 = new AlignmentNode("b4", b4Nodes);
		// AlignmentNode b3 = new AlignmentNode("b3");
		//
		// ArrayList<TNode> a2Nodes = new ArrayList<TNode>();
		// a2Nodes.add(b3);
		// a2Nodes.add(b4);
		//
		// AlignmentNode c3 = new AlignmentNode("c3");
		// ArrayList<TNode> b5Nodes = new ArrayList<TNode>();
		// b5Nodes.add(c3);
		// AlignmentNode b5 = new AlignmentNode("b5", b5Nodes);
		// a2Nodes.add(b5);
		// AlignmentNode a2 = new AlignmentNode("a2", a2Nodes);
		// roots.add(a2);

	}

	public static AlignmentForest constructFromSteinerTree(
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree,
			Vertex treeRoot) {

		AlignmentForest forest = new AlignmentForest();
		
		// Recursively add the vertices to the forest
		TNode root = forest.populateWithVertex(treeRoot, tree, null);
		forest.addRoot(root);

		return forest;
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
		Set<LabeledWeightedEdge> edges = tree.outgoingEdgesOf(vertex);
		for (LabeledWeightedEdge edge : edges) {
			TNode child = populateWithVertex(edge.getTarget(), tree, edge);
			children.add(child);
		}

		AlignmentNode node = new AlignmentNode(vertex.getID(), children,
				parentLink, vertex.getLabel());
		return node;
	}
}
