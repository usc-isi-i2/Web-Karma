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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;

public class DisplayModel {

	private static Logger logger = LoggerFactory.getLogger(DisplayModel.class);

	private List<DirectedWeightedMultigraph<Node, LabeledLink>> models;
	private HashMap<Node, Integer> nodesLevel;
	private HashMap<Node, Set<ColumnNode>> nodesSpan;
	private HTable hTable;
	
	public DisplayModel(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		this.models = new ArrayList<>();
		this.computeModels(model);
		this.nodesLevel = new HashMap<>();
		this.nodesSpan = new HashMap<>();
		this.hTable = null;
		
		for(DirectedWeightedMultigraph<Node, LabeledLink> subModel : models) {
			levelingCyclicGraph(subModel);
			computeNodeSpan(subModel);
		}
	}
	
	public DisplayModel(DirectedWeightedMultigraph<Node, LabeledLink> model, HTable hTable) {
		this.models = new ArrayList<>();
		this.computeModels(model);
		this.nodesLevel = new HashMap<>();
		this.nodesSpan = new HashMap<>();
		this.hTable = hTable;

		int modelNum = 1;
		for(DirectedWeightedMultigraph<Node, LabeledLink> subModel : this.models) {
			logger.debug(modelNum++ + "Start levelingCyclicGraph");
			levelingCyclicGraph(subModel);
			logger.debug(modelNum + "After levelingCyclicGraph");
			printLevels();
	
			computeNodeSpan(subModel);
			printSpans();
			
			updateNodeLevelsConsideringOverlaps(subModel);
		}
		
		logger.debug("After updateNodeLevelsConsideringOverlaps");
		printLevels();
		printSpans();
		
		//1. Now get the nodes that have no node spans. These are unconnected nodes.
		List<Node> spanNodes = new ArrayList<>();
		List<Node> noSpanNodes = new ArrayList<>();
		int maxLevel = getMaxLevel(true);

		for(Entry<Node, Set<ColumnNode>> nodeSetEntry : nodesSpan.entrySet()) {
			if(nodeSetEntry.getValue().isEmpty()) {
				noSpanNodes.add(nodeSetEntry.getKey());
			} else {
				spanNodes.add(nodeSetEntry.getKey());
			}
			nodesLevel.put(nodeSetEntry.getKey(), maxLevel - nodesLevel.get(nodeSetEntry.getKey()));
		}
		
		maxLevel = getMaxLevel(spanNodes);
		if(maxLevel == 0) maxLevel++;
		for(Node n : noSpanNodes) {
			nodesLevel.put(n, nodesLevel.get(n)+maxLevel);
		}
		
		//Remove missing Levels
		int newMaxLevel = getMaxLevel(false);
		Map<Integer, Set<Node>> nodesAtLevel = getLevelToNodes(null, false);
		for(int i=maxLevel; i<newMaxLevel; i++) {
			Set<Node> nodes = nodesAtLevel.get(i);
			if(nodes == null || nodes.isEmpty()) {
				//move all at i+1 here
				int next = i+1;
				boolean done = false;
				while(!done && next <= newMaxLevel) {
					Set<Node> upper = nodesAtLevel.get(next);
					if(upper != null && !upper.isEmpty()) {
						done = true;
						nodesAtLevel.put(next, null);
						for(Node n : upper) {
							nodesLevel.put(n, i);
						}
					}
					next++;
				}
			}
		}
		
		logger.debug("Final Levels");
		printLevels();
		printSpans();
		
		logger.debug("finished leveling the model.");
	}

	public Set<LabeledLink> getOutgoingEdgesOf(Node node) {
		Set<LabeledLink> edges = new HashSet<>();
		for(DirectedWeightedMultigraph<Node, LabeledLink> model : models) {
			GraphUtil.printGraph(GraphUtil.asDefaultGraph(model));
			if(model.containsVertex(node))
				edges.addAll(model.outgoingEdgesOf(node));
		}
		return edges;
	}

