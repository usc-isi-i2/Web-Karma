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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.learner.ModelLearner;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.ontology.OntologyUpdateListener;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubClassLink;



public class Alignment implements OntologyUpdateListener {

	static Logger logger = LoggerFactory.getLogger(Alignment.class);

	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, LabeledLink> steinerTree = null;
	private Node root = null;
	private NodeIdFactory nodeIdFactory;
	private Set<ColumnNode> sourceColumnNodes;


	public Alignment(OntologyManager ontologyManager) {

		this.ontologyManager = ontologyManager;
		this.ontologyManager.subscribeListener(this);
		this.sourceColumnNodes = new HashSet<ColumnNode>(); 
		if (ModelingConfiguration.isLearnAlignmentEnabled()) {
			this.graphBuilder = 
					ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact).getGraphBuilderClone();
		} else {
			this.graphBuilder =  new GraphBuilder(this.ontologyManager, true);
		}
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		logger.debug("loading learning graph ...");
	}  

	public boolean isEmpty() {
		return (this.graphBuilder.getGraph().edgeSet().size() == 0 || this.steinerTree == null);
	}
	
	public Node GetTreeRoot() {
		return this.root;
	}
	
	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}
	
	
	public Set<ColumnNode> getSourceColumnNodes() {
		return sourceColumnNodes;
	}

	public Alignment getAlignmentClone() {
		Cloner cloner = new Cloner();
//		cloner.setDumpClonedClasses(true);
		cloner.dontClone(OntologyManager.class); 
		cloner.dontCloneInstanceOf(OntologyManager.class); 
		cloner.dontClone(DirectedWeightedMultigraph.class); 
		cloner.dontCloneInstanceOf(DirectedWeightedMultigraph.class); 
		return cloner.deepClone(this);
	}
	
	public DirectedWeightedMultigraph<Node, LabeledLink> getSteinerTree() {
		if (this.steinerTree == null) align();
		// GraphUtil.printGraph(this.steinerTree);
		return this.steinerTree;
	}
	
	public DirectedWeightedMultigraph<Node, DefaultLink> getGraph() {
		return this.graphBuilder.getGraph();
	}
	
	public void setGraph(DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		this.graphBuilder.setGraph(graph);
	}
	
	public Set<Node> getGraphNodes() {
		return this.graphBuilder.getGraph().vertexSet();
	}
	
//	public Set<DefaultLink> getGraphLinks() {
//		return this.graphBuilder.getGraph().edgeSet();
//	}
	
	public Node getNodeById(String nodeId) {
		return this.graphBuilder.getIdToNodeMap().get(nodeId);
	}
	
	public Set<Node> getNodesByUri(String uri) {
		return this.graphBuilder.getUriToNodesMap().get(uri);
	}
	
	public Set<Node> getNodesByType(NodeType type) {
		return this.graphBuilder.getTypeToNodesMap().get(type);
	}
	
	public Set<Node> getForcedNodes() {
		return this.graphBuilder.getForcedNodes();
	}
	
	public LabeledLink getLinkById(String linkId) {
		return this.graphBuilder.getIdToLinkMap().get(linkId);
	}
	
	public Set<LabeledLink> getLinksByUri(String uri) {
		return this.graphBuilder.getUriToLinksMap().get(uri);
	}
	
	public Set<LabeledLink> getLinksByType(LinkType type) {
		return this.graphBuilder.getTypeToLinksMap().get(type);
	}
	
	public Set<LabeledLink> getLinksByStatus(LinkStatus status) {
		return this.graphBuilder.getStatusToLinksMap().get(status);
	}

	public int getLastIndexOfNodeUri(String uri) {
		return this.nodeIdFactory.lastIndexOf(uri);
	}
	
