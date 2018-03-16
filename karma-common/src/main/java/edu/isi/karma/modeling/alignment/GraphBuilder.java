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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.learner.SemanticTypeMapping;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.CompactLink;
import edu.isi.karma.rep.alignment.CompactObjectPropertyLink;
import edu.isi.karma.rep.alignment.CompactSubClassLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class GraphBuilder {

	static Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

	protected DirectedWeightedMultigraph<Node, DefaultLink> graph;
	protected OntologyManager ontologyManager;
	
	private NodeIdFactory nodeIdFactory;
	
	private HashSet<String> visitedSourceTargetPairs; 

	// HashMaps
	
	private HashMap<String, Node> idToNodeMap;
	private HashMap<String, LabeledLink> idToLinkMap;

	private HashMap<String, Set<Node>> uriToNodesMap;
	private HashMap<String, Set<LabeledLink>> uriToLinksMap;
	
	private HashMap<NodeType, Set<Node>> typeToNodesMap;
	private HashMap<LinkType, Set<LabeledLink>> typeToLinksMap;

	private HashMap<LinkStatus, Set<LabeledLink>> statusToLinksMap;
	
	private HashMap<String, Set<String>> uriClosure;

	private Set<Node> forcedNodes;
	
	// To be used in matching semantic types with graph nodes
	private HashSet<String> modelIds;
	private HashMap<String, Integer> linkCountMap;
	private HashMap<String, Integer> nodeDataPropertyCount; // nodeId + dataPropertyUri --> count
	private HashMap<String, Set<Node>> nodeDataProperties; // nodeId + dataPropertyUri --> ColumnNode
	private HashMap<String, Set<LabeledLink>> nodeIncomingLinks;
	private HashMap<String, Set<LabeledLink>> nodeOutgoingLinks;
	private HashMap<String, Set<SemanticTypeMapping>> semanticTypeMatches; // nodeUri + dataPropertyUri --> SemanticType Mapping
	private HashMap<String, List<LabeledLink>> patternLinks;
	private int numberOfModelLinks = 0;

	// Constructor
	
	public GraphBuilder(OntologyManager ontologyManager, boolean addThingNode) { 
		
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = new NodeIdFactory();

		this.idToNodeMap = new HashMap<>();
		this.idToLinkMap = new HashMap<>();
		this.uriToNodesMap = new HashMap<>();
		this.uriToLinksMap = new HashMap<>();
		this.typeToNodesMap = new HashMap<>();
		this.typeToLinksMap = new HashMap<>();
		this.statusToLinksMap = new HashMap<>();
		
		this.uriClosure = new HashMap<>();

		this.graph = new DirectedWeightedMultigraph<>(DefaultLink.class);
		
		this.visitedSourceTargetPairs = new HashSet<>();
			
		this.modelIds = new HashSet<>();
		this.linkCountMap = new HashMap<>();
		this.nodeDataPropertyCount = new HashMap<>();
		this.semanticTypeMatches = new HashMap<>();
		this.patternLinks = new HashMap<>();

		this.nodeDataProperties= new HashMap<>(); 
		
		this.nodeIncomingLinks = new HashMap<>();
		this.nodeOutgoingLinks = new HashMap<>();
		
		this.forcedNodes = new HashSet<>();
		if (addThingNode) 
			this.initialGraph();
		
	}
	
	public GraphBuilder(OntologyManager ontologyManager, DirectedWeightedMultigraph<Node, DefaultLink> graph, boolean useOriginalWeights) {
		
		this(ontologyManager, false);
		if (graph == null)
			return;
		
		for (Node node : graph.vertexSet()) {
			
			this.addNode(node);
			// building NodeIdFactory
			if (node.getLabel() != null) {
				nodeIdFactory.getNodeId(node.getUri());
			}						
		}
		
		Node source;
		Node target;
		
		for (DefaultLink link : graph.edgeSet()) {
			
			source = link.getSource();
			target = link.getTarget();
			
			DefaultLink l = link.getCopy(link.getId());
			if (useOriginalWeights)
				this.addLink(source, target, l, link.getWeight());
			else
				this.addLink(source, target, l);
		}
		
		logger.debug("graph has been loaded.");
	}
	
	public NodeIdFactory getNodeIdFactory() {
		return nodeIdFactory;
	}

	public OntologyManager getOntologyManager() {
		return this.ontologyManager;
	}
	
	public DirectedWeightedMultigraph<Node, DefaultLink> getGraph() {
		return this.graph;
	}
	
	public void setGraph(DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		this.graph = graph;
	}
	
	public HashMap<String, Node> getIdToNodeMap() {
		return idToNodeMap;
	}

	public HashMap<String, LabeledLink> getIdToLinkMap() {
		return idToLinkMap;
	}

	public HashMap<String, Set<Node>> getUriToNodesMap() {
		return uriToNodesMap;
	}

	public HashMap<String, Set<LabeledLink>> getUriToLinksMap() {
		return uriToLinksMap;
	}

	public HashMap<NodeType, Set<Node>> getTypeToNodesMap() {
		return typeToNodesMap;
	}

	public HashMap<LinkType, Set<LabeledLink>> getTypeToLinksMap() {
		return typeToLinksMap;
	}

	public HashMap<LinkStatus, Set<LabeledLink>> getStatusToLinksMap() {
		return statusToLinksMap;
	}
	
	public Set<String> getModelIds() {
		return Collections.unmodifiableSet(modelIds);
	}

	public HashMap<String, Integer> getLinkCountMap() {
		return linkCountMap;
	}
	
	public HashMap<String, Integer> getNodeDataPropertyCount() {
		return nodeDataPropertyCount;
	}

	public HashMap<String, Set<SemanticTypeMapping>> getSemanticTypeMatches() {
		return semanticTypeMatches;
	}

	public int getNumberOfModelLinks() {
		return numberOfModelLinks;
	}

	public HashMap<String, Set<Node>> getNodeDataProperties() {
		return nodeDataProperties;
	}

	public final HashMap<String, Set<LabeledLink>> getIncomingLinksMap() {
		return nodeIncomingLinks;
	}
	
	public final HashMap<String, Set<LabeledLink>> getOutgoingLinksMap() {
		return nodeOutgoingLinks;
	}
	
	public HashMap<String, List<LabeledLink>> getPatternLinks() {
		return patternLinks;
	}

	public void resetOntologyMaps() {
		String[] currentUris = this.uriClosure.keySet().toArray(new String[0]);
		this.uriClosure.clear();
		for (String uri : currentUris)
			computeUriClosure(uri);
	}

	public boolean addNodeAndUpdate(Node node) {
		return this.addNodeAndUpdate(node, null);
	}
	
	public boolean addNodeAndUpdate(Node node, Set<Node> addedNodes) {
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome());
		boolean result = addNode(node);
		if (!result) 
			return result;

		if (!modelingConfiguration.getOntologyAlignment()) 
			return result;

		if (addedNodes == null) 
			addedNodes = new HashSet<>();
		addedNodes.add(node);
		if (node instanceof InternalNode) 
			addClosureAndUpdateLinks((InternalNode)node, addedNodes);
		
		return result;
	}
	
	public InternalNode copyNodeAndUpdate(Node node, boolean copyLinksToColumnNodes) {
		InternalNode copyNode = null;
		if (node instanceof InternalNode) {
			copyNode = this.copyNode((InternalNode)node, copyLinksToColumnNodes);
		} else {
			logger.error("only can copy an internal node");
			return null;
		}
		return copyNode;
	}
	
	private void addClosureAndUpdateLinks(InternalNode node, Set<Node> addedNodes) {
		
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome());
		if (addedNodes == null) addedNodes = new HashSet<>();
		if (node instanceof InternalNode) {

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			addedNodes.add(node);
			
			if (modelingConfiguration.getNodeClosure()) {
				addNodeClosure(node, addedNodes);
			}
			
			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start)/1000F;
			logger.debug("time to add nodes closure: " + elapsedTimeSec);

			updateLinks();
			
			// if we consider the set of current nodes as S1 and the set of new added nodes as S2:
			// (*) the direction of all the subclass links between S1 and S2 is from S2 to S1
			//		This is because superclasses of all the nodes in S1 are already added to the graph. 
			// (*) the direction of all the object property links between S1 and S2 is from S1 to S2
			//		This is because all the nodes that are reachable from S1 are added to the graph before adding new nodes in S2.
			long updateLinks = System.currentTimeMillis();
			elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
			logger.debug("time to update links of the graph: " + elapsedTimeSec);
			
