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

package edu.isi.karma.rep.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.HTable;

public class DisplayModel {

	private static Logger logger = LoggerFactory.getLogger(DisplayModel.class);

	private DirectedWeightedMultigraph<Node, Link> model;
	private HashMap<Node, Integer> nodesLevel;
	private HashMap<Node, Set<ColumnNode>> nodesSpan;
	private HTable hTable;
	
	public DisplayModel(DirectedWeightedMultigraph<Node, Link> model) {
		this.model = model;
		this.nodesLevel = new HashMap<Node, Integer>();
		this.nodesSpan = new HashMap<Node, Set<ColumnNode>>();
		this.hTable = null;
		
		levelingCyclicGraph();
		computeNodeSpan();
	}
	
	public DisplayModel(DirectedWeightedMultigraph<Node, Link> model, HTable hTable) {
		this.model = model;
		this.nodesLevel = new HashMap<Node, Integer>();
		this.nodesSpan = new HashMap<Node, Set<ColumnNode>>();
		this.hTable = hTable;
		
		levelingCyclicGraph();
		computeNodeSpan();
		updateNodeLevelsConsideringOverlaps();
	}

	public DirectedWeightedMultigraph<Node, Link> getModel() {
		return model;
	}

	public void setModel(DirectedWeightedMultigraph<Node, Link> model) {
		this.model = model;
	}

	public HashMap<Node, Integer> getNodesLevel() {
		return nodesLevel;
	}

	public void setNodesLevel(HashMap<Node, Integer> nodesLevel) {
		this.nodesLevel = nodesLevel;
	}

	public HashMap<Node, Set<ColumnNode>> getNodesSpan() {
		return nodesSpan;
	}

	public void setNodesSpan(HashMap<Node, Set<ColumnNode>> nodesSpan) {
		this.nodesSpan = nodesSpan;
	}
	
	private void computeNodeSpan() {
		
		if (this.model == null || this.model.vertexSet() == null || this.model.vertexSet().size() == 0) {
			logger.debug("graph does not have any node.");
			return;
		}
		
		// Add empty set for all internal nodes
		for (Node n : this.model.vertexSet()) {
			Set<ColumnNode> columnNodes = new HashSet<ColumnNode>();
			nodesSpan.put(n, columnNodes);
		}
		
		HashMap<Integer, List<Node>> levelToNodes = 
				new HashMap<Integer, List<Node>>();
		
		int maxLevel = 0;
		for (Entry<Node, Integer> entry : nodesLevel.entrySet()) {
			List<Node> nodes = levelToNodes.get(entry.getValue());
			if (nodes == null) {
				nodes = new ArrayList<Node>();
				levelToNodes.put(entry.getValue(), nodes);
			}
			nodes.add(entry.getKey());
			
			if (entry.getValue().intValue() > maxLevel) {
				maxLevel = entry.getValue().intValue();
			}
		}
		
		int i = maxLevel;
		while (i >= 0) {
			
			List<Node> nodes = levelToNodes.get(i);
			if (nodes != null && !nodes.isEmpty()) {
				for (Node n : nodes) {
					
					if (n instanceof ColumnNode) {
						this.nodesSpan.get(n).add((ColumnNode)n);
						continue;
					}
					
					List<Node> neighborsInLowerLevel = new ArrayList<Node>();
					
					// finding the nodes connected to n (incoming & outgoing) from a lower leve
					Set<Link> outgoingLinks = this.model.outgoingEdgesOf(n);
					if (outgoingLinks != null && !outgoingLinks.isEmpty()) 
						for (Link l : outgoingLinks) 
							if (nodesLevel.get(l.getTarget()) > nodesLevel.get(n))
								neighborsInLowerLevel.add(l.getTarget());
					
					Set<Link> incomingLinks = this.model.incomingEdgesOf(n);
					if (incomingLinks != null && !incomingLinks.isEmpty()) 
						for (Link l : incomingLinks) 
							if (nodesLevel.get(l.getSource()) > nodesLevel.get(n))
								neighborsInLowerLevel.add(l.getSource());
					
					for (Node nn : neighborsInLowerLevel) {
						if (nn instanceof ColumnNode) {
							this.nodesSpan.get(n).add((ColumnNode)nn);
						} else if (nn instanceof InternalNode) {
							this.nodesSpan.get(n).addAll(this.nodesSpan.get((InternalNode)nn));
						}
					}
					
				}
			}
			
			i--;
		}
	}
	
