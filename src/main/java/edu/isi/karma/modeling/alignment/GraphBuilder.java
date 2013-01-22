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

import edu.isi.karma.modeling.FixedUris;
import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SubClassOfLink;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);

	private List<Node> semanticNodes;
	private DirectedWeightedMultigraph<Node, Link> graph;
	private OntologyManager ontologyManager;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	private HashMap<String, Node> idToNodes;
	private HashMap<String, Link> idToLinks;

	private HashMap<String, List<Node>> uriToNodes;
	private HashMap<String, List<Link>> uriToLinks;

	private List<String> sourceToTargetLinkUris; 
	private Node thingNode;
	
	// Constructor
	
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
		sourceToTargetLinkUris = new ArrayList<String>();
			
		initialGraph();

		logger.info("graph has been created.");
	}
	
	// Public Methods
	
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

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graph;
	}
	
	public List<Node> getSemanticNodes() {
		return this.semanticNodes;
	}
	
	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (!addSingleNode(node))
			return false;
			
		if (node instanceof ColumnNode)
			semanticNodes.add(node);
				
		if (node instanceof InternalNode) {

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			List<Node> newNodes = addNodeClosure(node);
			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start)/1000F;
			logger.info("time to add nodes closure: " + elapsedTimeSec);

			updateLinks(newNodes);
			long updateLinks = System.currentTimeMillis();
			elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
			logger.info("time to update links of the graph: " + elapsedTimeSec);
			
			updateLinksFromThing();
			long updateLinksFromThing = System.currentTimeMillis();
			elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
			logger.info("time to update links to Thing (root): " + elapsedTimeSec);

		}

		logger.debug("exit>");		
		return true;
	}
	
	public boolean addLink(Node source, Node target, Link link) {

		logger.debug("<enter");

		if (source == null || target == null || link == null) {
			logger.error("Source, target, and the link cannot be null.");
			return false;
		}
		
		if (this.idToLinks.containsKey(link.getID())) {
			logger.debug("The node with id=" + link.getID() + " already exists in the graph");
			return false;
		}
		
		String key = source.getID() + target.getID() + link.getUriString();
		// check to see if the link is duplicate or not
		if (sourceToTargetLinkUris.indexOf(key) != -1)
		{
			logger.debug("There is already a link with label " + link.getUriString() + 
					" from " + source.getID() + " to " + target.getID());
			return false;
		}

		this.graph.addEdge(source, target, link);
		double weight = ModelingParams.DEFAULT_WEIGHT;
		if (link instanceof SubClassOfLink)
			weight = ModelingParams.MAX_WEIGHT;
		this.graph.setEdgeWeight(link, weight);
		
		// update the corresponding lists and hashmaps
		
		this.idToLinks.put(link.getID(), link);
		
		List<Link> linksWithSameUri = uriToLinks.get(link.getUriString());
		if (linksWithSameUri == null) {
			linksWithSameUri = new ArrayList<Link>();
			uriToLinks.put(link.getUriString(), linksWithSameUri);
		}
		linksWithSameUri.add(link);
		
		sourceToTargetLinkUris.add(key);

		logger.debug("exit>");		
		return true;
	}
	
	// Private Methods
	
	private void initialGraph() {
		
		logger.debug("<enter");
		
		// Add Thing to the Graph 
		String id = nodeIdFactory.getNodeId(FixedUris.THING_URI);
		Label label = new Label(FixedUris.THING_URI, Namespaces.OWL, Prefixes.OWL);
		thingNode = new InternalNode(id, label);			
		addSingleNode(thingNode);
		
		logger.debug("exit>");
	}
	
	private boolean addWeightedLink(Node source, Node target, Link link, double weight) {
		if (addLink(source, target, link)) {
			this.graph.setEdgeWeight(link, weight);
			return true;
		} else
			return false;
	}

	private boolean addSingleNode(Node node) {
		
		logger.debug("<enter");

		if (node == null) {
			logger.debug("The node is null.");
			return false;
		}
		
		if (idToNodes.get(node.getID()) != null) {
			logger.debug("The node with id=" + node.getID() + " already exists in the graph.");
			return false;
		}
		
		this.graph.addVertex(node);
		
		this.idToNodes.put(node.getID(), node);
		
		List<Node> nodesWithSameUri = uriToNodes.get(node.getUriString());
		if (nodesWithSameUri == null) {
			nodesWithSameUri = new ArrayList<Node>();
			uriToNodes.put(node.getUriString(), nodesWithSameUri);
		}
		nodesWithSameUri.add(node);
				
		logger.debug("exit>");		
		return true;
	}
	
	private List<Node> addNodeClosure(Node Node) {

		logger.debug("<enter");
		
		List<Node> nodeClosureList = new ArrayList<Node>();

		String uriString;
		List<Node> recentlyAddedNodes = new ArrayList<Node>();
		List<Node> newNodes;

		List<String> dpDomainClasses = new ArrayList<String>();
		List<String> opDomainClasses = new ArrayList<String>();
		List<String> superClasses = new ArrayList<String>();
		List<String> newAddedClasses = new ArrayList<String>();

		recentlyAddedNodes.add(Node);

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
					uriString = newAddedClasses.get(j);
					Node n = new InternalNode(nodeIdFactory.getNodeId(uriString), 
							ontologyManager.getLabelFromUriString(uriString));
					if (addSingleNode(n))	
						newNodes.add(n);
				}
			}

			recentlyAddedNodes = newNodes;
			newAddedClasses.clear();
		}

		logger.debug("exit>");
		return nodeClosureList;
	}

	private void updateLinks(List<Node> newNodes) {

		logger.debug("<enter");

		Node[] allNodes = this.graph.vertexSet().toArray(new Node[0]);
		List<String> objectProperties = new ArrayList<String>();
		//List<String> dataProperties = new ArrayList<String>();

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String uriString = null;
		String id = null;
		Label label = null;

		logger.debug("number of set1 vertices (first loop): " + allNodes.length);
		logger.debug("number of set2 vertices (second loop): " + newNodes.size());

		for (int i = 0; i < allNodes.length; i++) {
			for (int j = 0; j < newNodes.size(); j++) {

//				logger.debug("node1: " + vertices[i]);
//				logger.debug("node2: " + newNodes.get(u));

				if (allNodes[i].equals(newNodes.get(j)))
					continue;

				for (int k = 0; k < 2; k++) {	

					if (k == 0) {
						source = allNodes[i];
						target = newNodes.get(j);
					} else {
						source = newNodes.get(j);
						target = allNodes[i];
					}

//					logger.debug("examining the links between nodes " + i + "," + u);

					sourceUri = source.getUriString();
					targetUri = target.getUriString();

					// There is no outgoing link from column nodes
					if (source instanceof ColumnNode)
						break;

					// The alignment explicitly adds a link from the domain to the column node, 
					// so we don't need to worry about it.
					if (target instanceof ColumnNode) 
						continue;

					// Add object property links between internal nodes
					// The code for the case that both source and target are internal nodes
					
					boolean inherited = true;
					// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
					objectProperties = ontologyManager.getObjectProperties(sourceUri, targetUri, true);

					for (int u = 0; u < objectProperties.size(); u++) {
						uriString = objectProperties.get(u);

						List<String> dirDomains = ontologyManager.getPropertyDirectDomains().get(uriString);
						List<String> dirRanges = ontologyManager.getPropertyDirectRanges().get(uriString);

						if (dirDomains != null && dirDomains.indexOf(sourceUri) != -1 &&
								dirRanges != null && dirRanges.indexOf(targetUri) != -1)
							inherited = false;

						id = linkIdFactory.getLinkId(uriString);
						label = ontologyManager.getLabelFromUriString(uriString);
						Link link = new ObjectPropertyLink(id, label);
						// prefer the links that are actually defined between source and target in the ontology 
						// over inherited ones.
						if (inherited) 
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
						else
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT);
					}

					// Add subclass links between internal nodes
					// we have to check both sides.
					if (ontologyManager.isSubClass(targetUri, sourceUri, false) ||
							ontologyManager.isSuperClass(sourceUri, targetUri, false)) {
						id = linkIdFactory.getLinkId(SubClassOfLink.getLabel().getUriString());
						SubClassOfLink subClassOfLink = new SubClassOfLink(id);
						// target is subclass of source
						addWeightedLink(target, source, subClassOfLink, ModelingParams.MAX_WEIGHT);
					}

				}
			}
		}

		logger.debug("exit>");
	}

	private void updateLinksFromThing() {

		logger.debug("<enter");

		Node[] nodes = this.graph.vertexSet().toArray(new Node[0]);

		for (Node n : nodes) {

			if (n.getUriString().equalsIgnoreCase(FixedUris.THING_URI))
				continue;

			if (!(n instanceof InternalNode))
				continue;

			boolean parentExist = false;
			boolean parentThing = false;
			
			Link[] outgoingLinks = this.graph.outgoingEdgesOf(n).toArray(new Link[0]); 
			for (Link outLink: outgoingLinks) {
				if (outLink.getUriString().equalsIgnoreCase(FixedUris.RDFS_SUBCLASS_OF_URI)) {
					if (!outLink.getTarget().getUriString().equalsIgnoreCase(FixedUris.THING_URI))
						parentExist = true;
					else
						parentThing = true;
				}
			}
			
			// The node already has a parent other than Thing
			if (parentExist) {
				this.graph.removeAllEdges(n, thingNode);
				continue;
			}

			// The node does not have a parent other than Thing
			if (parentThing)
				continue;

			// Create a link from Thing node to nodes who don't have any superclasses
			String id = linkIdFactory.getLinkId(SubClassOfLink.getLabel().getUriString());
			SubClassOfLink subClassOfLink = new SubClassOfLink(id);
			addWeightedLink(n, thingNode, subClassOfLink, ModelingParams.MAX_WEIGHT);
		}

		logger.debug("exit>");

	}


}