//			updateLinksFromThing();
			
//			long updateLinksFromThing = System.currentTimeMillis();
//			elapsedTimeSec = (updateLinksFromThing - updateLinks)/1000F;
//			logger.debug("time to update links to Thing (root): " + elapsedTimeSec);

			logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
			logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		}
		
	}
	
	public void addClosureAndUpdateLinks(Set<InternalNode> internalNodes, Set<Node> addedNodes) {
		
		logger.debug("<enter");
		if (addedNodes == null) addedNodes = new HashSet<>();

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		if (internalNodes != null) {
			Node[] nodes = internalNodes.toArray(new Node[0]);
			for (Node node : nodes)
				if (this.idToNodeMap.containsKey(node.getId()))
					addNodeClosure(node, addedNodes);
		}

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start)/1000F;
		logger.debug("time to add nodes closure: " + elapsedTimeSec);

		updateLinks();
		
		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure)/1000F;
		logger.debug("time to update links of the graph: " + elapsedTimeSec);
		
		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");		
	}
	
	public boolean addNode(Node node) {
		
		logger.debug("<enter");

		if (node == null) {
			logger.error("The node is null.");
			return false;
		}
		
		if (idToNodeMap.get(node.getId()) != null) {
			logger.debug("The node with id=" + node.getId() + " already exists in the graph.");
			return false;
		}
		
		if (node instanceof InternalNode) {
			String uri = node.getUri();
			Label label = this.ontologyManager.getUriLabel(uri);
			if (label == null) {
				logger.debug("The resource " + uri + " does not exist in the ontology.");
				return false;
			}
			node.getLabel().setNs(label.getNs());
			node.getLabel().setPrefix(label.getPrefix());
			node.getLabel().setRdfsLabel(label.getRdfsLabel());
			node.getLabel().setRdfsComment(label.getRdfsComment());
		}
		
		if(node.isForced())
			this.forcedNodes.add(node);
		
		this.graph.addVertex(node);
		
		this.idToNodeMap.put(node.getId(), node);
//		logger.info("Added in idToNodeMap:" + node.getId());
		Set<Node> nodesWithSameUri = uriToNodesMap.get(node.getUri());
		if (nodesWithSameUri == null) {
			nodesWithSameUri = new HashSet<>();
			uriToNodesMap.put(node.getUri(), nodesWithSameUri);
		}
		nodesWithSameUri.add(node);
		
		Set<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType == null) {
			nodesWithSameType = new HashSet<>();
			typeToNodesMap.put(node.getType(), nodesWithSameType);
		}
		nodesWithSameType.add(node);
		
		if (node.getModelIds() != null)
			this.modelIds.addAll(node.getModelIds());
					
		this.uriClosure.put(node.getUri(), null);

		logger.debug("exit>");		
		return true;
	}
	
	public InternalNode copyNode(InternalNode node, boolean copyLinksToColumnNodes) {
		
		if (node == null) {
			logger.error("input node is null");
			return null;
		}
		
		String id = this.nodeIdFactory.getNodeId(node.getUri());
		InternalNode newNode = new InternalNode(id, node.getLabel());
		
		if (!this.addNode(newNode)) {
			logger.error("could not add the new node " + newNode.getId());
			return null;
		}
		
		Node source , target;
		String newId;
		Set<DefaultLink> incomingLinks = this.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {
			for (DefaultLink l : incomingLinks) {
				source = l.getSource();
				if (source instanceof ColumnNode) continue;
				target = newNode;
				newId = LinkIdFactory.getLinkId(l.getUri(), source.getId(), target.getId());
				DefaultLink copyLink = l.getCopy(newId);
				this.addLink(source, target, copyLink, l.getWeight());
			}
		}
		
		Set<DefaultLink> outgoingLinks = this.getGraph().outgoingEdgesOf(node);
		if (outgoingLinks != null) {
			for (DefaultLink l : outgoingLinks) {
				source = newNode;
				target = l.getTarget();
				if (!copyLinksToColumnNodes && target instanceof ColumnNode) continue; // skip links to column nodes
				newId = LinkIdFactory.getLinkId(l.getUri(), source.getId(), target.getId());
				DefaultLink copyLink = l.getCopy(newId);
				this.addLink(source, target, copyLink, l.getWeight());
			}
		}
		
		return newNode;

	}
	
	public Set<Node> getForcedNodes() {
		return this.forcedNodes;
	}

	protected boolean sourceIsConnectedToTarget(Node source, Node target) {
		return this.visitedSourceTargetPairs.contains(source.getId() + target.getId());
	}
	
	public boolean addLink(Node source, Node target, DefaultLink link, Double weight) {
		if (addLink(source, target, link)) {
			if (weight != null) changeLinkWeight(link, weight);
			return true;
		}
		return false;
	}
	
	public boolean addLink(Node source, Node target, DefaultLink link) {

		logger.debug("<enter");

		if (source == null || target == null || link == null) {
			logger.error("Source, target, and the link cannot be null.");
			return false;
		}
		
		if (this.idToLinkMap.containsKey(link.getId())) {
			logger.debug("The link with id=" + link.getId() + " already exists in the graph");
			return false;
		}
		
		if (!this.idToNodeMap.containsKey(source.getId())) {
			logger.debug("The link source " + source.getId() + " does not exist in the graph");
			return false;
		}

		if (!this.idToNodeMap.containsKey(target.getId())) {
			logger.debug("The link target " + target.getId() + " does not exist in the graph");
			return false;
		}

		if (link instanceof LabeledLink) {
			String uri = link.getUri();
			Label label = this.ontologyManager.getUriLabel(uri);
			if (label == null) {
				logger.debug("The resource " + uri + " does not exist in the ontology.");
				return false;
			}
			((LabeledLink)link).getLabel().setNs(label.getNs());
			((LabeledLink)link).getLabel().setPrefix(label.getPrefix());
		}
			
		this.graph.addEdge(source, target, link);
		
		this.visitedSourceTargetPairs.add(source.getId() + target.getId());
		
		double w = computeWeight(link);
		
		this.graph.setEdgeWeight(link, w);
				
		if (link instanceof CompactLink) {
			logger.debug("exit>");		
			return true;
		}
			
		// update the corresponding lists and hashmaps
		LabeledLink labeledLink = null;
//		if (link instanceof LabeledLink)
		labeledLink = (LabeledLink)link;
		
		this.idToLinkMap.put(labeledLink.getId(), labeledLink);
		
		Set<LabeledLink> linksWithSameUri = uriToLinksMap.get(labeledLink.getUri());
		if (linksWithSameUri == null) {
			linksWithSameUri = new HashSet<>();
			uriToLinksMap.put(labeledLink.getUri(), linksWithSameUri);
		}
		linksWithSameUri.add(labeledLink);
				
		Set<LabeledLink> linksWithSameType = typeToLinksMap.get(labeledLink.getType());
		if (linksWithSameType == null) {
			linksWithSameType = new HashSet<>();
			typeToLinksMap.put(labeledLink.getType(), linksWithSameType);
		}
		linksWithSameType.add(labeledLink);
		
		if (labeledLink.getStatus() != LinkStatus.Normal) {
			Set<LabeledLink> linksWithSameStatus = statusToLinksMap.get(labeledLink.getStatus());
			if (linksWithSameStatus == null) { 
				linksWithSameStatus = new HashSet<>();
				statusToLinksMap.put(labeledLink.getStatus(), linksWithSameStatus);
			}
		}

		Set<LabeledLink> inLinks = nodeIncomingLinks.get(target.getId());
		if(inLinks == null) inLinks = new HashSet<>();
		inLinks.add(labeledLink);
		nodeIncomingLinks.put(target.getId(), inLinks);
		
		Set<LabeledLink> outLinks = nodeOutgoingLinks.get(source.getId());
		if(outLinks == null) outLinks = new HashSet<>();
		outLinks.add(labeledLink);
		nodeOutgoingLinks.put(source.getId(), outLinks);
		
		
		if (source instanceof InternalNode && target instanceof ColumnNode) {

			String key = source.getId() + link.getUri();
			Integer count = this.nodeDataPropertyCount.get(key);
			if (count == null) this.nodeDataPropertyCount.put(key, 1);
			else this.nodeDataPropertyCount.put(key, count.intValue() + 1);
			
			Set<Node> dataPropertyColumnNodes = this.nodeDataProperties.get(key);
			if (dataPropertyColumnNodes == null) {
				dataPropertyColumnNodes = new HashSet<>();
				this.nodeDataProperties.put(key, dataPropertyColumnNodes);
			}
			dataPropertyColumnNodes.add(target);
			
			key = source.getUri() + link.getUri();
			Set<SemanticTypeMapping> SemanticTypeMappings = this.semanticTypeMatches.get(key);
			if (SemanticTypeMappings == null) {
				SemanticTypeMappings = new HashSet<>();
				this.semanticTypeMatches.put(key, SemanticTypeMappings);
			}
			SemanticTypeMappings.add(new SemanticTypeMapping(null, null, (InternalNode)source, labeledLink, (ColumnNode)target));
		}
				
		if (labeledLink.getModelIds() != null) {
			this.modelIds.addAll(labeledLink.getModelIds());
			this.numberOfModelLinks++;
		}
		
		this.updateLinkCountMap(link);
		
		logger.debug("exit>");		
		return true;
	}
	
	public void savePatternLink(LabeledLink l) {
		String key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
		List<LabeledLink> links = this.patternLinks.get(key);
		if (links == null) {
			links = new LinkedList<>();
			this.patternLinks.put(key, links);
		}
		links.add(l);
	}
	
	private double computeWeight(DefaultLink link) {
		double w = 0.0;
		
		if (link instanceof LabeledLink && ((LabeledLink)link).getStatus() == LinkStatus.PreferredByUI)
			w = ModelingParams.PROPERTY_UI_PREFERRED_WEIGHT;
		
		if (link instanceof LabeledLink && 
				((LabeledLink)link).getModelIds() != null &&
				!((LabeledLink)link).getModelIds().isEmpty()) 
			w = ModelingParams.PATTERN_LINK_WEIGHT;

		if (link instanceof LabeledLink && ((LabeledLink)link).getStatus() == LinkStatus.ForcedByUser)
			w = ModelingParams.PROPERTY_USER_PREFERRED_WEIGHT;
		
		if (w != 0.0)
			return w;
		
		if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Direct)
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		else if (link instanceof CompactObjectPropertyLink && ((CompactObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Direct)
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Indirect)
			w = ModelingParams.PROPERTY_INDIRECT_WEIGHT;
		else if (link instanceof CompactObjectPropertyLink && ((CompactObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Indirect)
			w = ModelingParams.PROPERTY_INDIRECT_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyDomain)
			w = ModelingParams.PROPERTY_WITH_ONLY_DOMAIN_WEIGHT;
		else if (link instanceof CompactObjectPropertyLink && ((CompactObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyDomain)
			w = ModelingParams.PROPERTY_WITH_ONLY_DOMAIN_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyRange)
			w = ModelingParams.PROPERTY_WITH_ONLY_RANGE_WEIGHT;
		else if (link instanceof CompactObjectPropertyLink && ((CompactObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyRange)
			w = ModelingParams.PROPERTY_WITH_ONLY_RANGE_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithoutDomainAndRange)
			w = ModelingParams.PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT;
		else if (link instanceof CompactObjectPropertyLink && ((CompactObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithoutDomainAndRange)
			w = ModelingParams.PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT;
		else if (link instanceof SubClassLink)
			w = ModelingParams.SUBCLASS_WEIGHT;
		else if (link instanceof CompactSubClassLink)
			w = ModelingParams.SUBCLASS_WEIGHT;
		else 
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		
		return w;
	}
	
	public void changeLinkStatus(LabeledLink link, LinkStatus newStatus) {

		if (link == null) return;
		LabeledLink graphLink = this.getIdToLinkMap().get(link.getId()); // use the graph link, not the tree link sent by the alignment (steiner tree has a copy of the graph link)
		if (graphLink == null) return;
		
		LinkStatus oldStatus = graphLink.getStatus();
		if (newStatus == oldStatus)
			return;
		
		graphLink.setStatus(newStatus);
		link.setStatus(newStatus);
		this.changeLinkWeight(graphLink, computeWeight(graphLink));
		
		Set<LabeledLink> linksWithOldStatus = this.statusToLinksMap.get(oldStatus);
		if (linksWithOldStatus != null) linksWithOldStatus.remove(graphLink);

		if (newStatus == LinkStatus.Normal) // we don't need to index normal links 
			return;
		
		Set<LabeledLink> linksWithNewStatus = this.statusToLinksMap.get(newStatus);
		if (linksWithNewStatus == null) {
			linksWithNewStatus = new HashSet<>();
			statusToLinksMap.put(newStatus, linksWithNewStatus);
		}
		linksWithNewStatus.add(graphLink);
	}
	
	public void changeLinkWeight(DefaultLink link, double weight) {
		this.graph.setEdgeWeight(link, weight);
	}
	
	public boolean removeLink(DefaultLink link) {
		
		if (link == null) {
			logger.debug("The link is null.");
			return false;
		}
		
		if (link instanceof CompactLink)
			return false;

		if (idToLinkMap.get(link.getId()) == null) {
			logger.debug("The link with id=" + link.getId() + " does not exists in the graph.");
			return false;
		}
		
		logger.debug("removing the link " + link.getId() + "...");
		
		if (!this.graph.removeEdge(link))
			return false;

		// update hashmaps

		if (link instanceof LabeledLink) {
			this.idToLinkMap.remove(link.getId());
	
			Set<LabeledLink> linksWithSameUri = uriToLinksMap.get(link.getUri());
			if (linksWithSameUri != null) 
				linksWithSameUri.remove(link);
			
			Set<LabeledLink> linksWithSameType = typeToLinksMap.get(((LabeledLink)link).getType());
			if (linksWithSameType != null) 
				linksWithSameType.remove(link);
			
			Set<LabeledLink> linksWithSameStatus = statusToLinksMap.get(((LabeledLink)link).getStatus());
			if (linksWithSameStatus != null) 
				linksWithSameStatus.remove(link);
			
			Node source = link.getSource();
			Set<LabeledLink> sourceLinks = nodeOutgoingLinks.get(source.getId());
			if(sourceLinks != null)
				sourceLinks.remove(link);
			
			Node target = link.getTarget();
			Set<LabeledLink> targetLinks = nodeIncomingLinks.get(target.getId());
			if(targetLinks != null)
				targetLinks.remove(link);
		}
		
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

		logger.debug("removing the node " + node.getId() + "...");
		
		Set<DefaultLink> incomingLinks = this.graph.incomingEdgesOf(node);
		if (incomingLinks != null) {
			DefaultLink[] incomingLinksArray = incomingLinks.toArray(new DefaultLink[0]);
			for (DefaultLink inLink: incomingLinksArray) {
				this.removeLink(inLink);
			}
		}

		Set<DefaultLink> outgoingLinks = this.graph.outgoingEdgesOf(node);
		if (outgoingLinks != null) {
			DefaultLink[] outgoingLinksArray = outgoingLinks.toArray(new DefaultLink[0]);
			for (DefaultLink outLink: outgoingLinksArray) {
				this.removeLink(outLink);
			}
		}
		
		if(node.isForced())
			this.forcedNodes.remove(node);
		
		if (!this.graph.removeVertex(node))
			return false;
		
		// updating hashmaps
		
		this.idToNodeMap.remove(node.getId());
		
		Set<Node> nodesWithSameUri = uriToNodesMap.get(node.getUri());
		if (nodesWithSameUri != null) 
			nodesWithSameUri.remove(node);
		
		Set<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType != null) 
			nodesWithSameType.remove(node);
		
		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());
		
		return true;
	}
	
	public LinkFrequency getMoreFrequentLinkBetweenNodes(String sourceUri, String targetUri) {

		List<String> possibleLinksFromSourceToTarget = new ArrayList<>();

		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();

		possibleLinksFromSourceToTarget.clear();

		objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
		if (objectPropertiesDirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesDirect);

		objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
		if (objectPropertiesIndirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesIndirect);

		objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(sourceUri);
		if (objectPropertiesWithOnlyDomain != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyDomain);

		objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(targetUri);
		if (objectPropertiesWithOnlyRange != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyRange);

		if (ontologyManager.isSubClass(sourceUri, targetUri, true)) 
			possibleLinksFromSourceToTarget.add(Uris.RDFS_SUBCLASS_URI);

		if (objectPropertiesWithoutDomainAndRange != null) {
			possibleLinksFromSourceToTarget.addAll(objectPropertiesWithoutDomainAndRange.keySet());
		}
		
//		Collection<String> userLinks = this.sourceToTargetLinks.get(sourceUri + "---" + targetUri);
//		if (userLinks != null) {
//			for (String s : userLinks)
//				possibleLinksFromSourceToTarget.add(s);
//		}

		String selectedLinkUri1 = null;
		int maxCount1 = 0;

		String selectedLinkUri2 = null;
		int maxCount2 = 0;

		String selectedLinkUri3 = null;
		int maxCount3 = 0;

		String selectedLinkUri4 = null;
		int maxCount4 = 0;

		String key;
		
		if (possibleLinksFromSourceToTarget != null  && !possibleLinksFromSourceToTarget.isEmpty()) {

			for (String linkUri : possibleLinksFromSourceToTarget) {
				key = "domain:" + sourceUri + ",link:" + linkUri + ",range:" + targetUri;
				Integer count1 = this.linkCountMap.get(key);
				if (count1 != null && count1.intValue() > maxCount1) {
					maxCount1 = count1.intValue();
					selectedLinkUri1 = linkUri;
				}
			}
			
			for (String linkUri : possibleLinksFromSourceToTarget) {
				key = "range:" + targetUri + ",link:" + linkUri ;
				Integer count2 = this.linkCountMap.get(key);
				if (count2 != null && count2.intValue() > maxCount2) {
					maxCount2 = count2.intValue();
					selectedLinkUri2 = linkUri;
				}
			}
			
			for (String linkUri : possibleLinksFromSourceToTarget) {
				key = "domain:" + sourceUri + ",link:" + linkUri;
				Integer count3 = this.linkCountMap.get(key);
				if (count3 != null && count3.intValue() > maxCount3) {
					maxCount3 = count3.intValue();
					selectedLinkUri3 = linkUri;
				}
			}

			for (String linkUri : possibleLinksFromSourceToTarget) {
				key = "link:" + linkUri;
				Integer count4 = this.linkCountMap.get(key);
				if (count4 != null && count4.intValue() > maxCount4) {
					maxCount4 = count4.intValue();
					selectedLinkUri4 = linkUri;
				}
			}

		} else {
			logger.error("Something is going wrong. There should be at least one possible object property between " +
					sourceUri + " and " + targetUri);
			return null;
		}
		
		String selectedLinkUri;
		int maxCount;
		int type;

		if (selectedLinkUri1 != null && selectedLinkUri1.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri1;
			maxCount = maxCount1;
			type = 1; // match domain and link and range
		} else if (selectedLinkUri2 != null && selectedLinkUri2.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri2;
			maxCount = maxCount2;
			type = 2; // match link and range
		} else if (selectedLinkUri3 != null && selectedLinkUri3.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri3;
			maxCount = maxCount3;
			type = 3; // match domain and link
		} else if (selectedLinkUri4 != null && selectedLinkUri4.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri4;
			maxCount = maxCount4;
			type = 4; // match link label
		} else {
			if (objectPropertiesDirect != null && !objectPropertiesDirect.isEmpty()) {
				selectedLinkUri = objectPropertiesDirect.iterator().next();
				type = 5;
			} else 	if (objectPropertiesIndirect != null && !objectPropertiesIndirect.isEmpty()) {
				selectedLinkUri = objectPropertiesIndirect.iterator().next();
				type = 6;
			} else 	if (objectPropertiesWithOnlyDomain != null && !objectPropertiesWithOnlyDomain.isEmpty()) {
				selectedLinkUri = objectPropertiesWithOnlyDomain.iterator().next();
				type = 7;
			} else 	if (objectPropertiesWithOnlyRange != null && !objectPropertiesWithOnlyRange.isEmpty()) {
				selectedLinkUri = objectPropertiesWithOnlyRange.iterator().next();;
				type = 8;
			} else if (ontologyManager.isSubClass(sourceUri, targetUri, true)) {
				selectedLinkUri = Uris.RDFS_SUBCLASS_URI;
				type = 9;
			} else {	// if (objectPropertiesWithoutDomainAndRange != null && objectPropertiesWithoutDomainAndRange.keySet().size() > 0) {
				selectedLinkUri = new ArrayList<>(objectPropertiesWithoutDomainAndRange.keySet()).get(0);
				type = 10;
			}

			maxCount = 0;
		} 
		
		LinkFrequency lf = new LinkFrequency(selectedLinkUri, type, maxCount);
		
		return lf;
		
	}
		
	private void initialGraph() {
		
		logger.debug("<enter");
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome());
		// Add Thing to the Graph 
		if (modelingConfiguration.getThingNode()) {
			String id = nodeIdFactory.getNodeId(Uris.THING_URI);
			Label label = new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL);
			Node thing = new InternalNode(id, label);
			addNode(thing);
		}
		
		logger.debug("exit>");
	}

	private void updateLinkCountMap(DefaultLink link) {

		String key, sourceUri, targetUri, linkUri;
		Integer count;
		Node source, target;
		
		source = link.getSource();
		target = link.getTarget();
		
		sourceUri = source.getUri();
		targetUri = target.getUri();
		linkUri = link.getUri();

//		if (link instanceof DataPropertyLink) return;

		if (target instanceof InternalNode) {
			key = "domain:" + sourceUri + ",link:" + linkUri + ",range:" + targetUri;
			count = this.linkCountMap.get(key);
			if (count == null) this.linkCountMap.put(key, 1);
			else this.linkCountMap.put(key, count.intValue() + 1);
			
			key = "range:" + targetUri + ",link:" + linkUri ;
			count = this.linkCountMap.get(key);
			if (count == null) this.linkCountMap.put(key, 1);
			else this.linkCountMap.put(key, count.intValue() + 1);
		}
		
		key = "domain:" + sourceUri + ",link:" + linkUri;
		count = this.linkCountMap.get(key);
		if (count == null) this.linkCountMap.put(key, 1);
		else this.linkCountMap.put(key, count.intValue() + 1);

		key = "link:" + linkUri;
		count = this.linkCountMap.get(key);
		if (count == null) this.linkCountMap.put(key, 1);
		else this.linkCountMap.put(key, count.intValue() + 1);
	}

	private HashSet<String> getUriDirectConnections(String uri) {
		
		HashSet<String> uriDirectConnections = new HashSet<>();
		
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

	private Set<String> computeUriClosure(String uri) {
		
		Set<String> closure = this.uriClosure.get(uri);
		if (closure != null) 
			return closure;
	
		closure = new HashSet<>();
		List<String> closedList = new ArrayList<>();
		HashMap<String, Set<String>> dependentUrisMap = new HashMap<>();
		computeUriClosureRecursive(uri, closure, closedList, dependentUrisMap);
		if (closedList.contains(uri) && !closure.contains(uri))
			closure.add(uri);
		
		int count = 1;
		while (count != 0) {
			count = 0;
			for (Map.Entry<String, Set<String>> stringSetEntry : dependentUrisMap.entrySet()) {
				Set<String> temp = this.uriClosure.get(stringSetEntry.getKey());
				Set<String> dependentUris = stringSetEntry.getValue();
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

	private void computeUriClosureRecursive(String uri, Set<String> closure, 
			List<String> closedList, HashMap<String, Set<String>> dependentUrisMap) {
		
		logger.debug("<enter");
		
		closedList.add(uri);
		Set<String> currentClosure = this.uriClosure.get(uri);
		if (currentClosure != null) {
			closure.addAll(currentClosure);
			return;
		}

		HashSet<String> uriDirectConnections = getUriDirectConnections(uri);
		if (uriDirectConnections.isEmpty()) {
			this.uriClosure.put(uri, new HashSet<String>());
		} else {
			for (String c : uriDirectConnections) {
				if (closedList.contains(c)) {
					Set<String> dependentUris = dependentUrisMap.get(uri);
					if (dependentUris == null) {
						dependentUris = new HashSet<>();
						dependentUrisMap.put(uri, dependentUris);
					}
					if (!dependentUris.contains(c)) dependentUris.add(c);
					continue;
				}
				if (!closure.contains(c)) closure.add(c);
				if (!closedList.contains(c)) closedList.add(c);
				Set<String> localClosure = new HashSet<>();
				computeUriClosureRecursive(c, localClosure, closedList, dependentUrisMap);
				for (String s : localClosure)
					if (!closure.contains(s)) closure.add(s);
			}
			this.uriClosure.put(uri, closure);
		}
		
		logger.debug("exit>");
	}
	
	public List<Node> getNodeClosure(Node node) {
		
		List<Node> nodeClosure = new ArrayList<>();
		if (node instanceof ColumnNode) return nodeClosure;
		
		String uri = node.getUri();
		Set<String> closure = this.uriClosure.get(uri); 
		if (closure == null) {  // the closure has already been computed.
			closure = computeUriClosure(uri);
		} 
		for (String s : closure) {
			Set<Node> nodes = uriToNodesMap.get(s);
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
		
		if (newAddedNodes == null) newAddedNodes = new HashSet<>();
		
		String uri = node.getUri();
		if (this.uriClosure.get(uri) != null) // the closure is already computed and added to the graph.
			return;

		Set<String> uriClosure = computeUriClosure(uri);

		for (String c : uriClosure) {
			Set<Node> nodesOfSameUri = this.uriToNodesMap.get(c);
			if (nodesOfSameUri == null || nodesOfSameUri.isEmpty()) { // the internal node is not added to the graph before
				Node nn = new InternalNode(nodeIdFactory.getNodeId(c), 
						ontologyManager.getUriLabel(c));
				if (addNode(nn)) newAddedNodes.add(nn);
			} 
		}
		
		logger.debug("exit>");
	}
	
	private void updateLinks() {
		
		logger.debug("<enter");
		
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome());
		
		Set<Node> nodeSet = this.typeToNodesMap.get(NodeType.InternalNode);
		if (nodeSet == null || nodeSet.isEmpty())
			return;
		
		List<Node> nodes = new ArrayList<>(nodeSet);
		logger.debug("number of internal nodes: " + nodes.size());
		
		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String id1 = null, id2 = null;
		
		for (int i = 0; i < nodes.size(); i++) {
			
			Node n1 = nodes.get(i);
			for (int j = i+1; j < nodes.size(); j++) {

				Node n2 = nodes.get(j);

				if (n1.equals(n2))
					continue;

				if (this.visitedSourceTargetPairs.contains(n1.getId() + n2.getId()))
					continue;
				if (this.visitedSourceTargetPairs.contains(n2.getId() + n1.getId()))
					continue;
				
				source = n1;
				target = n2;

				sourceUri = source.getUri();
				targetUri = target.getUri();

				id1 = LinkIdFactory.getLinkId(Uris.DEFAULT_LINK_URI, source.getId(), target.getId());
				id2 = LinkIdFactory.getLinkId(Uris.DEFAULT_LINK_URI, target.getId(), source.getId());
				CompactLink link = null; 

//				boolean connected = false;
				boolean sourceConnectedToTarget = false;
				boolean targetConnectedToSource = false;
				
				// order of adding the links is based on the ascending sort of their weight value
				

				if (modelingConfiguration.getPropertiesDirect()) {
					if (this.ontologyManager.isConnectedByDirectProperty(sourceUri, targetUri)) {
						logger.debug( sourceUri + " and " + targetUri + " are connected by a direct object property.");
						link = new CompactObjectPropertyLink(id1, ObjectPropertyType.Direct);
						addLink(source, target, link);
						sourceConnectedToTarget = true;
					}
					if (this.ontologyManager.isConnectedByDirectProperty(targetUri, sourceUri)) {
						logger.debug( targetUri + " and " + sourceUri + " are connected by a direct object property.");
						link = new CompactObjectPropertyLink(id2, ObjectPropertyType.Direct);
						addLink(target, source, link);
						targetConnectedToSource = true;
					}				
				}
				

				if (modelingConfiguration.getPropertiesIndirect()) {
					if (!sourceConnectedToTarget && this.ontologyManager.isConnectedByIndirectProperty(sourceUri, targetUri)) { 
						logger.debug( sourceUri + " and " + targetUri + " are connected by an indirect object property.");
						link = new CompactObjectPropertyLink(id1, ObjectPropertyType.Indirect);
						addLink(source, target, link);
						sourceConnectedToTarget = true;
					}
					if (!targetConnectedToSource && this.ontologyManager.isConnectedByIndirectProperty(targetUri, sourceUri)) { 
						logger.debug( targetUri + " and " + sourceUri + " are connected by an indirect object property.");
						link = new CompactObjectPropertyLink(id2, ObjectPropertyType.Indirect);
						addLink(target, source, link);
						targetConnectedToSource = true;
					}
				}
				

				if (modelingConfiguration.getPropertiesWithOnlyRange()) {
					if (!sourceConnectedToTarget && this.ontologyManager.isConnectedByDomainlessProperty(sourceUri, targetUri)) { 
						logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose range is " + sourceUri + " or " + targetUri);
						link = new CompactObjectPropertyLink(id1, ObjectPropertyType.WithOnlyRange);
						addLink(source, target, link);
						sourceConnectedToTarget = true;
					}

					if (!targetConnectedToSource && this.ontologyManager.isConnectedByDomainlessProperty(targetUri, sourceUri)) { 
						logger.debug( targetUri + " and " + sourceUri + " are connected by an object property whose range is " + targetUri + " or " + sourceUri);
						link = new CompactObjectPropertyLink(id2, ObjectPropertyType.WithOnlyRange);
						addLink(target, source, link);
						targetConnectedToSource = true;
					}
				}
				
				if (modelingConfiguration.getPropertiesWithOnlyDomain()) {
					if (!sourceConnectedToTarget && this.ontologyManager.isConnectedByRangelessProperty(sourceUri, targetUri)) { 
						logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose domain is " + sourceUri + " or " + targetUri);
						link = new CompactObjectPropertyLink(id1, ObjectPropertyType.WithOnlyDomain);
						addLink(source, target, link);
						sourceConnectedToTarget = true;
					}
					if (!targetConnectedToSource && this.ontologyManager.isConnectedByRangelessProperty(targetUri, sourceUri)) { 
						logger.debug( targetUri + " and " + sourceUri + " are connected by an object property whose domain is " + targetUri + " or " + sourceUri);
						link = new CompactObjectPropertyLink(id2, ObjectPropertyType.WithOnlyDomain);
						addLink(target, source, link);
						targetConnectedToSource = true;
					}
				}
				
				if (modelingConfiguration.getPropertiesWithoutDomainRange() && !sourceConnectedToTarget && !targetConnectedToSource) {
					if (this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(sourceUri, targetUri)) {// ||
	//						this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(targetUri, sourceUri)) { 
						link = new CompactObjectPropertyLink(id1, ObjectPropertyType.WithoutDomainAndRange);
						addLink(source, target, link);
						link = new CompactObjectPropertyLink(id2, ObjectPropertyType.WithoutDomainAndRange);
						addLink(target, source, link);
						sourceConnectedToTarget = true;
						targetConnectedToSource = true;
					}
				}

				if (!sourceConnectedToTarget && modelingConfiguration.getPropertiesSubClass()) {
					if (this.ontologyManager.isSubClass(sourceUri, targetUri, false)) {
						logger.debug( sourceUri + " and " + targetUri + " are connected by a subClassOf relation.");
						link = new CompactSubClassLink(id1);
						addLink(source, target, link);
						sourceConnectedToTarget = true;
					}
					if (!targetConnectedToSource && this.ontologyManager.isSubClass(targetUri, sourceUri, false)) {
						logger.debug( targetUri + " and " + sourceUri + " are connected by a subClassOf relation.");
						link = new CompactSubClassLink(id2);
						addLink(target, source, link);
						targetConnectedToSource = true;
					}
				}
				
				if (!sourceConnectedToTarget && !targetConnectedToSource) {
					this.visitedSourceTargetPairs.add(n1.getId() + n2.getId());
					logger.debug("did not put a link between (" + n1.getId() + ", " + n2.getId() + ")");
				}
			}
		}

		logger.debug("exit>");
	}
	
	public List<LabeledLink> getPossibleLinks(String sourceId, String targetId) {
		return getPossibleLinks(sourceId, targetId, null, null);
	}
	
	public List<LabeledLink> getPossibleLinks(String sourceId, String targetId, LinkType linkType, 
			ObjectPropertyType objectProertyType) {
		
		List<LabeledLink> sortedLinks = new ArrayList<>();

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
		sourceUri = source.getUri();
		targetUri = target.getUri();

		HashSet<String> links = 
				this.ontologyManager.getPossibleUris(sourceUri, targetUri);

		String id;
		Label label;
		ObjectPropertyType linkObjectPropertyType; 

		for (String uri : links) {
			
			if (linkType == LinkType.SubClassLink && !uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				continue;
			
			if (linkType == LinkType.ObjectPropertyLink && uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				continue;
			
			linkObjectPropertyType = ontologyManager.getObjectPropertyType(sourceUri, targetUri, uri);
			if (linkType == LinkType.ObjectPropertyLink && 
					objectProertyType != null && 
					objectProertyType != ObjectPropertyType.None &&
					objectProertyType != linkObjectPropertyType)
				continue;
			
			id = LinkIdFactory.getLinkId(uri, sourceId, targetId);
			label = new Label(ontologyManager.getUriLabel(uri));
			
			LabeledLink newLink;
			if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				newLink = new SubClassLink(id);
			else
				newLink = new ObjectPropertyLink(id, label, linkObjectPropertyType);
			
			sortedLinks.add(newLink);
		}
		
		Collections.sort(sortedLinks, new LinkPriorityComparator());
		
		return sortedLinks;
	}
	
	public List<LabeledLink> getLinks(String sourceId, String targetId) {
		
		List<LabeledLink> links  = new LinkedList<>();

		Node source = this.getIdToNodeMap().get(sourceId);
		if (source == null) return links;

		Node target = this.getIdToNodeMap().get(targetId);
		if (target == null) return links;

		Set<DefaultLink> allLinks = this.getGraph().getAllEdges(source, target);
		if (allLinks != null) {
			for (DefaultLink l : allLinks) {
				if (l instanceof LabeledLink) {
					links.add((LabeledLink)l);
				}
			}
		}
		
		return links;
	}

	public List<LabeledLink> getIncomingLinks(String nodeId) {
		
		List<LabeledLink> incomingLinks  = new LinkedList<>();

		Node node = this.getIdToNodeMap().get(nodeId);
		if (node == null) return incomingLinks;
		
		Set<DefaultLink> allIncomingLinks = this.getGraph().incomingEdgesOf(node);
		if (allIncomingLinks != null) {
			for (DefaultLink l : allIncomingLinks) {
				if (l instanceof LabeledLink) {
					incomingLinks.add((LabeledLink)l);
				}
			}
		}
		
		return incomingLinks;
	}
	
	public List<LabeledLink> getOutgoingLinks(String nodeId) {
		
		List<LabeledLink> outgoingLinks  = new LinkedList<>();

		Node node = this.getIdToNodeMap().get(nodeId);
		if (node == null) return outgoingLinks;
		
		Set<DefaultLink> allOutgoingLinks = this.getGraph().outgoingEdgesOf(node);
		if (allOutgoingLinks != null) {
			for (DefaultLink l : allOutgoingLinks) {
				if (l instanceof LabeledLink) {
					outgoingLinks.add((LabeledLink)l);
				}
			}
		}
		
		return outgoingLinks;
	}
	
	public static void main(String[] args) throws Exception {
		
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		
		logger.info(Integer.class.getFields()[0].getName());
		
		/** Check if any ontology needs to be preloaded **/
		File ontDir = new File(Params.ONTOLOGY_DIR);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			OntologyManager mgr = new OntologyManager(contextParameters.getId());
			for (File ontology: ontologies) {
				System.out.println(ontology.getName());
				if (ontology.getName().endsWith(".owl") || 
						ontology.getName().endsWith(".rdf") || 
						ontology.getName().endsWith(".n3") || 
						ontology.getName().endsWith(".ttl") || 
						ontology.getName().endsWith(".xml")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						mgr.doImport(ontology, encoding);
					} catch (Exception t) {
//						logger.error("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				} else {
					logger.error ("the file: " + ontology.getAbsolutePath() + " does not have proper format: xml/rdf/n3/ttl/owl");
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
			Set<String> test = mgr.getPossibleUris("http://example.com/layout/C01_", "http://example.com/layout/C02_");
			for (String s : test) {
				System.out.println(s);
			}
			Alignment al = new Alignment(mgr);
			ColumnNode c1 = al.addColumnNode("h1", "c1", null, null);
			ColumnNode c2 = al.addColumnNode("h2", "c2", null, null);
			InternalNode n1 = al.addInternalNode(new Label("http://example.com/layout/C01_"));
			InternalNode n2 = al.addInternalNode(new Label("http://example.com/layout/C02_"));
			al.addDataPropertyLink(n1, c1, new Label("http://example.com/layout/d1"), false);
			al.addDataPropertyLink(n2, c2, new Label("http://example.com/layout/d2"), false);
			al.align();
			System.out.println(GraphUtil.labeledGraphToString(al.getSteinerTree()));
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}
				
//		DirectedWeightedMultigraph<Node, DefaultLink> g = new 
//				DirectedWeightedMultigraph<Node, DefaultLink>(DefaultLink.class);
//		
//		Node n1 = new InternalNode("n1", null);
//		Node n2 = new InternalNode("n2", null);
//		Node n3 = new InternalNode("n3", null);
//		Node n4 = new InternalNode("n4", null);
//		Node n8 = new ColumnNode("n8", "h1", "B", null);
//		Node n9 = new ColumnNode("n9", "h2", "B", null);
//		
//		DefaultLink l1 = new ObjectPropertyLink("e1", null, ObjectPropertyType.None);
//		DefaultLink l2 = new ObjectPropertyLink("e2", null, ObjectPropertyType.None);
//		DefaultLink l3 = new ObjectPropertyLink("e3", null, ObjectPropertyType.None);
//		DefaultLink l4 = new ObjectPropertyLink("e4", null, ObjectPropertyType.None);
//		DefaultLink l5 = new ObjectPropertyLink("e5", null, ObjectPropertyType.None);
//		DefaultLink l6 = new ObjectPropertyLink("e6", null, ObjectPropertyType.None);
////		Link l7 = new ObjectPropertyLink("e7", null);
////		Link l8 = new DataPropertyLink("e8", null);
////		Link l9 = new DataPropertyLink("e9", null);
//		
//		g.addVertex(n1);
//		g.addVertex(n2);
//		g.addVertex(n3);
//		g.addVertex(n4);
//		g.addVertex(n8);
//		g.addVertex(n9);
//		
//		g.addEdge(n1, n2, l1);
//		g.addEdge(n1, n3, l2);
//		g.addEdge(n2, n3, l6);
//		g.addEdge(n2, n4, l3);
//		g.addEdge(n4, n8, l4);
//		g.addEdge(n3, n9, l5);
//		
//		GraphUtil.printGraph(g);
//		
//		DisplayModel dm = new DisplayModel(GraphUtil.asLabeledGraph(g));
//		HashMap<Node, Integer> nodeLevels = dm.getNodesLevel();
//		for (Node n : g.vertexSet())
//			logger.info(n.getId() + " --- " + nodeLevels.get(n));
//		
//		HashMap<Node, Set<ColumnNode>> coveredColumnNodes = dm.getNodesSpan();
//		
//		logger.info("Internal Nodes Coverage ...");
//		for (Entry<Node, Set<ColumnNode>> entry : coveredColumnNodes.entrySet()) {
//			logger.info(entry.getKey().getId());
//			for (Node n : entry.getValue())
//				logger.info("-----" + n.getId());
//		}
		
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