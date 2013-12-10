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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.ontology.OntologyUpdateListener;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkPriorityComparator;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubClassLink;



public class Alignment implements OntologyUpdateListener {

	static Logger logger = LoggerFactory.getLogger(Alignment.class);

	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, Link> steinerTree = null;
	private Node root = null;
	
	private NodeIdFactory nodeIdFactory;
	
	public Alignment(OntologyManager ontologyManager) {

		this.nodeIdFactory = new NodeIdFactory();

		ontologyManager.subscribeListener(this);

		logger.debug("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory, true);//, linkIdFactory);
		
	}
	
	public boolean isEmpty() {
		return (this.graphBuilder.getGraph().edgeSet().size() == 0);
	}
	
	public Node GetTreeRoot() {
		return this.root;
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
	
	public DirectedWeightedMultigraph<Node, Link> getSteinerTree() {
		if (this.steinerTree == null) align();
		// GraphUtil.printGraph(this.steinerTree);
		return this.steinerTree;
	}
	
	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graphBuilder.getGraph();
	}
	
	public void setGraph(DirectedWeightedMultigraph<Node, Link> graph) {
		this.graphBuilder.setGraph(graph);
	}
	
	public Set<Node> getGraphNodes() {
		return this.graphBuilder.getGraph().vertexSet();
	}
	
	public Set<Link> getGraphLinks() {
		return this.graphBuilder.getGraph().edgeSet();
	}
	
	public Node getNodeById(String nodeId) {
		return this.graphBuilder.getIdToNodeMap().get(nodeId);
	}
	
	public Set<Node> getNodesByUri(String uriString) {
		return this.graphBuilder.getUriToNodesMap().get(uriString);
	}
	
	public Set<Node> getNodesByType(NodeType type) {
		return this.graphBuilder.getTypeToNodesMap().get(type);
	}
	
	public Link getLinkById(String linkId) {
		return this.graphBuilder.getIdToLinkMap().get(linkId);
	}
	
	public Set<Link> getLinksByUri(String uriString) {
		return this.graphBuilder.getUriToLinksMap().get(uriString);
	}
	
	public Set<Link> getLinksByType(LinkType type) {
		return this.graphBuilder.getTypeToLinksMap().get(type);
	}
	
	public Set<Link> getLinksByStatus(LinkStatus status) {
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
	
	public ColumnNode addColumnNode(String hNodeId, String columnName, String rdfLiteralType, List<SemanticType> crfSuggestedSemanticTypes) {
		
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName, rdfLiteralType, crfSuggestedSemanticTypes);
		if (this.graphBuilder.addNode(node)) 
			return node;
		return null;
	}
	
	public InternalNode addInternalNode(Label label) {
		
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNode(node)) return node;
		return null;	
	}

	public void updateGraph() {
		this.graphBuilder.updateGraph();
	}
	
	// AddLink methods

