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
import edu.isi.karma.rep.alignment.URI;

public class TreePostProcess {
	
	static Logger logger = Logger.getLogger(TreePostProcess.class);

	private DirectedWeightedMultigraph<Node, Link> tree;
	private Node root = null;
	private List<Node> dangledVertexList;

	public TreePostProcess(WeightedMultigraph<Node, Link> tree) {
		
		this.tree = (DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(tree);
		dangledVertexList = new ArrayList<Node>();
		selectRoot(findPossibleRoots());
		updateLinksDirections(this.root, null);
		removeDanglingNodes();
	}
	
	
	
	private List<Node> findPossibleRoots() {

		List<Node> possibleRoots = new ArrayList<Node>();

		int maxReachableNodes = -1;
		int reachableNodes = -1;
		
		List<Node> vertexList = new ArrayList<Node>();
		List<Integer> reachableNodesList = new ArrayList<Integer>();
		
//		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedTree = 
//			new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.tree);

//		boolean connectedToSemanticType = false;
		for (Node v: this.tree.vertexSet()) {
			BreadthFirstIterator<Node, Link> i = 
				new BreadthFirstIterator<Node, Link>(this.tree, v);
//			connectedToSemanticType = false;
			
			reachableNodes = -1;
			while (i.hasNext()) {
//				Vertex temp = i.next();
				i.next();
//				if (temp.getSemanticType() != null)
//					connectedToSemanticType = true;
				reachableNodes ++;
			}
			
//			if (connectedToSemanticType == false)
//				dangledVertexList.add(v);
//			else 
			{
				vertexList.add(v);
				reachableNodesList.add(reachableNodes);
				
				if (reachableNodes > maxReachableNodes) {
					maxReachableNodes = reachableNodes;
				}
			}
		}
		
		for (int i = 0; i < vertexList.size(); i++)
			if (reachableNodesList.get(i).intValue() == maxReachableNodes)
				possibleRoots.add(vertexList.get(i));
		
//		for (Vertex v : dangledVertexList)
//			this.tree.removeVertex(v);
		
		return possibleRoots;
	}
	
	private void selectRoot(List<Node> possibleRoots) {
		
		if (possibleRoots == null || possibleRoots.size() == 0)
			return;
		
		VertexComparatorByID vComp = new VertexComparatorByID();
		Collections.sort(possibleRoots, vComp);
		
//		for (int i = 0; i < possibleRoots.size(); i++)
//			System.out.print(possibleRoots.get(i).getLocalID());
//		System.out.println();
		
		this.root = possibleRoots.get(0);
	}
	
	private void updateLinksDirections(Node root, Link e) {
		
		if (root == null)
			return;
		
		Node source, target;
		Link inLink;
		
		Link[] incomingLinks = this.tree.incomingEdgesOf(root).toArray(new Link[0]);
		if (incomingLinks != null && incomingLinks.length != 0) {
			for (int i = 0; i < incomingLinks.length; i++) {
				
				inLink = incomingLinks[i];
				source = inLink.getSource();
				target = inLink.getTarget();
				
				// don't remove the incoming link from parent to this node
				if (e != null && inLink.getID().equalsIgnoreCase(e.getID()))
					continue;
				
				Link inverseLink = new Link(inLink.getID(), new URI(inLink.getUriString(), inLink.getNs(), inLink.getPrefix()), inLink.getLinkType(), true);
				
				this.tree.addEdge(target, source, inverseLink);
				this.tree.setEdgeWeight(inverseLink, inLink.getWeight());
				this.tree.removeEdge(inLink);
			}
		}

		Link[] outgoingLinks = this.tree.outgoingEdgesOf(root).toArray(new Link[0]);

		if (outgoingLinks == null || outgoingLinks.length == 0)
			return;
		
		
		for (int i = 0; i < outgoingLinks.length; i++) {
			target = outgoingLinks[i].getTarget();
			updateLinksDirections(target, outgoingLinks[i]);
		}
	}
	
	private void removeDanglingNodes() {

		boolean connectedToSemanticType = false;
		for (Node v: this.tree.vertexSet()) {
			BreadthFirstIterator<Node, Link> i = 
				new BreadthFirstIterator<Node, Link>(this.tree, v);

			connectedToSemanticType = false;
			
			while (i.hasNext()) {
				Node temp = i.next();
				if (temp.getSemanticType() != null)
					connectedToSemanticType = true;
			}
			
			if (connectedToSemanticType == false)
				dangledVertexList.add(v);
		}
		
		for (Node v : dangledVertexList)
			this.tree.removeVertex(v);
		
	}
	
	public DirectedWeightedMultigraph<Node, Link> getTree() {
		return this.tree;
	}
	
	public Node getRoot() {
		return this.root;
	}

	public List<Node> getDangledVertexList() {
		return dangledVertexList;
	}
	
	
}
