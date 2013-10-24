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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.LinkPriorityType;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SimpleLink;
import edu.isi.karma.rep.alignment.SubClassLink;

public class GraphBuilder {

	static Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

	private DirectedWeightedMultigraph<Node, Link> graph;
	private OntologyManager ontologyManager;
	
	private NodeIdFactory nodeIdFactory;
//	private LinkIdFactory linkIdFactory;
	
	private HashSet<String> visitedSourceTargetPairs; 
	private HashSet<String> sourceToTargetLinkUris;
	private HashSet<String> sourceToTargetConnectivity; 

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
	
	public GraphBuilder(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory) { //, LinkIdFactory linkIdFactory) {
		
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = nodeIdFactory;
//		this.linkIdFactory = linkIdFactory;

		this.idToNodeMap = new HashMap<String, Node>();
		this.idToLinkMap = new HashMap<String, Link>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.uriToLinksMap = new HashMap<String, List<Link>>();
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.typeToLinksMap = new HashMap<LinkType, List<Link>>();
		this.statusToLinksMap = new HashMap<LinkStatus, List<Link>>();
		
		this.nodeReferences = new HashMap<Node, Integer>();
		this.uriClosure = new HashMap<String, List<String>>();
		
		this.graph = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		this.visitedSourceTargetPairs = new HashSet<String>();
		this.sourceToTargetLinkUris = new HashSet<String>();
		this.sourceToTargetConnectivity = new HashSet<String>();
			
		this.initialGraph();
		
		logger.debug("initial graph has been created.");
	}
	
	public GraphBuilder(OntologyManager ontologyManager, DirectedWeightedMultigraph<Node, Link> graph) {
		
		this.graph = graph;
		this.ontologyManager = ontologyManager;
		
		this.idToNodeMap = new HashMap<String, Node>();
		this.idToLinkMap = new HashMap<String, Link>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.uriToLinksMap = new HashMap<String, List<Link>>();
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.typeToLinksMap = new HashMap<LinkType, List<Link>>();
		this.statusToLinksMap = new HashMap<LinkStatus, List<Link>>();
		
		this.visitedSourceTargetPairs = new HashSet<String>();
		this.sourceToTargetLinkUris = new HashSet<String>();
		this.sourceToTargetConnectivity = new HashSet<String>();

		this.nodeIdFactory = new NodeIdFactory();
//		this.linkIdFactory = new LinkIdFactory();
		
		for (Node node : this.graph.vertexSet()) {
			
			if (node instanceof InternalNode)
				nodeIdFactory.getNodeId(node.getLabel().getUri());

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
						
		}
		
		Node source;
		Node target;
		
		for (Link link : this.graph.edgeSet()) {
			
			source = link.getSource();
			target = link.getTarget();
			
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
			
			String key = source.getId() + target.getId() + link.getLabel().getUri();
			sourceToTargetLinkUris.add(key);
			
			this.visitedSourceTargetPairs.add(source.getId() + target.getId());
		}

		this.nodeReferences = new HashMap<Node, Integer>();
		this.uriClosure = new HashMap<String, List<String>>();
			
		logger.debug("graph has been loaded.");
	}
	
	
	
	public NodeIdFactory getNodeIdFactory() {
		return nodeIdFactory;
	}

	public boolean isConnected(String nodeId1, String nodeId2) {
		return this.sourceToTargetConnectivity.contains(nodeId1 + nodeId2);
	}
	
//	public LinkIdFactory getLinkIdFactory() {
//		return linkIdFactory;
//	}