	public DataPropertyLink addDataPropertyLink(Node source, Node target, Label label, boolean partOfKey) {
		
		String id = LinkIdFactory.getLinkId(label.getUri(), source.getId(), target.getId());	
		DataPropertyLink link = new DataPropertyLink(id, label, partOfKey);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	

    public ObjectPropertyLink addObjectPropertyLink(Node source, Node target, Label label) {
            
            String id = LinkIdFactory.getLinkId(label.getUri(), source.getId(), target.getId());        
            ObjectPropertyLink link = new ObjectPropertyLink(id, label, 
            		this.graphBuilder.getOntologyManager().getObjectPropertyType(source.getLabel().getUri(), target.getLabel().getUri(), label.getUri()));
            if (this.graphBuilder.addLink(source, target, link)) return link;
            return null;        
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
	
	public DataPropertyOfColumnLink addDataPropertyOfColumnLink(Node source, Node target, String specializedColumnHNodeId) {
		
		String id = LinkIdFactory.getLinkId(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI, source.getId(), target.getId());
		DataPropertyOfColumnLink link = new DataPropertyOfColumnLink(id, specializedColumnHNodeId);
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
		
		Link link = this.getLinkById(linkId);
		if (link == null) {
			logger.error("Could not find the link with the id " + linkId);
			return;
		}
		
		this.graphBuilder.changeLinkWeight(link, weight);
	}
	
	public void changeLinkStatus(String linkId, LinkStatus newStatus) {

		logger.debug("changing the status of link " + linkId + " to " + newStatus.name());
		Link link = this.getLinkById(linkId);
		if (link == null) {
			if (newStatus == LinkStatus.ForcedByUser) {
				Node source = this.getNodeById(LinkIdFactory.getLinkSourceId(linkId));
				Node target = this.getNodeById(LinkIdFactory.getLinkTargetId(linkId));
				String linkUri = LinkIdFactory.getLinkUri(linkId);
				Link newLink;
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
		
		Link link = this.getLinkById(linkId);
		if (link != null)
			return this.graphBuilder.removeLink(link);
		logger.debug("Cannot find the link " + linkId + " in the graph.");
		return false;
	}
	
	public Set<Link> getCurrentIncomingLinksToNode(String nodeId) {
		
		Node node = this.getNodeById(nodeId);
		if (node == null) return null;
		if (!this.steinerTree.containsVertex(node)) return null;
			
		return this.steinerTree.incomingEdgesOf(node);
	}
	
	public Set<Link> getCurrentOutgoingLinksToNode(String nodeId) {
		
		Node node = this.getNodeById(nodeId);
		if (node == null) return null;
		if (!this.steinerTree.containsVertex(node)) return null;
			
		return this.steinerTree.outgoingEdgesOf(node);
	}

	public List<Link> getLinks(String sourceId, String targetId) {
		return this.graphBuilder.getPossibleLinks(sourceId, targetId);
	}

	public List<Link> getIncomingLinks(String nodeId) {
		
		List<Link> possibleLinks  = new ArrayList<Link>();
		List<Link> temp = null;
		HashSet<Link> allLinks = new HashSet<Link>();

		Node node = this.getNodeById(nodeId);
		if (node == null) return possibleLinks;
		
		Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {
			temp = Arrays.asList(incomingLinks.toArray(new Link[0]));
			allLinks.addAll(temp);
		}
		
		if (node instanceof ColumnNode) {
			if (temp != null)
				possibleLinks.addAll(temp);
		} else {
			Set<Link> outgoingLinks = this.graphBuilder.getGraph().outgoingEdgesOf(node);
			if (outgoingLinks != null) {
				temp = Arrays.asList(outgoingLinks.toArray(new Link[0]));
				allLinks.addAll(outgoingLinks);
			}
			
			if (allLinks.size() == 0)
				return possibleLinks;
			
			String sourceId, targetId;
			for (Link e : allLinks) {
				if (e.getSource().getId().equals(nodeId)) { // outgoing link
					sourceId = e.getTarget().getId();
					targetId = nodeId;
				} else { // incoming link
					sourceId = e.getSource().getId();
					targetId = nodeId;
				}
				temp = getLinks(sourceId, targetId);
				if (temp != null)
					possibleLinks.addAll(temp);
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
	
	public List<Link> getOutgoingLinks(String nodeId) {
		
		List<Link> possibleLinks  = new ArrayList<Link>();
		List<Link> temp;
		HashSet<Link> allLinks = new HashSet<Link>();

		Node node = this.getNodeById(nodeId);
		if (node == null || node instanceof ColumnNode) return possibleLinks;
		
		Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {
			temp = Arrays.asList(incomingLinks.toArray(new Link[0]));
			allLinks.addAll(temp);
		}
		Set<Link> outgoingLinks = this.graphBuilder.getGraph().outgoingEdgesOf(node);
		if (outgoingLinks != null) {
			temp = Arrays.asList(outgoingLinks.toArray(new Link[0]));
			allLinks.addAll(outgoingLinks);
		}
		
		if (allLinks.size() == 0)
			return possibleLinks;
		
		String sourceId, targetId;
		for (Link e : allLinks) {
			if (e.getSource().getId().equals(nodeId)) { // outgoing link
				sourceId = nodeId;
				targetId = e.getTarget().getId();
			} else { // incoming link
				sourceId = nodeId;
				targetId = e.getSource().getId();
			}
			temp = getLinks(sourceId, targetId);
			if (temp != null)
				possibleLinks.addAll(temp);
		}
		
		Collections.sort(possibleLinks, new LinkPriorityComparator());

		logger.debug("Finished obtaining the outgoing links.");
		return possibleLinks;
	}
	
	private void updateLinksPreferredByUI() {
		
		if (this.steinerTree == null)
			return;
		
		// Change the status of previously preferred links to normal
		Set<Link> linksInPreviousTree = this.getLinksByStatus(LinkStatus.PreferredByUI);
		if (linksInPreviousTree != null) {
			Link[] links = linksInPreviousTree.toArray(new Link[0]);
			for (Link link : links)
				this.graphBuilder.changeLinkStatus(link, LinkStatus.Normal);
		}
		
		Set<Link> linksForcedByUser = this.getLinksByStatus(LinkStatus.ForcedByUser);
		for (Link link: this.steinerTree.edgeSet()) {
			if (linksForcedByUser == null || !linksForcedByUser.contains(link)) {
				this.graphBuilder.changeLinkStatus(link, LinkStatus.PreferredByUI);
				logger.debug("link " + link.getId() + " has been added to preferred UI links.");
			}
		}
	}
	
	// FIXME: What if a column node has more than one domain?
	private List<Node> computeSteinerNodes() {
		List<Node> steinerNodes = new ArrayList<Node>();
		
		// Add column nodes and their domain
		Set<Node> columnNodes = this.getNodesByType(NodeType.ColumnNode);
		if (columnNodes != null) {
			for (Node n : columnNodes) {
				if (!(n instanceof ColumnNode)) continue;
				Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(n);
				if (incomingLinks != null && incomingLinks.size() == 1) {
					Node domain = incomingLinks.toArray(new Link[0])[0].getSource();
					// adding the column node
					steinerNodes.add(n);
					// adding the domain
					steinerNodes.add(domain);
				} else 
					logger.debug("The column node " + ((ColumnNode)n).getColumnName() + " does not have any domain or it has more than one domain.");
			}
		}

		// Add source and target of the links forced by the user
		Set<Link> linksForcedByUser = this.getLinksByStatus(LinkStatus.ForcedByUser);
		if (linksForcedByUser != null) {
			for (Link link : linksForcedByUser) {
				
				if (!steinerNodes.contains(link.getSource()))
					steinerNodes.add(link.getSource());
	
				if (!steinerNodes.contains(link.getTarget()))
					steinerNodes.add(link.getTarget());			
			}
		}
		
		return steinerNodes;
	}
	
	public void align() {
		
//    	logger.debug("*** Graph ***");
//		GraphUtil.printGraphSimple(this.graphBuilder.getGraph());

		long start = System.currentTimeMillis();
		
		logger.debug("updating UI preferred links ...");
		this.updateLinksPreferredByUI();

		logger.debug("forced links ...");
		if (this.getLinksByStatus(LinkStatus.ForcedByUser) != null) {
			for (Link link: this.getLinksByStatus(LinkStatus.ForcedByUser))
				logger.debug("\t" + link.getId());
		}
		
		logger.debug("preparing G Prime for steiner algorithm input ...");
		
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), 
				this.getLinksByStatus(LinkStatus.PreferredByUI),
				this.getLinksByStatus(LinkStatus.ForcedByUser));		
		UndirectedGraph<Node, Link> undirectedGraph = graphPreProcess.getUndirectedGraph();

		logger.debug("computing steiner nodes ...");
		List<Node> steinerNodes = this.computeSteinerNodes();

		logger.debug("steiner nodes ...");
		if (steinerNodes != null) {
			for (Node node: steinerNodes)
				logger.debug("\t" + node.getId());
		}

		logger.debug("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Node, Link> tree = steinerTree.getSteinerTree();
		if (tree == null) {
			logger.debug("resulting tree is null ...");
			return;
		}

		logger.debug("*** steiner tree before post processing step ***");
		logger.info(GraphUtil.graphToString(this.steinerTree));
//		logger.debug("selecting a root for the tree ...");
		TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, tree, 
				getLinksByStatus(LinkStatus.ForcedByUser), this.graphBuilder.getThingNode());

		this.steinerTree = treePostProcess.getTree();
		this.root = treePostProcess.getRoot();

		logger.debug("*** steiner tree after post processing step ***");
		logger.info(GraphUtil.graphToString(this.steinerTree));

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		logger.debug("total number of nodes in steiner tree: " + this.steinerTree.vertexSet().size());
		logger.debug("total number of edges in steiner tree: " + this.steinerTree.edgeSet().size());
		logger.debug("time to compute steiner tree: " + elapsedTimeSec);
	}

	@Override
	public void ontologyModelUpdated() {
		this.graphBuilder.resetOntologyMaps();
		
	}
	
	
}