//	public int getLastIndexOfLinkUri(String uri) {
//		return this.linkIdFactory.lastIndexOf(uri);
//	}

	public ColumnNode getColumnNodeByHNodeId(String hNodeId) {

		Node n = this.graphBuilder.getIdToNodeMap().get(hNodeId);
		if (n != null && n instanceof ColumnNode)		
			return (ColumnNode)n;
		else 
			return null;
		
//		List<Node> columnNodes = this.getNodesByType(NodeType.ColumnNode);
//		if (columnNodes == null) return null;
//		for (Node cNode : columnNodes) {
//			if (((ColumnNode)cNode).getHNodeId().equals(hNodeId))
//				return (ColumnNode)cNode;
//		}
//		return null;
	}
	
	// AddNode methods
	
	public ColumnNode addColumnNode(String hNodeId, String columnName, Label rdfLiteralType) {
		
		if(this.getNodeById(hNodeId) != null)
		{
			return null;
		}
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName, rdfLiteralType);
		if (this.graphBuilder.addNodeAndUpdate(node)) {
			this.sourceColumnNodes.add(node);
			return node;
		}
		return null;
	}
	
	public InternalNode addInternalNode(Label label) {
		
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNodeAndUpdate(node)) return node;
		return null;	
	}
	
	public InternalNode addForcedInternalNode(Label label) {
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		node.setForced(true);
		if (this.graphBuilder.addNodeAndUpdate(node)) return node;
		return null;
	}
	
	public LiteralNode addLiteralNode(String value, String type, boolean isUri) {
		
		type = type.replace(Prefixes.XSD + ":", Namespaces.XSD);
		Label literalType = new Label(type, Namespaces.XSD, Prefixes.XSD);
		
		String id = nodeIdFactory.getNodeId(value);
		
		LiteralNode node = new LiteralNode(id, value, literalType, isUri);
		if(this.graphBuilder.addNode(node)) return node;
		return null;
	}
	
	public void updateLiteralNode(String nodeId, String value, String type, boolean isUri) {
		LiteralNode node = (LiteralNode) getNodeById(nodeId);
		type = type.replace(Prefixes.XSD + ":", Namespaces.XSD);
		Label literalType = new Label(type, Namespaces.XSD, Prefixes.XSD);
		
		node.setDatatype(literalType);
		node.setValue(value);
		node.setUri(isUri);
	}
	
	public void deleteForcedInternalNode(String nodeId) {
		Node node = getNodeById(nodeId);
		if(node != null) {
			this.graphBuilder.removeNode(node);
		}
	}
	
	// AddLink methods

	public DataPropertyLink addDataPropertyLink(Node source, Node target, Label label) {
		
		String id = LinkIdFactory.getLinkId(label.getUri(), source.getId(), target.getId());	
		DataPropertyLink link = new DataPropertyLink(id, label);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	

    public ObjectPropertyLink addObjectPropertyLink(Node source, Node target, Label label) {
            
            String id = LinkIdFactory.getLinkId(label.getUri(), source.getId(), target.getId());        
            ObjectPropertyLink link = new ObjectPropertyLink(id, label, 
            		this.graphBuilder.getOntologyManager().getObjectPropertyType(source.getLabel().getUri(), target.getLabel().getUri(), label.getUri()));
            if (this.graphBuilder.addLink(source, target, link)) 
            {
            	return link;
            }
            else if(this.graphBuilder.getIdToLinkMap().containsKey(link.getId()))
            {
            	return (ObjectPropertyLink) this.graphBuilder.getIdToLinkMap().get(link.getId());
            }
            else
            {
            	return null;
            }
    }
	
	// Probably we don't need this function in the interface to GUI
	public SubClassLink addSubClassOfLink(Node source, Node target) {
		
		String id = LinkIdFactory.getLinkId(Uris.RDFS_SUBCLASS_URI, source.getId(), target.getId());
		SubClassLink link = new SubClassLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public ClassInstanceLink addClassInstanceLink(Node source, Node target, LinkKeyInfo keyInfo) {
		
		String id = LinkIdFactory.getLinkId(Uris.CLASS_INSTANCE_LINK_URI, source.getId(), target.getId());
		ClassInstanceLink link = new ClassInstanceLink(id, keyInfo);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	public DataPropertyOfColumnLink addDataPropertyOfColumnLink(Node source, Node target, String specializedColumnHNodeId, String specializedLinkId) {
		
		String id = LinkIdFactory.getLinkId(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI, source.getId(), target.getId());
		DataPropertyOfColumnLink link = new DataPropertyOfColumnLink(id, specializedColumnHNodeId, specializedLinkId);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public ObjectPropertySpecializationLink addObjectPropertySpecializationLink(Node source, Node target, String specializedLinkId) {
		String id = LinkIdFactory.getLinkId(Uris.OBJECTPROPERTY_SPECIALIZATION_LINK_URI, source.getId(), target.getId());
		ObjectPropertySpecializationLink link = new ObjectPropertySpecializationLink(id, specializedLinkId);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}

	public ColumnSubClassLink addColumnSubClassOfLink(Node source, Node target) {
		
		String id = LinkIdFactory.getLinkId(Uris.COLUMN_SUBCLASS_LINK_URI, source.getId(), target.getId());
		ColumnSubClassLink link = new ColumnSubClassLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public void changeLinkWeight(String linkId, double weight) {
		
		LabeledLink link = this.getLinkById(linkId);
		if (link == null) {
			logger.error("Could not find the link with the id " + linkId);
			return;
		}
		
		this.graphBuilder.changeLinkWeight(link, weight);
	}
	
	public void changeLinkStatus(String linkId, LinkStatus newStatus) {

		logger.debug("changing the status of link " + linkId + " to " + newStatus.name());
		LabeledLink link = this.getLinkById(linkId);
		if (link == null) {
			if (newStatus == LinkStatus.ForcedByUser) {
				Node source = this.getNodeById(LinkIdFactory.getLinkSourceId(linkId));
				Node target = this.getNodeById(LinkIdFactory.getLinkTargetId(linkId));
				String linkUri = LinkIdFactory.getLinkUri(linkId);
				LabeledLink newLink;
				if (linkUri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
					newLink = new SubClassLink(linkId);
				else
					newLink = new ObjectPropertyLink(linkId, 
							this.graphBuilder.getOntologyManager().getUriLabel(linkUri), 
							this.graphBuilder.getOntologyManager().getObjectPropertyType(source.getLabel().getUri(), target.getLabel().getUri(), linkUri));
				
				newLink.setStatus(LinkStatus.ForcedByUser);
				this.graphBuilder.addLink(source, target, newLink);
			}
		} else
			this.graphBuilder.changeLinkStatus(link, newStatus);
	}
	
//	/**
//	 * This method removes a node from the graph and also all the links and the nodes that 
//	 * are added to the graph by adding the specified node.
//	 * This method is useful when the user changes the semantic type assigned to a column.
//	 * The GUI needs to call the method by sending a Column Node  
//	 * @param nodeId
//	 */
//	public boolean removeNode(String nodeId) {
//
//		Node node = this.getNodeById(nodeId);
//		if (node == null) {
//			logger.debug("Cannot find the node " + nodeId + " in the graph.");
//			return false;
//		}
//			
//		this.graphBuilder.removeNode(node);
//
//		return false;
//	}

	/**
	 * This method just deletes the specified link from the graph and leaves the rest of the graph untouched.
	 * @param linkId
	 * @return
	 */
	public boolean removeLink(String linkId) {
		
		LabeledLink link = this.getLinkById(linkId);
		if (link != null)
			return this.graphBuilder.removeLink(link);
		logger.debug("Cannot find the link " + linkId + " in the graph.");
		return false;
	}
	
	public Set<LabeledLink> getCurrentIncomingLinksToNode(String nodeId) {
		Node node = this.getNodeById(nodeId);
		if (node == null)
			return null;
		
		return this.graphBuilder.getIncomingLinksMap().get(nodeId);
	}
	
	public Set<LabeledLink> getCurrentOutgoingLinksToNode(String nodeId) {
		Node node = this.getNodeById(nodeId);
		if (node == null)
			return null;
		return this.graphBuilder.getOutgoingLinksMap().get(nodeId);
	}

	public List<LabeledLink> getLinks(String sourceId, String targetId) {
		return this.graphBuilder.getPossibleLinks(sourceId, targetId);
	}

	public List<LabeledLink> getIncomingLinks(String nodeId) {
		
		List<LabeledLink> possibleLinks  = new ArrayList<LabeledLink>();
		List<DefaultLink> tempDefault = null;
		List<LabeledLink> tempLabeled = null;
		HashSet<DefaultLink> allLinks = new HashSet<DefaultLink>();

		Node node = this.getNodeById(nodeId);
		if (node == null) return possibleLinks;
		
		Set<DefaultLink> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {
			tempDefault = Arrays.asList(incomingLinks.toArray(new DefaultLink[0]));
			allLinks.addAll(tempDefault);
		}
		
		if (node instanceof ColumnNode) {
			if (tempDefault != null) {
				for (DefaultLink l : tempDefault)
					if (l instanceof LabeledLink)
						possibleLinks.add((LabeledLink)l);
			}
		} else {
			Set<DefaultLink> outgoingLinks = this.graphBuilder.getGraph().outgoingEdgesOf(node);
			if (outgoingLinks != null) {
				tempDefault = Arrays.asList(outgoingLinks.toArray(new DefaultLink[0]));
				allLinks.addAll(outgoingLinks);
			}
			
			if (allLinks.size() == 0)
				return possibleLinks;
			
			String sourceId, targetId;
			for (DefaultLink e : allLinks) {
				if (e.getSource().getId().equals(nodeId)) { // outgoing link
					sourceId = e.getTarget().getId();
					targetId = nodeId;
				} else { // incoming link
					sourceId = e.getSource().getId();
					targetId = nodeId;
				}
				tempLabeled = getLinks(sourceId, targetId);
				if (tempLabeled != null)
					possibleLinks.addAll(tempLabeled);
			}
		}
		
		Collections.sort(possibleLinks, new LinkPriorityComparator());
		
//		for (Link l : possibleLinks) {
//			StringBuilder sb = new StringBuilder();
//			sb.append(l.getId() + " === ");
//			sb.append(l.getSource().getId() + " === ");
//			sb.append(l.getSource().getLabel().getNs() + " === ");
//			sb.append(l.getSource().getLocalId() + " === ");
//			sb.append(l.getSource().getDisplayId());
//			sb.append("\n");
//			sb.append(l.getTarget().getId() + " === ");
//			sb.append(l.getLabel().getUri() + " === ");
//			sb.append("\n");
//			logger.debug(sb.toString());
//		}
		logger.debug("Finished obtaining the incoming links.");
		return possibleLinks;
	}
	
	public List<LabeledLink> getOutgoingLinks(String nodeId) {
		
		List<LabeledLink> possibleLinks  = new ArrayList<LabeledLink>();
		List<DefaultLink> tempDefault = null;
		List<LabeledLink> tempLabeled = null;
		HashSet<DefaultLink> allLinks = new HashSet<DefaultLink>();

		Node node = this.getNodeById(nodeId);
		if (node == null || node instanceof ColumnNode) return possibleLinks;
		
		Set<DefaultLink> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {
			tempDefault = Arrays.asList(incomingLinks.toArray(new DefaultLink[0]));
			allLinks.addAll(tempDefault);
		}
		Set<DefaultLink> outgoingLinks = this.graphBuilder.getGraph().outgoingEdgesOf(node);
		if (outgoingLinks != null) {
			tempDefault = Arrays.asList(outgoingLinks.toArray(new DefaultLink[0]));
			allLinks.addAll(outgoingLinks);
		}
		
		if (allLinks.size() == 0)
			return possibleLinks;
		
		String sourceId, targetId;
		for (DefaultLink e : allLinks) {
			if (e.getSource().getId().equals(nodeId)) { // outgoing link
				sourceId = nodeId;
				targetId = e.getTarget().getId();
			} else { // incoming link
				sourceId = nodeId;
				targetId = e.getSource().getId();
			}
			tempLabeled = getLinks(sourceId, targetId);
			if (tempLabeled != null)
				possibleLinks.addAll(tempLabeled);
		}
		
		Collections.sort(possibleLinks, new LinkPriorityComparator());

		logger.debug("Finished obtaining the outgoing links.");
		return possibleLinks;
	}
	
	private void updateLinksPreferredByUI() {
		
		if (this.steinerTree == null)
			return;
		
		// Change the status of previously preferred links to normal
		Set<LabeledLink> linksInPreviousTree = this.getLinksByStatus(LinkStatus.PreferredByUI);
		if (linksInPreviousTree != null) {
			LabeledLink[] links = linksInPreviousTree.toArray(new LabeledLink[0]);
			for (LabeledLink link : links)
				this.graphBuilder.changeLinkStatus(link, LinkStatus.Normal);
		}
		
		Set<LabeledLink> linksForcedByUser = this.getLinksByStatus(LinkStatus.ForcedByUser);
		for (LabeledLink link: this.steinerTree.edgeSet()) {
			if (linksForcedByUser == null || !linksForcedByUser.contains(link)) {
				this.graphBuilder.changeLinkStatus(link, LinkStatus.PreferredByUI);
				logger.debug("link " + link.getId() + " has been added to preferred UI links.");
			}
		}
	}
	
	// FIXME: What if a column node has more than one domain?
	public List<Node> computeSteinerNodes() {
		Set<Node> steinerNodes = new HashSet<Node>();
		
		// Add column nodes and their domain
		// it is better to set isForced flag when setting a semantic type
		Set<ColumnNode> columnNodes = this.sourceColumnNodes;
		if (columnNodes != null) {
			for (ColumnNode n : columnNodes) {
				steinerNodes.add(n);
				if (n.hasUserType()) {
					HashMap<SemanticType, LabeledLink> domainLinks = 
							GraphUtil.getDomainLinks(this.graphBuilder.getGraph(), n, n.getUserSemanticTypes());
					if (domainLinks != null) {
						for (LabeledLink l : domainLinks.values()) {
							if (l.getSource() == null || !(l.getSource() instanceof InternalNode))
								continue;
							steinerNodes.add(l.getSource());
						}
					}
				}
			}
		}

		Set<Node> forcedNodes = this.getForcedNodes();
		for (Node n : forcedNodes) {
			if (!steinerNodes.contains(n))
				steinerNodes.add(n);
		}
		
		// Add source and target of the links forced by the user
		Set<LabeledLink> linksForcedByUser = this.getLinksByStatus(LinkStatus.ForcedByUser);
		if (linksForcedByUser != null) {
			for (LabeledLink link : linksForcedByUser) {
				
				if (!steinerNodes.contains(link.getSource()))
					steinerNodes.add(link.getSource());
	
				if (!steinerNodes.contains(link.getTarget()))
					steinerNodes.add(link.getTarget());			
			}
		}
		
		ArrayList<Node> result = new ArrayList<>();
		result.addAll(steinerNodes);
		return result;
	}
	
	public void align() {
		
//    	logger.debug("*** Graph ***");
//		GraphUtil.printGraphSimple(this.graphBuilder.getGraph());

		long start = System.currentTimeMillis();
		
		logger.debug("updating UI preferred links ...");
		this.updateLinksPreferredByUI();

		logger.debug("forced links ...");
		if (this.getLinksByStatus(LinkStatus.ForcedByUser) != null) {
			for (LabeledLink link: this.getLinksByStatus(LinkStatus.ForcedByUser))
				logger.debug("\t" + link.getId());
		}
		
		if (!ModelingConfiguration.getManualAlignment() && ModelingConfiguration.isLearnAlignmentEnabled())
			learnFromKnownSemanticModels();
		else
			learnFromOntology();
		
		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		if (this.steinerTree != null) {
			logger.debug("total number of nodes in steiner tree: " + this.steinerTree.vertexSet().size());
			logger.debug("total number of edges in steiner tree: " + this.steinerTree.edgeSet().size());
		}
		logger.debug("time to compute steiner tree: " + elapsedTimeSec);
		logger.info(GraphUtil.labeledGraphToString(this.steinerTree));

	}

	@Override
	public void ontologyModelUpdated() {
		this.graphBuilder.resetOntologyMaps();
		
	}

	public void cleanup() {
		this.ontologyManager.unsubscribeListener(this);
	}
	
	private void learnFromOntology() {
		
		logger.debug("preparing G Prime for steiner algorithm input ...");
		
//		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), 
//				this.getLinksByStatus(LinkStatus.PreferredByUI),
//				this.getLinksByStatus(LinkStatus.ForcedByUser));		
		UndirectedGraph<Node, DefaultLink> undirectedGraph = new AsUndirectedGraph<Node, DefaultLink>(this.getGraph());

		logger.debug("computing steiner nodes ...");
		List<Node> steinerNodes = this.computeSteinerNodes();

		logger.debug("steiner nodes ...");
		if (steinerNodes != null) {
			for (Node node: steinerNodes)
				logger.debug("\t" + node.getId());
		}

		logger.debug("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Node, DefaultLink> tree = steinerTree.getDefaultSteinerTree();
		if (tree == null) {
			logger.debug("resulting tree is null ...");
			return;
		}

		logger.info("*** steiner tree before post processing step ***");
		logger.info(GraphUtil.defaultGraphToString(tree));
		TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, tree, getLinksByStatus(LinkStatus.ForcedByUser), true);

		this.steinerTree = treePostProcess.getTree();
		this.root = treePostProcess.getRoot();

		logger.info("*** steiner tree after post processing step ***");
		logger.info(GraphUtil.labeledGraphToString(this.steinerTree));
	}
	
	private void learnFromKnownSemanticModels() {
		
		if (!ModelingConfiguration.isLearnAlignmentEnabled())
			return;
		
		List<Node> steinerNodes = this.computeSteinerNodes();
		if (steinerNodes == null || steinerNodes.isEmpty()) {
			return;
		}
		
		ModelLearner modelLearner = new ModelLearner(this.graphBuilder, steinerNodes);

		SemanticModel model = modelLearner.getModel();
		if (model == null) {
			logger.error("could not learn any model for this source!");
			return ;
		}

		this.updateAlignment(model, null);
	}
	
	private void addForcedLinks() {
		Set<LabeledLink> forcedLinks = getLinksByStatus(LinkStatus.ForcedByUser);
		if (forcedLinks != null)
		for (LabeledLink link : forcedLinks) {
			if (!this.steinerTree.containsEdge(link) &&
					this.steinerTree.containsVertex(link.getSource()) &&
					this.steinerTree.containsVertex(link.getTarget())) {
						this.steinerTree.addEdge(link.getSource(), link.getTarget(), link);
			}
		}
	}
	
	public void updateAlignment(SemanticModel model, List<SemanticType> semanticTypes) {
		
		if (model == null) 
			return;
		
		if (semanticTypes == null) semanticTypes = new LinkedList<SemanticType>();
		
		DirectedWeightedMultigraph<Node, LabeledLink> tree = 
				new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);

		HashMap<Node, Node> modelToAlignmentNode = new HashMap<Node, Node>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {

				InternalNode iNode;

				iNode = (InternalNode)this.getNodeById(n.getId());
				if (iNode != null) {
					modelToAlignmentNode.put(n, iNode);
				} else {
					iNode = this.addInternalNode(n.getLabel());
					modelToAlignmentNode.put(n, iNode);
				}
				
				tree.addVertex(iNode);
			}
			
			if (n instanceof ColumnNode) {
				if (model.getMappingToSourceColumns() != null) {
					ColumnNode cn = model.getMappingToSourceColumns().get(n);
					modelToAlignmentNode.put(n, cn);
					tree.addVertex(cn);
				}
			}
		}
		
		Node source, target;
		for (LabeledLink l : model.getGraph().edgeSet()) {
			
			if (!(l.getSource() instanceof InternalNode)) {
				logger.error("column node cannot have an outgoing link!");
				return;
			}

			source = modelToAlignmentNode.get(l.getSource());
			target = modelToAlignmentNode.get(l.getTarget());
			
			if (source == null || target == null)
				continue;

			String id = LinkIdFactory.getLinkId(l.getUri(), source.getId(), target.getId());
			LabeledLink newLink = l.copy(id);
			
	    	if (newLink == null) continue;
			
			this.getGraphBuilder().addLink(source, target, newLink); // returns false if link already exists
			tree.addEdge(source, target, newLink);
						
			if (target instanceof ColumnNode) {
				SemanticType st = new SemanticType(((ColumnNode)target).getHNodeId(), 
						newLink.getLabel(), source.getLabel(), SemanticType.Origin.User, 1.0);
				semanticTypes.add(st);
			}
			
		}

		this.steinerTree = tree;
		this.addForcedLinks();
		this.root = TreePostProcess.selectRoot(GraphUtil.asDefaultGraph(tree));

	}

	public void updateColumnNodesInAlignment(Worksheet worksheet) {

		for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
			HNode node = path.getLeaf();
			String hNodeId = node.getId();
			Node n = getNodeById(hNodeId);
			if (n == null) {
				addColumnNode(hNodeId, node.getColumnName(), null);
			}
		}
	
		
	}
	
}
