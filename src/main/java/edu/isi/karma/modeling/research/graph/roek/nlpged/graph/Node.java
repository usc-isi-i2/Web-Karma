package edu.isi.karma.modeling.research.graph.roek.nlpged.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {

	private String id;
	private String label;
	private List<String> attributes;

	public Node(String id, String label) {
		this.id = id;
		this.label = label;
		this.attributes = new ArrayList<String>();
	}

	public Node(String id, String label, String[] attributes) {
		this.id = id;
		this.label = label;
		this.attributes  = Arrays.asList(attributes);
	}

	public String getId() {
		return id;
	}

	public String  getLabel() {
		return label;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return label;
	}

	public void addAttribute(String attr) {
		attributes.add(attr);
	}

	@Override
	public  boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Node other = (Node) obj;
			return label.equals(other.getLabel());
		}
		return false;
	}
}
