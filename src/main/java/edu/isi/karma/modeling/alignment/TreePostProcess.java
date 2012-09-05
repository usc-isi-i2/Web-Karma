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

public class TreePostProcess {
	
	static Logger logger = Logger.getLogger(TreePostProcess.class);

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree;
	private Vertex root = null;
	private List<Vertex> dangledVertexList;

	public TreePostProcess(WeightedMultigraph<Vertex, LabeledWeightedEdge> tree) {
		
		this.tree = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>)GraphUtil.asDirectedGraph(tree);
		dangledVertexList = new ArrayList<Vertex>();
		selectRoot(findPossibleRoots());
		updateLinksDirections(this.root, null);
		removeDanglingNodes();
	}
	
	
	
	private List<Vertex> findPossibleRoots() {

		List<Vertex> possibleRoots = new ArrayList<Vertex>();

		int maxReachableNodes = -1;
		int reachableNodes = -1;
		
		List<Vertex> vertexList = new ArrayList<Vertex>();
		List<Integer> reachableNodesList = new ArrayList<Integer>();
		
//		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedTree = 
//			new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.tree);

//		boolean connectedToSemanticType = false;
		for (Vertex v: this.tree.vertexSet()) {
			BreadthFirstIterator<Vertex, LabeledWeightedEdge> i = 
				new BreadthFirstIterator<Vertex, LabeledWeightedEdge>(this.tree, v);
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
	
	private void selectRoot(List<Vertex> possibleRoots) {
		
		if (possibleRoots == null || possibleRoots.size() == 0)
			return;
		
		VertexComparatorByID vComp = new VertexComparatorByID();
		Collections.sort(possibleRoots, vComp);
		
//		for (int i = 0; i < possibleRoots.size(); i++)
//			System.out.print(possibleRoots.get(i).getLocalID());
//		System.out.println();
		
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
				if (e != null && inLink.getID().equalsIgnoreCase(e.getID()))
					continue;
				
				LabeledWeightedEdge inverseLink = new LabeledWeightedEdge(inLink.getID(), new URI(inLink.getUriString(), inLink.getNs(), inLink.getPrefix()), inLink.getLinkType(), true);
				
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
	
	private void removeDanglingNodes() {

		boolean connectedToSemanticType = false;
		for (Vertex v: this.tree.vertexSet()) {
			BreadthFirstIterator<Vertex, LabeledWeightedEdge> i = 
				new BreadthFirstIterator<Vertex, LabeledWeightedEdge>(this.tree, v);

			connectedToSemanticType = false;
			
			while (i.hasNext()) {
				Vertex temp = i.next();
				if (temp.getSemanticType() != null)
					connectedToSemanticType = true;
			}
			
			if (connectedToSemanticType == false)
				dangledVertexList.add(v);
		}
		
		for (Vertex v : dangledVertexList)
			this.tree.removeVertex(v);
		
	}
	
	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getTree() {
		return this.tree;
	}
	
	public Vertex getRoot() {
		return this.root;
	}

	public List<Vertex> getDangledVertexList() {
		return dangledVertexList;
	}
	
	
}