	public OntologyManager getOntologyManager() {
		return this.ontologyManager;
	}
	
	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graph;
	}
	
	public void setGraph(DirectedWeightedMultigraph<Node, Link> graph) {
		this.graph = graph;
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
			computeUriClosure(uri);
	}
	
	public void addNodeList(List<Node> nodes) {
		addNodeList(nodes, null);
	}
	
	public void addNodeList(List<Node> nodes, Set<Node> addedNodes) {
		
		logger.debug("<enter");
		if (addedNodes == null) addedNodes = new HashSet<Node>();

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		for (Node node : nodes) {
			
			if (!addSingleNode(node))
				continue;
				
			addedNodes.add(node);
			if (node instanceof InternalNode) {
				addNodeClosure(node, addedNodes);
			}
		}

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start)/1000F;
		logger.debug("time to add nodes closure: " + elapsedTimeSec);

		updateLinks2();
		
		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
		logger.debug("time to update links of the graph: " + elapsedTimeSec);
		
		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");		
	}

	public boolean addNode(Node node) {
		return addNode(node, null);
	}

	public boolean addNode(Node node, Set<Node> addedNodes) {
		
		logger.debug("<enter");
		if (addedNodes == null) addedNodes = new HashSet<Node>();

		if (!addSingleNode(node))
			return false;
			
		if (node instanceof InternalNode) {

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			
//			List<Node> newNodes = new ArrayList<Node>();
//			addNodeClosure(node, newNodes);
//			newNodes.add(node);
//			addNodeClosure(node, newNodes);
			
			addedNodes.add(node);
			addNodeClosure(node, addedNodes);
			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start)/1000F;
			logger.debug("time to add nodes closure: " + elapsedTimeSec);

			updateLinks2();
//			updateLinks();
			
			// if we consider the set of current nodes as S1 and the set of new added nodes as S2:
			// (*) the direction of all the subclass links between S1 and S2 is from S2 to S1
			//		This is because superclasses of all the nodes in S1 are already added to the graph. 
			// (*) the direction of all the object property links between S1 and S2 is from S1 to S2
			//		This is because all the nodes that are reachable from S1 are added to the graph before adding new nodes in S2.
			long updateLinks = System.currentTimeMillis();
			elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
			logger.debug("time to update links of the graph: " + elapsedTimeSec);
			
//			updateLinksFromThing();
			
			long updateLinksFromThing = System.currentTimeMillis();
			elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
			logger.debug("time to update links to Thing (root): " + elapsedTimeSec);

			logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
			logger.debug("total number of links in graph: " + this.graph.edgeSet().size());
		}

		logger.debug("exit>");		
		return true;
	}
	
	public boolean addNodeWithoutUpdatingGraph(Node node) {
		
		logger.debug("<enter");

		if (!addSingleNode(node))
			return false;

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
			logger.debug("The link with id=" + link.getId() + " already exists in the graph");
			return false;
		}
		
		String key = source.getId() + target.getId() + link.getLabel().getUri();
		// check to see if the link is duplicate or not
		if (sourceToTargetLinkUris.contains(key))
		{
			logger.debug("There is already a link with label " + link.getLabel().getUri() + 
					" from " + source.getId() + " to " + target.getId());
			return false;
		}

		this.graph.addEdge(source, target, link);
		
		this.sourceToTargetConnectivity.add(source.getId() + target.getId());
		this.sourceToTargetConnectivity.add(target.getId() + source.getId());
		
		double w = 0.0;
		if (link.getPriorityType() == LinkPriorityType.DirectObjectProperty)
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		else if (link.getPriorityType() == LinkPriorityType.IndirectObjectProperty)
			w = ModelingParams.PROPERTY_INDIRECT_WEIGHT;
		else if (link.getPriorityType() == LinkPriorityType.ObjectPropertyWithOnlyDomain)
			w = ModelingParams.PROPERTY_WITH_ONLY_DOMAIN_WEIGHT;
		else if (link.getPriorityType() == LinkPriorityType.ObjectPropertyWithOnlyRange)
			w = ModelingParams.PROPERTY_WITH_ONLY_RANGE_WEIGHT;
		else if (link.getPriorityType() == LinkPriorityType.ObjectPropertyWithoutDomainAndRange)
			w = ModelingParams.PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT;
		else if (link.getPriorityType() == LinkPriorityType.SubClassOf)
			w = ModelingParams.SUBCLASS_WEIGHT;
		else
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		
		this.graph.setEdgeWeight(link, w);
		
		// update the corresponding lists and hashmaps
		
		this.idToLinkMap.put(link.getId(), link);
		
		List<Link> linksWithSameUri = uriToLinksMap.get(link.getLabel().getUri());
		if (linksWithSameUri == null) {
			linksWithSameUri = new ArrayList<Link>();
			uriToLinksMap.put(link.getLabel().getUri(), linksWithSameUri);
		}
		linksWithSameUri.add(link);
		
