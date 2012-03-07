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

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class GraphUtil {

	private static String getNodeTypeString(Vertex vertex) {
    	if (vertex.getNodeType() == NodeType.Class)
    		return "Class";
    	else if (vertex.getNodeType() == NodeType.DataProperty)
    		return "DataProperty";
    	else
    		return null;
	}

	private static String getLinkTypeString(LabeledWeightedEdge link) {
    	if (link.getLinkType() == LinkType.ObjectProperty)
    		return "ObjectProperty";
    	if (link.getLinkType() == LinkType.DataProperty)
    		return "DataProperty";
    	if (link.getLinkType() == LinkType.HasSubClass)
    		return "HasSubClass";
    	if (link.getLinkType() == LinkType.None)
    		return "None";
    	else
    		return null;
	}
	
	public static void printVertex(Vertex vertex) {
    	System.out.print("(");
    	System.out.print( vertex.getLocalID());
//    	System.out.print( vertex.getID());
    	System.out.print(", ");
    	System.out.print(vertex.getUri());
    	System.out.print(", ");
    	System.out.print(getNodeTypeString(vertex));
    	System.out.print(")");
	}
	
	public static void printEdge(LabeledWeightedEdge edge) {
    	System.out.print("(");
		// FIXME
		if (edge.isInverse()) {
			System.out.print( "inverseOf(" + edge.getLocalID() + ")" );
		} else 
			System.out.print( edge.getLocalID());
//    	System.out.print( edge.getID());
    	System.out.print(", ");
    	System.out.print(edge.getUri());
    	System.out.print(", ");
    	System.out.print(getLinkTypeString(edge));
    	System.out.print(", ");
    	System.out.print(edge.getWeight());
    	System.out.print(") - From ");
    	printVertex(edge.getSource());
    	System.out.print(" To ");
    	printVertex(edge.getTarget());
	}

	public static DirectedGraph<Vertex, LabeledWeightedEdge> asDirectedGraph(UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph) {
		
		DirectedGraph<Vertex, LabeledWeightedEdge> g = new DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		for (Vertex v : undirectedGraph.vertexSet())
			g.addVertex(v);
		
		for (LabeledWeightedEdge e: undirectedGraph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static void printGraph(Graph<Vertex, LabeledWeightedEdge> graph) {
		
    	System.out.println("*** Nodes ***");
		for (Vertex vertex : graph.vertexSet()) {
			printVertex(vertex);
			System.out.println();
        }
    	System.out.println("*** Links ***");
		for (LabeledWeightedEdge edge : graph.edgeSet()) {
			printEdge(edge);
			System.out.println();
        }
		System.out.println("------------------------------------------");
		
	}
	
	public static void printGraphSimple(Graph<Vertex, LabeledWeightedEdge> graph) {
		
//    	System.out.println("*** Nodes ***");
//		for (Vertex vertex : graph.vertexSet()) {
//			printVertex(vertex);
//			System.out.println();
//        }
    	System.out.println("*** Graph ***");
		for (LabeledWeightedEdge edge : graph.edgeSet()) {
			System.out.print("(");
			System.out.print(edge.getSource().getLocalID());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			if (edge.isInverse())
				System.out.print("invOf:");
			System.out.print(edge.getLocalID());
			System.out.print(")");
			System.out.print(" - ");
			System.out.print("(");
			System.out.print(edge.getTarget().getLocalID());
			System.out.print(")");
			System.out.println();
        }
		System.out.println("------------------------------------------");
		
	}
	
}
