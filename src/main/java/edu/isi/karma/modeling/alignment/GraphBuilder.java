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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SubClassLink;

public class GraphBuilder {

	static Logger logger = Logger.getLogger(GraphBuilder.class);

	private DirectedWeightedMultigraph<Node, Link> graph;
	private OntologyManager ontologyManager;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	private List<String> sourceToTargetLinkUris; 
	private Node thingNode;
	
	// HashMaps
	
	private HashMap<String, Node> idToNodeMap;
	private HashMap<String, Link> idToLinkMap;

	private HashMap<String, List<Node>> uriToNodesMap;
	private HashMap<String, List<Link>> uriToLinksMap;
	
	private HashMap<NodeType, List<Node>> typeToNodesMap;
	private HashMap<LinkType, List<Link>> typeToLinksMap;

	private HashMap<LinkStatus, List<Link>> statusToLinksMap;

	// Constructor
	
	public GraphBuilder(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, LinkIdFactory linkIdFactory) {
		
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = nodeIdFactory;
		this.linkIdFactory = linkIdFactory;

		this.idToNodeMap = new HashMap<String, Node>();
		this.idToLinkMap = new HashMap<String, Link>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.uriToLinksMap = new HashMap<String, List<Link>>();
		this.statusToLinksMap = new HashMap<LinkStatus, List<Link>>();
		
		graph = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		sourceToTargetLinkUris = new ArrayList<String>();
			
		initialGraph();

		logger.info("graph has been created.");
	}
	
