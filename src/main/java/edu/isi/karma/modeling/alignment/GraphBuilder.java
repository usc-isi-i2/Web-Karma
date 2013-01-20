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

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.Label;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);
	

	private List<Node> semanticNodes;
	private DirectedWeightedMultigraph<Node, Link> graph;
	private OntologyManager ontologyManager;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	HashMap<String, Node> idToNodes;
	HashMap<String, Link> idToLinks;

	HashMap<String, List<Node>> uriToNodes;
	HashMap<String, List<Link>> uriToLinks;

	private static String THING_URI = "http://www.w3.org/2002/07/owl#Thing";
	private static String THING_NS = "http://www.w3.org/2002/07/owl#";
	private static String THING_PREFIX = "owl";
	
	private static String SUBCLASS_URI = "hasSubClass";
	private static String SUBCLASS_NS = "http://example.com#";
	private static String SUBCLASS_PREFIX = "";

	public static double DEFAULT_WEIGHT = 1.0;	
	public static double MIN_WEIGHT = 0.000001; // need to be fixed later	
	public static double MAX_WEIGHT = 1000000;
	
	public GraphBuilder(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, LinkIdFactory linkIdFactory) {
		
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = nodeIdFactory;
		this.linkIdFactory = linkIdFactory;

		this.idToNodes = new HashMap<String, Node>();
		this.idToLinks = new HashMap<String, Link>();
		
		this.uriToNodes = new HashMap<String, List<Node>>();
		this.uriToLinks = new HashMap<String, List<Link>>();
		
		graph = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		semanticNodes = new ArrayList<Node>();
			
		initialGraph();

		logger.debug("graph has been created.");
	}
	
	
	public HashMap<String, Node> getIdToNodes() {
		return idToNodes;
	}


	public HashMap<String, Link> getIdToLinks() {
		return idToLinks;
	}


	public HashMap<String, List<Node>> getUriToNodes() {
		return uriToNodes;
	}


	public HashMap<String, List<Link>> getUriToLinks() {
		return uriToLinks;
	}


//	private boolean duplicateLink(Node source, Node target, String label) {
//		for (Link e : this.graph.outgoingEdgesOf(source)) {
//			if (e.getTarget() == target && e.getUriString().equalsIgnoreCase(label))
//				return true;
//		}
//		return false;
//	}
	
	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (node == null)
			return false;
		
		if (idToNodes.get(node.getID()) != null) {
			logger.debug("The node with id=" + node.getID() + " already exists in the graph");
			return false;
		}
			
		if (node instanceof ColumnNode)
			semanticNodes.add(node);
		
		graph.addVertex(node);
		
		if (node instanceof InternalNode) {
			addNode(node);
		}

		logger.debug("exit>");		
		return true;
	}
	
	private void addNodeClosure(Node node) {
		
		logger.debug("<enter");

		String uriString;
		List<Node> recentlyAddedNodes = new ArrayList<Node>();
		recentlyAddedNodes.add(node);
		List<Node> newNodes;
		List<String> dpDomainClasses = new ArrayList<String>();
		List<String> opDomainClasses = new ArrayList<String>();
		List<String> superClasses = new ArrayList<String>();
		List<String> newAddedClasses = new ArrayList<String>();

		// We don't need to add subclasses of each class separately.
		// The only place in which we add children is where we are looking for domain class of a property.
		// In this case, in addition to the domain class, we add all of its children too.
		
		List<String> processedLabels = new ArrayList<String>();
		while (recentlyAddedNodes.size() > 0) {
			
			newNodes = new ArrayList<Node>();
			for (int i = 0; i < recentlyAddedNodes.size(); i++) {
				
				uriString = recentlyAddedNodes.get(i).getUriString();
				if (processedLabels.indexOf(uriString) != -1) 
					continue;
				
				processedLabels.add(uriString);
				
				if (recentlyAddedNodes.get(i) instanceof InternalNode) {
					opDomainClasses = ontologyManager.getDomainsGivenRange(uriString, true);
					superClasses = ontologyManager.getSuperClasses(uriString, false);
				} else if (recentlyAddedNodes.get(i) instanceof ColumnNode) {
					dpDomainClasses = ontologyManager.getDomainsGivenProperty(uriString, true);
				}
				
				if (opDomainClasses != null)
					newAddedClasses.addAll(opDomainClasses);
				if (dpDomainClasses != null)
					newAddedClasses.addAll(dpDomainClasses);
				if (superClasses != null)
					newAddedClasses.addAll(superClasses);
				
				for (int j = 0; j < newAddedClasses.size(); j++) {
					if (!nodesLabelCounter.containsKey(newAddedClasses.get(j))) { // if node is not in graph yet
						uriString = newAddedClasses.get(j);
						Node v = new Node(createNodeID(uriString), ontologyManager.getURIFromString(newAddedClasses.get(j)), NodeType.Class);
						newNodes.add(v);
						this.graph.addVertex(v);
					}
				}
			}
			
			recentlyAddedNodes = newNodes;
			newAddedClasses.clear();
		}

		logger.debug("exit>");
	}

	private void addLinks(Node vertex) {
		
		logger.debug("<enter");

		Node[] vertices = this.graph.vertexSet().toArray(new Node[0]);
		List<String> objectProperties = new ArrayList<String>();
		//List<String> dataProperties = new ArrayList<String>();
		
		Node source;
		Node target;
		String sourceLabel;
		String targetLabel;
		
		String id;
		String label;
		
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < 2; j++) {	
				
				if (vertices[i].getID().equalsIgnoreCase(vertex.getID()))
					continue;
				
				if (j == 0) {
					source = vertices[i];
					target = vertex;
				} else {
					source = vertex;
					target = vertices[i];
				}
				
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
						if (!duplicateLink(source, target, label)) {
							id = createLinkID(label);
	//						id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
							Link e = new Link(id, ontologyManager.getURIFromString(label), LinkType.DataProperty);
							this.graph.addEdge(source, target, e);
							this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
						}

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
						
						if (!duplicateLink(source, target, label)) {
							id = createLinkID(label);
	//						id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
							Link e = new Link(id, ontologyManager.getURIFromString(label), LinkType.ObjectProperty);
							this.graph.addEdge(source, target, e);
							
							// prefer the links which are actually defined between source and target in ontology over inherited ones.
							if (inherited)
								this.graph.setEdgeWeight(e, DEFAULT_WEIGHT + MIN_WEIGHT);
							else
								this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
						}
					}
				}
				
				if (target.getNodeType() == NodeType.Class) {
					// we have to check both sides.
					if (ontologyManager.isSubClass(targetLabel, sourceLabel, false) ||
							ontologyManager.isSuperClass(sourceLabel, targetLabel, false)) {
						if (!duplicateLink(source, target, SUBCLASS_URI)) {
							id = createLinkID(SUBCLASS_URI);
	//						id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), SUBCLASS_URI);
							Link e = new Link(id, 
									new Label(SUBCLASS_URI, SUBCLASS_NS, SUBCLASS_PREFIX), 
									LinkType.HasSubClass);
							this.graph.addEdge(source, target, e);
							this.graph.setEdgeWeight(e, MAX_WEIGHT);	
						}
					}
				}
				
			}
		}
		