	private void updateNodeLevelsConsideringOverlaps() {
		
		if (hTable == null)
			return;
		
		
	}
	
	private void levelingCyclicGraph() {
		
		if (this.model == null || this.model.vertexSet() == null || this.model.vertexSet().size() == 0) {
			logger.debug("graph does not have any node.");
			return ;
		}
		
		Set<Node> markedNodes = new HashSet<Node>();
		for (Node u : this.model.vertexSet()) {
			if (u instanceof ColumnNode)
				markedNodes.add(u);
		}
		
		Queue<Node> q = new LinkedList<Node>();
		int maxLevel = this.model.vertexSet().size();
		
		List<Set<Node>> nodesIndexedByLevel = new ArrayList<Set<Node>>();
		for (int i = 0; i < maxLevel; i++) nodesIndexedByLevel.add(new HashSet<Node>());
		
		for (Node u : this.model.vertexSet()) {
			if (!markedNodes.contains(u)) {
				q.add(u);
				markedNodes.add(u);
				
				nodesLevel.put(u, 0);
				nodesIndexedByLevel.get(0).add(u);
				
				while (!q.isEmpty()) {
					Node v = q.remove();
					Set<Node> neighbors = GraphUtil.getOutNeighbors(this.model, v);
					for (Node w : neighbors) {
						if (!markedNodes.contains(w)) {
							markedNodes.add(w);
							int level = nodesLevel.get(v).intValue() + 1;
							nodesLevel.put(w, level);
							nodesIndexedByLevel.get(level).add(w);
							q.add(w);
						}
					}
				}
			}
		}
		
		// find in/out degree in each level
		int k = 0;
		while (true) {
			
			if (k >= maxLevel) break;
			
			Node nodeWithMaxDegree = null;
			while (true) { // until there is a direct link between two nodes in the same level
				
				Set<Node> nodes = nodesIndexedByLevel.get(k);
				if (nodes == null || nodes.size() == 0) break;
				
				HashMap<Node, Integer> nodeToInDegree = GraphUtil.inDegreeInSet(this.model, nodes, false);
				HashMap<Node, Integer> nodeToOutDegree = GraphUtil.outDegreeInSet(this.model, nodes, false);
				
				int sum = 0, d = 0;
				int maxDegree = -1;
				
				for (Node u : nodes) {
					d = nodeToInDegree.get(u);
					sum += d;
					if (d > maxDegree) {
						maxDegree = d;
						nodeWithMaxDegree = u;
					}
					d = nodeToOutDegree.get(u);
					sum += d;
					if (d > maxDegree) {
						maxDegree = d;
						nodeWithMaxDegree = u;
					}
				}
				if (sum == 0) break; // there is no interlink in level k
				
				// moving nodeWithMaxDegree to the next level 
				nodesLevel.put(nodeWithMaxDegree, k + 1);
				nodesIndexedByLevel.get(k).remove(nodeWithMaxDegree);
				nodesIndexedByLevel.get(k + 1).add(nodeWithMaxDegree);
			}
			
			k ++; // checking next level
		}
		
		
		// add all column nodes to the (last level + 1).
		int lastLevel = 0;
		for (k = maxLevel - 1; k > 0; k--) {
			if (!nodesIndexedByLevel.get(k).isEmpty()) {
				lastLevel = k;
				break;
			}
		}
		for (Node u : this.model.vertexSet()) {
			if (u instanceof ColumnNode)
				nodesLevel.put(u, lastLevel + 1);
		}
		
	}
	
}
