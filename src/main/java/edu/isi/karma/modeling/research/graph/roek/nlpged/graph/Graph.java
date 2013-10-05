package edu.isi.karma.modeling.research.graph.roek.nlpged.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph {

	protected String id, originalText;
	protected List<Node> nodes;
	protected HashMap<String, List<Edge>> edges;


	public Graph(String id) {
		this();
		this.id = id;
	}
	
	public Graph(String id, String originalText) {
		this();
		this.id = id;
		this.originalText = originalText;
	}

	public Graph() {
		nodes = new ArrayList<Node>();
		edges = new HashMap<String, List<Edge>>();
	}

	public void addNode(Node node) {
		if(!edges.containsKey(node.getId())) {
			edges.put(node.getId(), new ArrayList<Edge>());
		}
		nodes.add(node);
	}

	public int getSize() {
		return nodes.size();
	}


	public HashMap<String, List<Edge>> getEdges() {
		return edges;
	}

	public void addEdge(Edge edge) {
		edges.get(edge.getFrom().getId()).add(edge);
	}

	public List<Edge> getEdges(Node node) {
		return getEdges(node.getId());
	}

	public List<Edge> getEdges(String nodeId) {
		return edges.get(nodeId);
	}

	public void removeNode(int i) {
		nodes.remove(i);
	}

	public Node getNode(int i) {
		return nodes.get(i);
	}
	
	public Node getNode(String id) {
		for (Node node : nodes) {
			if(node.getId().equals(id)) {
				return node;
			}
		}
		return null;
	}


	public List<Node> getNodes() {
		return nodes;
	}

	public String getId() {
		return id;
	}

	public String getOriginalText() {
		return originalText;
	}
}