//		if (link.getId().equals("http://km.aifb.kit.edu/projects/d3/cruiser#Vehicle1---http://km.aifb.kit.edu/projects/d3/cruiser#at---http://www.w3.org/2003/01/geo/wgs84_pos#Point1"))
//			logger.debug("debug1");

		changeLinkStatus(link, link.getStatus());
		
		List<Link> linksWithSameType = typeToLinksMap.get(link.getType());
		if (linksWithSameType == null) {
			linksWithSameType = new ArrayList<Link>();
			typeToLinksMap.put(link.getType(), linksWithSameType);
		}
		linksWithSameType.add(link);
		
		sourceToTargetLinkUris.add(key);
		
		logger.debug("adding the link " + link.getId());
		logger.debug("<<< ref count of " + source.getId() + " : " + this.nodeReferences.get(source));

		if (source instanceof InternalNode && target instanceof ColumnNode) {
			List<Node> closure = this.getNodeClosure(source);
			List<Node> closureIncludingSelf = new ArrayList<Node>();
			if (closure != null) closureIncludingSelf.addAll(closure);
			if (!closureIncludingSelf.contains(source)) closureIncludingSelf.add(source);
			
			for (Node n : closureIncludingSelf) {
				Integer refCount = this.nodeReferences.get(n);
				if (refCount != null) this.nodeReferences.put(n, ++refCount);
			}
			
			// Example: if A, B are added before and C is a new node which is in the closure of both A and B,
			// in this case, deleting the links from A and B to column nodes should not cause to delete C
//			List<Node> columnNodes = this.typeToNodesMap.get(NodeType.ColumnNode);
//			for (Node node : columnNodes) {
//				if (node == target) continue;
//				Node domain;
//				Set<Link> incomingLinks = this.getGraph().incomingEdgesOf(node);
//				if (incomingLinks != null && !incomingLinks.isEmpty()) {
//					domain = incomingLinks.toArray(new Link[0])[0].getSource();
//					if (domain == source) continue;
//					closure = this.getNodeClosure(domain);
//					if (closure.contains(source)) {
//						Integer refCount = this.nodeReferences.get(source);
//						if (refCount != null) this.nodeReferences.put(source, ++refCount);
//					}					
//				}
//			}
			
		}
		
		logger.debug(">>> ref count of " + source.getId() + " : " + this.nodeReferences.get(source));
			
		logger.debug("exit>");		
		return true;
	}
	
	public void changeLinkStatus(Link link, LinkStatus newStatus) {

//		if (link.getId().equals("http://km.aifb.kit.edu/projects/d3/cruiser#Vehicle1---http://km.aifb.kit.edu/projects/d3/cruiser#at---http://www.w3.org/2003/01/geo/wgs84_pos#Point1"))
//			logger.debug("debug3");
		
		LinkStatus oldStatus = link.getStatus();
//		if (newStatus == oldStatus)
//			return;
		
		link.setStatus(newStatus);
		
		List<Link> linksWithOldStatus = this.statusToLinksMap.get(oldStatus);
		if (linksWithOldStatus != null) linksWithOldStatus.remove(link);

		List<Link> linksWithNewStatus = this.statusToLinksMap.get(newStatus);
		if (linksWithNewStatus == null) {
			linksWithNewStatus = new ArrayList<Link>();
			statusToLinksMap.put(newStatus, linksWithNewStatus);
		}
		linksWithNewStatus.add(link);
	}
	
	public void changeLinkWeight(Link link, double weight) {
		this.graph.setEdgeWeight(link, weight);
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
		
		logger.debug("removing the link " + link.getId());
		logger.debug("<<< ref count of " + source.getId() + " : " + this.nodeReferences.get(source));

		if (source instanceof InternalNode && target instanceof ColumnNode) {
			
//			Integer refCount = this.nodeReferences.get(source);
//			if (refCount != null && refCount != 0) this.nodeReferences.put(source, --refCount);
			
			List<Node> closure = this.getNodeClosure(source);
			List<Node> closureIncludingSelf = new ArrayList<Node>();
			if (closure != null) closureIncludingSelf.addAll(closure);
			if (!closureIncludingSelf.contains(source)) closureIncludingSelf.add(source);
			
			for (Node n : closureIncludingSelf) {
				Integer refCount = this.nodeReferences.get(n);
				if (refCount != null && refCount != 0) this.nodeReferences.put(n, --refCount);
			}
		}
		
		logger.debug(">>> ref count of " + source.getId() + " : " + this.nodeReferences.get(source));

		if (!removeSingleLink(link))
			return false;
		
		return true;
	}
	
	public boolean removeNode(Node node) {
		
		if (node == null) {
			logger.error("The node is null");
			return false;
		}
		
		if (idToNodeMap.get(node.getId()) == null) {
			logger.error("The node with id=" + node.getId() + " does not exists in the graph.");
			return false;
		}

		Integer refCount = this.nodeReferences.get(node);
		if (refCount != null && refCount.intValue() != 0) { 
				logger.error("The node with id=" + node.getId() + " cannot be deleted because it has at least one reference.");
				return false;
		}

		logger.debug("removing the node " + node.getId() + "...");
		logger.debug("<<< ref count of " + node.getId() + " : " + this.nodeReferences.get(node));
		
		List<Node> closure = this.getNodeClosure(node);
		List<Node> closureIncludingSelf = new ArrayList<Node>();
		if (closure != null) closureIncludingSelf.addAll(closure);
		if (!closureIncludingSelf.contains(node)) closureIncludingSelf.add(node);

		for (Node n : closureIncludingSelf) {
			refCount = this.nodeReferences.get(n);
			if (refCount != null) {
				if (refCount.intValue() == 0) 
					removeSingleNode(n);
//				else
//					this.nodeReferences.put(n, --refCount);
			}
		}

		logger.debug(">>> ref count of " + node.getId() + " : " + this.nodeReferences.get(node));

		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());
		
		return true;
	}

	public void updateGraph() {
		updateGraph(null);
	}
	
	public void updateGraph(Set<Node> addedNodes) {
		
		logger.debug("<enter");
		if (addedNodes == null) addedNodes = new HashSet<Node>();

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		List<Node> internalNodes = this.typeToNodesMap.get(NodeType.InternalNode);
		if (internalNodes != null) {
			Node[] nodes = internalNodes.toArray(new Node[0]);
			for (Node node : nodes) 
				addNodeClosure(node, addedNodes);
		}

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start)/1000F;
		logger.debug("time to add nodes closure: " + elapsedTimeSec);

		updateLinks2();
		
		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
		logger.debug("time to update links of the graph: " + elapsedTimeSec);
		
