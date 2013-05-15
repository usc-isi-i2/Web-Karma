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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class GraphUtil {

	private static Logger logger = Logger.getLogger(GraphUtil.class);
	
	// FIXME: change methods to get an Outputstream as input and write on it.
	
	private static String getNodeTypeString(Node node) {
		
		if (node == null) {
			System.out.println("node is null.");
			return null;
		}
		
		String s = node.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}

	private static String getLinkTypeString(Link link) {

		if (link == null) {
			System.out.println("link is null.");
			return null;
		}
		
		String s = link.getClass().getName();
		if (s.indexOf(".") != -1)
			s = s.substring(s.lastIndexOf(".") + 1);
    	return s;
	}
	
	public static void printVertex(Node node) {
		
		if (node == null) {
			System.out.println("node is null.");
			return;
		}
		
    	System.out.print("(");
    	System.out.print( node.getLocalId());
//    	System.out.print( vertex.getID());
    	System.out.print(", ");
		if (node instanceof ColumnNode)
			System.out.print( ((ColumnNode)node).getColumnName());
		else
			System.out.print(node.getLabel().getLocalName());
    	System.out.print(", ");
    	System.out.print(getNodeTypeString(node));
    	System.out.print(")");
	}
	
	public static void printEdge(Link link) {
		
		if (link == null) {
			System.out.println("link is null.");
			return;
		}
		
    	System.out.print("(");
    	System.out.print( link.getLocalId());
    	System.out.print(", ");
    	System.out.print(link.getLabel().getLocalName());
    	System.out.print(", ");
    	System.out.print(getLinkTypeString(link));
    	System.out.print(", ");
    	System.out.print(link.getWeight());
    	System.out.print(") - From ");
    	printVertex(link.getSource());
    	System.out.print(" To ");
    	printVertex(link.getTarget());
	}

	public static DirectedGraph<Node, Link> asDirectedGraph(UndirectedGraph<Node, Link> undirectedGraph) {
		
		if (undirectedGraph == null) {
			System.out.println("graph is null.");
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
			System.out.println("graph is null.");
			return;
		}		

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
		
		if (graph == null) {
			System.out.println("The input graph is null.");
			return;
		}		

		for (Link edge : graph.edgeSet()) {
			System.out.print("(");
			if (edge.getSource() instanceof ColumnNode)
				System.out.print(edge.getSource().getLocalId() + "-" + ((ColumnNode)edge.getSource()).getColumnName());
			else
				System.out.print(edge.getSource().getLocalId());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			System.out.print(edge.getId());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			if (edge.getTarget() instanceof ColumnNode)
				System.out.print(edge.getTarget().getLocalId() + "-" + ((ColumnNode)edge.getTarget()).getColumnName());
			else
				System.out.print(edge.getTarget().getLocalId());
			System.out.print(")");
			System.out.print(" - w=" + edge.getWeight());
			System.out.println();
        }
		System.out.println("------------------------------------------");
		
	}
	
	@SuppressWarnings("unchecked")
	public static DirectedWeightedMultigraph<Node, Link> treeToRootedTree(
			DirectedWeightedMultigraph<Node, Link> tree, Node root, Set<String> reversedLinks) {
		
		if (tree == null) {
			logger.error("The input tree is null.");
			return null;
		}		

		DirectedWeightedMultigraph<Node, Link> rootedTree = 
				(DirectedWeightedMultigraph<Node, Link>)tree.clone();
		if (reversedLinks == null)
			reversedLinks = new HashSet<String>();
		treeToRootedTree(rootedTree, root, null, reversedLinks);
		return rootedTree;
	}
	
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