	// Public Methods
	
	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graph;
	}
	
	public HashMap<String, Node> getIdToNodeMap() {
		return idToNodeMap;
	}

	public HashMap<String, Link> getIdToLinkMap() {
		return idToLinkMap;
	}

	public HashMap<String, List<Node>> getUriToNodesMap() {
		return uriToNodesMap;
	}

	public HashMap<String, List<Link>> getUriToLinksMap() {
		return uriToLinksMap;
	}

	public HashMap<NodeType, List<Node>> getTypeToNodesMap() {
		return typeToNodesMap;
	}

	public HashMap<LinkType, List<Link>> getTypeToLinksMap() {
		return typeToLinksMap;
	}

	public HashMap<LinkStatus, List<Link>> getStatusToLinksMap() {
		return statusToLinksMap;
	}
	
	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (!addSingleNode(node))
			return false;
			
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
		
		if (this.idToLinkMap.containsKey(link.getId())) {
			logger.debug("The node with id=" + link.getId() + " already exists in the graph");
			return false;
		}
		
		String key = source.getId() + target.getId() + link.getUriString();
		// check to see if the link is duplicate or not
		if (sourceToTargetLinkUris.indexOf(key) != -1)
		{
			logger.debug("There is already a link with label " + link.getUriString() + 
					" from " + source.getId() + " to " + target.getId());
			return false;
		}

		this.graph.addEdge(source, target, link);
		double weight = ModelingParams.DEFAULT_WEIGHT;
		if (link instanceof SubClassLink)
			weight = ModelingParams.MAX_WEIGHT;
		this.graph.setEdgeWeight(link, weight);
		
		// update the corresponding lists and hashmaps
		
		this.idToLinkMap.put(link.getId(), link);
		
		List<Link> linksWithSameUri = uriToLinksMap.get(link.getUriString());
		if (linksWithSameUri == null) {
			linksWithSameUri = new ArrayList<Link>();
			uriToLinksMap.put(link.getUriString(), linksWithSameUri);
		}
		linksWithSameUri.add(link);
		
		List<Link> linksWithSameStatus = statusToLinksMap.get(link.getStatus());
		if (linksWithSameStatus == null) {
			linksWithSameStatus = new ArrayList<Link>();
			statusToLinksMap.put(link.getStatus(), linksWithSameUri);
		}
		linksWithSameStatus.add(link);
		
		
		List<Link> linksWithSameType = typeToLinksMap.get(link.getType());
		if (linksWithSameType == null) {
			linksWithSameType = new ArrayList<Link>();
			typeToLinksMap.put(link.getType(), linksWithSameType);
		}
		linksWithSameType.add(link);
		
		sourceToTargetLinkUris.add(key);

		logger.debug("exit>");		
		return true;
	}
	
	public void changeLinkStatus(Link link, LinkStatus newStatus) {

		LinkStatus oldStatus = link.getStatus();
		if (newStatus == oldStatus)
			return;
		
		List<Link> linksWithOldStatus = this.statusToLinksMap.get(oldStatus);
		if (linksWithOldStatus != null) linksWithOldStatus.remove(link);

		List<Link> linksWithNewStatus = this.statusToLinksMap.get(oldStatus);
		if (linksWithNewStatus == null) {
			linksWithNewStatus = new ArrayList<Link>();
			statusToLinksMap.put(newStatus, linksWithNewStatus);
		}
		linksWithNewStatus.add(link);
	}
	
	// Private Methods
	
	private void initialGraph() {
		
		logger.debug("<enter");
		
		// Add Thing to the Graph 
		String id = nodeIdFactory.getNodeId(Uris.THING_URI);
		Label label = new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL);
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
		
		if (idToNodeMap.get(node.getId()) != null) {
			logger.debug("The node with id=" + node.getId() + " already exists in the graph.");
			return false;
		}
		
		this.graph.addVertex(node);
		
		this.idToNodeMap.put(node.getId(), node);
		
		List<Node> nodesWithSameUri = uriToNodesMap.get(node.getUriString());
		if (nodesWithSameUri == null) {
			nodesWithSameUri = new ArrayList<Node>();
			uriToNodesMap.put(node.getUriString(), nodesWithSameUri);
		}
		nodesWithSameUri.add(node);
		
		List<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType == null) {
			nodesWithSameType = new ArrayList<Node>();
			typeToNodesMap.put(node.getType(), nodesWithSameType);
		}
		nodesWithSameType.add(node);
				
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

	private void updateLinksBetweenTwoSets(List<Node> set1, List<Node> set2) {
		
		logger.debug("<enter");

		List<String> objectProperties = new ArrayList<String>();
		//List<String> dataProperties = new ArrayList<String>();

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String uriString = null;
		String id = null;
		Label label = null;

		logger.debug("number of set1 vertices (first loop): " + set1.size());
		logger.debug("number of set2 vertices (second loop): " + set2.size());


		for (Node n1 : set1) {
			for (Node n2 : set2) {

//				logger.debug("node1: " + n1.getID());
//				logger.debug("node2: " + n2.getID());

				if (n1.equals(n2))
					continue;

				for (int k = 0; k < 2; k++) {	

					if (k == 0) {
						source = n1;
						target = n2;
					} else {
						source = n2;
						target = n1;
					}

//					logger.debug("examining the links between nodes " + i + "," + u);

					sourceUri = source.getUriString();
					targetUri = target.getUriString();

					// There is no outgoing link from column nodes and literal nodes
					if (!(source instanceof InternalNode))
						break;

					// The alignment explicitly adds a link from the domain to the column node and literal node, 
					// so we don't need to worry about it.
					if (!(target instanceof InternalNode))
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
					if (ontologyManager.isSubClass(targetUri, sourceUri, false)) {
						id = linkIdFactory.getLinkId(SubClassLink.getLabel().getUriString());
						SubClassLink subClassOfLink = new SubClassLink(id);
						// target is subclass of source
						addWeightedLink(target, source, subClassOfLink, ModelingParams.MAX_WEIGHT);
					}

				}
			}
		}		

		logger.debug("exit>");

	}
	
	private void updateLinks(List<Node> newNodes) {

		logger.debug("<enter");

		Node[] nodes = this.graph.vertexSet().toArray(new Node[0]);
		updateLinksBetweenTwoSets(Arrays.asList(nodes), newNodes);

		logger.debug("exit>");
	}

	private void updateLinksFromThing() {

		logger.debug("<enter");

		Node[] nodes = this.graph.vertexSet().toArray(new Node[0]);

		for (Node n : nodes) {

			if (n.getUriString().equalsIgnoreCase(Uris.THING_URI))
				continue;

			if (!(n instanceof InternalNode))
				continue;

			boolean parentExist = false;
			boolean parentThing = false;
			
			Link[] outgoingLinks = this.graph.outgoingEdgesOf(n).toArray(new Link[0]); 
			for (Link outLink: outgoingLinks) {
				if (outLink.getUriString().equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI)) {
					if (!outLink.getTarget().getUriString().equalsIgnoreCase(Uris.THING_URI))
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
			String id = linkIdFactory.getLinkId(SubClassLink.getLabel().getUriString());
			SubClassLink subClassOfLink = new SubClassLink(id);
			addWeightedLink(n, thingNode, subClassOfLink, ModelingParams.MAX_WEIGHT);
		}

		logger.debug("exit>");

	}


}