//		updateLinksFromThing();
		
//		long updateLinksFromThing = System.currentTimeMillis();
//		elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
//		logger.info("time to update links to Thing (root): " + elapsedTimeSec);

		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");		
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
		logger.debug("removing the node " + node.getId() + "...");

		Set<Link> incomingLinks = this.graph.incomingEdgesOf(node);
		if (incomingLinks != null) {
			Link[] incomingLinksArray = incomingLinks.toArray(new Link[0]);
			for (Link inLink: incomingLinksArray) {
				this.removeSingleLink(inLink);
			}
		}

		Set<Link> outgoingLinks = this.graph.outgoingEdgesOf(node);
		if (outgoingLinks != null) {
			Link[] outgoingLinksArray = outgoingLinks.toArray(new Link[0]);
			for (Link outLink: outgoingLinksArray) {
				this.removeSingleLink(outLink);
			}
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
	
	private boolean removeSingleLink(Link link) {
		
		logger.debug("<enter");
		logger.debug("removing the node " + link.getId() + "...");

		if (!this.graph.removeEdge(link))
			return false;

		// update hashmaps

		this.idToLinkMap.remove(link.getId());

		List<Link> linksWithSameUri = uriToLinksMap.get(link.getLabel().getUri());
		if (linksWithSameUri != null) 
			linksWithSameUri.remove(link);
		
		List<Link> linksWithSameType = typeToLinksMap.get(link.getType());
		if (linksWithSameType != null) 
			linksWithSameType.remove(link);
		
		List<Link> linksWithSameStatus = statusToLinksMap.get(link.getStatus());
		if (linksWithSameStatus != null) 
			linksWithSameStatus.remove(link);
		
		this.sourceToTargetLinkUris.remove(link.getSource().getId() + 
				link.getTarget().getId() + 
				link.getLabel().getUri());
		
		logger.debug("exit>");
		return true;
	}

	private HashSet<String> getUriDirectConnections(String uri) {
		
		HashSet<String> uriDirectConnections = new HashSet<String>();
		
		HashSet<String> opDomainClasses = null;
		HashMap<String, Label> superClasses = null;

		// We don't need to add subclasses of each class separately.
		// The only place in which we add children is where we are looking for domain class of a property.
		// In this case, in addition to the domain class, we add all of its children too.
		
		opDomainClasses = ontologyManager.getDomainsGivenRange(uri, true);
		superClasses = ontologyManager.getSuperClasses(uri, false);

		if (opDomainClasses != null)
			uriDirectConnections.addAll(opDomainClasses);
		if (superClasses != null)
			uriDirectConnections.addAll(superClasses.keySet());

		return uriDirectConnections;
	}

	private List<String> computeUriClosure(String uri) {
		
		List<String> closure = this.uriClosure.get(uri);
		if (closure != null) 
			return closure;
	
		closure = new ArrayList<String>();
		List<String> closedList = new ArrayList<String>();
		HashMap<String, List<String>> dependentUrisMap = new HashMap<String, List<String>>();
		computeUriClosureRecursive(uri, closure, closedList, dependentUrisMap);
		if (closedList.contains(uri) && !closure.contains(uri))
			closure.add(uri);
		
		int count = 1;
		while (count != 0) {
			count = 0;
			for (String s : dependentUrisMap.keySet()) {
				List<String> temp = this.uriClosure.get(s);
				List<String> dependentUris = dependentUrisMap.get(s);
				for (String ss : dependentUris) {
					if (!temp.contains(ss)) { temp.add(ss); count++;}
					if (this.uriClosure.get(ss) != null) {
						String[] cc = this.uriClosure.get(ss).toArray(new String[0]);
						for (String c : cc) {
							if (!temp.contains(c)) {temp.add(c); count++;}
						}
					}
						
				}
			}
		}

		return closure;
	}

	private void computeUriClosureRecursive(String uri, List<String> closure, List<String> closedList, HashMap<String, List<String>> dependentUrisMap) {
		
		logger.debug("<enter");
		
		closedList.add(uri);
		List<String> currentClosure = this.uriClosure.get(uri);
		if (currentClosure != null) {
			closure.addAll(currentClosure);
			return;
		}

		HashSet<String> uriDirectConnections = getUriDirectConnections(uri);
		if (uriDirectConnections.size() == 0) {
			this.uriClosure.put(uri, new ArrayList<String>());
		} else {
			for (String c : uriDirectConnections) {
				if (closedList.contains(c)) {
					List<String> dependentUris = dependentUrisMap.get(uri);
					if (dependentUris == null) {
						dependentUris = new ArrayList<String>();
						dependentUrisMap.put(uri, dependentUris);
					}
					if (!dependentUris.contains(c)) dependentUris.add(c);
					continue;
				}
				if (!closure.contains(c)) closure.add(c);
				if (!closedList.contains(c)) closedList.add(c);
				List<String> localClosure = new ArrayList<String>();
				computeUriClosureRecursive(c, localClosure, closedList, dependentUrisMap);
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
			closure = computeUriClosure(uri);
		} 
		for (String s : closure) {
			List<Node> nodes = uriToNodesMap.get(s);
			if (nodes != null) nodeClosure.addAll(nodes);
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
	private void addNodeClosure(Node node, Set<Node> newAddedNodes) {

		logger.debug("<enter");
		
		if (newAddedNodes == null) newAddedNodes = new HashSet<Node>();
		
		String uri = node.getLabel().getUri();

		List<String> uriClosure = computeUriClosure(uri);

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
	
	@SuppressWarnings("unused")
	private void updateLinks() {
		
		logger.debug("<enter");
		
		List<Node> nodes = this.typeToNodesMap.get(NodeType.InternalNode);
		logger.debug("number of vertices: " + nodes.size());

		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String id = null;
		Label label = null;
		String key;

//		logger.debug("size:" + nodes.size() * nodes.size());
//		int count = 0;
		
		logger.debug("Number of nodes in the graph: " + nodes.size());
		
		for (Node n1 : nodes) {
			for (Node n2 : nodes) {

//				logger.debug(count);
//				count++;
				
				if (n1.equals(n2))
					continue;
				
				if (this.visitedSourceTargetPairs.contains(n1.getId() + n2.getId()))
					continue;
				
				this.visitedSourceTargetPairs.add(n1.getId() + n2.getId());

//				logger.debug(n1.getId() + " --- " + n2.getId());

				source = n1;
				target = n2;

				sourceUri = source.getLabel().getUri();
				targetUri = target.getLabel().getUri();
				
				// create a link from the domain to the range
				objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
				if (objectPropertiesDirect != null)
				for (String uri : objectPropertiesDirect) {

					key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = LinkIdFactory.getLinkId(uri, source.getId(), target.getId());
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					link.setPriorityType(LinkPriorityType.DirectObjectProperty);
					addLink(source, target, link);
				}
				
				// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
				objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
				if (objectPropertiesIndirect != null)
				for (String uri : objectPropertiesIndirect) {

					key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = LinkIdFactory.getLinkId(uri, source.getId(), target.getId());
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					// prefer the links that are actually defined between source and target in the ontology 
					// over inherited ones.
					link.setPriorityType(LinkPriorityType.IndirectObjectProperty);
					addLink(source, target, link);
				}

				objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(sourceUri, targetUri);
				if (objectPropertiesWithOnlyDomain != null)
				for (String uri : objectPropertiesWithOnlyDomain) {

					key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = LinkIdFactory.getLinkId(uri, source.getId(), target.getId());
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					// prefer the links that are actually defined between source and target in the ontology 
					// over inherited ones.
					link.setPriorityType(LinkPriorityType.ObjectPropertyWithOnlyDomain);
					addLink(source, target, link);
				}
				
				objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(sourceUri, targetUri);
				if (objectPropertiesWithOnlyRange != null)
				for (String uri : objectPropertiesWithOnlyRange) {

					key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = LinkIdFactory.getLinkId(uri, source.getId(), target.getId());
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					// prefer the links that are actually defined between source and target in the ontology 
					// over inherited ones.
					link.setPriorityType(LinkPriorityType.ObjectPropertyWithOnlyRange);
					addLink(source, target, link);
				}
				
				// Add subclass links between internal nodes
				if (ontologyManager.isSubClass(sourceUri, targetUri, false)) {
					// target is subclass of source
					key = source.getId() + target.getId() + SubClassLink.getFixedLabel().getUri();
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;
					id = LinkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri(), source.getId(), target.getId());
					SubClassLink subClassOfLink = new SubClassLink(id);
					addLink(source, target, subClassOfLink);
				}
			}
		}		

		logger.debug("exit>");
	}
	
	private void updateLinks2() {
		
		logger.debug("<enter");
		
		List<Node> nodes = this.typeToNodesMap.get(NodeType.InternalNode);
		logger.debug("number of internal nodes: " + nodes.size());
		
		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String id = null;
//		int count = 0;

//		logger.debug("size:" + nodes.size() * nodes.size());
		
		for (int i = 0; i < nodes.size(); i++) {
			
			Node n1 = nodes.get(i);
			for (int j = i+1; j < nodes.size(); j++) {

				Node n2 = nodes.get(j);
//				logger.debug(count);
//				count++;

				if (n1.equals(n2))
					continue;

				if (this.visitedSourceTargetPairs.contains(n1.getId() + n2.getId()))
					continue;
				if (this.visitedSourceTargetPairs.contains(n2.getId() + n1.getId()))
					continue;
				
				this.visitedSourceTargetPairs.add(n1.getId() + n2.getId());

				source = n1;
				target = n2;

				sourceUri = source.getLabel().getUri();
				targetUri = target.getLabel().getUri();
				
//				if ((sourceUri.contains("Person") && targetUri.contains("CulturalHeritage")) ||
//						(sourceUri.contains("CulturalHeritage") && targetUri.contains("Person")))
//					logger.debug("debug1");
				
//				if ((sourceUri.contains("E42") && targetUri.contains("E54")) ||
//						(sourceUri.contains("E54") && targetUri.contains("E42")))
//					logger.debug("debug1");
//				
//				if ((sourceUri.contains("E22") && targetUri.contains("E54")) ||
//						(sourceUri.contains("E54") && targetUri.contains("E22")))
//					logger.debug("debug2");

				
//				if (sourceUri.endsWith("Vehicle") && targetUri.endsWith("Observation") ||
//						targetUri.endsWith("Vehicle") && sourceUri.endsWith("Observation"))
//					logger.debug("debug");

				id = LinkIdFactory.getLinkId(SimpleLink.getFixedLabel().getUri(), source.getId(), target.getId());
				Link link = new SimpleLink(id, SimpleLink.getFixedLabel());

				// order of adding the links is based on the ascending sort of their weight value
				if (this.ontologyManager.isConnectedByDirectProperty(sourceUri, targetUri) ||
						this.ontologyManager.isConnectedByDirectProperty(targetUri, sourceUri)) {
					logger.debug( sourceUri + " and " + targetUri + " are connected by a direct object property.");
					link.setPriorityType(LinkPriorityType.DirectObjectProperty);
					addLink(source, target, link);
				}
				
				else if (this.ontologyManager.isConnectedByIndirectProperty(sourceUri, targetUri) ||
						this.ontologyManager.isConnectedByIndirectProperty(targetUri, sourceUri)) { 
					logger.debug( sourceUri + " and " + targetUri + " are connected by an indirect object property.");
					link.setPriorityType(LinkPriorityType.IndirectObjectProperty);
					addLink(source, target, link);				
				}
				
				else if (this.ontologyManager.isConnectedByDomainlessProperty(sourceUri, targetUri) ||
						this.ontologyManager.isConnectedByDomainlessProperty(targetUri, sourceUri)) { 
					logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose range is " + sourceUri + " or " + targetUri);
					link.setPriorityType(LinkPriorityType.ObjectPropertyWithOnlyRange);
					addLink(source, target, link);				
				}
				
				else if (this.ontologyManager.isConnectedByRangelessProperty(sourceUri, targetUri) ||
						this.ontologyManager.isConnectedByRangelessProperty(targetUri, sourceUri)) { 
					logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose domain is " + sourceUri + " or " + targetUri);
					link.setPriorityType(LinkPriorityType.ObjectPropertyWithOnlyDomain);
					addLink(source, target, link);				
				}
				
				else if (this.ontologyManager.isSubClass(sourceUri, targetUri, false) ||
						this.ontologyManager.isSubClass(targetUri, sourceUri, false)) {
					logger.debug( sourceUri + " and " + targetUri + " are connected by a subClassOf relation.");
					link.setPriorityType(LinkPriorityType.SubClassOf);
					addLink(source, target, link);
				}
				
				else if (this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(sourceUri, targetUri)) {// ||
//						this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(targetUri, sourceUri)) { 
					link.setPriorityType(LinkPriorityType.ObjectPropertyWithoutDomainAndRange);
					addLink(source, target, link);
				}

			}
		}

		logger.debug("exit>");
	}
	
	public HashMap<String, LinkPriorityType> getPossibleUris(String sourceUri, String targetUri) {

		HashMap<String, LinkPriorityType> linkUris = 
				new HashMap<String, LinkPriorityType>();

		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();
							
//		if (targetUri.endsWith("Person") && sourceUri.endsWith("Organisation"))
//			logger.debug("debug");
		
//		if (sourceUri.endsWith("Vehicle") && targetUri.endsWith("Observation") ||
//		targetUri.endsWith("Vehicle") && sourceUri.endsWith("Observation"))
//				logger.debug("debug");

		objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
		if (objectPropertiesDirect != null) {
			for (String s : objectPropertiesDirect)
			linkUris.put(s, LinkPriorityType.DirectObjectProperty);
		}

		objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
		if (objectPropertiesIndirect != null) {
			for (String s : objectPropertiesIndirect)
			linkUris.put(s, LinkPriorityType.IndirectObjectProperty);
		}		

		objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(sourceUri, targetUri);
		if (objectPropertiesWithOnlyDomain != null) {
			for (String s : objectPropertiesWithOnlyDomain)
			linkUris.put(s, LinkPriorityType.ObjectPropertyWithOnlyDomain);
		}	
	
		objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(sourceUri, targetUri);
		if (objectPropertiesWithOnlyRange != null) {
			for (String s : objectPropertiesWithOnlyRange)
			linkUris.put(s, LinkPriorityType.ObjectPropertyWithOnlyRange);
		}	

		if (ontologyManager.isSubClass(sourceUri, targetUri, true)) 
			linkUris.put(Uris.RDFS_SUBCLASS_URI, LinkPriorityType.SubClassOf);
		
		if (objectPropertiesWithoutDomainAndRange != null) {
			for (String s : objectPropertiesWithoutDomainAndRange.keySet())
			linkUris.put(s, LinkPriorityType.ObjectPropertyWithoutDomainAndRange);
		}

		return linkUris;
	}

	public List<Link> getPossibleLinks(String sourceId, String targetId) {

		List<Link> sortedLinks = new ArrayList<Link>();

		Node source = this.idToNodeMap.get(sourceId);
		Node target = this.idToNodeMap.get(targetId);
		
		if (source == null || target == null) {
			logger.debug("Cannot find source or target in the graph.");
			return sortedLinks;
		}

		if (source instanceof ColumnNode || target instanceof ColumnNode) {
			logger.debug("Source or target is a column node.");
			return sortedLinks;
		}

		String sourceUri, targetUri;
		sourceUri = source.getLabel().getUri();
		targetUri = target.getLabel().getUri();

		HashMap<String, LinkPriorityType> links = 
				this.getPossibleUris(sourceUri, targetUri);

		String id;
		Label label;
		String uri;

		for (Entry<String, LinkPriorityType> entry : links.entrySet()) {
			
			uri = entry.getKey();
			id = LinkIdFactory.getLinkId(uri, sourceId, targetId);
			label = new Label(ontologyManager.getUriLabel(uri));
			
			Link newLink;
			if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				newLink = new SubClassLink(id);
			else
				newLink = new ObjectPropertyLink(id, label);
			
			newLink.setPriorityType(entry.getValue());
			sortedLinks.add(newLink);
		}
		
		Collections.sort(sortedLinks, new LinkPriorityComparator());
		
		return sortedLinks;
	}
	
//	public void serialize(String fileName) throws IOException {
//		
//		/**
//		 * Kryo
//		 */
//
//		// Kryo: problem with classes that do not have zero-arg constructors
//		Kryo kryo = new Kryo();
//		JavaSerializer javaSerializer = new JavaSerializer();
//		kryo.register(DirectedWeightedMultigraph.class, javaSerializer);		
//		kryo.register(Node.class, javaSerializer);
//		kryo.register(InternalNode.class, javaSerializer);
//		kryo.register(ColumnNode.class, javaSerializer);
//		kryo.register(DataPropertyLink.class, javaSerializer);
//		kryo.register(ObjectPropertyLink.class, javaSerializer);
//		Output output = new Output(new FileOutputStream(fileName));
//		kryo.writeObject(output, this);
//		output.close();
//		
//		/**
//		 * Protostuff
//		 */
//
//		Schema<GraphBuilder> schema = RuntimeSchema.getSchema(GraphBuilder.class);
//		FileOutputStream f = new FileOutputStream(fileName);
//		ObjectOutputStream out = new ObjectOutputStream(f);
//		LinkedBuffer buffer = LinkedBuffer.allocate(512);
//		try
//		{
////		    int totalBytes = ProtobufIOUtil.writeTo(out, this, schema, buffer);
//		    ProtobufIOUtil.writeTo(out, this, schema, buffer);
//		}
//		finally
//		{
//		    buffer.clear();
//		}
//		
//		/**
//		 * Gson
//		 */
//
//		Gson gson = new Gson();
//		FileOutputStream f = new FileOutputStream(fileName);
//		ObjectOutputStream out = new ObjectOutputStream(f);
//        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
//        writer.setIndent("  ");
//        writer.beginArray();
//        gson.toJson(this, GraphBuilder.class, writer);
//        writer.endArray();
//        writer.close();
//		
//		Kryo kryo = new Kryo();
//		kryo.register(GraphBuilder.class, new Serializer<GraphBuilder>() {
//		    
//			public void write (Kryo kryo, Output output, GraphBuilder object) {
//				output.getOutputStream().w.writeInt(object.);
//		        output.writeInt(object.y);
//		        kryo.writeClassAndObject(output, object);
//		    }
//
//		    public GraphBuilder read (Kryo kryo, Input input, Class<GraphBuilder> type) {
//		        Tile tile = new Tile();
//		        kryo.reference(tile); // Only necessary if Kryo#setReferences is true AND Tile#something could reference this tile.
//		        tile.x = input.readInt();
//		        tile.y = input..readInt();
//		        tile.something = kryo.readClassAndObject(input);
//		        return tile;
//		    }
//		}
//		
//	class MyNullKeySerializer extends JsonSerializer<Object>
//	{
//	  @Override
//	  public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) 
//	      throws IOException, JsonProcessingException
//	  {
//	    jsonGenerator.writeFieldName("");
//	  }
//	}
//	
//		FileOutputStream f = new FileOutputStream(fileName);
//		ObjectOutputStream out = new ObjectOutputStream(f);
//		ObjectMapper mapper = new ObjectMapper();
////        mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
//		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
//	    mapper.setSerializationInclusion(Include.NON_NULL);
//	    mapper.getSerializerProvider().setNullKeySerializer(new MyNullKeySerializer());
//	    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        mapper.writeValue(out, this);
//
//	}
	
//	public static GraphBuilder deserialize(String fileName) throws IOException {
//
//		/**
//		 * Kryo
//		 */
//		
//		Kryo kryo = new KryoReflectionFactorySupport();
//		JavaSerializer javaSerializer = new JavaSerializer();
//		kryo.register(DirectedWeightedMultigraph.class, javaSerializer);
//		kryo.register(InternalNode.class, javaSerializer);
//		kryo.register(ColumnNode.class, javaSerializer);
//		kryo.register(DataPropertyLink.class, javaSerializer);
//		kryo.register(ObjectPropertyLink.class, javaSerializer);		
//		Input input = new Input(new FileInputStream(fileName));
//		GraphBuilder graphBuilder = kryo.readObject(input, GraphBuilder.class);
//		input.close();
//		return graphBuilder;
//		
//		/**
//		 * Protostuff
//		 */
//		
//		Schema<GraphBuilder> schema = RuntimeSchema.getSchema(GraphBuilder.class);
//		FileInputStream f = new FileInputStream(fileName);
//      ObjectInputStream in = new ObjectInputStream(f);
//		LinkedBuffer buffer = LinkedBuffer.allocate(512);
//		
//		GraphBuilder g = new GraphBuilder();
//		ProtobufIOUtil.mergeFrom(in, g, schema, buffer);
//		return g;
//		
//		/**
//		 * Gson
//		 */
//
//		Gson gson = new Gson();
//		FileInputStream f = new FileInputStream(fileName);
//		ObjectInputStream in = new ObjectInputStream(f);
//        JsonReader reader = new JsonReader(new InputStreamReader(in));
//        reader.beginArray();
//        GraphBuilder graphBuilder = gson.fromJson(reader, GraphBuilder.class);
//        reader.endArray();
//        reader.close();
//        return graphBuilder;
//		
//		FileInputStream f = new FileInputStream(fileName);
//		ObjectInputStream in = new ObjectInputStream(f);
//        ObjectMapper mapper = new ObjectMapper();
//        GraphBuilder graphBuilder = mapper.readValue(in, GraphBuilder.class);
//        return graphBuilder;
//
//	}
	
	public static void main(String[] args) throws Exception {
		
		logger.info(Integer.class.getFields()[0].getName());
		
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = "/Users/mohsen/Documents/Academic/ISI/_GIT/Web-Karma/preloaded-ontologies/";
		File ontDir = new File(preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			OntologyManager mgr = new OntologyManager();
			for (File ontology: ontologies) {
				if (ontology.getName().endsWith(".owl") || ontology.getName().endsWith(".rdf")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						mgr.doImport(ontology);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}
		
		DirectedWeightedMultigraph<Node, Link> g = new 
				DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		Node n1 = new InternalNode("n1", null);
		Node n2 = new InternalNode("n2", null);
		Node n3 = new InternalNode("n3", null);
		Node n4 = new InternalNode("n4", null);
		Node n8 = new ColumnNode("n8", "h1", "B", null);
		Node n9 = new ColumnNode("n9", "h2", "B", null);
		
		Link l1 = new ObjectPropertyLink("e1", null);
		Link l2 = new ObjectPropertyLink("e2", null);
		Link l3 = new ObjectPropertyLink("e3", null);
		Link l4 = new ObjectPropertyLink("e4", null);
		Link l5 = new ObjectPropertyLink("e5", null);
		Link l6 = new ObjectPropertyLink("e6", null);
//		Link l7 = new ObjectPropertyLink("e7", null);
//		Link l8 = new DataPropertyLink("e8", null);
//		Link l9 = new DataPropertyLink("e9", null);
		
		g.addVertex(n1);
		g.addVertex(n2);
		g.addVertex(n3);
		g.addVertex(n4);
		g.addVertex(n8);
		g.addVertex(n9);
		
		g.addEdge(n1, n2, l1);
		g.addEdge(n1, n3, l2);
		g.addEdge(n2, n3, l6);
		g.addEdge(n2, n4, l3);
		g.addEdge(n4, n8, l4);
		g.addEdge(n3, n9, l5);
		
		GraphUtil.printGraph(g);
		
		HashMap<Node, Integer> nodeLevels = GraphUtil.levelingCyclicGraph(g);
		for (Node n : g.vertexSet())
			logger.info(n.getId() + " --- " + nodeLevels.get(n));
		
		HashMap<Node, Set<ColumnNode>> coveredColumnNodes = 
				GraphUtil.getNodesCoverage(g, nodeLevels);
		
		logger.info("Internal Nodes Coverage ...");
		for (Entry<Node, Set<ColumnNode>> entry : coveredColumnNodes.entrySet()) {
			logger.info(entry.getKey().getId());
			for (Node n : entry.getValue())
				logger.info("-----" + n.getId());
		}
		
//		GraphUtil.serialize(g, "test");
//		DirectedWeightedMultigraph<Node, Link> gprime = GraphUtil.deserialize("test");
//		
//		GraphUtil.printGraph(gprime);

//		g.removeEdge(l1);
//		GraphUtil.printGraph(g);
		
//		g.removeVertex(n2);
//		GraphUtil.printGraph(g);
	}


}
