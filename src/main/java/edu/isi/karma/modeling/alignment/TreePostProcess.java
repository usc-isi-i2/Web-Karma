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
package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class TreePostProcess {
	
	static Logger logger = Logger.getLogger(TreePostProcess.class);

	private DirectedWeightedMultigraph<Node, Link> tree;
	private Node root = null;
//	private List<Node> dangledVertexList;

	// Constructor
	
	public TreePostProcess(WeightedMultigraph<Node, Link> tree) {
		
		this.tree = (DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(tree);
		selectRoot(findPossibleRoots());

//		dangledVertexList = new ArrayList<Node>();
//		updateLinksDirections(this.root, null);
//		removeDanglingNodes();
		
	}
	
	// Public Methods
	
	public DirectedWeightedMultigraph<Node, Link> getTree() {
		return this.tree;
	}
	
	public Node getRoot() {
		return this.root;
	}
	
	// Private Methods
	
	private List<Node> findPossibleRoots() {

		List<Node> possibleRoots = new ArrayList<Node>();

		int maxReachableNodes = -1;
		int reachableNodes = -1;
		
		List<Node> vertexList = new ArrayList<Node>();
		List<Integer> reachableNodesList = new ArrayList<Integer>();
		
		for (Node v: this.tree.vertexSet()) {
			BreadthFirstIterator<Node, Link> i = 
				new BreadthFirstIterator<Node, Link>(this.tree, v);
			
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
	
	private void selectRoot(List<Node> possibleRoots) {
		
		if (possibleRoots == null || possibleRoots.size() == 0)
			return;
		
		Collections.sort(possibleRoots);
		
		this.root = possibleRoots.get(0);
	}
	
//	// Why do we need this function? I remember Maria said she is using it ...
//	private void updateLinksDirections(Node root, Link e) {
//		
//		if (root == null)
//			return;
//		
//		Node source, target;
//		Link inLink;
//		
//		Link[] incomingLinks = this.tree.incomingEdgesOf(root).toArray(new Link[0]);
//		if (incomingLinks != null && incomingLinks.length != 0) {
//			for (int i = 0; i < incomingLinks.length; i++) {
//				
//				inLink = incomingLinks[i];
//				source = inLink.getSource();
//				target = inLink.getTarget();
//				
//				// don't remove the incoming link from parent to this node
//				if (e != null && inLink.getID().equalsIgnoreCase(e.getID()))
//					continue;
//				
//				Label label = new Label(inLink.getUriString(), inLink.getNs(), inLink.getPrefix());
//				Link inverseLink = new Link(inLink.getID(), label);
//				
//				this.tree.addEdge(target, source, inverseLink);
//				this.tree.setEdgeWeight(inverseLink, inLink.getWeight());
//				this.tree.removeEdge(inLink);
//			}
//		}
//
//		Link[] outgoingLinks = this.tree.outgoingEdgesOf(root).toArray(new Link[0]);
//
//		if (outgoingLinks == null || outgoingLinks.length == 0)
//			return;
//		
//		
//		for (int i = 0; i < outgoingLinks.length; i++) {
//			target = outgoingLinks[i].getTarget();
//			updateLinksDirections(target, outgoingLinks[i]);
//		}
//	}
	
//	private void removeDanglingNodes() {
//
//		boolean connectedToColumn = false;
//		for (Node v: this.tree.vertexSet()) {
//			BreadthFirstIterator<Node, Link> i = 
//				new BreadthFirstIterator<Node, Link>(this.tree, v);
//
//			connectedToColumn = false;
//			
//			while (i.hasNext()) {
//				Node temp = i.next();
//				if (temp instanceof ColumnNode)
//					connectedToColumn = true;
//			}
//			
//			if (connectedToColumn == false)
//				dangledVertexList.add(v);
//		}
//		
//		for (Node v : dangledVertexList)
//			this.tree.removeVertex(v);
//		
//	}

//	public List<Node> getDangledVertexList() {
//		return dangledVertexList;
//	}
	
	
}
