package edu.isi.karma.modeling.alignment;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class GraphUtil {

	public static void printGraph(Graph<Vertex, DefaultWeightedEdge> graph) {
		
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
        	System.out.print( ((LabeledWeightedEdge)edge).getLabel());
        	System.out.print(" (w= ");
        	System.out.print(graph.getEdgeWeight(edge));
        	System.out.print(") : ");
        	System.out.print(graph.getEdgeSource(edge).getLabel());
        	System.out.print(" --- ");
        	System.out.println(graph.getEdgeTarget(edge).getLabel());
        }
		System.out.println("------------------------------------------");
		
	}
	
	public static void printGraph(Graph<Vertex, DefaultWeightedEdge> graph, Set<DefaultWeightedEdge> edges) {
		
		for (DefaultWeightedEdge edge : edges) {
        	System.out.print( ((LabeledWeightedEdge)edge).getLabel());
        	System.out.print(" (w= ");
        	System.out.print(graph.getEdgeWeight(edge));
        	System.out.print(") : ");
        	System.out.print(graph.getEdgeSource(edge).getLabel());
        	System.out.print(" --- ");
        	System.out.println(graph.getEdgeTarget(edge).getLabel());
        }
		System.out.println("------------------------------------------");
		
	}
}
