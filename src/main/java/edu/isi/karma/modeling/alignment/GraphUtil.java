package edu.isi.karma.modeling.alignment;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class GraphUtil {

	private static String getNodeTypeString(Vertex vertex) {
    	if (vertex.getType() == NodeType.Class)
    		return "Class";
    	else if (vertex.getType() == NodeType.DataProperty)
    		return "DataProperty";
    	else
    		return null;
	}

	private static String getLinkTypeString(LabeledWeightedEdge link) {
    	if (link.getType() == LinkType.ObjectProperty)
    		return "ObjectProperty";
    	if (link.getType() == LinkType.DataProperty)
    		return "DataProperty";
    	if (link.getType() == LinkType.HasSubClass)
    		return "HasSubClass";
    	if (link.getType() == LinkType.None)
    		return "None";
    	else
    		return null;
	}
	
	public static void printVertex(Vertex vertex) {
    	System.out.print("(");
    	System.out.print( vertex.getLocalID());
//    	System.out.print( vertex.getID());
//    	System.out.print(", ");
//    	System.out.print(vertex.getLabel());
    	System.out.print(", ");
    	System.out.print(getNodeTypeString(vertex));
    	System.out.print(")");
	}
	
	public static void printEdge(LabeledWeightedEdge edge) {
    	System.out.print("(");
    	System.out.print( edge.getLocalID());
//    	System.out.print( edge.getID());
//    	System.out.print(", ");
//    	System.out.print(edge.getLabel());
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
	
}
