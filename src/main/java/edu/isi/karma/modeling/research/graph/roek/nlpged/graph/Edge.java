package edu.isi.karma.modeling.research.graph.roek.nlpged.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Edge {

	protected String id;
	protected Node from;
	protected Node to;
	protected String label;
	protected List<String> attributes;
	
	public Edge(String id, Node from, Node to, String label) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.label = label;
		this.attributes = new ArrayList<String>();
	}
	
	public Edge(String id, Node from, Node to, String label, List<String> attributes) {
		this(id, from, to, label);
		this.attributes = attributes;
	}
	
	public Edge(String id, Node from, Node to, String label, String[] attributes) {
		this(id, from, to, label, Arrays.asList(attributes));
	}
	
	
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}
	

	public List<String> getAttributes() {
		return attributes;
	}
	
	public void addAttribute(String attr) {
		attributes.add(attr);
	}
	
	@Override
	public String toString() {
		return from + "-" +to;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Edge other = (Edge) obj;
			return label.equals(other.getLabel());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return label.hashCode();
	}
}
