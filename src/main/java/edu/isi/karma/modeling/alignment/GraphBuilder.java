package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.semantictypes.SemanticType;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);
	

	private List<SemanticType> semanticTypes;
	private List<Vertex> semanticNodes;
	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph;
	
	private List<LabeledWeightedEdge> linksForcedByDomains = new ArrayList<LabeledWeightedEdge>();

	private HashMap<String, Integer> nodesLabelCounter;
	private HashMap<String, Integer> linksLabelCounter;
	
	private static String THING_URI = "http://www.w3.org/2002/07/owl#Thing";
	private static String SUBCLASS_URI = "hasSubClass";
	private static double DEFAULT_WEIGHT = 1.0;	
	public static double MIN_WEIGHT = 0.000001; // need to be fixed later	
	public static double MAX_WEIGHT = 1000000;
	
	public GraphBuilder(List<SemanticType> semanticTypes) {
		nodesLabelCounter = new HashMap<String, Integer>();
		linksLabelCounter = new HashMap<String, Integer>();

		this.semanticTypes = semanticTypes;
		graph = new DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		semanticNodes = new ArrayList<Vertex>();
			
		// create Thing Node
		buildInitialGraph();
	}
	
	private String createNodeID(String label) {
		
		int index;
		String id;
		
		if (nodesLabelCounter.containsKey(label)) {
			index = nodesLabelCounter.get(label).intValue();
			nodesLabelCounter.put(label, ++index);
			id = label + "" + index;
		} else {
			index = 1;
			nodesLabelCounter.put(label, index);
			id = label + "" + index;
//			id = label;
		}
		return id;
	}
	
	private String createLinkID(String label) {

		String id;
		int index;
		
		if (linksLabelCounter.containsKey(label)) {
			index = linksLabelCounter.get(label).intValue();
			linksLabelCounter.put(label, ++index);
			id = label + "" + index;
		} else {
			index = 1;
			linksLabelCounter.put(label, index);
			id = label + "" + index;
//			id = label;
		}
		return id;
	}
	
	private void addSemanticTypesToGraph() {
		
		logger.debug("<enter");
		String id;
		SemanticType semanticType;
		NodeType nodeType;
		String label;
		
		for (int i = 0; i < this.semanticTypes.size(); i++) {
			
			semanticType =semanticTypes.get(i); 
			label = semanticType.getType();
			id = createNodeID(label);
			
			if (OntologyManager.Instance().isClass(label))
				nodeType = NodeType.Class;
			else if (OntologyManager.Instance().isDataProperty(label))
				nodeType = NodeType.DataProperty;
			else
				nodeType = null;
			
			if (nodeType == null) {
				logger.debug("could not find type of " + label + " in the ontology.");
				continue;
			}
			
			Vertex v = new Vertex(id, semanticType, nodeType);
			semanticNodes.add(v);
			graph.addVertex(v);
		}


		// Add Thing to Graph if it is not added before.
		// Preventing from have an unconnected graph
		if (!nodesLabelCounter.containsKey(THING_URI)) {
			Vertex v = new Vertex(createNodeID(THING_URI), THING_URI, NodeType.Class);			
			this.graph.addVertex(v);
		}
		
		logger.debug("exit>");
	}
	
	private void addNodesClosure() {
		
		logger.debug("<enter");

		String label;
		List<Vertex> recentlyAddedNodes = new ArrayList<Vertex>(graph.vertexSet());
		List<Vertex> newNodes;
		List<String> dpDomainClasses = new ArrayList<String>();
		List<String> opDomainClasses = new ArrayList<String>();
		List<String> superClasses = new ArrayList<String>();
		List<String> newAddedClasses = new ArrayList<String>();

		// We don't need to add subclasses of each class separately.
		// The only place in which we add children is where we are looking for domain class of a property.
		// In this case, in addition to the domain class, we add all of its children too.
		
		List<String> processedLabels = new ArrayList<String>();
		while (recentlyAddedNodes.size() > 0) {
			
			newNodes = new ArrayList<Vertex>();
			for (int i = 0; i < recentlyAddedNodes.size(); i++) {
				
				label = recentlyAddedNodes.get(i).getLabel();
				if (processedLabels.indexOf(label) != -1) 
					continue;
				
				processedLabels.add(label);
				
				if (recentlyAddedNodes.get(i).getNodeType() == NodeType.Class) {
					opDomainClasses = OntologyManager.Instance().getDomainsGivenRange(label, true);
					superClasses = OntologyManager.Instance().getSuperClasses(label, false);
				} else if (recentlyAddedNodes.get(i).getNodeType() == NodeType.DataProperty) {
					dpDomainClasses = OntologyManager.Instance().getDomainsGivenProperty(label, true);
				}
				
				newAddedClasses.addAll(opDomainClasses);
				newAddedClasses.addAll(dpDomainClasses);
				newAddedClasses.addAll(superClasses);
				
				for (int j = 0; j < newAddedClasses.size(); j++) {
					if (!nodesLabelCounter.containsKey(newAddedClasses.get(j))) { // if node is not in graph yet
						label = newAddedClasses.get(j);
						Vertex v = new Vertex(createNodeID(label), newAddedClasses.get(j), NodeType.Class);
						newNodes.add(v);
						this.graph.addVertex(v);
					}
				}
			}
			
			recentlyAddedNodes = newNodes;
			opDomainClasses.clear();
			dpDomainClasses.clear();
			superClasses.clear();
			newAddedClasses.clear();
		}

		logger.debug("exit>");
	}
	
	private void addLinks() {
		
		logger.debug("<enter");

		Vertex[] vertices = this.graph.vertexSet().toArray(new Vertex[0]);
		List<String> objectProperties = new ArrayList<String>();
		List<String> dataProperties = new ArrayList<String>();
		
		Vertex source;
		Vertex target;
		String sourceLabel;
		String targetLabel;
		
		String id;
		String label;
		
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices.length; j++) {
				
				if (j == i)
					continue;
				
				source = vertices[i];
				target = vertices[j];
				sourceLabel = source.getLabel();
				targetLabel = target.getLabel();
				
				String targetDomain = "";
				
				if (target.getSemanticType() != null)
					targetDomain = target.getSemanticType().getDomain();
				
				// There is no outgoing link from DataProperty nodes
				if (source.getNodeType() == NodeType.DataProperty)
					break;
				
				// create a link from the domain and all its subclasses of this DataProperty to range
				if (target.getNodeType() == NodeType.DataProperty) {
					
					dataProperties = OntologyManager.Instance().getDataProperties(sourceLabel, targetLabel, true);
					
					for (int k = 0; k < dataProperties.size(); k++) {
						label = dataProperties.get(k);
						id = createLinkID(label);
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, LinkType.DataProperty);
						this.graph.addEdge(source, target, e);
						this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);

						// put epsilon as weights of the links from domains that specified by user 
						if (targetDomain != null && targetDomain.trim().length() > 0) 
							if (targetDomain.trim().equalsIgnoreCase(sourceLabel))  // the source is not the domain specified by user
								this.linksForcedByDomains.add(e);
					}
				}

				// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
				if (target.getNodeType() == NodeType.Class) {
					objectProperties = OntologyManager.Instance().getObjectProperties(sourceLabel, targetLabel, true);
					
					for (int k = 0; k < objectProperties.size(); k++) {
						label = objectProperties.get(k);
						id = createLinkID(label);
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, LinkType.DataProperty);
						this.graph.addEdge(source, target, e);
						this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
					}
				}
				
				if (target.getNodeType() == NodeType.Class) {
					// we have to check both sides.
					if (OntologyManager.Instance().isSubClass(targetLabel, sourceLabel, false) ||
							OntologyManager.Instance().isSuperClass(sourceLabel, targetLabel, false)) {
						id = createLinkID(SUBCLASS_URI);
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, SUBCLASS_URI, LinkType.HasSubClass);
						this.graph.addEdge(source, target, e);
						this.graph.setEdgeWeight(e, MAX_WEIGHT);					
					}
				}
				
			}
		}
		
		logger.debug("exit>");
	}
	
	private void addLinksFromThing() {
		
		logger.debug("<enter");

		Vertex[] vertices = this.graph.vertexSet().toArray(new Vertex[0]);
		
		Vertex source;
		Vertex target;
		String sourceLabel;
		String targetLabel;
		
		String id;

		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices.length; j++) {
				
				if (j == i)
					continue;
				
				source = vertices[i];
				target = vertices[j];
				sourceLabel = source.getLabel();
				targetLabel = target.getLabel();
				
				// There is no outgoing link from DataProperty nodes
				if (source.getNodeType() != NodeType.Class)
					break;
				
				if (target.getNodeType() != NodeType.Class)
					continue;
				
				if (!sourceLabel.equalsIgnoreCase(THING_URI))
					continue;
				
				if (OntologyManager.Instance().getSuperClasses(targetLabel, false).size() != 0)
					continue;
				
				// create a link from all Thing nodes to nodes who don't have any superclasses
				if (target.getNodeType() == NodeType.Class) {
					id = createLinkID(SUBCLASS_URI);
					LabeledWeightedEdge e = new LabeledWeightedEdge(id, SUBCLASS_URI, LinkType.HasSubClass);
					this.graph.addEdge(source, target, e);
					this.graph.setEdgeWeight(e, MAX_WEIGHT);					
				}
			}
		}
		
		logger.debug("exit>");

	}
	
	public Vertex copyNode(Vertex node) {
		
		if (node.getNodeType() != NodeType.Class) {
			logger.debug("nodes other than type of Class cannot be duplicated.");
			return null;
		}
		
		String id;
		String label;
		
		label = node.getLabel();
		id = createNodeID(label);
		
		Vertex newNode = new Vertex(id, label, node.getNodeType());
		
		this.graph.addVertex(newNode);
		
		return newNode;
	}
	
	public void copyLinks(Vertex source, Vertex target) {
		
		LabeledWeightedEdge[] outgoing =  graph.outgoingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		LabeledWeightedEdge[] incoming = graph.incomingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		
		String id;
		String label;
		
		Vertex s, t;
		
		if (outgoing != null)
			for (int i = 0; i < outgoing.length; i++) {
				label = outgoing[i].getLabel();
				id = createLinkID(label);
				LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, outgoing[i].getLinkType());
				s = target;
				t = outgoing[i].getTarget();
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, outgoing[i].getWeight());
			}
		
		if (incoming != null)
			for (int i = 0; i < incoming.length; i++) {
				label = incoming[i].getLabel();
				id = createLinkID(label);
				LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, incoming[i].getLinkType());
				s = incoming[i].getSource();
				t = target;
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, incoming[i].getWeight());
			}
		
		if (source.getNodeType() != NodeType.Class || target.getNodeType() != NodeType.Class) 
			return;

		// interlinks from source to target
		s = source; t= target;
		List<String> objectProperties = OntologyManager.Instance().getObjectProperties(s.getLabel(), t.getLabel(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			id = createLinkID(label);
			LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, LinkType.DataProperty);
			this.graph.addEdge(s, t, e);
			this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
		}

		// interlinks from target to source
		s = target; t= source;
		objectProperties = OntologyManager.Instance().getObjectProperties(s.getLabel(), t.getLabel(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			id = createLinkID(label);
			LabeledWeightedEdge e = new LabeledWeightedEdge(id, label, LinkType.DataProperty);
			this.graph.addEdge(s, t, e);
			this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
		}

	}
	
	private void buildInitialGraph() {

		if (this.semanticTypes == null) {
			logger.debug("semantic types list is null.");
			return;
		}

		addSemanticTypesToGraph();
		addNodesClosure();
		addLinks();
		addLinksFromThing();
	}

	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getGraph() {
		return this.graph;
	}
	
	public List<Vertex> getSemanticNodes() {
		return this.semanticNodes;
	}
	
	public List<LabeledWeightedEdge> getLinksForcedByDomain() {
		return this.linksForcedByDomains;
	}
}
