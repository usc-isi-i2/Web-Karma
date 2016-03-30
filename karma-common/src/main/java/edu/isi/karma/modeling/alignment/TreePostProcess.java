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
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.alignment.CompactLink;
import edu.isi.karma.rep.alignment.CompactObjectPropertyLink;
import edu.isi.karma.rep.alignment.CompactSubClassLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;


public class TreePostProcess {
	
	static Logger logger = LoggerFactory.getLogger(TreePostProcess.class);

	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, DefaultLink> tree;
	private Node root = null;
//	private List<Node> dangledVertexList;

	// Constructor
	
	public TreePostProcess(
			GraphBuilder graphBuilder,
			UndirectedGraph<Node, DefaultLink> tree, 
			Set<LabeledLink> newLinks,
			boolean findRoot) {
		
		this.graphBuilder = graphBuilder;
		this.tree = (DirectedWeightedMultigraph<Node, DefaultLink>)GraphUtil.asDirectedGraph(tree);
		buildOutputTree(true);
		addLinks(newLinks);
		if (findRoot) {
			this.root = selectRoot(this.tree);
		}

	}
	
	public TreePostProcess(
			GraphBuilder graphBuilder,
			UndirectedGraph<Node, DefaultLink> tree) {
		
		this.graphBuilder = graphBuilder;
		this.tree = (DirectedWeightedMultigraph<Node, DefaultLink>)GraphUtil.asDirectedGraph(tree);
		buildOutputTree(false);
	}
	
	// Public Methods
	
	public DirectedWeightedMultigraph<Node, LabeledLink> getTree() {
		return (DirectedWeightedMultigraph<Node, LabeledLink>)GraphUtil.asLabeledGraph(this.tree);
	}
	
	public Node getRoot() {
		return this.root;
	}
	
	// Private Methods
	
	private static List<Node> findPossibleRoots(DirectedWeightedMultigraph<Node, DefaultLink> tree) {

		List<Node> possibleRoots = new ArrayList<>();

		// If tree contains the Thing, we return it as the root
		for (Node v: tree.vertexSet()) { 
			if (v.getLabel() != null && v.getLabel().getUri() != null && v.getLabel().getUri().equals(Uris.THING_URI)) {
				possibleRoots.add(v);
			}
		}

		int maxReachableNodes = -1;
		int reachableNodes;
		
		List<Node> vertexList = new ArrayList<>();
		List<Integer> reachableNodesList = new ArrayList<>();
		
		for (Node v: tree.vertexSet()) {
			BreadthFirstIterator<Node, DefaultLink> i =
					new BreadthFirstIterator<>(tree, v);
			
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
	
	public static Node selectRoot(DirectedWeightedMultigraph<Node, DefaultLink> tree) {
		
		List<Node> possibleRoots = findPossibleRoots(tree);
		
		if (possibleRoots == null || possibleRoots.isEmpty())
			return null;
		
		Collections.sort(possibleRoots);
		
		return possibleRoots.get(0);
	}

	private void buildOutputTree(boolean allowedChangingGraph) {
		
		String sourceId, targetId;
		DefaultLink[] links = tree.edgeSet().toArray(new DefaultLink[0]);
//		String linkSourceId;//, linkTargetId;

		List<LabeledLink> temp1 = null;
//		List<LabeledLink> temp2 = null;
		List<LabeledLink> possibleLinks = new ArrayList<>();
		
		for (DefaultLink link : links) {
			if (link instanceof CompactLink) {
			
				// links from source to target
				sourceId = link.getSource().getId();
				targetId = link.getTarget().getId();
				
				possibleLinks.clear();
				
				if (link instanceof CompactSubClassLink) {
					temp1 = this.graphBuilder.getPossibleLinks(sourceId, targetId, LinkType.SubClassLink, null);
//					temp2 = this.graphBuilder.getPossibleLinks(targetId, sourceId, LinkType.SubClassLink, null);
				} else if (link instanceof CompactObjectPropertyLink) {
					temp1 = this.graphBuilder.getPossibleLinks(sourceId, targetId, 
							LinkType.ObjectPropertyLink, ((CompactObjectPropertyLink) link).getObjectPropertyType());
//					temp2 = this.graphBuilder.getPossibleLinks(targetId, sourceId, 
//							LinkType.ObjectPropertyLink, ((CompactObjectPropertyLink) link).getObjectPropertyType());
				}
				if (temp1 != null) possibleLinks.addAll(temp1);
//				if (temp2 != null) possibleLinks.addAll(temp2);
	
				Collections.sort(possibleLinks, new LinkPriorityComparator());
				if (!possibleLinks.isEmpty()) {
					
					// pick the first one 
					LabeledLink newLink = possibleLinks.get(0);
					
//					linkSourceId = LinkIdFactory.getLinkSourceId(newLink.getId());
					//linkTargetId = LinkIdFactory.getLinkTargetId(newLink.getId());
					
//					if (linkSourceId.equals(sourceId)) {
						tree.addEdge(link.getSource(), link.getTarget(), newLink);
						tree.setEdgeWeight(newLink, link.getWeight());
						if (allowedChangingGraph) this.graphBuilder.addLink(link.getSource(), link.getTarget(), newLink);
//					} else {
//						tree.addEdge(link.getTarget(), link.getSource(), newLink);
//						tree.setEdgeWeight(newLink, link.getWeight());
//						if (allowedChangingGraph) this.graphBuilder.addLink(link.getTarget(), link.getSource(), newLink);
//					}
					
					tree.removeEdge(link);
					if (allowedChangingGraph) this.graphBuilder.removeLink(link);
	
				} else {
					logger.error("Something is going wrong. " +
							"There should be at least one possible object property between " +
							link.getSource().getLabel().getUri() + 
							" and " + link.getTarget().getLabel().getUri());
					return;
				}
			}
		}
	}
	
	private void addLinks(Set<LabeledLink> links) {
		if (links == null)
			return;
		
		for (LabeledLink link : links) {
			if (!this.tree.containsEdge(link) &&
					this.tree.containsVertex(link.getSource()) &&
					this.tree.containsVertex(link.getTarget())) {
				this.tree.addEdge(link.getSource(), link.getTarget(), link);
			}
		}
	}
	
	
}
