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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DisplayModel;
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
	
	public static String graphToString(Graph<Node, Link> graph) {
		
		if (graph == null) {
			logger.error("The input graph is null.");
			return "";
		}		

		StringBuffer sb = new StringBuffer();
		for (Link edge : graph.edgeSet()) {
			sb.append("(");
			sb.append(edge.getId());
			sb.append(" - status=" + edge.getStatus().name());
			sb.append(" - w=" + edge.getWeight());
			sb.append("\n");
        }
		sb.append("------------------------------------------");
		return sb.toString();
		
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
	
	public static Set<Node> getOutNeighbors(DirectedWeightedMultigraph<Node, Link> g, Node n) {
		
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

	public static Set<Node> getInNeighbors(DirectedWeightedMultigraph<Node, Link> g, Node n) {
		
		Set<Node> neighbors = new HashSet<Node>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		Set<Link> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (Link l : incomingLinks) {
				neighbors.add(l.getSource());
			}
		}
		
		return neighbors;
	}
	
	public static HashMap<Node, Integer> inDegreeInSet(DirectedWeightedMultigraph<Node, Link> g, 
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
	
	public static HashMap<Node, Integer> outDegreeInSet(DirectedWeightedMultigraph<Node, Link> g, 
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
	
	public static DisplayModel getDisplayModel(DirectedWeightedMultigraph<Node, Link> g, HTable hTable) {
		DisplayModel displayModel = new DisplayModel(g, hTable);
		return displayModel;
	}

	public static void treeToRootedTree(
			DirectedWeightedMultigraph<Node, Link> tree, 
			Node node, Link e, 
			Set<Node> visitedNodes, 
			Set<String> reversedLinks, 
			Set<String> removedLinks) {
		
		if (node == null)
			return;
		
		if (visitedNodes.contains(node)) // prevent having loop in the tree
			return;
		
		visitedNodes.add(node);
		
		Node source, target;
		
		Set<Link> incomingLinks = tree.incomingEdgesOf(node);
		if (incomingLinks != null) {
			Link[] incomingLinksArr = incomingLinks.toArray(new Link[0]);
			for (Link inLink : incomingLinksArr) {
				
				source = inLink.getSource();
				target = inLink.getTarget();
				
				// don't remove the incoming link from parent to this node
				if (e != null && inLink.equals(e))
					continue;
				
				// removeEdge method should always be called before addEdge because the new edge has the same id
				// and JGraph does not add the duplicate link
//				Label label = new Label(inLink.getLabel().getUri(), inLink.getLabel().getNs(), inLink.getLabel().getPrefix());
				Link reverseLink = inLink.clone(); //new Link(inLink.getId(), label);
				tree.removeEdge(inLink);
				tree.addEdge(target, source, reverseLink);
				tree.setEdgeWeight(reverseLink, inLink.getWeight());
				
				// Save the reversed links information
				reversedLinks.add(inLink.getId());
			}
		}

		Set<Link> outgoingLinks = tree.outgoingEdgesOf(node);

		if (outgoingLinks == null)
			return;
		
		
		Link[] outgoingLinksArr = outgoingLinks.toArray(new Link[0]);
		for (Link outLink : outgoingLinksArr) {
			target = outLink.getTarget();
			if (visitedNodes.contains(target)) {
				tree.removeEdge(outLink);
				removedLinks.add(outLink.getId());
			} else {
				treeToRootedTree(tree, target, outLink, visitedNodes, reversedLinks, removedLinks);
			}
		}
	}
	
}
