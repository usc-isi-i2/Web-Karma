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

import edu.isi.karma.modeling.*;
import edu.isi.karma.modeling.alignment.learner.SemanticTypeMapping;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.util.EncodingDetector;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class GraphBuilder
{

	static Logger logger = LoggerFactory.getLogger(GraphBuilder.class);

	protected DirectedWeightedMultigraph<Node, Link> graph;
	protected OntologyManager ontologyManager;

	private NodeIdFactory nodeIdFactory;

	private HashSet<String> visitedSourceTargetPairs;
	private HashSet<String> sourceToTargetLinkUris;
	private HashSet<String> sourceToTargetConnectivity;

	// HashMaps

	private HashMap<String, Node> idToNodeMap;
	private HashMap<String, Link> idToLinkMap;

	private HashMap<String, Set<Node>> uriToNodesMap;
	private HashMap<String, Set<Link>> uriToLinksMap;

	private HashMap<NodeType, Set<Node>> typeToNodesMap;
	private HashMap<LinkType, Set<Link>> typeToLinksMap;

	private HashMap<LinkStatus, Set<Link>> statusToLinksMap;

	private HashMap<String, Set<String>> uriClosure;


	// To be used in matching semantic types with graph nodes
	private HashSet<String> modelIds;
	private HashMap<String, Integer> linkCountMap;
	private HashMap<String, Integer> nodeDataPropertyCount; // nodeId + dataPropertyUri --> count
	private HashMap<String, Set<SemanticTypeMapping>> semanticTypeMatches; // nodeUri + dataPropertyUri --> SemanticType Mapping
	private int numberOfModelLinks = 0;

	// Constructor

	public GraphBuilder(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, boolean addThingNode)
	{ //, LinkIdFactory linkIdFactory) {

		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = nodeIdFactory;

		this.idToNodeMap = new HashMap<String, Node>();
		this.idToLinkMap = new HashMap<String, Link>();
		this.uriToNodesMap = new HashMap<String, Set<Node>>();
		this.uriToLinksMap = new HashMap<String, Set<Link>>();
		this.typeToNodesMap = new HashMap<NodeType, Set<Node>>();
		this.typeToLinksMap = new HashMap<LinkType, Set<Link>>();
		this.statusToLinksMap = new HashMap<LinkStatus, Set<Link>>();

		this.uriClosure = new HashMap<String, Set<String>>();

		this.graph = new DirectedWeightedMultigraph<Node, Link>(Link.class);

		this.visitedSourceTargetPairs = new HashSet<String>();
		this.sourceToTargetLinkUris = new HashSet<String>();
		this.sourceToTargetConnectivity = new HashSet<String>();

		this.modelIds = new HashSet<String>();
		this.linkCountMap = new HashMap<String, Integer>();
		this.nodeDataPropertyCount = new HashMap<String, Integer>();
		this.semanticTypeMatches = new HashMap<String, Set<SemanticTypeMapping>>();

		if (addThingNode)
			this.initialGraph();
	}

	public GraphBuilder(OntologyManager ontologyManager, DirectedWeightedMultigraph<Node, Link> graph)
	{

		this(ontologyManager, new NodeIdFactory(), false);
		if (graph == null)
			return;

		for (Node node : graph.vertexSet())
		{

			this.addNode(node);
			// building NodeIdFactory
			if (node.getLabel() != null)
			{
				nodeIdFactory.getNodeId(node.getLabel().getUri());
			}
		}

		Node source;
		Node target;

		for (Link link : graph.edgeSet())
		{

			source = link.getSource();
			target = link.getTarget();

			double w = link.getWeight();
			if (this.addLink(source, target, link))
				changeLinkWeight(link, w);
		}

		logger.debug("graph has been loaded.");
	}

	public NodeIdFactory getNodeIdFactory()
	{
		return nodeIdFactory;
	}

	public boolean isConnected(String nodeId1, String nodeId2)
	{
		return this.sourceToTargetConnectivity.contains(nodeId1 + nodeId2);
	}

	public OntologyManager getOntologyManager()
	{
		return this.ontologyManager;
	}

	public DirectedWeightedMultigraph<Node, Link> getGraph()
	{
		return this.graph;
	}

	public void setGraph(DirectedWeightedMultigraph<Node, Link> graph)
	{
		this.graph = graph;
	}

	public HashMap<String, Node> getIdToNodeMap()
	{
		return idToNodeMap;
	}

	public HashMap<String, Link> getIdToLinkMap()
	{
		return idToLinkMap;
	}

	public HashMap<String, Set<Node>> getUriToNodesMap()
	{
		return uriToNodesMap;
	}

	public HashMap<String, Set<Link>> getUriToLinksMap()
	{
		return uriToLinksMap;
	}

	public HashMap<NodeType, Set<Node>> getTypeToNodesMap()
	{
		return typeToNodesMap;
	}

	public HashMap<LinkType, Set<Link>> getTypeToLinksMap()
	{
		return typeToLinksMap;
	}

	public HashMap<LinkStatus, Set<Link>> getStatusToLinksMap()
	{
		return statusToLinksMap;
	}

	public Set<String> getModelIds()
	{
		return Collections.unmodifiableSet(modelIds);
	}

	public HashMap<String, Integer> getLinkCountMap()
	{
		return linkCountMap;
	}

	public HashMap<String, Integer> getNodeDataPropertyCount()
	{
		return nodeDataPropertyCount;
	}

	public HashMap<String, Set<SemanticTypeMapping>> getSemanticTypeMatches()
	{
		return semanticTypeMatches;
	}

	public int getNumberOfModelLinks() {
		return numberOfModelLinks;
	}

	public void resetOntologyMaps() {
		String[] currentUris = this.uriClosure.keySet().toArray(new String[0]);
		this.uriClosure.clear();
		for (String uri : currentUris)
		{
			computeUriClosure(uri);
		}
	}

	public boolean addNodeAndUpdate(Node node)
	{
		if (ModelingConfiguration.getManualAlignment())
		{
			return addNode(node);
		} else
			return addNodeAndUpdate(node, null);
	}

	public boolean addNodeAndUpdate(Node node, Set<Node> addedNodes)
	{

		logger.debug("<enter");
		if (addedNodes == null)
			addedNodes = new HashSet<Node>();

		if (!addNode(node))
			return false;

		if (node instanceof InternalNode)
		{

			long start = System.currentTimeMillis();
			float elapsedTimeSec;

			addedNodes.add(node);

			if (ModelingConfiguration.getNodeClosure())
			{
				addNodeClosure(node, addedNodes);
			}

			long addNodesClosure = System.currentTimeMillis();
			elapsedTimeSec = (addNodesClosure - start) / 1000F;
			logger.debug("time to add nodes closure: " + elapsedTimeSec);

			updateLinks();

			// if we consider the set of current nodes as S1 and the set of new added nodes as S2:
			// (*) the direction of all the subclass links between S1 and S2 is from S2 to S1
			//		This is because superclasses of all the nodes in S1 are already added to the graph. 
			// (*) the direction of all the object property links between S1 and S2 is from S1 to S2
			//		This is because all the nodes that are reachable from S1 are added to the graph before adding new nodes in S2.
			long updateLinks = System.currentTimeMillis();
			elapsedTimeSec = (updateLinks - addNodesClosure) / 1000F;
			logger.debug("time to update links of the graph: " + elapsedTimeSec);

//			updateLinksFromThing();

			long updateLinksFromThing = System.currentTimeMillis();
			elapsedTimeSec = (updateLinksFromThing - updateLinks) / 1000F;
			logger.debug("time to update links to Thing (root): " + elapsedTimeSec);

			logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
			logger.debug("total number of links in graph: " + this.graph.edgeSet().size());
		}

		logger.debug("exit>");
		return true;
	}


	public boolean addNode(Node node)
	{

		logger.debug("<enter");

		if (node == null)
		{
			logger.error("The node is null.");
			return false;
		}

		if (idToNodeMap.get(node.getId()) != null)
		{
			logger.error("The node with id=" + node.getId() + " already exists in the graph.");
			return false;
		}

		if (node instanceof InternalNode)
		{
			String uri = node.getLabel().getUri();
			Label label = this.ontologyManager.getUriLabel(uri);
			if (label == null)
			{
				logger.error("The resource " + uri + " does not exist in the ontology.");
				return false;
			}
			node.getLabel().setNs(label.getNs());
			node.getLabel().setPrefix(label.getPrefix());
		}

		this.graph.addVertex(node);

		this.idToNodeMap.put(node.getId(), node);

		Set<Node> nodesWithSameUri = uriToNodesMap.get(node.getLabel().getUri());
		if (nodesWithSameUri == null)
		{
			nodesWithSameUri = new HashSet<Node>();
			uriToNodesMap.put(node.getLabel().getUri(), nodesWithSameUri);
		}
		nodesWithSameUri.add(node);

		Set<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType == null)
		{
			nodesWithSameType = new HashSet<Node>();
			typeToNodesMap.put(node.getType(), nodesWithSameType);
		}
		nodesWithSameType.add(node);

		if (node.getModelIds() != null)
			this.modelIds.addAll(node.getModelIds());

		this.uriClosure.put(node.getLabel().getUri(), null);

		logger.debug("exit>");
		return true;
	}

	public boolean addLink(Node source, Node target, Link link)
	{

		logger.debug("<enter");

		if (source == null || target == null || link == null)
		{
			logger.error("Source, target, and the link cannot be null.");
			return false;
		}

		if (this.idToLinkMap.containsKey(link.getId()))
		{
			logger.error("The link with id=" + link.getId() + " already exists in the graph");
			return false;
		}

		if (!this.idToNodeMap.containsKey(source.getId()))
		{
			logger.error("The link source " + link.getSource().getId() + " does not exist in the graph");
			return false;
		}

		if (!this.idToNodeMap.containsKey(target.getId()))
		{
			logger.error("The link target " + link.getTarget().getId() + " does not exist in the graph");
			return false;
		}

		String key = source.getId() + target.getId() + link.getLabel().getUri();
		// check to see if the link is duplicate or not
		if (sourceToTargetLinkUris.contains(key))
		{
			logger.error("There is already a link with label " + link.getLabel().getUri() +
					             " from " + source.getId() + " to " + target.getId());
			return false;
		}

		String uri = link.getLabel().getUri();
		Label label = this.ontologyManager.getUriLabel(uri);
		if (label == null)
		{
			logger.error("The resource " + uri + " does not exist in the ontology.");
			return false;
		}
		link.getLabel().setNs(label.getNs());
		link.getLabel().setPrefix(label.getPrefix());

		this.graph.addEdge(source, target, link);

		this.sourceToTargetConnectivity.add(source.getId() + target.getId());
		this.sourceToTargetConnectivity.add(target.getId() + source.getId());

		this.visitedSourceTargetPairs.add(source.getId() + target.getId());
		this.visitedSourceTargetPairs.add(target.getId() + source.getId());

		double w = 0.0;
		if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink) link).getObjectPropertyType() == ObjectPropertyType.Direct)
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink) link).getObjectPropertyType() == ObjectPropertyType.Indirect)
			w = ModelingParams.PROPERTY_INDIRECT_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink) link).getObjectPropertyType() == ObjectPropertyType.WithOnlyDomain)
			w = ModelingParams.PROPERTY_WITH_ONLY_DOMAIN_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink) link).getObjectPropertyType() == ObjectPropertyType.WithOnlyRange)
			w = ModelingParams.PROPERTY_WITH_ONLY_RANGE_WEIGHT;
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink) link).getObjectPropertyType() == ObjectPropertyType.WithoutDomainAndRange)
			w = ModelingParams.PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT;
		else if (link instanceof SubClassLink)
			w = ModelingParams.SUBCLASS_WEIGHT;
		else
			w = ModelingParams.PROPERTY_DIRECT_WEIGHT;

		this.graph.setEdgeWeight(link, w);

		// update the corresponding lists and hashmaps

		this.idToLinkMap.put(link.getId(), link);

		Set<Link> linksWithSameUri = uriToLinksMap.get(link.getLabel().getUri());
		if (linksWithSameUri == null)
		{
			linksWithSameUri = new HashSet<Link>();
			uriToLinksMap.put(link.getLabel().getUri(), linksWithSameUri);
		}
		linksWithSameUri.add(link);

		Set<Link> linksWithSameType = typeToLinksMap.get(link.getType());
		if (linksWithSameType == null)
		{
			linksWithSameType = new HashSet<Link>();
			typeToLinksMap.put(link.getType(), linksWithSameType);
		}
		linksWithSameType.add(link);

		if (link.getStatus() != LinkStatus.Normal)
		{
			Set<Link> linksWithSameStatus = statusToLinksMap.get(link.getStatus());
			if (linksWithSameStatus == null)
			{
				linksWithSameStatus = new HashSet<Link>();
				statusToLinksMap.put(link.getStatus(), linksWithSameStatus);
			}
		}

		if (source instanceof InternalNode && target instanceof ColumnNode)
		{

			key = source.getId() + link.getLabel().getUri();
			Integer count = this.nodeDataPropertyCount.get(key);
			if (count == null)
				this.nodeDataPropertyCount.put(key, 1);
			else
				this.nodeDataPropertyCount.put(key, count.intValue() + 1);

			key = source.getLabel().getUri() + link.getLabel().getUri();
			Set<SemanticTypeMapping> SemanticTypeMappings = this.semanticTypeMatches.get(key);
			if (SemanticTypeMappings == null)
			{
				SemanticTypeMappings = new HashSet<SemanticTypeMapping>();
				this.semanticTypeMatches.put(key, SemanticTypeMappings);
			}
			SemanticTypeMappings.add(new SemanticTypeMapping(null, null, (InternalNode) source, link, (ColumnNode) target));
		}

		this.sourceToTargetLinkUris.add(key);
		
		if (link.getModelIds() != null) {
			this.modelIds.addAll(link.getModelIds());
			this.numberOfModelLinks++;
		}
		
				this.updateLinkCountMap(link);

		logger.debug("exit>");
		return true;
	}

	public void changeLinkStatus(Link link, LinkStatus newStatus)
	{

		LinkStatus oldStatus = link.getStatus();
		if (newStatus == oldStatus)
			return;

		link.setStatus(newStatus);

		Set<Link> linksWithOldStatus = this.statusToLinksMap.get(oldStatus);
		if (linksWithOldStatus != null)
			linksWithOldStatus.remove(link);

		if (newStatus == LinkStatus.Normal) // we don't need to index normal links 
			return;

		Set<Link> linksWithNewStatus = this.statusToLinksMap.get(newStatus);
		if (linksWithNewStatus == null)
		{
			linksWithNewStatus = new HashSet<Link>();
			statusToLinksMap.put(newStatus, linksWithNewStatus);
		}
		linksWithNewStatus.add(link);
	}

	public void changeLinkWeight(Link link, double weight)
	{
		this.graph.setEdgeWeight(link, weight);
	}

	public boolean removeLink(Link link)
	{

		if (link == null)
		{
			logger.debug("The link is null.");
			return false;
		}

		if (idToLinkMap.get(link.getId()) == null)
		{
			logger.debug("The link with id=" + link.getId() + " does not exists in the graph.");
			return false;
		}

		if (link.getLabel().getUri().equalsIgnoreCase(Uris.PLAIN_LINK_URI))
			return false;

		logger.debug("removing the link " + link.getId() + "...");

		if (!this.graph.removeEdge(link))
			return false;

		// update hashmaps

		this.idToLinkMap.remove(link.getId());

		Set<Link> linksWithSameUri = uriToLinksMap.get(link.getLabel().getUri());
		if (linksWithSameUri != null)
			linksWithSameUri.remove(link);

		Set<Link> linksWithSameType = typeToLinksMap.get(link.getType());
		if (linksWithSameType != null)
			linksWithSameType.remove(link);

		Set<Link> linksWithSameStatus = statusToLinksMap.get(link.getStatus());
		if (linksWithSameStatus != null)
			linksWithSameStatus.remove(link);

		this.sourceToTargetLinkUris.remove(link.getSource().getId() +
				                                   link.getTarget().getId() +
				                                   link.getLabel().getUri());

		return true;
	}

	public boolean removeNode(Node node)
	{

		if (node == null)
		{
			logger.error("The node is null");
			return false;
		}

		if (idToNodeMap.get(node.getId()) == null)
		{
			logger.error("The node with id=" + node.getId() + " does not exists in the graph.");
			return false;
		}

		logger.debug("removing the node " + node.getId() + "...");

		Set<Link> incomingLinks = this.graph.incomingEdgesOf(node);
		if (incomingLinks != null)
		{
			Link[] incomingLinksArray = incomingLinks.toArray(new Link[0]);
			for (Link inLink : incomingLinksArray)
			{
				this.removeLink(inLink);
			}
		}

		Set<Link> outgoingLinks = this.graph.outgoingEdgesOf(node);
		if (outgoingLinks != null)
		{
			Link[] outgoingLinksArray = outgoingLinks.toArray(new Link[0]);
			for (Link outLink : outgoingLinksArray)
			{
				this.removeLink(outLink);
			}
		}

		if (!this.graph.removeVertex(node))
			return false;

		// updating hashmaps

		this.idToNodeMap.remove(node.getId());

		Set<Node> nodesWithSameUri = uriToNodesMap.get(node.getLabel().getUri());
		if (nodesWithSameUri != null)
			nodesWithSameUri.remove(node);

		Set<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
		if (nodesWithSameType != null)
			nodesWithSameType.remove(node);

		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		return true;
	}

	public void addClosureAndLinksOfNodes(Set<InternalNode> internalNodes, Set<Node> addedNodes)
	{

		logger.debug("<enter");
		if (addedNodes == null)
			addedNodes = new HashSet<Node>();

		long start = System.currentTimeMillis();
		float elapsedTimeSec;

		if (internalNodes != null)
		{
			Node[] nodes = internalNodes.toArray(new Node[0]);
			for (Node node : nodes)
			{
				if (this.idToNodeMap.containsKey(node.getId()))
					addNodeClosure(node, addedNodes);
			}
		}

		long addNodesClosure = System.currentTimeMillis();
		elapsedTimeSec = (addNodesClosure - start) / 1000F;
		logger.debug("time to add nodes closure: " + elapsedTimeSec);

		updateLinks();

		long updateLinks = System.currentTimeMillis();
		elapsedTimeSec = (updateLinks - addNodesClosure) / 1000F;
		logger.debug("time to update links of the graph: " + elapsedTimeSec);

		logger.debug("total number of nodes in graph: " + this.graph.vertexSet().size());
		logger.debug("total number of links in graph: " + this.graph.edgeSet().size());

		logger.debug("exit>");
	}


	public LinkFrequency getMoreFrequentLinkBetweenNodes(String sourceUri, String targetUri)
	{

		List<String> possibleLinksFromSourceToTarget = new ArrayList<String>();

		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange =
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();

		possibleLinksFromSourceToTarget.clear();

		objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
		if (objectPropertiesDirect != null)
			possibleLinksFromSourceToTarget.addAll(objectPropertiesDirect);

		objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
		if (objectPropertiesIndirect != null)
			possibleLinksFromSourceToTarget.addAll(objectPropertiesIndirect);

		objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(sourceUri, targetUri);
		if (objectPropertiesWithOnlyDomain != null)
			possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyDomain);

		objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(sourceUri, targetUri);
		if (objectPropertiesWithOnlyRange != null)
			possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyRange);

		if (ontologyManager.isSubClass(sourceUri, targetUri, true))
			possibleLinksFromSourceToTarget.add(Uris.RDFS_SUBCLASS_URI);

		if (objectPropertiesWithoutDomainAndRange != null)
		{
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

		if (possibleLinksFromSourceToTarget != null && possibleLinksFromSourceToTarget.size() > 0)
		{

			for (String linkUri : possibleLinksFromSourceToTarget)
			{
				key = "domain:" + sourceUri + ",link:" + linkUri + ",range:" + targetUri;
				Integer count1 = this.linkCountMap.get(key);
				if (count1 != null && count1.intValue() > maxCount1)
				{
					maxCount1 = count1.intValue();
					selectedLinkUri1 = linkUri;
				}
			}

			for (String linkUri : possibleLinksFromSourceToTarget)
			{
				key = "range:" + targetUri + ",link:" + linkUri;
				Integer count2 = this.linkCountMap.get(key);
				if (count2 != null && count2.intValue() > maxCount2)
				{
					maxCount2 = count2.intValue();
					selectedLinkUri2 = linkUri;
				}
			}

			for (String linkUri : possibleLinksFromSourceToTarget)
			{
				key = "domain:" + sourceUri + ",link:" + linkUri;
				Integer count3 = this.linkCountMap.get(key);
				if (count3 != null && count3.intValue() > maxCount3)
				{
					maxCount3 = count3.intValue();
					selectedLinkUri3 = linkUri;
				}
			}

			for (String linkUri : possibleLinksFromSourceToTarget)
			{
				key = "link:" + linkUri;
				Integer count4 = this.linkCountMap.get(key);
				if (count4 != null && count4.intValue() > maxCount4)
				{
					maxCount4 = count4.intValue();
					selectedLinkUri4 = linkUri;
				}
			}
		} else
		{
			logger.error("Something is going wrong. There should be at least one possible object property between " +
					             sourceUri + " and " + targetUri);
			return null;
		}

		String selectedLinkUri;
		int maxCount;
		int type;

		if (selectedLinkUri1 != null && selectedLinkUri1.trim().length() > 0)
		{
			selectedLinkUri = selectedLinkUri1;
			maxCount = maxCount1;
			type = 1; // match domain and link and range
		} else if (selectedLinkUri2 != null && selectedLinkUri2.trim().length() > 0)
		{
			selectedLinkUri = selectedLinkUri2;
			maxCount = maxCount2;
			type = 2; // match link and range
		} else if (selectedLinkUri3 != null && selectedLinkUri3.trim().length() > 0)
		{
			selectedLinkUri = selectedLinkUri3;
			maxCount = maxCount3;
			type = 3; // match domain and link
		} else if (selectedLinkUri4 != null && selectedLinkUri4.trim().length() > 0)
		{
			selectedLinkUri = selectedLinkUri4;
			maxCount = maxCount4;
			type = 4; // match link label
		} else
		{
			if (objectPropertiesDirect != null && objectPropertiesDirect.size() > 0)
			{
				selectedLinkUri = objectPropertiesDirect.iterator().next();
				type = 5;
			} else if (objectPropertiesIndirect != null && objectPropertiesIndirect.size() > 0)
			{
				selectedLinkUri = objectPropertiesIndirect.iterator().next();
				type = 6;
			} else if (objectPropertiesWithOnlyDomain != null && objectPropertiesWithOnlyDomain.size() > 0)
			{
				selectedLinkUri = objectPropertiesWithOnlyDomain.iterator().next();
				type = 7;
			} else if (objectPropertiesWithOnlyRange != null && objectPropertiesWithOnlyRange.size() > 0)
			{
				selectedLinkUri = objectPropertiesWithOnlyRange.iterator().next();
				;
				type = 8;
			} else if (ontologyManager.isSubClass(sourceUri, targetUri, true))
			{
				selectedLinkUri = Uris.RDFS_SUBCLASS_URI;
				type = 9;
			} else
			{    // if (objectPropertiesWithoutDomainAndRange != null && objectPropertiesWithoutDomainAndRange.keySet().size() > 0) {
				selectedLinkUri = new ArrayList<String>(objectPropertiesWithoutDomainAndRange.keySet()).get(0);
				type = 10;
			}

			maxCount = 0;
		}

		LinkFrequency lf = new LinkFrequency(selectedLinkUri, type, maxCount);

		return lf;
	}

	// Private Methods

	private void initialGraph()
	{

		logger.debug("<enter");

		// Add Thing to the Graph 
		if (!ModelingConfiguration.getManualAlignment())
		{
			String id = nodeIdFactory.getNodeId(Uris.THING_URI);
			Label label = new Label(Uris.THING_URI, Namespaces.OWL, Prefixes.OWL);
			Node thing = new InternalNode(id, label);
			addNode(thing);
		}

		logger.debug("exit>");
	}

	private void updateLinkCountMap(Link link)
	{

		String key, sourceUri, targetUri, linkUri;
		Integer count;
		Node source, target;

		source = link.getSource();
		target = link.getTarget();

		sourceUri = source.getLabel().getUri();
		targetUri = target.getLabel().getUri();
		linkUri = link.getLabel().getUri();

//		if (link instanceof DataPropertyLink) return;

		if (target instanceof InternalNode)
		{
			key = "domain:" + sourceUri + ",link:" + linkUri + ",range:" + targetUri;
			count = this.linkCountMap.get(key);
			if (count == null)
				this.linkCountMap.put(key, 1);
			else
				this.linkCountMap.put(key, count.intValue() + 1);

			key = "range:" + targetUri + ",link:" + linkUri;
			count = this.linkCountMap.get(key);
			if (count == null)
				this.linkCountMap.put(key, 1);
			else
				this.linkCountMap.put(key, count.intValue() + 1);
		}

		key = "domain:" + sourceUri + ",link:" + linkUri;
		count = this.linkCountMap.get(key);
		if (count == null)
			this.linkCountMap.put(key, 1);
		else
			this.linkCountMap.put(key, count.intValue() + 1);

		key = "link:" + linkUri;
		count = this.linkCountMap.get(key);
		if (count == null)
			this.linkCountMap.put(key, 1);
		else
			this.linkCountMap.put(key, count.intValue() + 1);
	}

	private HashSet<String> getUriDirectConnections(String uri)
	{

		HashSet<String> uriDirectConnections = new HashSet<String>();

		HashSet<String> opDomainClasses = null;
//		HashMap<String, Label> superClasses = null;

		// We don't need to add subclasses of each class separately.
		// The only place in which we add children is where we are looking for domain class of a property.
		// In this case, in addition to the domain class, we add all of its children too.

		opDomainClasses = ontologyManager.getDomainsGivenRange(uri, true);
//		superClasses = ontologyManager.getSuperClasses(uri, false);

		if (opDomainClasses != null)
			uriDirectConnections.addAll(opDomainClasses);
//		if (superClasses != null)
//			uriDirectConnections.addAll(superClasses.keySet());

		return uriDirectConnections;
	}

	private Set<String> computeUriClosure(String uri)
	{

		Set<String> closure = this.uriClosure.get(uri);
		if (closure != null)
			return closure;

		closure = new HashSet<String>();
		List<String> closedList = new ArrayList<String>();
		HashMap<String, Set<String>> dependentUrisMap = new HashMap<String, Set<String>>();
		computeUriClosureRecursive(uri, closure, closedList, dependentUrisMap);
		if (closedList.contains(uri) && !closure.contains(uri))
			closure.add(uri);

		int count = 1;
		while (count != 0)
		{
			count = 0;
			for (String s : dependentUrisMap.keySet())
			{
				Set<String> temp = this.uriClosure.get(s);
				Set<String> dependentUris = dependentUrisMap.get(s);
				for (String ss : dependentUris)
				{
					if (!temp.contains(ss))
					{
						temp.add(ss);
						count++;
					}
					if (this.uriClosure.get(ss) != null)
					{
						String[] cc = this.uriClosure.get(ss).toArray(new String[0]);
						for (String c : cc)
						{
							if (!temp.contains(c))
							{
								temp.add(c);
								count++;
							}
						}
					}
				}
			}
		}

		return closure;
	}

	private void computeUriClosureRecursive(String uri, Set<String> closure,
	                                        List<String> closedList, HashMap<String, Set<String>> dependentUrisMap)
	{

		logger.debug("<enter");

		closedList.add(uri);
		Set<String> currentClosure = this.uriClosure.get(uri);
		if (currentClosure != null)
		{
			closure.addAll(currentClosure);
			return;
		}

		HashSet<String> uriDirectConnections = getUriDirectConnections(uri);
		if (uriDirectConnections.size() == 0)
		{
			this.uriClosure.put(uri, new HashSet<String>());
		} else
		{
			for (String c : uriDirectConnections)
			{
				if (closedList.contains(c))
				{
					Set<String> dependentUris = dependentUrisMap.get(uri);
					if (dependentUris == null)
					{
						dependentUris = new HashSet<String>();
						dependentUrisMap.put(uri, dependentUris);
					}
					if (!dependentUris.contains(c))
						dependentUris.add(c);
					continue;
				}
				if (!closure.contains(c))
					closure.add(c);
				if (!closedList.contains(c))
					closedList.add(c);
				Set<String> localClosure = new HashSet<String>();
				computeUriClosureRecursive(c, localClosure, closedList, dependentUrisMap);
				for (String s : localClosure)
				{
					if (!closure.contains(s))
						closure.add(s);
				}
			}
			this.uriClosure.put(uri, closure);
		}

		logger.debug("exit>");
	}

	public List<Node> getNodeClosure(Node node)
	{

		List<Node> nodeClosure = new ArrayList<Node>();
		if (node instanceof ColumnNode)
			return nodeClosure;

		String uri = node.getLabel().getUri();
		Set<String> closure = this.uriClosure.get(uri);
		if (closure == null)
		{  // the closure has already been computed.
			closure = computeUriClosure(uri);
		}
		for (String s : closure)
		{
			Set<Node> nodes = uriToNodesMap.get(s);
			if (nodes != null)
				nodeClosure.addAll(nodes);
		}
		return nodeClosure;
	}

	/**
	 * returns all the new nodes that should be added to the graph due to adding the input node
	 *
	 * @param node
	 *
	 * @return
	 */
	private void addNodeClosure(Node node, Set<Node> newAddedNodes)
	{

		logger.debug("<enter");

		if (newAddedNodes == null)
			newAddedNodes = new HashSet<Node>();

		String uri = node.getLabel().getUri();
		if (this.uriClosure.get(uri) != null) // the closure is already computed and added to the graph.
			return;

		Set<String> uriClosure = computeUriClosure(uri);

		for (String c : uriClosure)
		{
			Set<Node> nodesOfSameUri = this.uriToNodesMap.get(c);
			if (nodesOfSameUri == null || nodesOfSameUri.size() == 0)
			{ // the internal node is not added to the graph before
				Node nn = new InternalNode(nodeIdFactory.getNodeId(c),
				                           ontologyManager.getUriLabel(c));
				if (addNode(nn))
					newAddedNodes.add(nn);
			}
		}

		logger.debug("exit>");
	}

	private void updateLinks()
	{

		logger.debug("<enter");

		Set<Node> nodeSet = this.typeToNodesMap.get(NodeType.InternalNode);
		if (nodeSet == null || nodeSet.isEmpty())
			return;

		List<Node> nodes = new ArrayList<Node>(nodeSet);
		logger.debug("number of internal nodes: " + nodes.size());

		Node source;
		Node target;
		String sourceUri;
		String targetUri;

		String id = null;

		for (int i = 0; i < nodes.size(); i++)
		{

			Node n1 = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++)
			{

				Node n2 = nodes.get(j);

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

				id = LinkIdFactory.getLinkId(Uris.PLAIN_LINK_URI, source.getId(), target.getId());
				Label plainLinkLabel = new Label(Uris.PLAIN_LINK_URI);
				Link link = null;

				boolean connected = false;

				// order of adding the links is based on the ascending sort of their weight value

				if (ModelingConfiguration.getPropertiesDirect())
				{
					if (this.ontologyManager.isConnectedByDirectProperty(sourceUri, targetUri) ||
							this.ontologyManager.isConnectedByDirectProperty(targetUri, sourceUri))
					{
						logger.debug(sourceUri + " and " + targetUri + " are connected by a direct object property.");
						link = new ObjectPropertyLink(id, plainLinkLabel, ObjectPropertyType.Direct);
						addLink(source, target, link);
						connected = true;
					}
				}

//				if (ModelingConfiguration.getPropertiesIndirect() && !connected) {
//					if (this.ontologyManager.isConnectedByIndirectProperty(sourceUri, targetUri) ||
//							this.ontologyManager.isConnectedByIndirectProperty(targetUri, sourceUri)) {
//						logger.debug( sourceUri + " and " + targetUri + " are connected by an indirect object property.");
//						link = new ObjectPropertyLink(id, plainLinkLabel, ObjectPropertyType.Indirect);
//						addLink(source, target, link);
//						connected = true;
//					}
//				}

//				if (ModelingConfiguration.getPropertiesWithOnlyRange() && !connected) {
//					if (this.ontologyManager.isConnectedByDomainlessProperty(sourceUri, targetUri) ||
//							this.ontologyManager.isConnectedByDomainlessProperty(targetUri, sourceUri)) {
//						logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose range is " + sourceUri + " or " + targetUri);
//						link = new ObjectPropertyLink(id, plainLinkLabel, ObjectPropertyType.WithOnlyRange);
//						addLink(source, target, link);
//						connected = true;
//					}
//				}

//				if (ModelingConfiguration.getPropertiesWithOnlyDomain() && !connected) {
//					if (this.ontologyManager.isConnectedByRangelessProperty(sourceUri, targetUri) ||
//							this.ontologyManager.isConnectedByRangelessProperty(targetUri, sourceUri)) {
//						logger.debug( sourceUri + " and " + targetUri + " are connected by an object property whose domain is " + sourceUri + " or " + targetUri);
//						link = new ObjectPropertyLink(id, plainLinkLabel, ObjectPropertyType.WithOnlyDomain);
//						addLink(source, target, link);
//						connected = true;
//					}
//				}

//				if (ModelingConfiguration.getPropertiesSubClass() && !connected) {
//					if (this.ontologyManager.isSubClass(sourceUri, targetUri, false) ||
//							this.ontologyManager.isSubClass(targetUri, sourceUri, false)) {
//						logger.debug( sourceUri + " and " + targetUri + " are connected by a subClassOf relation.");
//						link = new SubClassLink(id);
//						addLink(source, target, link);
//						connected = true;
//					}
//				}
//
//				if (ModelingConfiguration.getPropertiesWithoutDomainRange() && !connected) {
//					if (this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(sourceUri, targetUri)) {// ||
//	//						this.ontologyManager.isConnectedByDomainlessAndRangelessProperty(targetUri, sourceUri)) {
//						link = new ObjectPropertyLink(id, plainLinkLabel, ObjectPropertyType.WithoutDomainAndRange);
//						addLink(source, target, link);
//						connected = true;
//					}
//				}

				if (!connected)
				{
					logger.debug("did not put a link between (" + n1.getId() + ", " + n2.getId() + ")");
				}
			}
		}

		logger.debug("exit>");
	}


	public List<Link> getPossibleLinks(String sourceId, String targetId)
	{
		return getPossibleLinks(sourceId, targetId, null, null);
	}

	public List<Link> getPossibleLinks(String sourceId, String targetId, LinkType linkType,
	                                   ObjectPropertyType objectProertyType)
	{

		List<Link> sortedLinks = new ArrayList<Link>();

		Node source = this.idToNodeMap.get(sourceId);
		Node target = this.idToNodeMap.get(targetId);

		if (source == null || target == null)
		{
			logger.debug("Cannot find source or target in the graph.");
			return sortedLinks;
		}

		if (source instanceof ColumnNode || target instanceof ColumnNode)
		{
			logger.debug("Source or target is a column node.");
			return sortedLinks;
		}

		String sourceUri, targetUri;
		sourceUri = source.getLabel().getUri();
		targetUri = target.getLabel().getUri();

		HashSet<String> links =
				this.ontologyManager.getPossibleUris(sourceUri, targetUri);

		String id;
		Label label;
		ObjectPropertyType linkObjectPropertyType;

		for (String uri : links)
		{

//			if (linkType == LinkType.SubClassLink && !uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
//				continue;
//
//			if (linkType == LinkType.ObjectPropertyLink && uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
//				continue;

			linkObjectPropertyType = ontologyManager.getObjectPropertyType(sourceUri, targetUri, uri);
			if (linkType == LinkType.ObjectPropertyLink &&
					objectProertyType != null &&
					objectProertyType != ObjectPropertyType.None &&
					objectProertyType != linkObjectPropertyType)
				continue;

			id = LinkIdFactory.getLinkId(uri, sourceId, targetId);
			label = new Label(ontologyManager.getUriLabel(uri));

			Link newLink;
			if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				newLink = new SubClassLink(id);
			else
				newLink = new ObjectPropertyLink(id, label, linkObjectPropertyType);

			sortedLinks.add(newLink);
		}

		Collections.sort(sortedLinks, new LinkPriorityComparator());

		return sortedLinks;
	}

	public static void main(String[] args) throws Exception
	{

		logger.info(Integer.class.getFields()[0].getName());

		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = "/Users/mohsen/Documents/Academic/ISI/_GIT/Web-Karma/preloaded-ontologies/";
		File ontDir = new File(preloadedOntDir);
		if (ontDir.exists())
		{
			File[] ontologies = ontDir.listFiles();
			OntologyManager mgr = new OntologyManager();
			for (File ontology : ontologies)
			{
				if (ontology.getName().endsWith(".owl") || ontology.getName().endsWith(".rdf"))
				{
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try
					{
						String encoding = EncodingDetector.detect(ontology);
						mgr.doImport(ontology, encoding);
					} catch (Exception t)
					{
						logger.error("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
		} else
		{
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

		Link l1 = new ObjectPropertyLink("e1", null, ObjectPropertyType.None);
		Link l2 = new ObjectPropertyLink("e2", null, ObjectPropertyType.None);
		Link l3 = new ObjectPropertyLink("e3", null, ObjectPropertyType.None);
		Link l4 = new ObjectPropertyLink("e4", null, ObjectPropertyType.None);
		Link l5 = new ObjectPropertyLink("e5", null, ObjectPropertyType.None);
		Link l6 = new ObjectPropertyLink("e6", null, ObjectPropertyType.None);
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

		DisplayModel dm = new DisplayModel(g);
		HashMap<Node, Integer> nodeLevels = dm.getNodesLevel();
		for (Node n : g.vertexSet())
		{
			logger.info(n.getId() + " --- " + nodeLevels.get(n));
		}

		HashMap<Node, Set<ColumnNode>> coveredColumnNodes = dm.getNodesSpan();

		logger.info("Internal Nodes Coverage ...");
		for (Entry<Node, Set<ColumnNode>> entry : coveredColumnNodes.entrySet())
		{
			logger.info(entry.getKey().getId());
			for (Node n : entry.getValue())
			{
				logger.info("-----" + n.getId());
			}
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
