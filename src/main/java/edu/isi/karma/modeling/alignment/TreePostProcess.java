package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class TreePostProcess {
	
	static Logger logger = Logger.getLogger(TreePostProcess.class);

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree;
	private Vertex root = null;

	public TreePostProcess(WeightedMultigraph<Vertex, LabeledWeightedEdge> tree) {
		
		this.tree = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>)GraphUtil.asDirectedGraph(tree);
		selectRoot(findPossibleRoots());
		updateLinksDirections(this.root, null);
	}
	
	
	
	private List<Vertex> findPossibleRoots() {

		List<Vertex> possibleRoots = new ArrayList<Vertex>();

		int maxReachableNodes = -1;
		int reachableNodes = -1;
		
		List<Vertex> vertexList = new ArrayList<Vertex>();
		List<Integer> reachableNodesList = new ArrayList<Integer>();
		
		for (Vertex v: this.tree.vertexSet()) {
			BreadthFirstIterator<Vertex, LabeledWeightedEdge> i = new BreadthFirstIterator<Vertex, LabeledWeightedEdge>(this.tree, v);
			
			reachableNodes = -1;
			while (i.hasNext()) {
				i.next();
				reachableNodes ++;
			}
			
			vertexList.add(v);
			reachableNodesList.add(reachableNodes);
			
			if (reachableNodes > maxReachableNodes) {
				maxReachableNodes = reachableNodes;
			}
		}
		
		for (int i = 0; i < vertexList.size(); i++)
			if (reachableNodesList.get(i).intValue() == maxReachableNodes)
				possibleRoots.add(vertexList.get(i));
		
		return possibleRoots;
	}
	
	private void selectRoot(List<Vertex> possibleRoots) {
		
		if (possibleRoots == null || possibleRoots.size() == 0)
			return;
		
		this.root = possibleRoots.get(0);
	}
	
	private void updateLinksDirections(Vertex root, LabeledWeightedEdge e) {
		
		if (root == null)
			return;
		
		Vertex source, target;
		LabeledWeightedEdge inLink;
		
		LabeledWeightedEdge[] incomingLinks = this.tree.incomingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);
		if (incomingLinks != null && incomingLinks.length != 0) {
			for (int i = 0; i < incomingLinks.length; i++) {
				
				inLink = incomingLinks[i];
				source = inLink.getSource();
				target = inLink.getTarget();
				
				// don't remove the incoming link from parent to this node
				if (inLink.getID().equalsIgnoreCase(e.getID()))
					continue;
				
				LabeledWeightedEdge inverseLink = new LabeledWeightedEdge(inLink.getID(), inLink.getName(), inLink.getType(), true);
				
				this.tree.addEdge(target, source, inverseLink);
				this.tree.setEdgeWeight(inverseLink, inLink.getWeight());
				this.tree.removeEdge(inLink);
			}
		}

		LabeledWeightedEdge[] outgoingLinks = this.tree.outgoingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);

		if (outgoingLinks == null || outgoingLinks.length == 0)
			return;
		
		
		for (int i = 0; i < outgoingLinks.length; i++) {
			target = outgoingLinks[i].getTarget();
			updateLinksDirections(target, outgoingLinks[i]);
		}
	}
	
	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getTree() {
		return this.tree;
	}
}
