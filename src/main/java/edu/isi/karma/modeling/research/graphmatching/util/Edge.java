package edu.isi.karma.modeling.research.graphmatching.util;

import java.util.Hashtable;

public class Edge {

	/** the identifier of the edge */
	private String edgeID;
	
	/** the attributes of the edge*/
	private Hashtable<String, String> attributes;

	/** the start and end node of the edge*/
	private Node startNode;
	private Node endNode;
	
	
	
	/**
	 * Constructor
	 */
	public Edge() {
		this.attributes = new Hashtable<String, String>();
	}
	
	/**
	 * gets the other end of the edge
	 */
	public Node getOtherEnd(Node n) {
		if (n.equals(this.startNode)){
			return this.endNode;
		} else {
			return this.startNode;
		}
		
	}
	
	/**
	 * puts a new attribute 
	 * in the attribute-table
	 */
	public void put(String key, String value){
		this.attributes.put(key, value);
	}
	
	/**
	 * @return the attribute-value of
	 * @param key
	 */
	public String getValue(String key){
		return this.attributes.get(key);
	}
	
	
	/** 
	 * generates a printable string of the edge
	 */
	public String toString(){
		String edge = this.edgeID+" ";
		edge += this.attributes;
		return edge;
	}
	
	
	
	/**
	 * some getters and setters
	 */
		
	public String getEdgeID() {
		return edgeID;
	}

	public void setEdgeID(String edgeID) {
		this.edgeID = edgeID;
	}
	
	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	

	

}