	public HashMap<Node, Integer> getNodesLevel() {
		return nodesLevel;
	}

	public HashMap<Node, Set<ColumnNode>> getNodesSpan() {
		return nodesSpan;
	}

	private static HashMap<Node, Integer> inDegreeInSet(DirectedWeightedMultigraph<Node, LabeledLink> g, 
			Set<Node> nodes, boolean includeSelfLinks) {
		
		HashMap<Node, Integer> nodeToInDegree = new HashMap<>();
		if (g == null || nodes == null) return nodeToInDegree;
		for (Node n : nodes) {
			Set<LabeledLink> incomingLinks = g.incomingEdgesOf(n);
			if (incomingLinks == null || incomingLinks.isEmpty()) {
				nodeToInDegree.put(n, 0);
			} else {
				int count = 0;
				for (LabeledLink l : incomingLinks) {
					if (includeSelfLinks) {
						if (nodes.contains(l.getSource())) count++;
					} else {
						if (nodes.contains(l.getSource()) && !n.equals(l.getSource())) count++;
					}
				}
				nodeToInDegree.put(n, count);
			}
		}
		return nodeToInDegree;
	}
	
	private static HashMap<Node, Integer> outDegreeInSet(DirectedWeightedMultigraph<Node, LabeledLink> g, 
			Set<Node> nodes, boolean includeSelfLinks) {
		
		HashMap<Node, Integer> nodeToOutDegree = new HashMap<>();
		if (g == null || nodes == null) return nodeToOutDegree;
		for (Node n : nodes) {
			Set<LabeledLink> outgoingLinks = g.outgoingEdgesOf(n);
			if (outgoingLinks == null || outgoingLinks.isEmpty()) {
				nodeToOutDegree.put(n, 0);
			} else {
				int count = 0;
				for (LabeledLink l : outgoingLinks) {
					if (includeSelfLinks) {
						if (nodes.contains(l.getSource())) count++;
					} else {
						if (nodes.contains(l.getSource()) && !n.equals(l.getSource())) count++;
					}
				}
				nodeToOutDegree.put(n, count);
			}
		}
		return nodeToOutDegree;
	}
	
	public boolean isModelEmpty() {
		if(models.isEmpty())
			return true;
		
		for(DirectedWeightedMultigraph<Node, LabeledLink> model : models) {
			if (model != null && model.vertexSet() != null && !model.vertexSet().isEmpty()) 
				return false;
		}
		return true;
	}
	
	private Set<Node> getAllColumnNodes(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		Set<Node> columnNodes = new HashSet<>();
		for (Node u : model.vertexSet()) {
			if (u instanceof ColumnNode)
				columnNodes.add(u);
		}
		return columnNodes;
	}
	
	private boolean cycleExits(DirectedWeightedMultigraph<Node, LabeledLink> model, Set<Node> columnNodes, Set<Node> traversedNodes, Node start, Node end) {
		Set<Node> neighbors = GraphUtil.getOutNeighbors(GraphUtil.asDefaultGraph(model), start);
		logger.debug("start:" + start.getDisplayId() + ", end:" + end.getDisplayId());
		for (Node w : neighbors) {
			if(w == end) {
				return true;
			}
			if(columnNodes.contains(w) || traversedNodes.contains(w))
				continue;
			
			traversedNodes.add(w);
			logger.debug("neighbour:" + w.getDisplayId());
			boolean innerCycle = cycleExits(model, columnNodes, traversedNodes, w, end);
			if(innerCycle)
				return true;
		}
		return false;
	}
	
