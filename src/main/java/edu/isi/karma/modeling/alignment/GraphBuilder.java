package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.NameSet;
import edu.isi.karma.modeling.ontology.OntologyManager;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);
	

	private List<NameSet> semanticTypes;
	private List<Vertex> semanticNodes;
	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph;

	private HashMap<String, Integer> nodesLabelCounter;
	private HashMap<String, Integer> linksLabelCounter;
	
	private static String OWL_NameSpace = "http://www.w3.org/2002/07/owl#";
	private static String Thing_CLASS = "Thing";
	private static String SUBCLASS_LINKNAME = "hasSubClass";
	private static double DEFAULT_WEIGHT = 1.0;	
	private static double MAX_WEIGHT = 1000000;
	
	public GraphBuilder(List<NameSet> semanticTypes) {
		nodesLabelCounter = new HashMap<String, Integer>();
		linksLabelCounter = new HashMap<String, Integer>();

		this.semanticTypes = semanticTypes;
		graph = new DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		semanticNodes = new ArrayList<Vertex>();
			
		// create Thing Node
		buildInitialGraph();
	}
	
	private String createNodeID(String label) {
		String result = "";
		int index;
		if (nodesLabelCounter.containsKey(label)) {
			index = nodesLabelCounter.get(label).intValue();
			nodesLabelCounter.put(label, ++index);
		} else {
			index = 1;
			nodesLabelCounter.put(label, index);
		}
		result = label + "_" + index;
		return result;
	}
	
	private String createLinkID(String label) {
		String result = "";
		int index;
		if (linksLabelCounter.containsKey(label)) {
			index = linksLabelCounter.get(label).intValue();
			linksLabelCounter.put(label, ++index);
		} else {
			index = 1;
			linksLabelCounter.put(label, index);
		}
		result = label + "_" + index;
		return result;
	}
	
	private void addSemanticTypesToGraph() {
		
		logger.debug("<enter");
		String id;
		NameSet name;
		NodeType type;
		String label;
		
		for (int i = 0; i < this.semanticTypes.size(); i++) {
			name = semanticTypes.get(i);
			label = name.getLabel();
			id = createNodeID(label);
			
			if (OntologyManager.Instance().isClass(label))
				type = NodeType.Class;
			else if (OntologyManager.Instance().isDataProperty(label))
				type = NodeType.DataProperty;
			else
				type = null;
			
			if (type == null) {
				logger.debug("could not find type of " + label + " in the ontology.");
				continue;
			}
			
			Vertex v = new Vertex(id, name, type);
			semanticNodes.add(v);
			graph.addVertex(v);
		}


		// Add Thing to Graph if it is not added before.
		// Preventing from have an unconnected graph
		NameSet thingName = new NameSet(OWL_NameSpace, Thing_CLASS);
		String thingLabel = thingName.getLabel();
		if (!nodesLabelCounter.containsKey(thingLabel)) {
			Vertex v = new Vertex(createNodeID(thingLabel), thingName, NodeType.Class);			
			this.graph.addVertex(v);
		}
		
		logger.debug("exit>");
	}
	
	private void addNodesClosure() {
		
		logger.debug("<enter");

		String label;
		List<Vertex> recentlyAddedNodes = new ArrayList<Vertex>(graph.vertexSet());
		List<Vertex> newNodes;
		List<NameSet> dpDomainClasses = new ArrayList<NameSet>();
		List<NameSet> opDomainClasses = new ArrayList<NameSet>();
		List<NameSet> superClasses = new ArrayList<NameSet>();
		List<NameSet> newAddedClasses = new ArrayList<NameSet>();

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
				
				if (recentlyAddedNodes.get(i).getType() == NodeType.Class) {
						opDomainClasses = OntologyManager.Instance().getDomainsGivenRange(label, true);
						superClasses = OntologyManager.Instance().getSuperClasses(label, false);
				} else if (recentlyAddedNodes.get(i).getType() == NodeType.DataProperty) {
					dpDomainClasses = OntologyManager.Instance().getDomainsGivenProperty(label, true);
				}
				
				newAddedClasses.addAll(opDomainClasses);
				newAddedClasses.addAll(dpDomainClasses);
				newAddedClasses.addAll(superClasses);
				
				for (int j = 0; j < newAddedClasses.size(); j++) {
					if (!nodesLabelCounter.containsKey(newAddedClasses.get(j).getLabel())) { // if node is not in graph yet
						label = newAddedClasses.get(j).getLabel();
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
		List<NameSet> objectProperties = new ArrayList<NameSet>();
		List<NameSet> dataProperties = new ArrayList<NameSet>();
		
		Vertex source;
		Vertex target;
		String sourceLabel;
		String targetLabel;
		
		String id;
		NameSet name;
		
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices.length; j++) {
				
				if (j == i)
					continue;
				
				source = vertices[i];
				target = vertices[j];
				sourceLabel = source.getLabel();
				targetLabel = target.getLabel();
				
				// There is no outgoing link from DataProperty nodes
				if (source.getType() == NodeType.DataProperty)
					break;
				
				// create a link from the domain and all its subclasses of this DataProperty to range
				if (target.getType() == NodeType.DataProperty) {
					dataProperties = OntologyManager.Instance().getDataProperties(sourceLabel, targetLabel, true);
					
					for (int k = 0; k < dataProperties.size(); k++) {
						name = new NameSet(dataProperties.get(k).getNs(), dataProperties.get(k).getLocalName());
						id = createLinkID(name.getLabel());
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, LinkType.DataProperty);
						this.graph.addEdge(source, target, e);
						this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
					}
				}

				// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
				if (target.getType() == NodeType.Class) {
					objectProperties = OntologyManager.Instance().getObjectProperties(sourceLabel, targetLabel, true);
					
					for (int k = 0; k < objectProperties.size(); k++) {
						name = new NameSet(objectProperties.get(k).getNs(), objectProperties.get(k).getLocalName());
						id = createLinkID(name.getLabel());
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, LinkType.DataProperty);
						this.graph.addEdge(source, target, e);
						this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
					}
				}
				
				if (target.getType() == NodeType.Class) {
					// we have to check both sides.
					if (OntologyManager.Instance().isSubClass(targetLabel, sourceLabel, false) ||
							OntologyManager.Instance().isSuperClass(sourceLabel, targetLabel, false)) {
						name = new NameSet(OWL_NameSpace, SUBCLASS_LINKNAME);
						id = createLinkID(name.getLabel());
						LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, LinkType.HasSubClass);
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
		NameSet thingLinkName = new NameSet(OWL_NameSpace, SUBCLASS_LINKNAME);

		
		NameSet thingName = new NameSet(OWL_NameSpace, Thing_CLASS);
		String thingLabel = thingName.getLabel();

		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices.length; j++) {
				
				if (j == i)
					continue;
				
				source = vertices[i];
				target = vertices[j];
				sourceLabel = source.getLabel();
				targetLabel = target.getLabel();
				
				// There is no outgoing link from DataProperty nodes
				if (source.getType() != NodeType.Class)
					break;
				
				if (target.getType() != NodeType.Class)
					continue;
				
				if (!sourceLabel.equalsIgnoreCase(thingLabel))
					continue;
				
				if (OntologyManager.Instance().getSuperClasses(targetLabel, false).size() != 0)
					continue;
				
				// create a link from all Thing nodes to nodes who don't have any superclasses
				if (target.getType() == NodeType.Class) {
					id = createLinkID(thingLinkName.getLabel());
					LabeledWeightedEdge e = new LabeledWeightedEdge(id, thingLinkName, LinkType.HasSubClass);
					this.graph.addEdge(source, target, e);
					this.graph.setEdgeWeight(e, MAX_WEIGHT);					
				}
			}
		}
		
		logger.debug("exit>");

	}
	
	public Vertex copyNode(Vertex node) {
		
		if (node.getType() != NodeType.Class) {
			logger.debug("nodes other than type of Class cannot be duplicated.");
			return null;
		}
		
		String id;
		NameSet name;
		
		name = node.getName();
		id = createNodeID(name.getLabel());
		
		Vertex newNode = new Vertex(id, name, node.getType());
		
		this.graph.addVertex(newNode);
		
		return newNode;
	}
	
	public void copyLinks(Vertex source, Vertex target) {
		
		LabeledWeightedEdge[] outgoing =  graph.outgoingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		LabeledWeightedEdge[] incoming = graph.incomingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		
		String id;
		NameSet name;
		
		Vertex s, t;
		
		if (outgoing != null)
			for (int i = 0; i < outgoing.length; i++) {
				name = outgoing[i].getName();
				id = createLinkID(name.getLabel());
				LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, outgoing[i].getType());
				s = target;
				t = outgoing[i].getTarget();
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, outgoing[i].getWeight());
			}
		
		if (incoming != null)
			for (int i = 0; i < incoming.length; i++) {
				name = incoming[i].getName();
				id = createLinkID(name.getLabel());
				LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, incoming[i].getType());
				s = incoming[i].getSource();
				t = target;
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, incoming[i].getWeight());
			}
		
		if (source.getType() != NodeType.Class || target.getType() != NodeType.Class) 
			return;

		// interlinks from source to target
		s = source; t= target;
		List<NameSet> objectProperties = OntologyManager.Instance().getObjectProperties(s.getLabel(), t.getLabel(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			name = new NameSet(objectProperties.get(k).getNs(), objectProperties.get(k).getLocalName());
			id = createLinkID(name.getLabel());
			LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, LinkType.DataProperty);
			this.graph.addEdge(s, t, e);
			this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
		}

		// interlinks from target to source
		s = target; t= source;
		objectProperties = OntologyManager.Instance().getObjectProperties(s.getLabel(), t.getLabel(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			name = new NameSet(objectProperties.get(k).getNs(), objectProperties.get(k).getLocalName());
			id = createLinkID(name.getLabel());
			LabeledWeightedEdge e = new LabeledWeightedEdge(id, name, LinkType.DataProperty);
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
}
