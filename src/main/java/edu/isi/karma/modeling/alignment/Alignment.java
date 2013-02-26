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
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SubClassLink;



public class Alignment implements OntologyUpdateListener {

	static Logger logger = Logger.getLogger(Alignment.class);

	private GraphBuilder graphBuilder;
	private DirectedWeightedMultigraph<Node, Link> steinerTree = null;
	private Node root = null;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	public Alignment(OntologyManager ontologyManager) {

		this.nodeIdFactory = new NodeIdFactory();
		this.linkIdFactory = new LinkIdFactory();

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory, linkIdFactory);
		
	}
	
	public boolean isEmpty() {
		return (this.graphBuilder.getGraph().edgeSet().size() == 0);
	}
	
	public Node GetTreeRoot() {
		return this.root;
	}
	
	public Alignment getAlignmentClone() {
		Cloner cloner=new Cloner();
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

	public int getLastIndexOfNodeUri(String uri) {
		return this.nodeIdFactory.lastIndexOf(uri);
	}

	public int getLastIndexOfLinkUri(String uri) {
		return this.linkIdFactory.lastIndexOf(uri);
	}

	public ColumnNode getColumnNodeByHNodeId(String hNodeId) {

		List<Node> columnNodes = this.getNodesByType(NodeType.ColumnNode);
		if (columnNodes == null) return null;
		for (Node cNode : columnNodes) {
			if (((ColumnNode)cNode).getHNodeId().equals(hNodeId))
				return (ColumnNode)cNode;
		}
		return null;
	}
	
	// AddNode methods
	
	public ColumnNode addColumnNode(String hNodeId, String columnName) {
		
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName);
		if (this.graphBuilder.addNode(node)) return node;
		return null;
	}
	
	public InternalNode addInternalNode(Label label) {
		
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNode(node)) return node;
		return null;	
	}
	
	public ColumnNode addColumnNodeWithoutUpdatingGraph(String hNodeId, String columnName) {
		
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName);
		if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) return node;
		return null;
	}
	
	public InternalNode addInternalNodeWithoutUpdatingGraph(Label label) {
		
		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) return node;
		return null;	
	}
	
	public void updateGraph() {
		this.graphBuilder.updateGraph();
	}
	
	public ColumnNode createColumnNode(String hNodeId, String columnName) {
		
		// use hNodeId as id of the node
		ColumnNode node = new ColumnNode(hNodeId, hNodeId, columnName);
		return node;
	}
	
	public InternalNode createInternalNode(Label label) {

		String id = nodeIdFactory.getNodeId(label.getUri());
		InternalNode node = new InternalNode(id, label);
		return node;
	}
	
	public void addNodeList(List<Node> nodes) {
		this.graphBuilder.addNodeList(nodes);
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
	
	public DataPropertyOfColumnLink addDataPropertyOfColumnLink(Node source, Node target, String specializedColumnHNodeId) {
		
		String id = linkIdFactory.getLinkId(Uris.DATAPROPERTY_OF_COLUMN_LINK_URI);
		DataPropertyOfColumnLink link = new DataPropertyOfColumnLink(id, specializedColumnHNodeId);
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
	
	/**
	 * This method removes a node from the graph and also all the links and the nodes that 
	 * are added to the graph by adding the specified node.
	 * This method is useful when the user changes the semantic type assigned to a column.
	 * The GUI needs to call the method by sending a Column Node  
	 * @param nodeId
	 */
	public boolean removeNode(String nodeId) {

		Node node = this.getNodeById(nodeId);
		if (node == null) {
			logger.debug("Cannot find the node " + nodeId + " in the graph.");
			return false;
		}
			
//		if (!(node instanceof ColumnNode)) {
//			logger.debug("You can only request to delete a Column Node. Node " + node.getId() + " is not a Column Node");
//			return false;
//		}
//		
//		Set<Link> incomingLinks = this.steinerTree.incomingEdgesOf(node);
//		Node domain = null;
//		if (incomingLinks != null && incomingLinks.size() == 1)
//			domain = incomingLinks.toArray(new Link[0])[0].getSource();
		
		this.graphBuilder.removeNode(node);
//		this.graphBuilder.removeNode(domain);

		return false;
	}

	/**
	 * This method just deletes the specified link from the graph and leaves the rest of the graph untouched.
	 * Probably we don't need to use this function.
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
	
	//FIXME: This method should be commented, or make it private for now ... 
//	private List<SemanticType> getSemanticTypes() {
//		
//		List<SemanticType> semanticTypes = new ArrayList<SemanticType>();
//		
//		String hNodeId;
//		Label type;
//		Label domain;
//		Origin origin;
//		Double probability;
//		boolean partOfKey;
//		
//		List<Node> columnNodes = this.getNodesByType(NodeType.ColumnNode);
//		if (columnNodes != null) {
//			for (Node n : columnNodes) {
//				
//				hNodeId = ((ColumnNode)n).getHNodeId();
//				type = null;
//				domain = null;
//				origin = Origin.User;
//				probability = 1.0;
//				partOfKey = false;
//
//				
//				Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(n);
//				if (incomingLinks != null && incomingLinks.size() == 1) {
//					Link inLink = incomingLinks.toArray(new Link[0])[0];
//					Node source = inLink.getSource();
//					if (inLink instanceof DataPropertyLink) {
//						type = inLink.getLabel();
//						domain = source.getLabel();
//					} else
//						type = source.getLabel();
//					
//					if (inLink.getKeyType() == LinkKeyInfo.PartOfKey) partOfKey = true;
//				} else 
//					logger.error("The column node " + n.getId() + " does not have any domain or it has more than one domain.");
//				
//				SemanticType s = new SemanticType(hNodeId, type, domain, origin, probability, partOfKey);
//				semanticTypes.add(s);
//			}
//		}
//		
//		return semanticTypes;
//	}
	
	public Set<Link> getCurrentLinksToNode(String nodeId) {
		
		Node node = this.getNodeById(nodeId);
		if (node == null) return null;
		if (!this.steinerTree.containsVertex(node)) return null;
			
		return this.steinerTree.incomingEdgesOf(node);
	}
	
	public List<Link> getAllPossibleLinksToNode(String nodeId) {
		
		List<Link> possibleLinks = new ArrayList<Link>();
		Node node = this.getNodeById(nodeId);
		if (node == null) return possibleLinks;
		
		Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(node);
		if (incomingLinks != null) {

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
					steinerNodes.add(link.getTarget());			
			}
		}
		
		return steinerNodes;
	}
	
	public void align() {
		
//    	System.out.println("*** Graph ***");
//		GraphUtil.printGraphSimple(this.graphBuilder.getGraph());

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

	@Override
	public void ontologyModelUpdated() {
		this.graphBuilder.resetOntologyMaps();
		
	}
}