	private void levelingCyclicGraph(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		
		if (isModelEmpty()) {
			logger.debug("graph does not have any node.");
			return ;
		}
		
		Set<Node> columnNodes = getAllColumnNodes(model);
		Set<Node> markedNodes = new HashSet<>();
		markedNodes.addAll(columnNodes);
		
		Queue<Node> q = new LinkedList<>();
		int maxLevel = model.vertexSet().size();
				
		for (Node u : model.vertexSet()) {
			if (!markedNodes.contains(u)) {
				q.add(u);
				markedNodes.add(u);
				
				nodesLevel.put(u, 0);
				
				while (!q.isEmpty()) {
					Node v = q.remove();
					Set<Node> neighbors = GraphUtil.getOutNeighbors(GraphUtil.asDefaultGraph(model), v);
					for (Node w : neighbors) {
						if(!columnNodes.contains(w)) {
							int level = nodesLevel.get(v).intValue() + 1;
							boolean levelChanged = false;
							if(!nodesLevel.containsKey(w) || nodesLevel.get(w) < level) {
								if(nodesLevel.containsKey(w)) {
									if(cycleExits(model, columnNodes, new HashSet<Node>(), v, w))
										continue;
								}
								nodesLevel.put(w, level);
								levelChanged = true;
							}
							
							markedNodes.add(w);
							if(levelChanged)
								q.add(w);
						}
					}
				}
			}
		}
		
		Map<Integer, Set<Node>> levelToNodes = getLevelToNodes(model, false);
		
		// find in/out degree in each level
		int k = 0;
		while (true) {
			
			if (k >= maxLevel) break;
			
			Node nodeWithMaxDegree = null;
			while (true) { // until there is a direct link between two nodes in the same level
				
				Set<Node> nodes = levelToNodes.get(k);
				if (nodes == null || nodes.isEmpty()) break;
				
				HashMap<Node, Integer> nodeToInDegree = inDegreeInSet(model, nodes, false);
				HashMap<Node, Integer> nodeToOutDegree = outDegreeInSet(model, nodes, false);
				
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
				
				if (levelToNodes.get(k + 1) == null) {
					levelToNodes.put(k + 1, new HashSet<Node>());
				}
				// moving nodeWithMaxDegree to the next level 
				nodesLevel.put(nodeWithMaxDegree, k + 1);
				levelToNodes.get(k).remove(nodeWithMaxDegree);
				levelToNodes.get(k + 1).add(nodeWithMaxDegree);
			}
			
			k ++; // checking next level
		}
		
		
		// add all column nodes to the (last level + 1).
		int lastLevel = getMaxLevel(false);
		for (Node u : model.vertexSet()) {
			if (u instanceof ColumnNode)
				nodesLevel.put(u, lastLevel + 1);
		}
		
	}
	
	public HashMap<Integer, Set<Node>> getLevelToNodes(DirectedWeightedMultigraph<Node, LabeledLink> model, 
			boolean considerColumnNodes) {

		HashMap<Integer, Set<Node>> levelToNodes =
				new HashMap<>();
		
		if (this.nodesLevel == null)
			return levelToNodes;
		
		for (Entry<Node, Integer> entry : nodesLevel.entrySet()) {
			Set<Node> nodes = levelToNodes.get(entry.getValue());
			if (nodes == null) {
				nodes = new HashSet<>();
				levelToNodes.put(entry.getValue(), nodes);
			}
			
			if (!considerColumnNodes && entry.getKey() instanceof ColumnNode)
				continue;
			
			Node node = entry.getKey();
			if(model == null || model.containsVertex(node))
				nodes.add(node);
			
		}
		
		return levelToNodes;
	}
	
	public int getMaxLevel(boolean considerColumnNodes) {
		
		if (this.nodesLevel == null)
			return 0;

		int maxLevel = 0;

		for (Entry<Node, Integer> entry : nodesLevel.entrySet()) {

			if (!considerColumnNodes) {
				if (!(entry.getKey() instanceof ColumnNode) && entry.getValue().intValue() > maxLevel) 
					maxLevel = entry.getValue().intValue();
			} else {
				if (entry.getValue().intValue() > maxLevel) 
					maxLevel = entry.getValue().intValue();
			}
		}
		
		return maxLevel;
	}
	
