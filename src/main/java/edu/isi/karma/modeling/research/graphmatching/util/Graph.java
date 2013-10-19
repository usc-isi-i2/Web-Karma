/**
 * 
 */
package edu.isi.karma.modeling.research.graphmatching.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author riesen
 *
 */
@SuppressWarnings("serial")
public class Graph extends LinkedList<Node>{

	/** the class of this graph */
	private String className;
	
	/** the identifier of the graph */
	private String graphID;
	
	/** directed or undirected edges */
	private boolean directed;
		
	/** the adjacency-matrix of the graph */
	private Edge[][] adjacencyMatrix;

	
	
	
	/**
	 * Constructors
	 */
	public Graph(int n) {
		super();
		this.adjacencyMatrix = new Edge[n][n];
	}
	
	public Graph() {
		super();
	}
	
	/** 
	 * generates a printable string of the graph
	 */
	public String toString(){
		String graph = "*** Graph: "+this.graphID+" ***\n";
		graph += "Class: "+this.className+"\n";
		graph += "Nodes:\n";
		Iterator<Node> iter = this.iterator();
		while (iter.hasNext()){
			Node node = iter.next();
			graph += node.toString();
			graph += "\n";
		}
		graph += "\n";
		graph += "Edges of...\n";
		iter = this.iterator();
		while (iter.hasNext()){
			Node node = iter.next();
			graph+="... Node: "+node.getNodeID()+": ";
			Iterator<Edge> edgeIter = node.getEdges().iterator();
			while (edgeIter.hasNext()){
				Edge edge = edgeIter.next();
				graph+=edge.getEdgeID()+"\t";
			}
			graph+="\n";
		}
		graph += "\n";
		graph += "Adjacency Matrix:\n";
		for (int i = 0; i < this.adjacencyMatrix.length; i++){
			for (int j = 0; j < this.adjacencyMatrix.length; j++){
				if (this.adjacencyMatrix[i][j] != null){
					graph += "1";
				} else {
					graph += "0";
				}
				
				graph += "\t";
			}
			graph += "\n";
		}
		graph+="\n*** *** *** *** *** *** *** *** *** *** *** *** *** ***\n";
		return graph;
	}
	



	/**
	 * getters and setters
	 */

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getGraphID() {
		return graphID;
	}

	public void setGraphID(String graphID) {
		this.graphID = graphID;
	}

	public boolean isDirected() {
		return directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	public Edge[][] getAdjacenyMatrix() {
		return adjacencyMatrix;
	}

	public void setAdjacenyMatrix(Edge[][] edges) {
		this.adjacencyMatrix = edges;
	}

}
