package edu.isi.karma.modeling.alignment;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.steiner.topk.SteinerEdge;
import edu.isi.karma.modeling.steiner.topk.SteinerNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Node;

public class GraphBuilderTopK extends GraphBuilder {
	
	static Logger logger = LoggerFactory.getLogger(GraphBuilderTopK.class);

	private HashMap<SteinerNode, TreeSet<SteinerEdge>> topKGraph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
	private HashMap<String, SteinerNode> topKGraphNodes = new HashMap<String, SteinerNode>();


	public GraphBuilderTopK(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, boolean addThingNode) { 
		super(ontologyManager, nodeIdFactory, addThingNode);
	}
	
	public GraphBuilderTopK(OntologyManager ontologyManager, DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		super(ontologyManager, graph);
	}
	
	public boolean addNode(Node node) {
		return super.addNode(node);
	}
	
	public boolean addLink(Node source, Node target, DefaultLink link) {
		return super.addLink(source, target, link);
	}

}
