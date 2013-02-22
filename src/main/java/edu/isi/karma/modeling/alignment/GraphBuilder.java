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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.DomainRangePair;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.ontology.SubclassSuperclassPair;
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

	private class GraphUpdateLinksThread implements Runnable { 

		private Thread runner;
		private GraphBuilder graphBuilder;
		private String propertyUri;
		private List<DomainRangePair> domainRangePairs;
		
		public GraphUpdateLinksThread(GraphBuilder graphBuilder, String propertyUri, List<DomainRangePair> domainRangePairs) {
			this.runner = new Thread(this);
			this.graphBuilder = graphBuilder;
			this.propertyUri = propertyUri;
			this.domainRangePairs = domainRangePairs;
			runner.start();
		}
		
		@Override
		public void run() {
			
			String domainUri = "", rangeUri = "";
			
			String id = "";
			Label label = null;
			
			for (DomainRangePair dr : domainRangePairs) {
				domainUri = dr.getDomain();
				rangeUri = dr.getRange();
				
				List<Node> domains = graphBuilder.uriToNodesMap.get(domainUri);
				if (domains == null || domains.size() == 0) continue;
				List<Node> ranges = graphBuilder.uriToNodesMap.get(rangeUri);
				if (ranges == null || ranges.size() == 0) continue;
				
				for (Node source : domains)
					for (Node target : ranges) {
						String key = source.getId() + target.getId() + this.propertyUri;
						// check to see if the link is duplicate or not
						if (!this.graphBuilder.sourceToTargetLinkUris.contains(key)) {
							id = linkIdFactory.getLinkId(this.propertyUri);
							label = ontologyManager.getUriLabel(this.propertyUri);
							Link link = new ObjectPropertyLink(id, label);
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
						}
					}
			}		
		}
	}
	
	private class GraphUpdateLinksThread2 implements Runnable { 

		private Thread runner;
		private Node node;
		private List<Node> nodeList;
		
		public GraphUpdateLinksThread2(Node node, List<Node> nodeList) {
			this.runner = new Thread(this);
			this.node = node;
			this.nodeList = nodeList;
			runner.start();
		}
		
		@Override
		public void run() {
			
			List<String> directObjectProperties = new ArrayList<String>();
			List<String> inheritedObjectProperties = new ArrayList<String>();

			Node source;
			Node target;
			String sourceUri;
			String targetUri;

			String uri = null;
			String id = null;
			Label label = null;			

			Node n1 = this.node;
			for (Node n2 : nodeList) {

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
					
					// create a link from the domain to the range
					directObjectProperties = ontologyManager.getObjectProperties(sourceUri, targetUri, false);

					for (int u = 0; u < directObjectProperties.size(); u++) {
						uri = directObjectProperties.get(u);

						String key = source.getId() + target.getId() + uri;
						// check to see if the link is duplicate or not
						if (sourceToTargetLinkUris.contains(key)) continue;

						id = linkIdFactory.getLinkId(uri);
						label = ontologyManager.getUriLabel(uri);
						Link link = new ObjectPropertyLink(id, label);
						addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT);
					}
					
					// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
					inheritedObjectProperties = ontologyManager.getOnlyInheritedObjectProperties(sourceUri, targetUri);
					for (int u = 0; u < inheritedObjectProperties.size(); u++) {
						uri = inheritedObjectProperties.get(u);

						String key = source.getId() + target.getId() + uri;
						// check to see if the link is duplicate or not
						if (sourceToTargetLinkUris.contains(key)) continue;

						id = linkIdFactory.getLinkId(uri);
						label = ontologyManager.getUriLabel(uri);
						Link link = new ObjectPropertyLink(id, label);
						// prefer the links that are actually defined between source and target in the ontology 
						// over inherited ones.
						addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
					}

					// Add subclass links between internal nodes
					// we have to check both sides.
					if (ontologyManager.isSubClass(targetUri, sourceUri, false)) {
						// target is subclass of source
						String key = target.getId() + source.getId() + SubClassLink.getFixedLabel().getUri();
						// check to see if the link is duplicate or not
						if (sourceToTargetLinkUris.contains(key)) continue;
						id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
						SubClassLink subClassOfLink = new SubClassLink(id);
						addWeightedLink(target, source, subClassOfLink, ModelingParams.MAX_WEIGHT);
					}

				}
			}
		
		
		}
	}
	
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
			computeUriClosure(uri);
	}
	
	public void addNodeList(List<Node> nodes) {
		
		logger.debug("<enter");

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		for (Node node : nodes) {
			
			if (!addSingleNode(node))
				continue;
				
			if (node instanceof InternalNode) 
				addNodeClosure(node, new ArrayList<Node>());
		}

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start)/1000F;
		logger.info("time to add nodes closure: " + elapsedTimeSec);

		updateLinks();
		
		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
		logger.info("time to update links of the graph: " + elapsedTimeSec);
		
		updateLinksFromThing();
		
		long updateLinksFromThing = System.currentTimeMillis();
		elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
		logger.info("time to update links to Thing (root): " + elapsedTimeSec);

		logger.info("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.info("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");		
	}

	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (!addSingleNode(node))
			return false;
			
		if (node instanceof InternalNode) {

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			
//			List<Node> newNodes = new ArrayList<Node>();
//			addNodeClosure(node, newNodes);
//			newNodes.add(node);
//			addNodeClosure(node, newNodes);
			
			addNodeClosure(node, new ArrayList<Node>());
			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start)/1000F;
			logger.info("time to add nodes closure: " + elapsedTimeSec);

