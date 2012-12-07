/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.service.Namespaces;
import edu.isi.karma.service.Prefixes;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);
	

//	private List<SemanticType> semanticTypes;
	private List<Vertex> semanticNodes;
	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph;
	private OntologyManager ontologyManager;
	private boolean separateDomainInstancesForSameDataProperties;
	
	private HashMap<String, Integer> nodesLabelCounter;
	private HashMap<String, Integer> linksLabelCounter;
	private HashMap<String, Integer> dataPropertyWithDomainCounter;

	private HashMap<String, Vertex> nodes;
	private HashMap<String, LabeledWeightedEdge> links;
	
	// they might be used later
//	private HashMap<String, List<String>> uriNodeInstances;
//	private HashMap<String, List<String>> uriLinkInstances;

	// SourceId + TargetId -> Link Uri
	private HashMap<String, List<String>> linksFromSourceToTarget;

	public GraphBuilder(OntologyManager ontologyManager, List<SemanticType> semanticTypes, boolean separateDomainInstancesForSameDataProperties) {
		this.ontologyManager = ontologyManager;
		this.separateDomainInstancesForSameDataProperties = separateDomainInstancesForSameDataProperties;
		
		nodes = new HashMap<String, Vertex>();
		links = new HashMap<String, LabeledWeightedEdge>();
		
//		uriNodeInstances = new HashMap<String, List<String>>();
//		uriLinkInstances = new HashMap<String, List<String>>();
		
		linksFromSourceToTarget = new HashMap<String, List<String>>();
		
		nodesLabelCounter = new HashMap<String, Integer>();
		linksLabelCounter = new HashMap<String, Integer>();
		dataPropertyWithDomainCounter = new HashMap<String, Integer>();
		
		long start = System.currentTimeMillis();
		
//		this.semanticTypes = semanticTypes;
		graph = new DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		semanticNodes = new ArrayList<Vertex>();
			
		buildInitialGraph();
		for (SemanticType st : semanticTypes)
			addSemanticType(st);

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;

		logger.info("total number of nodes in the graph: " + this.graph.vertexSet().size());
		logger.info("total number of links in the graph: " + this.graph.edgeSet().size());
		logger.info("total time to build the graph: " + elapsedTimeSec);
	}

	private void addNode(Vertex v) {
		
		if (this.nodes.containsKey(v.getID()))
			return;

		this.graph.addVertex(v);

		this.nodes.put(v.getID(), v);
		
//		List<String> instances = this.uriNodeInstances.get(v.getUriString());
//		if (instances == null) {
//			instances = new ArrayList<String>();
//			instances.add(v.getID());
//			uriNodeInstances.put(v.getUriString(), instances);
//		} 
//		else instances.add(v.getID());

	}
	
	private void addLink(Vertex source, Vertex target, URI uri, LinkType linkType, double weight) {
		
		// check to see if the link is duplicate or not
		List<String> existingUris = this.linksFromSourceToTarget.get(source.getID() + target.getID());
		if (existingUris == null) {
			existingUris = new ArrayList<String>();
			existingUris.add(uri.getUriString());
			linksFromSourceToTarget.put(source.getID() + target.getID(), existingUris);
		} else if (existingUris.indexOf(uri.getUriString()) == -1) {
			existingUris.add(uri.getUriString());		
		} else { // this is a duplicate link
			return; 
		}
		
		String id = createLinkID(uri.getUriString());
		LabeledWeightedEdge e = new LabeledWeightedEdge(id, uri, linkType);
		this.graph.addEdge(source, target, e);
		this.graph.setEdgeWeight(e, weight);
		
		this.links.put(e.getID(), e);
		
//		List<String> instances = this.uriLinkInstances.get(e.getUriString());
//		if (instances == null) {
//			instances = new ArrayList<String>();
//			instances.add(e.getID());
//			uriLinkInstances.put(e.getUriString(), instances);
//		} 
//		else instances.add(e.getID());
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
//			id = label + "" + index;
			id = label;
		}
		return id;
	}
	
	private String createVisitedDataProperty(String label, String domain) {
		
		int index;
		String id;
		
		String key = domain + label;
		
		if (dataPropertyWithDomainCounter.containsKey(key)) {
			index = dataPropertyWithDomainCounter.get(key).intValue();
			dataPropertyWithDomainCounter.put(key, ++index);
			id = label + "" + index;
		} else {
			index = 1;
			dataPropertyWithDomainCounter.put(key, index);
//			id = label + "" + index;
			id = label;
		}
		return id;
	}
	
	private String getLastID(String label) {
		
		int index;
		
		if (nodesLabelCounter.containsKey(label)) {
			index = nodesLabelCounter.get(label).intValue();
			if (index == 1)
				return label;
			else
				return (label + "" + index);
		} else 
			return null;
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
//			id = label + "" + index;
			id = label;
		}
		return id;
	}
	
	private void buildInitialGraph() {

		logger.info("create a graph with a single Thing node");

		if (!nodesLabelCounter.containsKey(ModelingParams.THING_URI)) {
			Vertex v = new Vertex(createNodeID(ModelingParams.THING_URI), new URI(ModelingParams.THING_URI, Namespaces.OWL, Prefixes.OWL), NodeType.Class);			
			addNode(v);
		}
	
		logger.debug("exit>");
	}
	
	private Vertex addSemanticTypeToGraph(SemanticType semanticType) {
		
		logger.debug("<enter");
		String id;
		NodeType nodeType;
		String label;

		if (semanticType == null)
			return null;
		
		
		label = semanticType.getType().getUriString();
		id = createNodeID(label);
			
		if (ontologyManager.isClass(label))
			nodeType = NodeType.Class;
		else if (ontologyManager.isProperty(label))
			nodeType = NodeType.DataProperty;
		else
			nodeType = null;
		
		if (nodeType == null) {
			logger.debug("could not find type of " + label + " in the ontology.");
			return null;
		}
			
		Vertex v = new Vertex(id, semanticType.getType(), semanticType, nodeType);
		semanticNodes.add(v);
		addNode(v);
		
		logger.debug("exit>");		
		return v;
	}
	
	private Vertex addDomainOfDataPropertyNodeToGraph(Vertex vertex) {
		
		logger.debug("<enter");
		String id;
		
		Vertex domain = null;
		
//		List<String> visitedDataProperties = new ArrayList<String>();
		Vertex v = vertex;

			
		if (v.getNodeType() != NodeType.DataProperty)
			return null;
		
		if (v.getSemanticType() == null)
			return null;

		URI domainURI = v.getSemanticType().getDomain();
		if (domainURI == null || domainURI.getUriString() == null || domainURI.getUriString().trim().length() == 0)
			return null;
		
		String domainClass = v.getSemanticType().getDomain().getUriString();
	
		if (!ontologyManager.isClass(domainClass))
			return null;
		
		if (!separateDomainInstancesForSameDataProperties) {
			if (nodesLabelCounter.get(domainClass) == null) {
				id = createNodeID(domainClass);
				domain = new Vertex(id, domainURI, NodeType.Class);
				addNode(domain);
				v.setDomainVertexId(domain.getID());
			}
			else {
				String domainId = getLastID(domainClass);
				v.setDomainVertexId(domainId);
				domain = GraphUtil.getVertex(graph, domainId);
			}
		} else {
//			if (nodesLabelCounter.indexOf(domainClass + v.getUriString()) != -1 || nodesLabelCounter.get(domainClass) == null) {
			if (dataPropertyWithDomainCounter.get(domainClass + v.getUriString()) != null || nodesLabelCounter.get(domainClass) == null) {
				id = createNodeID(domainClass);
				domain = new Vertex(id, domainURI, NodeType.Class);
				addNode(domain);
				v.setDomainVertexId(domain.getID());
			}
			else {
				String domainId = getLastID(domainClass);
				v.setDomainVertexId(domainId);
				domain = GraphUtil.getVertex(graph, domainId);
			}
//			visitedDataProperties.add(domainClass + v.getUriString());
			createVisitedDataProperty(v.getUriString(), domainClass);
		}

		logger.debug("exit>");
		return domain;
	}
	
	private List<Vertex> addNodeClosure(Vertex vertex) {
		
		logger.debug("<enter");
		List<Vertex> nodeClosureList = new ArrayList<Vertex>();

		String label;
		List<Vertex> recentlyAddedNodes = new ArrayList<Vertex>();
		recentlyAddedNodes.add(vertex);
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
				
				label = recentlyAddedNodes.get(i).getUriString();
				if (processedLabels.indexOf(label) != -1) 
					continue;
				
				processedLabels.add(label);
				
				if (recentlyAddedNodes.get(i).getNodeType() == NodeType.Class) {
					opDomainClasses = ontologyManager.getDomainsGivenRange(label, true);
					superClasses = ontologyManager.getSuperClasses(label, false);
				} else if (recentlyAddedNodes.get(i).getNodeType() == NodeType.DataProperty) {
					dpDomainClasses = ontologyManager.getDomainsGivenProperty(label, true);
				}
				
				if (opDomainClasses != null)
					newAddedClasses.addAll(opDomainClasses);
				if (dpDomainClasses != null)
					newAddedClasses.addAll(dpDomainClasses);
				if (superClasses != null)
					newAddedClasses.addAll(superClasses);
				
				for (int j = 0; j < newAddedClasses.size(); j++) {
					if (!nodesLabelCounter.containsKey(newAddedClasses.get(j))) { // if node is not in graph yet
						label = newAddedClasses.get(j);
						Vertex v = new Vertex(createNodeID(label), ontologyManager.getURIFromString(newAddedClasses.get(j)), NodeType.Class);
						newNodes.add(v);
						nodeClosureList.add(v);
						addNode(v);
					}
				}
			}
			
			recentlyAddedNodes = newNodes;
			newAddedClasses.clear();
		}

		logger.debug("exit>");
		return nodeClosureList;
	}
	
	private void updateLinks(List<Vertex> newNodes) {
		
		logger.debug("<enter");

		Vertex[] vertices = this.graph.vertexSet().toArray(new Vertex[0]);
		List<String> objectProperties = new ArrayList<String>();
		//List<String> dataProperties = new ArrayList<String>();
		
		Vertex source;
		Vertex target;
		String sourceLabel;
		String targetLabel;
		
		String label;
		
		logger.debug("number of set1 vertices (first loop): " + vertices.length);
		logger.debug("number of set2 vertices (second loop): " + newNodes.size());
		
		for (int i = 0; i < vertices.length; i++) {
			for (int u = 0; u < newNodes.size(); u++) {
				
//				logger.debug("node1: " + vertices[i]);
//				logger.debug("node2: " + newNodes.get(u));

				if (vertices[i].equals(newNodes.get(u)))
					continue;

				for (int j = 0; j < 2; j++) {	
					
					if (j == 0) {
						source = vertices[i];
						target = newNodes.get(u);
					} else {
						source = newNodes.get(u);
						target = vertices[i];
					}
					
//					logger.debug("examining the links between nodes " + i + "," + u);
					
					sourceLabel = source.getUriString();
					targetLabel = target.getUriString();
	
					// There is no outgoing link from DataProperty nodes
					if (source.getNodeType() == NodeType.DataProperty)
						break;
					
					// create a link from the domain and all its subclasses of this DataProperty to range
					if (target.getNodeType() == NodeType.DataProperty) {
						
						String domain = "";
						if (target.getSemanticType() != null && 
								target.getSemanticType().getDomain() != null)
							domain = target.getSemanticType().getDomain().getUriString();
						
						if (domain != null && domain.trim().equalsIgnoreCase(sourceLabel.trim()))
						
						//dataProperties = ontologyManager.getDataProperties(sourceLabel, targetLabel, true);
						//for (int k = 0; k < dataProperties.size(); k++) 
						
						{
							
							// label of the data property nodes is equal to name of the data properties
							label = targetLabel; // dataProperties.get(k);
							addLink(source, target, ontologyManager.getURIFromString(label), LinkType.DataProperty, ModelingParams.DEFAULT_WEIGHT);
						}
					}
	
					boolean inherited = true;
					// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
					if (target.getNodeType() == NodeType.Class) {
						objectProperties = ontologyManager.getObjectProperties(sourceLabel, targetLabel, true);
						
						for (int k = 0; k < objectProperties.size(); k++) {
							label = objectProperties.get(k);
							
							List<String> dirDomains = ontologyManager.getOntCache().getPropertyDirectDomains().get(label);
							List<String> dirRanges = ontologyManager.getOntCache().getPropertyDirectRanges().get(label);
					
							if (dirDomains != null && dirDomains.indexOf(sourceLabel) != -1 &&
									dirRanges != null && dirRanges.indexOf(targetLabel) != -1)
								inherited = false;
							
							// prefer the links which are actually defined between source and target in ontology over inherited ones.
							if (inherited) 
								addLink(source, target, ontologyManager.getURIFromString(label), LinkType.ObjectProperty, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
							else
								addLink(source, target, ontologyManager.getURIFromString(label), LinkType.ObjectProperty, ModelingParams.DEFAULT_WEIGHT);
						}
					}
					
					if (target.getNodeType() == NodeType.Class) {
						// we have to check both sides.
						if (ontologyManager.isSubClass(targetLabel, sourceLabel, false) ||
								ontologyManager.isSuperClass(sourceLabel, targetLabel, false)) {
							URI uri = new URI(ModelingParams.HAS_SUBCLASS_URI, Namespaces.EXAMPLE, Prefixes.EXAMPLE);
							addLink(source, target, uri, LinkType.HasSubClass, ModelingParams.MAX_WEIGHT);
						}
					}
					
				}
			}
		}
		
		logger.debug("exit>");
	}
	
	
	private void updateLinksFromThing() {
		
		logger.debug("<enter");

		Vertex[] vertices = this.graph.vertexSet().toArray(new Vertex[0]);
		
		Vertex source;
		Vertex target;
		String sourceLabel;
//		String targetLabel;

		for (int i = 0; i < vertices.length; i++) {
			
			source = vertices[i];
			sourceLabel = source.getUriString();
			if (!sourceLabel.equalsIgnoreCase(ModelingParams.THING_URI))
				continue;
			
			for (int j = 0; j < vertices.length; j++) {
				
				if (j == i)
					continue;
				
				target = vertices[j];
//				targetLabel = target.getUriString();

			
				if (target.getNodeType() != NodeType.Class)
					continue;
				
				boolean parentExist = false;
				boolean parentThing = false;
				LabeledWeightedEdge[] incomingLinks = this.graph.incomingEdgesOf(target).toArray(new LabeledWeightedEdge[0]); 
				for (LabeledWeightedEdge inLink: incomingLinks) {
					if (inLink.getUriString().equalsIgnoreCase(ModelingParams.HAS_SUBCLASS_URI)) {
						if (!sourceLabel.equalsIgnoreCase(sourceLabel))
							parentExist = true;
						else
							parentThing = true;
					}
				}
				if (parentExist) {
					this.graph.removeAllEdges(source, target);
					continue;
				}

				if (parentThing)
					continue;
				
				// create a link from Thing node to nodes who don't have any superclasses
				if (target.getNodeType() == NodeType.Class) {
					URI uri = new URI(ModelingParams.HAS_SUBCLASS_URI, Namespaces.EXAMPLE, Prefixes.EXAMPLE);
					addLink(source, target, uri, LinkType.HasSubClass, ModelingParams.MAX_WEIGHT);
				}
			}
			
			break;
		}
		
		logger.debug("exit>");

	}
	
	public void addSemanticType(SemanticType semanticType) {

		if (semanticType == null) {
			logger.debug("semantic types list is null.");
			return;
		}

		long start = System.currentTimeMillis();
		float elapsedTimeSec;
		
		// Add the new node (single node)
		Vertex v = addSemanticTypeToGraph(semanticType);
		long addSemanticTypes = System.currentTimeMillis();
		elapsedTimeSec = (addSemanticTypes - start)/1000F;
		logger.info("time to add semantic type: " + elapsedTimeSec);
		if (v == null)
			return;

		// Add domains of the new node
		Vertex domain = addDomainOfDataPropertyNodeToGraph(v);
		long addDomainsOfDataPropertyNodes = System.currentTimeMillis();
		elapsedTimeSec = (addDomainsOfDataPropertyNodes - addSemanticTypes)/1000F;
		logger.info("time to add domain of data property node to graph: " + elapsedTimeSec);

		// Add the node cloure
		List<Vertex> newNodes = addNodeClosure(v);
		if (domain != null) {
			newNodes.add(domain);
			newNodes.addAll(addNodeClosure(domain));
		}
		newNodes.add(v);

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - addDomainsOfDataPropertyNodes)/1000F;
		logger.info("number of new nodes added to the graph: " + newNodes.size());
		logger.info("time to add nodes closure: " + elapsedTimeSec);
		
		// Add links 
		int previousNumOfLinks = this.links.size();
		updateLinks(newNodes);
		int newNumOfLinks = this.links.size();
		
		long addLinks = System.currentTimeMillis();
		elapsedTimeSec = (addLinks - addNodesClosure)/1000F;
		logger.info("number of new links added to the graph: " + (newNumOfLinks - previousNumOfLinks));
		logger.info("time to add links to graph: " + elapsedTimeSec);

		// Add links from Thing (root)
		updateLinksFromThing();
		long addLinksFromThing = System.currentTimeMillis();
		elapsedTimeSec = (addLinksFromThing - addLinks)/1000F;
		logger.info("time to update links from Thing (root): " + elapsedTimeSec);

		logger.info("total number of nodes: " + this.graph.vertexSet().size());
		logger.info("total number of links: " + this.graph.edgeSet().size());

	}
	
	public Vertex removeSemanticType(SemanticType semanticType) {
		
		logger.debug("<enter");

		if (semanticType.getHNodeId() == null || semanticType.getHNodeId().trim().length() == 0)
			return null;
		
		
		Vertex[] vertices = this.graph.vertexSet().toArray(new Vertex[0]);

		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].getSemanticType() != null && 
					vertices[i].getSemanticType().getHNodeId() != null &&
					vertices[i].getSemanticType().getHNodeId().equalsIgnoreCase(semanticType.getHNodeId()) ) {
				Vertex v = vertices[i];
				
				LabeledWeightedEdge[] incomingLinks = this.graph.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]); 
				for (LabeledWeightedEdge inLink: incomingLinks) {
					this.graph.removeAllEdges( inLink.getSource(), inLink.getTarget() );
				}
				LabeledWeightedEdge[] outgoingLinks = this.graph.outgoingEdgesOf(v).toArray(new LabeledWeightedEdge[0]); 
				for (LabeledWeightedEdge outLink: outgoingLinks) {
					this.graph.removeAllEdges( outLink.getSource(), outLink.getTarget() );
				}				
				this.graph.removeVertex(v);
				for (int k = 0; k < this.semanticNodes.size(); k++) {
					 Vertex semNode = this.semanticNodes.get(k);
					if (semNode.getID().equalsIgnoreCase(v.getID())) {
						this.semanticNodes.remove(k);
						break;
					}
				}
				
				URI domainURI = v.getSemanticType().getDomain();
				if (domainURI != null && domainURI.getUriString() != null && domainURI.getUriString().trim().length() != 0) {
					String domainClass = v.getSemanticType().getDomain().getUriString();
					// because the node is a dataproperty node, its Uri is the same as the Uri of the data property in the ontology
					Integer count = dataPropertyWithDomainCounter.get(domainClass + v.getUriString());
					if (count != null) {
						if (count == 1) {
							dataPropertyWithDomainCounter.remove(domainClass + v.getUriString());
						} else {
							count --;
						}
					}
				}
				
				
				logger.debug("exit>");
				return v;
			}
		}
		
		logger.debug("no node matched the input nodeID.");
		logger.debug("exit>");
		return null;
	}
	
	public Vertex copyNode(Vertex node) {
		
		if (node.getNodeType() != NodeType.Class) {
			logger.debug("nodes other than type of Class cannot be duplicated.");
			return null;
		}
		
		String id;
		String label;
		
		label = node.getUriString();
		id = createNodeID(label);
		
		Vertex newNode = new Vertex(id, ontologyManager.getURIFromString(label), node.getNodeType());
		
		addNode(newNode);
	
		return newNode;
	}
	
	public void copyLinks(Vertex source, Vertex target) {
		
		LabeledWeightedEdge[] outgoing =  graph.outgoingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		LabeledWeightedEdge[] incoming = graph.incomingEdgesOf(source).toArray(new LabeledWeightedEdge[0]);
		
		String label;
		
		Vertex s, t;
		
		if (outgoing != null)
			for (int i = 0; i < outgoing.length; i++) {
				label = outgoing[i].getUriString();
				URI uri = new URI(outgoing[i].getUriString(), 
							outgoing[i].getNs(),
							outgoing[i].getPrefix());
				s = target;
				t = outgoing[i].getTarget();
				addLink(s, t, uri, outgoing[i].getLinkType(), outgoing[i].getWeight());
			}
		
		if (incoming != null)
			for (int i = 0; i < incoming.length; i++) {
				label = incoming[i].getUriString();
				URI uri = new URI(incoming[i].getUriString(), 
						incoming[i].getNs(),
						incoming[i].getPrefix());
				s = incoming[i].getSource();
				t = target;
				addLink(s, t, uri, incoming[i].getLinkType(), incoming[i].getWeight());
			}
		
		if (source.getNodeType() != NodeType.Class || target.getNodeType() != NodeType.Class) 
			return;

		// interlinks from source to target
		s = source; t= target;
		List<String> objectProperties = ontologyManager.getObjectProperties(s.getUriString(), t.getUriString(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			addLink(s, t, ontologyManager.getURIFromString(label), LinkType.ObjectProperty, ModelingParams.DEFAULT_WEIGHT);
		}

		// interlinks from target to source
		s = target; t= source;
		objectProperties = ontologyManager.getObjectProperties(s.getUriString(), t.getUriString(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			addLink(s, t, ontologyManager.getURIFromString(label), LinkType.ObjectProperty, ModelingParams.DEFAULT_WEIGHT);
		}

	}
	
	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getGraph() {
		return this.graph;
	}
	
	public List<Vertex> getSemanticNodes() {
		return this.semanticNodes;
	}

	public Vertex getNodeById(String id) {
		return this.nodes.get(id);
	}
	
	public LabeledWeightedEdge geLinkById(String id) {
		return this.links.get(id);
	}
}
