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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SubClassLink;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);

	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, Link> steinerTree = null;
	private Node root = null;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
//	private List<String> duplicatedLinkIds;

	public Alignment(OntologyManager ontologyManager) {

		this.nodeIdFactory = new NodeIdFactory();
		this.linkIdFactory = new LinkIdFactory();

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory, linkIdFactory);
		
//		duplicatedLinkIds = new ArrayList<String>();
	}
	
//	public List<String> getDuplicatedLinkIds() {
//		return duplicatedLinkIds;
//	}
//
//	public List<SemanticType> getSemanticTypes() {
////		return this.semanticTypes;
//		return null;
//	}
//
//	private boolean duplicate(SemanticType st1, SemanticType st2) {
//		if (st1.getHNodeId().equalsIgnoreCase(st2.getHNodeId()) &&
//				st1.getType().getUriString().equalsIgnoreCase(st2.getType().getUriString()) &&
//				st1.isPartOfKey() == st2.isPartOfKey()) {
//			if (st1.getDomain() != null && st2.getDomain() != null) 
//				if (st1.getDomain().getUriString().equalsIgnoreCase(st2.getDomain().getUriString()))
//					return true;
//			
//			if (st1.getDomain() == null && st2.getDomain() == null)
//				return true;
//			
//			return false;
//		}
//		return false;
//	}
//	
//	public void updateSemanticTypes(List<SemanticType> semanticTypes) {
//		
//		List<SemanticType> updatedSemanticTypes = new ArrayList<SemanticType>();
//		List<Node> deletedVertices = new ArrayList<Node>(); 
//		for (SemanticType s : semanticTypes)
//			logger.debug("%%%%%%%%%%%%%%%%%%%" + s.getType().getUriString());
//		
//		for (SemanticType newType : semanticTypes) {
//			boolean found = false;
//			for (SemanticType prevType : this.semanticTypes) {				
//				if (duplicate(newType, prevType))
//					found = true;
//			}
//			if (!found) {
//				logger.debug(">>>>>>>>new>>>>>>" + newType.getType().getUriString());
//				this.graphBuilder.addSemanticType(newType);
//				updatedSemanticTypes.add(newType);
//			}
//		}
//		for (SemanticType prevType : this.semanticTypes) {
//			boolean found = false;
//			for (SemanticType newType : semanticTypes) {
//
//				if (duplicate(newType, prevType)) {
//					found = true;
//					updatedSemanticTypes.add(prevType);
//				}
//			}
//			if (!found) {
//				Node deletedNode = this.graphBuilder.removeSemanticType(prevType);
//				logger.debug("<<<<<<<<<delete<<<<<<<<<" + prevType.getType().getUriString());
//				if (deletedNode != null) deletedVertices.add(deletedNode);
//			}
//		}
//		this.semanticTypes = updatedSemanticTypes;
//		this.semanticNodes = this.graphBuilder.getSemanticNodes();
//		removeInvalidForcedLinks(deletedVertices);
//		align();
//	}
	
	public Link getCurrentLinkToNode(String nodeId) {
		
		Node node = this.getNodeById(nodeId);
		Set<Link> incomingLinks = this.steinerTree.incomingEdgesOf(node);
		if (incomingLinks != null && incomingLinks.size() == 1)
			return incomingLinks.toArray(new Link[0])[0];
		
		return null;
	}
	
	public List<Link> getAllPossibleLinksToNode(String nodeId) {
		
		List<Link> possibleLinks = new ArrayList<Link>();
		Node node = this.getNodeById(nodeId);
		
		List<String> nodesInSteinerTree = new ArrayList<String>();
		for (Node v : this.steinerTree.vertexSet())
			nodesInSteinerTree.add(v.getId());
		
		Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {

//			for (Link link : incomingLinks) {
//				// if the node is not in the UI, don't show it to the user
//				// Scenario: multiple domain, then again merge it. The created node is in the graph but not in the tree.
//				if (nodesInSteinerTree.contains(link.getSource()))
//					possibleLinks.add(link);
//				
//			}

			possibleLinks = Arrays.asList(incomingLinks.toArray(new Link[0]));
		}
		
		Collections.sort(possibleLinks);
		return possibleLinks;
	}
	
	private void updateLinksPreferredByUI() {
		
		if (this.steinerTree == null)
			return;
		
		// Change the status of previously preferred links to normal
		List<Link> linksInPreviousTree = this.getLinksByStatus(LinkStatus.PreferredByUI);
		if (linksInPreviousTree != null) {
			for (Link link : linksInPreviousTree)
				this.graphBuilder.changeLinkStatus(link, LinkStatus.Normal);
		}
		
		for (Link link: this.steinerTree.edgeSet()) {
			this.graphBuilder.changeLinkStatus(link, LinkStatus.PreferredByUI);
			logger.debug("link " + link.getId() + " has been added to preferred UI links.");
		}
	}
	
	private List<Node> computeSteinerNodes() {
		List<Node> steinerNodes = new ArrayList<Node>();
		
		// Add column nodes and their domain
		List<Node> columnNodes = this.getNodesByType(NodeType.ColumnNode);
		if (columnNodes != null) {
			for (Node n : columnNodes) {
				Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(n);
				if (incomingLinks != null && incomingLinks.size() == 1) {
					Node domain = incomingLinks.toArray(new Link[0])[0].getSource();
					// adding the column node
					steinerNodes.add(n);
					// adding the domain
					steinerNodes.add(domain);
				} else 
					logger.error("The column node " + n.getId() + " does not have any domain or it has more than one domain.");
			}
		}

		// Add source and target of the links forced by the user
		List<Link> linksForcedByUser = this.getLinksByStatus(LinkStatus.ForcedByUser);
		if (linksForcedByUser != null) {
			for (Link link : linksForcedByUser) {
				
				if (!steinerNodes.contains(link.getSource()))
					steinerNodes.add(link.getSource());
	
				if (!steinerNodes.contains(link.getTarget()))
					steinerNodes.add(link.getTarget());			}
		}
		
		return steinerNodes;
	}
	
	public void align() {
		
    	System.out.println("*** Graph ***");
		GraphUtil.printGraphSimple(this.graphBuilder.getGraph());

		long start = System.currentTimeMillis();
		
		logger.info("Updating UI preferred links ...");
		this.updateLinksPreferredByUI();

		logger.info("preparing G Prime for steiner algorithm input ...");
		
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), 
				this.getLinksByStatus(LinkStatus.PreferredByUI),
				this.getLinksByStatus(LinkStatus.ForcedByUser));		
		UndirectedGraph<Node, Link> undirectedGraph = graphPreProcess.getUndirectedGraph();

		logger.info("computing steiner nodes ...");
		List<Node> steinerNodes = this.computeSteinerNodes();

		logger.info("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Node, Link> tree = steinerTree.getSteinerTree();
		if (tree == null) {
			logger.info("resulting tree is null ...");
			return;
		}

		System.out.println("*** Steiner Tree ***");
		GraphUtil.printGraphSimple(tree);
		
		logger.info("selecting a root for the tree ...");
		TreePostProcess treePostProcess = new TreePostProcess(tree, this.graphBuilder.getThingNode());
//		removeInvalidForcedLinks(treePostProcess.getDangledVertexList());
		
		this.steinerTree = treePostProcess.getTree();
		this.root = treePostProcess.getRoot();

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		logger.info("total number of nodes in steiner tree: " + this.steinerTree.vertexSet().size());
		logger.info("total number of edges in steiner tree: " + this.steinerTree.edgeSet().size());
		logger.info("time to compute steiner tree: " + elapsedTimeSec);
	}

	public Node GetTreeRoot() {
		return this.root;
	}
	
	public DirectedWeightedMultigraph<Node, Link> getSteinerTree() {
		if (this.steinerTree == null) align();
		// GraphUtil.printGraph(this.steinerTree);
		return this.steinerTree;
	}

	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////

	/**** TO BE IMPLEMENTED ***/
	
	
	public Set<Node> getGraphNodes() {
		return this.graphBuilder.getGraph().vertexSet();
	}
	
	public Set<Link> getGraphLinks() {
		return this.graphBuilder.getGraph().edgeSet();
	}
	
	public Node getNodeById(String nodeId) {
		return this.graphBuilder.getIdToNodeMap().get(nodeId);
	}
	
	public List<Node> getNodesByUri(String uriString) {
		return this.graphBuilder.getUriToNodesMap().get(uriString);
	}
	
	public List<Node> getNodesByType(NodeType type) {
		return this.graphBuilder.getTypeToNodesMap().get(type);
	}
	
	public Link getLinkById(String linkId) {
		return this.graphBuilder.getIdToLinkMap().get(linkId);
	}
	
	public List<Link> getLinksByUri(String uriString) {
		return this.graphBuilder.getUriToLinksMap().get(uriString);
	}
	
	public List<Link> getLinksByType(LinkType type) {
		return this.graphBuilder.getTypeToLinksMap().get(type);
	}
	
	public List<Link> getLinksByStatus(LinkStatus status) {
		return this.graphBuilder.getStatusToLinksMap().get(status);
	}

	
	// AddNode methods
	
	public ColumnNode addColumnNode(String hNodeId, String columnName) {
		
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName);
		if (this.graphBuilder.addNode(node)) return node;
		return null;
	}
	
	public InternalNode addInternalClassNode(Label label) {
		
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNode(node)) return node;
		return null;	
	}
	
	// AddLink methods

	public DataPropertyLink addDataPropertyLink(Node source, Node target, Label label, boolean partOfKey) {
		
		String id = linkIdFactory.getLinkId(label.getUri());	
		DataPropertyLink link = new DataPropertyLink(id, label, partOfKey);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	// Probably we don't need this function in the interface to GUI
	public ObjectPropertyLink addObjectPropertyLink(Node source, Node target, Label label) {
		
		String id = linkIdFactory.getLinkId(label.getUri());		
		ObjectPropertyLink link = new ObjectPropertyLink(id, label);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	// Probably we don't need this function in the interface to GUI
	public SubClassLink addSubClassOfLink(Node source, Node target) {
		
		String id = linkIdFactory.getLinkId(Uris.RDFS_SUBCLASS_URI);
		SubClassLink link = new SubClassLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public ClassInstanceLink addClassInstanceLink(Node source, Node target, LinkKeyInfo keyInfo) {
		
		String id = linkIdFactory.getLinkId(Uris.CLASS_INSTANCE_LINK_URI);
		ClassInstanceLink link = new ClassInstanceLink(id, keyInfo);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	public DataPropertyOfColumnLink addDataPropertyOfColumnLink(Node source, Node target) {
		
		String id = linkIdFactory.getLinkId(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI);
		DataPropertyOfColumnLink link = new DataPropertyOfColumnLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}

	public ColumnSubClassLink addColumnSubClassOfLink(Node source, Node target) {
		
		String id = linkIdFactory.getLinkId(Uris.COLUMN_SUBCLASS_LINK_URI);
		ColumnSubClassLink link = new ColumnSubClassLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public void changeLinkStatus(String linkId, LinkStatus newStatus) {		
		
		Link link = this.getLinkById(linkId);
		if (link == null) {
			logger.error("Could not find the link with the id " + linkId);
			return;
		}
		
		this.graphBuilder.changeLinkStatus(link, newStatus);
	}
	
	public ColumnNode getColumnNodeByHNodeId(String hNodeId) {
		
		return null;
	}
	
	// Used in the case of semantic types
	public void deleteLink(String linkId) {
		
	}
	
	public boolean isEmpty() {
		return (this.graphBuilder.getGraph().edgeSet().size() == 0);
	}

}