//			updateLinks2();
			updateLinks();
			
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

			logger.info("total number of nodes in graph: " + this.graph.vertexSet().size());
			logger.info("total number of links in graph: " + this.graph.edgeSet().size());
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
	
	public void updateGraph() {
		
		logger.debug("<enter");

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		for (Node node : this.typeToNodesMap.get(NodeType.InternalNode)) 
			addNodeClosure(node, new ArrayList<Node>());

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start)/1000F;
		logger.info("time to add nodes closure: " + elapsedTimeSec);

		updateLinks();
		
		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
		logger.info("time to update links of the graph: " + elapsedTimeSec);
		
		updateLinksFromThing();
		
		long updateLinksFromThing = System.currentTimeMillis();
		elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
		logger.info("time to update links to Thing (root): " + elapsedTimeSec);

		logger.info("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.info("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");		
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
		if (sourceToTargetLinkUris.contains(key))
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
			if (closure != null) closureIncludingSelf.addAll(closure);
			if (!closureIncludingSelf.contains(source)) closureIncludingSelf.add(source);
			
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
			if (closure != null) closureIncludingSelf.addAll(closure);
			if (!closureIncludingSelf.contains(source)) closureIncludingSelf.add(source);
			
			for (Node n : closureIncludingSelf) {
				Integer refCount = this.nodeReferences.get(n);
				this.nodeReferences.put(source, --refCount);
			}
		}
		
		boolean result = this.graph.removeEdge(link);
		
		return result;
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
		if (closure != null) closureIncludingSelf.addAll(closure);
		if (!closureIncludingSelf.contains(node)) closureIncludingSelf.add(node);

		for (Node n : closureIncludingSelf) {
			Integer refCount = this.nodeReferences.get(n);
			if (refCount.intValue() == 0) 
				removeSingleNode(n);
			else
				this.nodeReferences.put(n, --refCount);
		}

		logger.info("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.info("total number of links in graph: " + this.graph.edgeSet().size());
		
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
						for (String c : this.uriClosure.get(ss))
							if (!temp.contains(c)) {temp.add(c); count++;}
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

		List<String> uriDirectConnections = getUriDirectConnections(uri);
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

	private void updateLinks() {

		logger.debug("<enter");

		HashMap<String, List<DomainRangePair>> opDirectDomainRangePairs = 
				this.ontologyManager.getObjectPropertyDirectDomainRangePairs();
		HashMap<String, List<DomainRangePair>> opNotDirectDomainRangePairs = 
				this.ontologyManager.getObjectPropertyNotDirectDomainRangePairs();
		List<SubclassSuperclassPair> indirectSubclassSuperclassPairs = 
				this.ontologyManager.getIndirectSubclassSuperclassPairs();
		
		String domainUri = "", rangeUri = "";
		String subclassUri = "", superclassUri = "";
		
		String id = "";
		Label label = null;
		
		for (String uri : opDirectDomainRangePairs.keySet()) {
			for (DomainRangePair dr : opDirectDomainRangePairs.get(uri)) {
				domainUri = dr.getDomain();
				rangeUri = dr.getRange();
				
				List<Node> domains = this.uriToNodesMap.get(domainUri);
				List<Node> ranges = this.uriToNodesMap.get(rangeUri);
				
				if (domains == null || ranges == null) continue;
				
				for (Node source : domains)
					for (Node target : ranges) {
						String key = source.getId() + target.getId() + uri;
						// check to see if the link is duplicate or not
						if (!sourceToTargetLinkUris.contains(key)) {
							id = linkIdFactory.getLinkId(uri);
							label = ontologyManager.getUriLabel(uri);
							Link link = new ObjectPropertyLink(id, label);
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT);
						}
					}
			}
		}

//		int poolSize = opNotDirectDomainRangePairs.keySet().size();
//		logger.info("thread pool size: " + poolSize);
//	    ExecutorService service = Executors.newFixedThreadPool(poolSize);
//	    List<Future<?>> futures = new ArrayList<Future<?>>();
//	    int count = 0;
//	    for (String uri : opNotDirectDomainRangePairs.keySet()) {
//	    	System.out.println(count);
//	    	System.out.println(uri + " * number of pairs * " + opNotDirectDomainRangePairs.get(uri).size());
//			Future<?> f = service.submit(new GraphUpdateLinksThread(this, uri, opNotDirectDomainRangePairs.get(uri)));
//		    futures.add(f);
//		    count ++;
//		}
//
//	    count = 0;
//	    System.out.println("done submitting ... ");
//	    // wait for all tasks to complete before continuing
//	    for (Future<?> f : futures)
//	    {
//	    	System.out.println(count);
//	    	try {
//				f.get();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			}
//	    	count ++;
//	    }
//
//		//shut down the executor service so that this thread can exit
//		service.shutdownNow();

	    for (String uri : opNotDirectDomainRangePairs.keySet()) {
			for (DomainRangePair dr : opNotDirectDomainRangePairs.get(uri)) {
				domainUri = dr.getDomain();
				rangeUri = dr.getRange();
				
				List<Node> domains = this.uriToNodesMap.get(domainUri);
				if (domains == null || domains.size() == 0) continue;
				List<Node> ranges = this.uriToNodesMap.get(rangeUri);
				if (ranges == null || ranges.size() == 0) continue;
				
				for (Node source : domains)
					for (Node target : ranges) {
						String key = source.getId() + target.getId() + uri;
						// check to see if the link is duplicate or not
						if (!sourceToTargetLinkUris.contains(key)) {
							id = linkIdFactory.getLinkId(uri);
							label = ontologyManager.getUriLabel(uri);
							Link link = new ObjectPropertyLink(id, label);
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
						}
					}
			}
		}
		
		// subclass links
		for (SubclassSuperclassPair ss : indirectSubclassSuperclassPairs) {
			subclassUri = ss.getSubclass();
			superclassUri = ss.getSuperclass();
			
			List<Node> subclasses = this.uriToNodesMap.get(subclassUri);
			List<Node> superclasses = this.uriToNodesMap.get(superclassUri);
			
			if (subclasses == null || superclasses == null) continue;
			
			for (Node subclass : subclasses)
				for (Node superclass : superclasses) {
					String key = subclass.getId() + superclass.getId() + SubClassLink.getFixedLabel().getUri();
					// check to see if the link is duplicate or not
					if (!sourceToTargetLinkUris.contains(key)) {
						id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
						SubClassLink subClassOfLink = new SubClassLink(id);
						addWeightedLink(subclass, superclass, subClassOfLink, ModelingParams.MAX_WEIGHT);
					}
				}
		}

		logger.debug("exit>");
	}

	private void updateLinksMultiThread() {

		logger.debug("<enter");

		HashMap<String, List<DomainRangePair>> opDirectDomainRangePairs = 
				this.ontologyManager.getObjectPropertyDirectDomainRangePairs();
		HashMap<String, List<DomainRangePair>> opNotDirectDomainRangePairs = 
				this.ontologyManager.getObjectPropertyNotDirectDomainRangePairs();
		List<SubclassSuperclassPair> indirectSubclassSuperclassPairs = 
				this.ontologyManager.getIndirectSubclassSuperclassPairs();
		
		String domainUri = "", rangeUri = "";
		String subclassUri = "", superclassUri = "";
		
		String id = "";
		Label label = null;
		
		for (String uri : opDirectDomainRangePairs.keySet()) {
			for (DomainRangePair dr : opDirectDomainRangePairs.get(uri)) {
				domainUri = dr.getDomain();
				rangeUri = dr.getRange();
				
				List<Node> domains = this.uriToNodesMap.get(domainUri);
				List<Node> ranges = this.uriToNodesMap.get(rangeUri);
				
				if (domains == null || ranges == null) continue;
				
				for (Node source : domains)
					for (Node target : ranges) {
						String key = source.getId() + target.getId() + uri;
						// check to see if the link is duplicate or not
						if (!sourceToTargetLinkUris.contains(key)) {
							id = linkIdFactory.getLinkId(uri);
							label = ontologyManager.getUriLabel(uri);
							Link link = new ObjectPropertyLink(id, label);
							addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT);
						}
					}
			}
		}

		int poolSize = opNotDirectDomainRangePairs.keySet().size();
		logger.info("thread pool size: " + poolSize);
	    ExecutorService service = Executors.newFixedThreadPool(poolSize);
	    List<Future<?>> futures = new ArrayList<Future<?>>();
	    int count = 0;
	    for (String uri : opNotDirectDomainRangePairs.keySet()) {
	    	System.out.println(count);
	    	System.out.println(uri + " * number of pairs * " + opNotDirectDomainRangePairs.get(uri).size());
			Future<?> f = service.submit(new GraphUpdateLinksThread(this, uri, opNotDirectDomainRangePairs.get(uri)));
		    futures.add(f);
		    count ++;
		}

	    count = 0;
	    System.out.println("done submitting ... ");
	    // wait for all tasks to complete before continuing
	    for (Future<?> f : futures)
	    {
	    	System.out.println(count);
	    	try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	    	count ++;
	    }

		//shut down the executor service so that this thread can exit
		service.shutdownNow();
		
		// subclass links
		for (SubclassSuperclassPair ss : indirectSubclassSuperclassPairs) {
			subclassUri = ss.getSubclass();
			superclassUri = ss.getSuperclass();
			
			List<Node> subclasses = this.uriToNodesMap.get(subclassUri);
			List<Node> superclasses = this.uriToNodesMap.get(superclassUri);
			
			if (subclasses == null || superclasses == null) continue;
			
			for (Node subclass : subclasses)
				for (Node superclass : superclasses) {
					String key = subclass.getId() + superclass.getId() + SubClassLink.getFixedLabel().getUri();
					// check to see if the link is duplicate or not
					if (!sourceToTargetLinkUris.contains(key)) {
						id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
						SubClassLink subClassOfLink = new SubClassLink(id);
						addWeightedLink(subclass, superclass, subClassOfLink, ModelingParams.MAX_WEIGHT);
					}
				}
		}

		logger.debug("exit>");
	}
	
	private void updateLinks2() {
		
		logger.debug("<enter");
		
		Node[] nodes = this.graph.vertexSet().toArray(new Node[0]);
		logger.debug("number of vertices: " + nodes.length);

		List<String> directObjectProperties = new ArrayList<String>();
		List<String> inheritedObjectProperties = new ArrayList<String>();

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String uri = null;
		String id = null;
		Label label = null;

		for (Node n1 : nodes) {
			for (Node n2 : nodes) {

				if (n1.equals(n2))
					continue;

				source = n1;
				target = n2;

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
				
				// create a link from the domain to the range
				directObjectProperties = ontologyManager.getObjectProperties(sourceUri, targetUri, false);

				for (int u = 0; u < directObjectProperties.size(); u++) {
					uri = directObjectProperties.get(u);

					String key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = linkIdFactory.getLinkId(uri);
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT);
				}
				
				// create a link from the domain and all its subclasses of ObjectProperties to range and all its subclasses
				inheritedObjectProperties = ontologyManager.getOnlyInheritedObjectProperties(sourceUri, targetUri);
				for (int u = 0; u < inheritedObjectProperties.size(); u++) {
					uri = inheritedObjectProperties.get(u);

					String key = source.getId() + target.getId() + uri;
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;

					id = linkIdFactory.getLinkId(uri);
					label = ontologyManager.getUriLabel(uri);
					Link link = new ObjectPropertyLink(id, label);
					// prefer the links that are actually defined between source and target in the ontology 
					// over inherited ones.
					addWeightedLink(source, target, link, ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT);
				}

				// Add subclass links between internal nodes
				// we have to check both sides.
				if (ontologyManager.isSubClass(targetUri, sourceUri, false)) {
					// target is subclass of source
					String key = target.getId() + source.getId() + SubClassLink.getFixedLabel().getUri();
					// check to see if the link is duplicate or not
					if (sourceToTargetLinkUris.contains(key)) continue;
					id = linkIdFactory.getLinkId(SubClassLink.getFixedLabel().getUri());
					SubClassLink subClassOfLink = new SubClassLink(id);
					addWeightedLink(target, source, subClassOfLink, ModelingParams.MAX_WEIGHT);
				}
			}
		}		

		logger.debug("exit>");
	}
	
	private void updateLinks2MultiThread() {
		
		logger.debug("<enter");

		Node[] nodes = this.graph.vertexSet().toArray(new Node[0]);
		int poolSize = nodes.length;
		logger.info("thread pool size: " + poolSize);
	    ExecutorService service = Executors.newFixedThreadPool(poolSize);
	    List<Future<?>> futures = new ArrayList<Future<?>>();
	    int count = 0;
	    
		for (Node n1 : nodes) {

	    	System.out.println(count);
	    	//System.out.println(n1.getId());
			Future<?> f = service.submit(new GraphUpdateLinksThread2(n1, Arrays.asList(nodes)));
		    futures.add(f);
		    count ++;
		}

		System.out.println("done submitting ... ");
	    // wait for all tasks to complete before continuing
	    for (Future<?> f : futures)
	    {
	    	System.out.println(count);
	    	try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	    	count ++;
	    }

		//shut down the executor service so that this thread can exit
		service.shutdownNow();

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
