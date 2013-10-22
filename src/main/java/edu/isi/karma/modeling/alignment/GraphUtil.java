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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class GraphUtil {

	private static Logger logger = LoggerFactory.getLogger(GraphUtil.class);
	
	// FIXME: change methods to get an Outputstream as input and write on it.
	
	private static String getNodeTypeString(Node node, StringBuffer sb) {
		
		if (node == null) {
			sb.append("node is null.");
			return null;
		}
		
		String s = node.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}

	private static String getLinkTypeString(Link link, StringBuffer sb) {

		if (link == null) {
			sb.append("link is null.");
			return null;
		}
		
		String s = link.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}
	
	public static void printVertex(Node node, StringBuffer sb) {
		
		if (node == null) {
			sb.append("node is null.");
			return;
		}
		
		sb.append("(");
		sb.append( node.getLocalId());
//    	sb.append( vertex.getID());
		sb.append(", ");
		if (node instanceof ColumnNode)
			sb.append( ((ColumnNode)node).getColumnName());
		else
			sb.append(node.getLabel().getLocalName());
		sb.append(", ");
		sb.append(getNodeTypeString(node, sb));
		sb.append(")");
	}
	
	public static void printEdge(Link link, StringBuffer sb) {
		
		if (link == null) {
			sb.append("link is null.");
			return;
		}
		
		sb.append("(");
    	sb.append( link.getLocalId());
    	sb.append(", ");
    	sb.append(link.getLabel().getLocalName());
    	sb.append(", ");
    	sb.append(getLinkTypeString(link, sb));
    	sb.append(", ");
    	sb.append(link.getWeight());
    	sb.append(") - From ");
    	printVertex(link.getSource(), sb);
    	sb.append(" To ");
    	printVertex(link.getTarget(), sb);
	}

	public static DirectedGraph<Node, Link> asDirectedGraph(UndirectedGraph<Node, Link> undirectedGraph) {
		
		if (undirectedGraph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		DirectedGraph<Node, Link> g = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		for (Node v : undirectedGraph.vertexSet())
			g.addVertex(v);
		
		for (Link e: undirectedGraph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static void printGraph(Graph<Node, Link> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return;
		}		
		StringBuffer sb = new StringBuffer();
		sb.append("*** Nodes ***");
		for (Node vertex : graph.vertexSet()) {
			printVertex(vertex, sb);
			sb.append("\n");
        }
		sb.append("*** Links ***");
		for (Link edge : graph.edgeSet()) {
			printEdge(edge, sb);
			sb.append("\n");
        }
		sb.append("------------------------------------------");
		logger.debug(sb.toString());
		
	}
	
	public static void printGraphSimple(Graph<Node, Link> graph) {
		
		if (graph == null) {
			logger.debug("The input graph is null.");
			return;
		}		

		StringBuffer sb = new StringBuffer();
		for (Link edge : graph.edgeSet()) {
			sb.append("(");
//			if (edge.getSource() instanceof ColumnNode)
//				sb.append(edge.getSource().getLocalId() + "-" + ((ColumnNode)edge.getSource()).getColumnName());
//			else
//				sb.append(edge.getSource().getLocalId());
//			sb.append(")");
//			sb.append(" - ");
//			sb.append("(");
			sb.append(edge.getId());
//			sb.append(")");
//			sb.append(" - ");
//			sb.append("(");
//			if (edge.getTarget() instanceof ColumnNode)
//				sb.append(edge.getTarget().getLocalId() + "-" + ((ColumnNode)edge.getTarget()).getColumnName());
//			else
//				sb.append(edge.getTarget().getLocalId());
//			sb.append(")");
			sb.append(" - status=" + edge.getStatus().name());
			sb.append(" - w=" + edge.getWeight());
			sb.append("\n");
        }
		sb.append("------------------------------------------");
		logger.debug(sb.toString());
		
	}
	
//	@SuppressWarnings("unchecked")
//	public static DirectedWeightedMultigraph<Node, Link> treeToRootedTree(
//			DirectedWeightedMultigraph<Node, Link> tree, Node root, Set<String> reversedLinks, Set<String> removedLinks) {
//		
//		if (tree == null) {
//			logger.error("The input tree is null.");
//			return null;
//		}		
//
//		DirectedWeightedMultigraph<Node, Link> rootedTree = 
//				(DirectedWeightedMultigraph<Node, Link>)tree.clone();
//		if (reversedLinks == null)
//			reversedLinks = new HashSet<String>();
//		if (removedLinks == null)
//			removedLinks = new HashSet<String>();
//		treeToRootedTree(rootedTree, root, null, new HashSet<Node>(), reversedLinks, removedLinks);
//		
//		logger.info("model after converting to a rooted tree: ");
//		printGraphSimple(rootedTree);
//		
//		logger.info("reversed links:");
//		for (String s : reversedLinks)
//			logger.info("\t" + s);
//		logger.info("removed links:");
//		for (String s : removedLinks)
//			logger.info("\t" + s);
//
//		return rootedTree;
//	}
	
	public static void serialize(DirectedWeightedMultigraph<Node, Link> graph, String fileName) throws Exception
	{
		
		if (graph == null) {
			logger.error("The input graph is null.");
			return;
		}		

//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		FileOutputStream f = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(f);

		out.writeObject(graph);
		out.flush();
		out.close();
	}
	
	@SuppressWarnings("unchecked")
	public static DirectedWeightedMultigraph<Node, Link> deserialize(String fileName) throws Exception
	{
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		FileInputStream f = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(f);

        Object obj  = in.readObject();
        in.close();
        
        if (obj instanceof DirectedWeightedMultigraph<?, ?>)
        	return (DirectedWeightedMultigraph<Node, Link>)obj;
        else 
        	return null;
	}
	
	private static Set<Node> getNeighbors(DirectedWeightedMultigraph<Node, Link> g, Node n) {
		
		Set<Node> neighbors = new HashSet<Node>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		Set<Link> outgoingLinks = g.outgoingEdgesOf(n);
		if (outgoingLinks != null) {
			for (Link l : outgoingLinks) {
				neighbors.add(l.getTarget());
			}
		}
		
		return neighbors;
	}

	private static HashMap<Node, Integer> inDegreeInSet(DirectedWeightedMultigraph<Node, Link> g, 
			Set<Node> nodes, boolean includeSelfLinks) {
		
		HashMap<Node, Integer> nodeToInDegree = new HashMap<Node, Integer>();
		if (g == null || nodes == null) return nodeToInDegree;
		for (Node n : nodes) {
			Set<Link> incomingLinks = g.incomingEdgesOf(n);
			if (incomingLinks == null || incomingLinks.size() == 0) {
				nodeToInDegree.put(n, 0);
			} else {
				int count = 0;
				for (Link l : incomingLinks) {
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
	
	private static HashMap<Node, Integer> outDegreeInSet(DirectedWeightedMultigraph<Node, Link> g, 
			Set<Node> nodes, boolean includeSelfLinks) {
		
		HashMap<Node, Integer> nodeToOutDegree = new HashMap<Node, Integer>();
		if (g == null || nodes == null) return nodeToOutDegree;
		for (Node n : nodes) {
			Set<Link> outgoingLinks = g.outgoingEdgesOf(n);
			if (outgoingLinks == null || outgoingLinks.size() == 0) {
				nodeToOutDegree.put(n, 0);
			} else {
				int count = 0;
				for (Link l : outgoingLinks) {
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
	
	public static HashMap<Node, Integer> levelingCyclicGraph(DirectedWeightedMultigraph<Node, Link> g) {
		
		HashMap<Node, Integer> nodeLevels = new HashMap<Node, Integer>();
		if (g == null || g.vertexSet() == null || g.vertexSet().size() == 0) {
			logger.debug("graph does not have any node.");
			return nodeLevels;
		}
		
//		if (root != null && !g.vertexSet().contains(root)) {
//			logger.error("graph does not contain the specified root node.");
//			return nodeLevels;
//		}
		
		Set<Node> markedNodes = new HashSet<Node>();
		for (Node u : g.vertexSet()) {
			if (u instanceof ColumnNode)
				markedNodes.add(u);
		}
		
		Queue<Node> q = new LinkedList<Node>();
		int maxLevel = g.vertexSet().size();
		
		List<Set<Node>> nodesIndexedByLevel = new ArrayList<Set<Node>>();
		for (int i = 0; i < maxLevel; i++) nodesIndexedByLevel.add(new HashSet<Node>());
		
//		Set<Node> initialSeed;
//		if (root == null) {
//			initialSeed = g.vertexSet();
//		} else {
//			initialSeed = new HashSet<Node>();
//			initialSeed.add(root);
//		}
		
//		for (Node u : initialSeed) {
		for (Node u : g.vertexSet()) {
			if (!markedNodes.contains(u)) {
				q.add(u);
				markedNodes.add(u);
				
				nodeLevels.put(u, 0);
				nodesIndexedByLevel.get(0).add(u);
				
				while (!q.isEmpty()) {
					Node v = q.remove();
					Set<Node> neighbors = getNeighbors(g, v);
					for (Node w : neighbors) {
						if (!markedNodes.contains(w)) {
							markedNodes.add(w);
							int level = nodeLevels.get(v).intValue() + 1;
							nodeLevels.put(w, level);
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
				
				HashMap<Node, Integer> nodeToInDegree = inDegreeInSet(g, nodes, false);
				HashMap<Node, Integer> nodeToOutDegree = outDegreeInSet(g, nodes, false);
				
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
				nodeLevels.put(nodeWithMaxDegree, k + 1);
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
		for (Node u : g.vertexSet()) {
			if (u instanceof ColumnNode)
				nodeLevels.put(u, lastLevel + 1);
		}
		
		return nodeLevels;
	}
	
	public static HashMap<Node, Set<ColumnNode>> getNodesCoverage(
			DirectedWeightedMultigraph<Node, Link> g, 
			HashMap<Node, Integer> nodeLevels) {
		
		HashMap<Node, Set<ColumnNode>> coveredColumnNodes = 
				new HashMap<Node, Set<ColumnNode>>();

		if (g == null || g.vertexSet() == null || g.vertexSet().size() == 0) {
			logger.debug("graph does not have any node.");
			return coveredColumnNodes;
		}
		
		// Add empty set for all internal nodes
		for (Node n : g.vertexSet()) {
			Set<ColumnNode> columnNodes = new HashSet<ColumnNode>();
			coveredColumnNodes.put(n, columnNodes);
		}
		
		HashMap<Integer, List<Node>> levelToNodes = 
				new HashMap<Integer, List<Node>>();
		
		int maxLevel = 0;
		for (Entry<Node, Integer> entry : nodeLevels.entrySet()) {
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
						coveredColumnNodes.get(n).add((ColumnNode)n);
						continue;
					}
					
					List<Node> neighborsInLowerLevel = new ArrayList<Node>();
					
					// finding the nodes connected to n (incoming & outgoing) from a lower leve
					Set<Link> outgoingLinks = g.outgoingEdgesOf(n);
					if (outgoingLinks != null && !outgoingLinks.isEmpty()) 
						for (Link l : outgoingLinks) 
							if (nodeLevels.get(l.getTarget()) > nodeLevels.get(n))
								neighborsInLowerLevel.add(l.getTarget());
					
					Set<Link> incomingLinks = g.incomingEdgesOf(n);
					if (incomingLinks != null && !incomingLinks.isEmpty()) 
						for (Link l : incomingLinks) 
							if (nodeLevels.get(l.getSource()) > nodeLevels.get(n))
								neighborsInLowerLevel.add(l.getSource());
					
					for (Node nn : neighborsInLowerLevel) {
						if (nn instanceof ColumnNode) {
							coveredColumnNodes.get(n).add((ColumnNode)nn);
						} else if (nn instanceof InternalNode) {
							coveredColumnNodes.get(n).addAll(coveredColumnNodes.get((InternalNode)nn));
						}
					}
					
				}
			}
			
			i--;
		}
		
		return coveredColumnNodes;
		
	}

//	private static void treeToRootedTree(
//			DirectedWeightedMultigraph<Node, Link> tree, 
//			Node node, Link e, 
//			Set<Node> visitedNodes, 
//			Set<String> reversedLinks, 
//			Set<String> removedLinks) {
//		
//		if (node == null)
//			return;
//		
//		if (visitedNodes.contains(node)) // prevent having loop in the tree
//			return;
//		
//		visitedNodes.add(node);
//		
//		Node source, target;
//		
//		Set<Link> incomingLinks = tree.incomingEdgesOf(node);
//		if (incomingLinks != null) {
//			Link[] incomingLinksArr = incomingLinks.toArray(new Link[0]);
//			for (Link inLink : incomingLinksArr) {
//				
//				source = inLink.getSource();
//				target = inLink.getTarget();
//				
//				// don't remove the incoming link from parent to this node
//				if (e != null && inLink.equals(e))
//					continue;
//				
//				// removeEdge method should always be called before addEdge because the new edge has the same id
//				// and JGraph does not add the duplicate link
////				Label label = new Label(inLink.getLabel().getUri(), inLink.getLabel().getNs(), inLink.getLabel().getPrefix());
//				Link reverseLink = inLink.clone(); //new Link(inLink.getId(), label);
//				tree.removeEdge(inLink);
//				tree.addEdge(target, source, reverseLink);
//				tree.setEdgeWeight(reverseLink, inLink.getWeight());
//				
//				// Save the reversed links information
//				reversedLinks.add(inLink.getId());
//			}
//		}
//
//		Set<Link> outgoingLinks = tree.outgoingEdgesOf(node);
//
//		if (outgoingLinks == null)
//			return;
//		
//		
//		Link[] outgoingLinksArr = outgoingLinks.toArray(new Link[0]);
//		for (Link outLink : outgoingLinksArr) {
//			target = outLink.getTarget();
//			if (visitedNodes.contains(target)) {
//				tree.removeEdge(outLink);
//				removedLinks.add(outLink.getId());
//			} else {
//				treeToRootedTree(tree, target, outLink, visitedNodes, reversedLinks, removedLinks);
//			}
//		}
//	}
	
}
