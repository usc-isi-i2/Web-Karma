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
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SimpleLink;

public class TreePostProcess {
	
	static Logger logger = Logger.getLogger(TreePostProcess.class);

	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, Link> tree;
	private Node root = null;
	private Node thingNode = null;
//	private List<Node> dangledVertexList;

	// Constructor
	
	public TreePostProcess(
			GraphBuilder graphBuilder,
			WeightedMultigraph<Node, Link> tree, 
			Node thingNode) {
		
		this.graphBuilder = graphBuilder;
		this.tree = (DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(tree);
		this.thingNode = thingNode;
		buildOutputTree();
		selectRoot(findPossibleRoots());

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

		// If tree contains the Thing, we return it as the root
		for (Node v: this.tree.vertexSet()) 
			if (v.equals(this.thingNode)) {
				possibleRoots.add(v);
				return possibleRoots;
			}

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

	private DirectedWeightedMultigraph<Node, Link> buildOutputTree() {
		
		String sourceId, targetId;
		Link[] links = tree.edgeSet().toArray(new Link[0]);

		List<Link> temp;
		List<Link> possibleLinks = new ArrayList<Link>();
		
		for (Link link : links) {
			if (!(link instanceof SimpleLink)) continue;
			
			// links from source to target
			sourceId = link.getSource().getId();
			targetId = link.getTarget().getId();
			
			possibleLinks.clear();

			temp = this.graphBuilder.getPossibleLinks(sourceId, targetId);
			if (temp != null) possibleLinks.addAll(temp);
			temp = this.graphBuilder.getPossibleLinks(targetId, sourceId);
			if (temp != null) possibleLinks.addAll(temp);

			Collections.sort(possibleLinks, new LinkPriorityComparator());
			if (possibleLinks.size() > 0) {
				
				// pick the first one 
				Link newLink = possibleLinks.get(0);
				
				tree.addEdge(link.getSource(), link.getTarget(), newLink);
				tree.removeEdge(link);
				
				this.graphBuilder.addLink(link.getSource(), link.getTarget(), newLink);
				this.graphBuilder.removeLink(link);

			} else {
				logger.error("Something is going wrong. " +
						"There should be at least one possible object property between " +
						link.getSource().getLabel().getUri() + 
						" and " + link.getTarget().getLabel().getUri());
				return null;
			}
		}
		
		return tree;
	}
	
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