//		logger.info("number of links added to graph: " + this.graph.edgeSet().size());
		logger.debug("exit>");
	}

	public void addSemanticType(SemanticType semanticType) {

		if (semanticType == null) {
			logger.debug("semantic types list is null.");
			return;
		}

		long start = System.currentTimeMillis();
		float elapsedTimeSec;
		
		Node v = addSemanticTypeToGraph(semanticType);
		long addSemanticTypes = System.currentTimeMillis();
		elapsedTimeSec = (addSemanticTypes - start)/1000F;
		logger.info("number of nodes: " + this.graph.vertexSet().size());
		logger.info("time to add semantic type: " + elapsedTimeSec);
		if (v == null)
			return;

		Node domain = addDomainOfDataPropertyNodeToGraph(v);
		long addDomainsOfDataPropertyNodes = System.currentTimeMillis();
		elapsedTimeSec = (addDomainsOfDataPropertyNodes - addSemanticTypes)/1000F;
		logger.info("time to add domain of data property node to graph: " + elapsedTimeSec);

		addNodeClosure(v);
		if (domain != null) addNodeClosure(domain);
		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - addDomainsOfDataPropertyNodes)/1000F;
		logger.info("time to add nodes closure: " + elapsedTimeSec);
		
		addLinks(v);
		if (domain != null) addLinks(domain);
		long addLinks = System.currentTimeMillis();
		elapsedTimeSec = (addLinks - addNodesClosure)/1000F;
		logger.info("time to add links to graph: " + elapsedTimeSec);

		addLinksFromThing();
		long addLinksFromThing = System.currentTimeMillis();
		elapsedTimeSec = (addLinksFromThing - addLinks)/1000F;
