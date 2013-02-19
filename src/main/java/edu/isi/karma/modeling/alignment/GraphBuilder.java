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
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
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
	
	// used for deleting a node
	private HashMap<Node, Integer> nodeReferences;
	private HashMap<String, List<String>> uriClosure;

	// Constructor
	
	public GraphBuilder(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, LinkIdFactory linkIdFactory) {
		
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = nodeIdFactory;
		this.linkIdFactory = linkIdFactory;

		this.idToNodeMap = new HashMap<String, Node>();
		this.idToLinkMap = new HashMap<String, Link>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.uriToLinksMap = new HashMap<String, List<Link>>();
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.typeToLinksMap = new HashMap<LinkType, List<Link>>();
		this.statusToLinksMap = new HashMap<LinkStatus, List<Link>>();
		
		this.nodeReferences = new HashMap<Node, Integer>();
		this.uriClosure = new HashMap<String, List<String>>();
		
		graph = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		sourceToTargetLinkUris = new ArrayList<String>();
			
		initialGraph();

		logger.info("initial graph has been created.");
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
	
	public Node getThingNode() {
		return thingNode;
	}

	public void resetOntologyMaps() {
		String[] currentUris = this.uriClosure.keySet().toArray(new String[0]);
		this.uriClosure.clear();
		for (String uri : currentUris)
			computeUriClosure(uri, new ArrayList<String>());
	}
	
	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (!addSingleNode(node))
			return false;
			
		if (node instanceof InternalNode) {

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			List<Node> newNodes = new ArrayList<Node>();
			addNodeClosure(node, newNodes);
			newNodes.add(node);
			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start)/1000F;
			logger.info("time to add nodes closure: " + elapsedTimeSec);

			updateLinks(newNodes);
			// if we consider the set of current nodes as S1 and the set of new added nodes as S2:
			// (*) the direction of all the subclass links between S1 and S2 is from S2 to S1
			//		This is because superclasses of all the nodes in S1 are already added to the graph. 
			// (*) the direction of all the object property links between S1 and S2 is from S1 to S2
			//		This is because all the nodes that are reachable from S1 are added to the graph before adding new nodes in S2.
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
		
		String key = source.getId() + target.getId() + link.getLabel().getUri();
		// check to see if the link is duplicate or not
		if (sourceToTargetLinkUris.indexOf(key) != -1)
		{
			logger.debug("There is already a link with label " + link.getLabel().getUri() + 
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
		
		List<Link> linksWithSameUri = uriToLinksMap.get(link.getLabel().getUri());
		if (linksWithSameUri == null) {
			linksWithSameUri = new ArrayList<Link>();
			uriToLinksMap.put(link.getLabel().getUri(), linksWithSameUri);
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
		
		if (source instanceof InternalNode && target instanceof ColumnNode) {
			List<Node> closure = this.getNodeClosure(source);
			List<Node> closureIncludingSelf = new ArrayList<Node>();
			closureIncludingSelf.add(source);
			if (closure != null) closureIncludingSelf.addAll(closure);
			
			for (Node n : closureIncludingSelf) {
				Integer refCount = this.nodeReferences.get(n);
				this.nodeReferences.put(source, ++refCount);
			}
		}
			
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
	
	public boolean removeLink(Link link) {
		
		if (link == null) {
			logger.debug("The link is null.");
			return false;
		}
		
		if (idToLinkMap.get(link.getId()) == null) {
			logger.debug("The link with id=" + link.getId() + " does not exists in the graph.");
			return false;
		}
		
		Node source = link.getSource();
		Node target = link.getTarget();
		
		if (source instanceof InternalNode && target instanceof ColumnNode) {
			List<Node> closure = this.getNodeClosure(source);
			List<Node> closureIncludingSelf = new ArrayList<Node>();
			closureIncludingSelf.add(source);
			if (closure != null) closureIncludingSelf.addAll(closure);
			
			for (Node n : closureIncludingSelf) {
				Integer refCount = this.nodeReferences.get(n);
				this.nodeReferences.put(source, --refCount);
			}
		}
		
		return this.graph.removeEdge(link);
	}
	
	public boolean removeNode(Node node) {
		
		if (node == null) {
			logger.debug("The node is null");
			return false;
		}
		
		if (idToNodeMap.get(node.getId()) == null) {
			logger.debug("The node with id=" + node.getId() + " does not exists in the graph.");
			return false;
		}
		
		List<Node> closure = this.getNodeClosure(node);
		List<Node> closureIncludingSelf = new ArrayList<Node>();
		closureIncludingSelf.add(node);
		if (closure != null) closureIncludingSelf.addAll(closure);
		
		for (Node n : closureIncludingSelf) {
			Integer refCount = this.nodeReferences.get(n);
			if (refCount.intValue() == 0) 
				removeSingleNode(n);
			else
				this.nodeReferences.put(n, --refCount);
		}

		return true;
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
		
		List<Node> nodesWithSameUri = uriToNodesMap.get(node.getLabel().getUri());
		if (nodesWithSameUri == null) {
			nodesWithSameUri = new ArrayList<Node>();
			uriToNodesMap.put(node.getLabel().getUri(), nodesWithSameUri);
		}
		nodesWithSameUri.add(node);
		
		List<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType == null) {
			nodesWithSameType = new ArrayList<Node>();
			typeToNodesMap.put(node.getType(), nodesWithSameType);
		}
		nodesWithSameType.add(node);
					
		this.nodeReferences.put(node, 0);
				
		logger.debug("exit>");		
		return true;
	}
	
	private boolean removeSingleNode(Node node) {
		
		logger.debug("<enter");

		if (node == null) {
			logger.debug("The node is null.");
			return false;
		}
		
		if (idToNodeMap.get(node.getId()) == null) {
			logger.debug("The node with id=" + node.getId() + " does not exists in the graph.");
			return false;
		}
		
		if (!this.graph.removeVertex(node))
			return false;
		
		// updating hashmaps
		
		this.idToNodeMap.remove(node.getId());
		
		List<Node> nodesWithSameUri = uriToNodesMap.get(node.getLabel().getUri());
		if (nodesWithSameUri != null) 
			nodesWithSameUri.remove(node);
		
		List<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType != null) 
			nodesWithSameType.remove(node);
		
		this.nodeReferences.remove(node);
				
		logger.debug("exit>");		
		return true;
	}

	private List<String> getUriDirectConnections(String uri) {
		
		List<String> uriDirectConnections = new ArrayList<String>();
		
		List<String> opDomainClasses = null;
		Set<String> superClasses = null;

		// We don't need to add subclasses of each class separately.
		// The only place in which we add children is where we are looking for domain class of a property.
		// In this case, in addition to the domain class, we add all of its children too.
		
		opDomainClasses = ontologyManager.getDomainsGivenRange(uri, true);
		superClasses = ontologyManager.getSuperClasses(uri, false).keySet();

		if (opDomainClasses != null)
			uriDirectConnections.addAll(opDomainClasses);
		if (superClasses != null)
			uriDirectConnections.addAll(superClasses);

		return uriDirectConnections;
	}

	private void computeUriClosure(String uri, List<String> closure) {
		
		logger.debug("<enter");
		
		List<String> currentClosure = this.uriClosure.get(uri);
		if (currentClosure != null) {
			closure.addAll(currentClosure);
			return;
		}

		List<String> uriDirectConnections = getUriDirectConnections(uri);
		if (uriDirectConnections.size() == 0) {
			this.uriClosure.put(uri, new ArrayList<String>());
		} else {
			for (String c : uriDirectConnections) {
				if (!closure.contains(c)) closure.add(c);
				List<String> localClosure = new ArrayList<String>();
				computeUriClosure(c, localClosure);
				for (String s : localClosure)
					if (!closure.contains(s)) closure.add(s);
			}
			this.uriClosure.put(uri, closure);
		}
		
		logger.debug("exit>");
	}
	
	private List<Node> getNodeClosure(Node node) {
		
		List<Node> nodeClosure = new ArrayList<Node>();
		if (node instanceof ColumnNode) return nodeClosure;
		
		String uri = node.getLabel().getUri();
		List<String> closure = this.uriClosure.get(uri); 
		if (closure == null) {  // the closure has already been computed.
			closure = new ArrayList<String>();
			computeUriClosure(uri, closure);
		} 
		for (String s : closure) {
			List<Node> nodes = uriToNodesMap.get(s);
			nodeClosure.addAll(nodes);
		}
		return nodeClosure;
	}
	
	/**
	 * returns all the new nodes that should be added to the graph due to adding the input node
	 * @param node
	 * @param closure: contains all the nodes that are connected to the input node 
	 * by ObjectProperty or SubClass links
	 * @return
	 */
	private void addNodeClosure(Node node, List<Node> newAddedNodes) {

		logger.debug("<enter");
		
		if (newAddedNodes == null) newAddedNodes = new ArrayList<Node>();
		
		String uri = node.getLabel().getUri();

		List<String> uriClosure = new ArrayList<String>();
		computeUriClosure(uri, uriClosure);

		for (String c : uriClosure) {
			List<Node> nodesOfSameUri = this.uriToNodesMap.get(c);
			if (nodesOfSameUri == null || nodesOfSameUri.size() == 0) { // the internal node is not added to the graph before
				Node nn = new InternalNode(nodeIdFactory.getNodeId(c), 
						ontologyManager.getUriLabel(c));
				if (addSingleNode(nn)) newAddedNodes.add(nn);
			} 
		}

		logger.debug("exit>");
	}

	private void updateLinksBetweenTwoSets(List<Node> set1, List<Node> set2) {
		
		logger.debug("<enter");

		List<String> objectProperties = new ArrayList<String>();
		//List<String> dataProperties = new ArrayList<String>();

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String uri = null;
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

					sourceUri = source.getLabel().getUri();
					targetUri = target.getLabel().getUri();

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
						uri = objectProperties.get(u);

						List<String> dirDomains = ontologyManager.getPropertyDirectDomains().get(uri);
						List<String> dirRanges = ontologyManager.getPropertyDirectRanges().get(uri);

						if (dirDomains != null && dirDomains.indexOf(sourceUri) != -1 &&
								dirRanges != null && dirRanges.indexOf(targetUri) != -1)
							inherited = false;

						id = linkIdFactory.getLinkId(uri);
						label = ontologyManager.getUriLabel(uri);
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
						id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
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

			String uri = n.getLabel().getUri();
			if (uri == null)
				continue;
			
			if (uri.equalsIgnoreCase(Uris.THING_URI))
				continue;

			if (!(n instanceof InternalNode))
				continue;

			boolean parentExist = false;
			boolean parentThing = false;
			
			Link[] outgoingLinks = this.graph.outgoingEdgesOf(n).toArray(new Link[0]); 
			for (Link outLink: outgoingLinks) {
				if (outLink.getLabel().getUri() == null)
					continue;
				if (outLink.getLabel().getUri().equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI)) {
					if (!outLink.getTarget().getLabel().getUri().equalsIgnoreCase(Uris.THING_URI))
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
			String id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
			SubClassLink subClassOfLink = new SubClassLink(id);
			addWeightedLink(n, thingNode, subClassOfLink, ModelingParams.MAX_WEIGHT);
		}

		logger.debug("exit>");

	}

	public static void main(String[] args) {
		DirectedWeightedMultigraph<Node, Link> g = new 
				DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		Node n1 = new ColumnNode("n1", "h1", "B");
		Node n2 = new InternalNode("n2", null);
		Link l1 = new DataPropertyLink("e1", null);
		
		g.addVertex(n1);
		g.addVertex(n2);
		g.addEdge(n1, n2, l1);
		GraphUtil.printGraph(g);

//		g.removeEdge(l1);
//		GraphUtil.printGraph(g);
		
		g.removeVertex(n2);
		GraphUtil.printGraph(g);
	}
}
