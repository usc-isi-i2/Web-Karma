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

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class GraphUtil {

//	private static Logger logger = Logger.getLogger(GraphUtil.class);
	
	private static String getNodeTypeString(Node node) {
		String s = node.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}

	private static String getLinkTypeString(Link link) {
		String s = link.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}
	
	public static void printVertex(Node vertex) {
    	System.out.print("(");
    	System.out.print( vertex.getLocalId());
//    	System.out.print( vertex.getID());
    	System.out.print(", ");
		if (vertex instanceof ColumnNode)
			System.out.print( ((ColumnNode)vertex).getColumnName());
		else
			System.out.print(vertex.getLabel().getLocalName());
    	System.out.print(", ");
    	System.out.print(getNodeTypeString(vertex));
    	System.out.print(")");
	}
	
	public static void printEdge(Link edge) {
    	System.out.print("(");
    	System.out.print( edge.getLocalId());
    	System.out.print(", ");
    	System.out.print(edge.getLabel().getLocalName());
    	System.out.print(", ");
    	System.out.print(getLinkTypeString(edge));
    	System.out.print(", ");
    	System.out.print(edge.getWeight());
    	System.out.print(") - From ");
    	printVertex(edge.getSource());
    	System.out.print(" To ");
    	printVertex(edge.getTarget());
	}

	public static DirectedGraph<Node, Link> asDirectedGraph(UndirectedGraph<Node, Link> undirectedGraph) {
		
		DirectedGraph<Node, Link> g = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		for (Node v : undirectedGraph.vertexSet())
			g.addVertex(v);
		
		for (Link e: undirectedGraph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static void printGraph(Graph<Node, Link> graph) {
		
    	System.out.println("*** Nodes ***");
		for (Node vertex : graph.vertexSet()) {
			printVertex(vertex);
			System.out.println();
        }
    	System.out.println("*** Links ***");
		for (Link edge : graph.edgeSet()) {
			printEdge(edge);
			System.out.println();
        }
		System.out.println("------------------------------------------");
		
	}
	
	public static void printGraphSimple(Graph<Node, Link> graph) {
		
		for (Link edge : graph.edgeSet()) {
			System.out.print("(");
			if (edge.getSource() instanceof ColumnNode)
				System.out.print( ((ColumnNode)edge.getSource()).getColumnName());
			else
				System.out.print(edge.getSource().getLocalId());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			System.out.print(edge.getLocalId());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			if (edge.getTarget() instanceof ColumnNode)
				System.out.print( ((ColumnNode)edge.getTarget()).getColumnName());
			else
				System.out.print(edge.getTarget().getLocalId());
			System.out.print(")");
			System.out.println();
        }
		System.out.println("------------------------------------------");
		
	}
	
	@SuppressWarnings("unchecked")
	public static DirectedWeightedMultigraph<Node, Link> treeToRootedTree(
			DirectedWeightedMultigraph<Node, Link> tree, Node root, Set<String> reversedLinks) {
		
		DirectedWeightedMultigraph<Node, Link> rootedTree = 
				(DirectedWeightedMultigraph<Node, Link>)tree.clone();
		if (reversedLinks == null)
			reversedLinks = new HashSet<String>();
		treeToRootedTree(rootedTree, root, null, reversedLinks);
		return rootedTree;
	}

	private static void treeToRootedTree(DirectedWeightedMultigraph<Node, Link> tree, Node node, Link e, Set<String> reversedLinks) {
		
		if (node == null)
			return;
		
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
		
		
		for (Link outLink : outgoingLinks) {
			target = outLink.getTarget();
			treeToRootedTree(tree, target, outLink, reversedLinks);
		}
	}
	
}