//		logger.info("time to add links from Thing (root): " + elapsedTimeSec);

	}
	public Node removeSemanticType(SemanticType semanticType) {
		
		logger.debug("<enter");

		if (semanticType.getHNodeId() == null || semanticType.getHNodeId().trim().length() == 0)
			return null;
		
		
		Node[] vertices = this.graph.vertexSet().toArray(new Node[0]);

		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].getSemanticType() != null && 
					vertices[i].getSemanticType().getHNodeId() != null &&
					vertices[i].getSemanticType().getHNodeId().equalsIgnoreCase(semanticType.getHNodeId()) ) {
				Node v = vertices[i];
				
				Link[] incomingLinks = this.graph.incomingEdgesOf(v).toArray(new Link[0]); 
				for (Link inLink: incomingLinks) {
					this.graph.removeAllEdges( inLink.getSource(), inLink.getTarget() );
				}
				Link[] outgoingLinks = this.graph.outgoingEdgesOf(v).toArray(new Link[0]); 
				for (Link outLink: outgoingLinks) {
					this.graph.removeAllEdges( outLink.getSource(), outLink.getTarget() );
				}				
				this.graph.removeVertex(v);
				for (int k = 0; k < this.semanticNodes.size(); k++) {
					 Node semNode = this.semanticNodes.get(k);
					if (semNode.getID().equalsIgnoreCase(v.getID())) {
						this.semanticNodes.remove(k);
						break;
					}
				}
				
				Label domainURI = v.getSemanticType().getDomain();
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
	
	private void initialGraph() {
		
		logger.debug("<enter");
		
		// Add Thing to the Graph 
		String id = graphIdGenerator.getNodeId(THING_URI);
		Label label = new Label(THING_URI, THING_NS, THING_PREFIX);
		Node v = new InternalNode(id, label);			
		this.graph.addVertex(v);
		
		logger.debug("exit>");
	}
	

	private void addLinksFromThing() {
		
		logger.debug("<enter");

		Node[] vertices = this.graph.vertexSet().toArray(new Node[0]);
		
		Node source;
		Node target;
		String sourceLabel;
//		String targetLabel;
		
		String id;

		for (int i = 0; i < vertices.length; i++) {
			
			source = vertices[i];
			sourceLabel = source.getUriString();
			if (!sourceLabel.equalsIgnoreCase(THING_URI))
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
				Link[] incomingLinks = this.graph.incomingEdgesOf(target).toArray(new Link[0]); 
				for (Link inLink: incomingLinks) {
					if (inLink.getUriString().equalsIgnoreCase(SUBCLASS_URI)) {
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
					id = createLinkID(SUBCLASS_URI);
//					id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), SUBCLASS_URI);
					Link e = new Link(id, 
							new Label(SUBCLASS_URI, SUBCLASS_NS, SUBCLASS_PREFIX), 
							LinkType.HasSubClass);
					this.graph.addEdge(source, target, e);
					this.graph.setEdgeWeight(e, MAX_WEIGHT);					
				}
			}
			
			break;
		}
		
		logger.debug("exit>");

	}
	
	public Node copyNode(Node node) {
		
		if (node.getNodeType() != NodeType.Class) {
			logger.debug("nodes other than type of Class cannot be duplicated.");
			return null;
		}
		
		String id;
		String label;
		
		label = node.getUriString();
		id = createNodeID(label);
		
		Node newNode = new Node(id, ontologyManager.getURIFromString(label), node.getNodeType());
		
		this.graph.addVertex(newNode);
	
		return newNode;
	}
	
	public void copyLinks(Node source, Node target) {
		
		Link[] outgoing =  graph.outgoingEdgesOf(source).toArray(new Link[0]);
		Link[] incoming = graph.incomingEdgesOf(source).toArray(new Link[0]);
		
		String id;
		String label;
		
		Node s, t;
		
		if (outgoing != null)
			for (int i = 0; i < outgoing.length; i++) {
				label = outgoing[i].getUriString();
				id = createLinkID(label);
//				id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
				Link e = new Link(id, 
						new Label(outgoing[i].getUriString(), 
								outgoing[i].getNs(),
								outgoing[i].getPrefix()), outgoing[i].getLinkType());
				s = target;
				t = outgoing[i].getTarget();
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, outgoing[i].getWeight());
			}
		
		if (incoming != null)
			for (int i = 0; i < incoming.length; i++) {
				label = incoming[i].getUriString();
				id = createLinkID(label);
//				id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
				Link e = new Link(id, 
						new Label(incoming[i].getUriString(), 
								incoming[i].getNs(),
								incoming[i].getPrefix()), incoming[i].getLinkType());
				s = incoming[i].getSource();
				t = target;
				this.graph.addEdge(s, t, e);
				this.graph.setEdgeWeight(e, incoming[i].getWeight());
			}
		
		if (source.getNodeType() != NodeType.Class || target.getNodeType() != NodeType.Class) 
			return;

		// interlinks from source to target
		s = source; t= target;
		List<String> objectProperties = ontologyManager.getObjectProperties(s.getUriString(), t.getUriString(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			id = createLinkID(label);
//			id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
			Link e = new Link(id, ontologyManager.getURIFromString(label), LinkType.ObjectProperty);
			this.graph.addEdge(s, t, e);
			this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
		}

		// interlinks from target to source
		s = target; t= source;
		objectProperties = ontologyManager.getObjectProperties(s.getUriString(), t.getUriString(), true);
			
		for (int k = 0; k < objectProperties.size(); k++) {
			label = objectProperties.get(k);
			id = createLinkID(label);
//			id = createLinkID(source.getLocalLabel(), target.getLocalLabel(), label);
			Link e = new Link(id, ontologyManager.getURIFromString(label), LinkType.ObjectProperty);
			this.graph.addEdge(s, t, e);
			this.graph.setEdgeWeight(e, DEFAULT_WEIGHT);
		}

	}

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graph;
	}
	
	public List<Node> getSemanticNodes() {
		return this.semanticNodes;
	}

}