	private int getMaxLevel(List<Node> nodes) {
		int maxLevel = 0;

		for (Node node : nodes) {
			int level = nodesLevel.get(node);
			if (level > maxLevel) 
				maxLevel = level;
		}
		
		return maxLevel;
	}
	
	private void computeNodeSpan(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		
		if (isModelEmpty()) {
			logger.debug("graph does not have any node.");
			return;
		}
		
		// Add empty set for all internal nodes
		for (Node n : model.vertexSet()) {
			Set<ColumnNode> columnNodes = new HashSet<>();
			nodesSpan.put(n, columnNodes);
		}
		
		Map<Integer, Set<Node>> levelToNodes = getLevelToNodes(model, true);
		Set<ColumnNode> allColumnNodes = new HashSet<>();
		
		int i = getMaxLevel(true);
		while (i >= 0) {
			
			Set<Node> nodes = levelToNodes.get(i);
			if (nodes != null && !nodes.isEmpty()) {
				for (Node n : nodes) {
					
					if (n instanceof ColumnNode) {
						this.nodesSpan.get(n).add((ColumnNode)n);
						allColumnNodes.add((ColumnNode)n);
						continue;
					}
					
					List<Node> neighborsInLowerLevel = new ArrayList<>();
					
					// finding the nodes connected to n (incoming & outgoing) from a lower level
					Set<LabeledLink> outgoingLinks = model.outgoingEdgesOf(n);
					if (outgoingLinks != null && !outgoingLinks.isEmpty()) 
						for (LabeledLink l : outgoingLinks) 
							if (nodesLevel.get(l.getTarget()) > nodesLevel.get(n))
								neighborsInLowerLevel.add(l.getTarget());
					
					Set<LabeledLink> incomingLinks = model.incomingEdgesOf(n);
					if (incomingLinks != null && !incomingLinks.isEmpty()) 
						for (LabeledLink l : incomingLinks) 
							if (nodesLevel.get(l.getSource()) > nodesLevel.get(n))
								neighborsInLowerLevel.add(l.getSource());
					
					// To handle a dangling internal node: put it in a completely separate level
					if (neighborsInLowerLevel == null || neighborsInLowerLevel.isEmpty()) {
						//if(!n.isForceAddedByUser()) //If node was added by user, then it might be ok to not give it any span
							this.nodesSpan.get(n).addAll(allColumnNodes);
					}
					
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
	
	private boolean overlap(Node n1, Node n2) {
		
		if (this.hTable == null || this.hTable.getOrderedNodeIds() == null)
			return false;

		Set<ColumnNode> n1Span = this.nodesSpan.get(n1);
		Set<ColumnNode> n2Span = this.nodesSpan.get(n2);
		
		if (n1Span == null || n2Span == null)
			return false;

		Set<String> n1NodeIds = new HashSet<>();
		Set<String> n2NodeIds = new HashSet<>();
		
		for (ColumnNode c : n1Span)
			if (c != null)
				n1NodeIds.add(c.getHNodeId());

		for (ColumnNode c : n2Span)
			if (c != null)
				n2NodeIds.add(c.getHNodeId());

		
		List<Integer> n1SpanPositions = new ArrayList<>();
		List<Integer> n2SpanPositions = new ArrayList<>();
		List<HNode> orderedNodeIds = new ArrayList<>();

		this.hTable.getSortedLeafHNodes(orderedNodeIds);
		if (orderedNodeIds != null)
		for (int i = 0; i < orderedNodeIds.size(); i++) {
			String hNodeId = orderedNodeIds.get(i).getId();
			if (n1NodeIds.contains(hNodeId))
				n1SpanPositions.add(i);
			if (n2NodeIds.contains(hNodeId))
				n2SpanPositions.add(i);
		}
		
		if (n1SpanPositions.isEmpty() || n2SpanPositions.isEmpty())
			return false;
		
		if (n1SpanPositions.get(0) <= n2SpanPositions.get(0) && n1SpanPositions.get(n1SpanPositions.size() - 1) >= n2SpanPositions.get(0)) {
			logger.debug("node " + n1.getId() + " overlaps node " + n2.getId());
			return true;
		}
		
		if (n2SpanPositions.get(0) <= n1SpanPositions.get(0) && n2SpanPositions.get(n2SpanPositions.size() - 1) >= n1SpanPositions.get(0)) {
			logger.debug("node " + n1.getId() + " overlaps node " + n2.getId());
			return true;
		}
		
		return false;
	}
	
	private HashMap<Node, Integer> getNodeOverlap(Set<Node> nodes) {
		
		HashMap<Node, Integer> nodesOverlap = new HashMap<>();
		
		int count;
		for (Node n1 : nodes) {
			count = 0;
			for (Node n2 : nodes) {
				if (n1.equals(n2))
					continue;
				if (overlap(n1, n2))
					count++;
			}
			nodesOverlap.put(n1, count);
		}
		return nodesOverlap;
	}
	
	private void updateNodeLevelsConsideringOverlaps(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		
		if (hTable == null || this.nodesLevel == null || this.nodesSpan == null)
			return;
		
		int maxLevel = model.vertexSet().size();

		Map<Integer, Set<Node>> levelToNodes = getLevelToNodes(model, false);

		// find in/out degree in each level
		int k = 0;
		while (true) {
			
			if (k >= maxLevel) break;
			
			Node nodeWithMaxDegree = null, nodeWithMinOverlap = null;
			while (true) { // until there is a direct link between two nodes in the same level
				
				Set<Node> nodes = levelToNodes.get(k);
				if (nodes == null || nodes.isEmpty()) break;
				
				HashMap<Node, Integer> nodesOverlap = getNodeOverlap(nodes);
				HashMap<Node, Integer> nodeToInDegree = inDegreeInSet(model, nodes, false);
				HashMap<Node, Integer> nodeToOutDegree = outDegreeInSet(model, nodes, false);
				
				int sumOfIntraLinks = 0, sumOfOverlaps = 0; 
				int d = 0, overlap = 0;
				int maxDegree = -1, minOverlap = Integer.MAX_VALUE;
				
				for (Node u : nodes) {
					
					d = nodeToInDegree.get(u);
					sumOfIntraLinks += d;
					if (d > maxDegree) {
						maxDegree = d;
						nodeWithMaxDegree = u;
					}
					
					d = nodeToOutDegree.get(u);
					sumOfIntraLinks += d;
					if (d > maxDegree) {
						maxDegree = d;
						nodeWithMaxDegree = u;
					}
					
					overlap = nodesOverlap.get(u); // move the node with minimum number of overlaps (probably higher span) to the next level
					sumOfOverlaps += overlap;
					if ( 
							(overlap > 0 && 
							overlap < minOverlap) 
							|| 
							(overlap == minOverlap && 
							this.nodesSpan.get(u) != null &&
							this.nodesSpan.get(nodeWithMinOverlap) != null && 
							this.nodesSpan.get(u).size() < this.nodesSpan.get(nodeWithMinOverlap).size())
						) {
						minOverlap = overlap;
						nodeWithMinOverlap = u;
					}
				}
				
				if (sumOfIntraLinks == 0 && sumOfOverlaps == 0) break; // there is no interlink in level k and there is no overlap
				
				if (levelToNodes.get(k + 1) == null) {
					levelToNodes.put(k + 1, new HashSet<Node>());
				}
				
				if (sumOfIntraLinks != 0 && sumOfOverlaps == 0) {
					// moving nodeWithMaxDegree to the next level 
					nodesLevel.put(nodeWithMaxDegree, k + 1);
					levelToNodes.get(k).remove(nodeWithMaxDegree);
					levelToNodes.get(k + 1).add(nodeWithMaxDegree);
				} else 	{
					// moving nodeWithMaxDegree to the next level 
					nodesLevel.put(nodeWithMinOverlap, k + 1);
					levelToNodes.get(k).remove(nodeWithMinOverlap);
					levelToNodes.get(k + 1).add(nodeWithMinOverlap);
				}
			}
			
			k ++; // checking next level
		}
		
		
		// add all column nodes to the (last level + 1).
		int lastLevel = getMaxLevel(false);
		for (Node u : model.vertexSet()) {
			if (u instanceof ColumnNode)
				nodesLevel.put(u, lastLevel + 1);
		}
		
		
	}
	
	public void printLevels() {
		for (Entry<Node, Integer> entry : this.nodesLevel.entrySet()) {
			logger.debug(entry.getKey().getId() + " ---> " + entry.getValue().intValue());
		}
	}
	
	public void printSpans() {
		for (Entry<Node, Set<ColumnNode>> entry : this.nodesSpan.entrySet()) {
			logger.debug(entry.getKey().getId() + " spans ---> ");
			if (entry.getValue() != null)
				for (ColumnNode columnNode : entry.getValue()) {
					logger.debug("\t" + columnNode.getColumnName());
				}
		}
	}

	public void printModel(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		logger.debug("Vertices: ");
		for(Node n : model.vertexSet()) {
			logger.debug("\t" + n.getId());
		}
		logger.debug("Edges: ");
		for(LabeledLink l : model.edgeSet()) {
			logger.debug("\t" + l.getSource().getId() + " --> " + l.getTarget().getId());
		}
	}
	
	private void computeModels(DirectedWeightedMultigraph<Node, LabeledLink> model) {
		
		for(Node n : model.vertexSet()) {
			DirectedWeightedMultigraph<Node, LabeledLink> nodeModel = findModel(n);
			if(nodeModel == null) {
				nodeModel = new DirectedWeightedMultigraph<>(LabeledLink.class); 
				this.models.add(nodeModel);
				nodeModel.addVertex(n);
			}
			
			for(LabeledLink link : model.incomingEdgesOf(n)) {
				Node source = link.getSource();
				Node target = link.getTarget();
				DirectedWeightedMultigraph<Node, LabeledLink> sourceModel = findModel(source);
				DirectedWeightedMultigraph<Node, LabeledLink> targetModel = findModel(target);
				if(sourceModel != null && sourceModel != nodeModel) {
					mergeModels(sourceModel, nodeModel);
				}
				if(targetModel != null && targetModel != nodeModel) {
					mergeModels(targetModel, nodeModel);
				}
				nodeModel.addVertex(source);
				nodeModel.addVertex(target);
				nodeModel.addEdge(source, target, link);
			}
		}
		
		logger.debug("Computed " + this.models.size() + " models");
		int modelNum = 1;
		for(DirectedWeightedMultigraph<Node, LabeledLink> subModel : this.models) {
			logger.debug("Model: " + modelNum++);
			printModel(subModel);
		}
	}
	
	private DirectedWeightedMultigraph<Node, LabeledLink> findModel(Node n) {
		for(DirectedWeightedMultigraph<Node, LabeledLink> model : this.models) {
			if(model.containsVertex(n))
				return model;
		}
		return null;
	}
	
	private void mergeModels(DirectedWeightedMultigraph<Node, LabeledLink> model1, DirectedWeightedMultigraph<Node, LabeledLink> model2) {
		for(Node n : model1.vertexSet()) {
			model2.addVertex(n);
			for(LabeledLink link : model1.incomingEdgesOf(n)) {
				model2.addVertex(link.getSource());
				model2.addVertex(link.getTarget());
				model2.addEdge(link.getSource(), link.getTarget(), link);
			}
			
		}
		this.models.remove(model1);
	}
}
