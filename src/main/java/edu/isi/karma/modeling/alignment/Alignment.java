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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import edu.isi.karma.modeling.FixedUris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ClassLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassOfLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubClassOfLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.UriOfClassLink;
import edu.isi.karma.webserver.KarmaException;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);


	private OntologyManager ontologyManager;
	private List<Node> semanticNodes;

	private List<Link> linksForcedByUser;
	private List<Link> linksPreferredByUI;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	private List<String> duplicatedLinkIds;

	private DirectedWeightedMultigraph<Node, Link> steinerTree = null;
	private Node root = null;
	
	private GraphBuilder graphBuilder;
	
	public Alignment(OntologyManager ontologyManager) {
		this.ontologyManager = ontologyManager;

		this.nodeIdFactory = new NodeIdFactory();
		this.linkIdFactory = new LinkIdFactory();

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory, linkIdFactory);
		
		linksForcedByUser = new ArrayList<Link>();
		linksPreferredByUI = new ArrayList<Link>();
		duplicatedLinkIds = new ArrayList<String>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	
	public List<Link> getLinksForcedByUser() {
		return linksForcedByUser;
	}
	
	public List<String> getDuplicatedLinkIds() {
		return duplicatedLinkIds;
	}

	public List<SemanticType> getSemanticTypes() {
//		return this.semanticTypes;
		return null;
	}
	
	private void addToLinksForcedByUserList(Link e) {
		Link[] links = linksForcedByUser.toArray(new Link[0]);
		for (Link link : links) {
			if (link.getTarget().getID().equalsIgnoreCase(e.getTarget().getID()))
				clearUserLink(link.getID());
		}
		linksForcedByUser.add(e);
		logger.info("link " + e.getID() + " has been added to user selected links.");
	}
	
	private void removeInvalidForcedLinks(List<Node> dangledVertexList) {
		Link[] links = linksForcedByUser.toArray(new Link[0]);
		for (Link link : links) {
			for (Node v : dangledVertexList) {
				if (link.getTarget().getID().equalsIgnoreCase(v.getID()) || 
						link.getSource().getID().equalsIgnoreCase(v.getID()))
					clearUserLink(link.getID());
			}
		}
	}
	
	public void addUserLink(String linkId) {
		Link[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new Link[0]);
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				logger.debug("link " + linkId + "has been added to the user selected links.");
				addToLinksForcedByUserList(allLinks[i]);
				align();
				return;
			}
		}
		
		logger.info("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void addUserLinks(List<String> linkIds) {
		Link[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new Link[0]);
		for (int j = 0; j < linkIds.size(); j++) {
			boolean found = false;
			for (int i = 0; i < allLinks.length; i++) {
				if (allLinks[i].getID().equalsIgnoreCase(linkIds.get(j))) {
					logger.debug("link " + linkIds.get(j) + "has been added to the user selected links.");
					addToLinksForcedByUserList(allLinks[i]);
					found = true;
				}
			}
			if (!found)
				logger.info("link with ID " + linkIds.get(j) + " does not exist in graph.");
		}
		align();
	}
	
	public void clearUserLink(String linkId) {
		for (int i = 0; i < linksForcedByUser.size(); i++) {
			if (linksForcedByUser.get(i).getID().equalsIgnoreCase(linkId)) {
				linksForcedByUser.remove(i);
				logger.info("link " + linkId + " has been removed from  user selected links.");
				align();
				return;
			}
		}
	}
	
	public void clearUserLinks(List<String> linkIds) {
		for (int j = 0; j < linkIds.size(); j++) {
			for (int i = 0; i < linksForcedByUser.size(); i++) {
				if (linksForcedByUser.get(i).getID().equalsIgnoreCase(linkIds.get(j))) {
					linksForcedByUser.remove(i);
					logger.info("link " + linkIds.get(j) + " has been removed from user selected links.");
				}
			}
		}
		align();
	}
	
	public void clearAllUserLinks() {
		linksForcedByUser.clear();
		logger.info("user selected links have been cleared.");
		align();
	}
	
	private boolean duplicate(SemanticType st1, SemanticType st2) {
		if (st1.getHNodeId().equalsIgnoreCase(st2.getHNodeId()) &&
				st1.getType().getUriString().equalsIgnoreCase(st2.getType().getUriString()) &&
				st1.isPartOfKey() == st2.isPartOfKey()) {
			if (st1.getDomain() != null && st2.getDomain() != null) 
				if (st1.getDomain().getUriString().equalsIgnoreCase(st2.getDomain().getUriString()))
					return true;
			
			if (st1.getDomain() == null && st2.getDomain() == null)
				return true;
			
			return false;
		}
		return false;
	}
	
	public void updateSemanticTypes(List<SemanticType> semanticTypes) {
		
		List<SemanticType> updatedSemanticTypes = new ArrayList<SemanticType>();
		List<Node> deletedVertices = new ArrayList<Node>(); 
		for (SemanticType s : semanticTypes)
			logger.debug("%%%%%%%%%%%%%%%%%%%" + s.getType().getUriString());
		
		for (SemanticType newType : semanticTypes) {
			boolean found = false;
			for (SemanticType prevType : this.semanticTypes) {				
				if (duplicate(newType, prevType))
					found = true;
			}
			if (!found) {
				logger.debug(">>>>>>>>new>>>>>>" + newType.getType().getUriString());
				this.graphBuilder.addSemanticType(newType);
				updatedSemanticTypes.add(newType);
			}
		}
		for (SemanticType prevType : this.semanticTypes) {
			boolean found = false;
			for (SemanticType newType : semanticTypes) {

				if (duplicate(newType, prevType)) {
					found = true;
					updatedSemanticTypes.add(prevType);
				}
			}
			if (!found) {
				Node deletedNode = this.graphBuilder.removeSemanticType(prevType);
				logger.debug("<<<<<<<<<delete<<<<<<<<<" + prevType.getType().getUriString());
				if (deletedNode != null) deletedVertices.add(deletedNode);
			}
		}
		this.semanticTypes = updatedSemanticTypes;
		this.semanticNodes = this.graphBuilder.getSemanticNodes();
		removeInvalidForcedLinks(deletedVertices);
		align();
	}
	
	
	public void duplicateDomainOfLink(String linkId) {
		
//		GraphUtil.printGraph(this.graphBuilder.getGraph());

		Link[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new Link[0]);
		Node source, target;
		
		
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				
				source = allLinks[i].getSource();
				target = allLinks[i].getTarget();
				
				Node v = this.graphBuilder.copyNode(source);
				this.graphBuilder.copyLinks(source, v);
				target.setDomainVertexId(v.getID());
				
//				GraphUtil.printGraph(this.graphBuilder.getGraph());
				addToLinksForcedByUserList(this.graphBuilder.getGraph().getEdge(v, target));
				
				// do we need to keep the outgoing links of the source which are already in the tree? 
				
				logger.info("domain of the link " + linkId + " has been replicated and graph has been changed successfully.");
				align();
				duplicatedLinkIds.add(linkId);
				return;
				
			}
		}
		
		logger.info("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void reset() {
		
		graphBuilder = new GraphBuilder(ontologyManager, this.semanticTypes, separateDomainInstancesForSameDataProperties);
		linksForcedByUser.clear();
		semanticNodes = graphBuilder.getSemanticNodes();
		align();
	}
	
	public Link getAssignedLink(String nodeId) {
		
		for (Node v : this.steinerTree.vertexSet()) {
			if (v.getID().equalsIgnoreCase(nodeId)) {
				Link[] incomingLinks = this.steinerTree.incomingEdgesOf(v).toArray(new Link[0]);
				if (incomingLinks != null && incomingLinks.length == 1)
					return incomingLinks[0];
			}
		}
		return null;
	}
	
	public List<Link> getAlternatives(String nodeId, boolean includeAssignedLink) {
		
		List<Link> alternatives = new ArrayList<Link>();
		Link assignedLink = null;
		
		if (!includeAssignedLink)
			assignedLink = getAssignedLink(nodeId);

		List<String> displayedNodes = new ArrayList<String>();
		for (Node v : this.steinerTree.vertexSet())
			displayedNodes.add(v.getID());
		
		for (Node v : this.graphBuilder.getGraph().vertexSet()) {
			if (v.getID().equalsIgnoreCase(nodeId)) {
				Link[] incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(v).toArray(new Link[0]);
				if (incomingLinks != null && incomingLinks.length > 0) {
					
					for (int i = 0; i < incomingLinks.length; i++) {
						if (!includeAssignedLink) {
							if (assignedLink.getID().equalsIgnoreCase(incomingLinks[i].getID()))
								continue;
						}
						
						// if the node is not in the UI, don't show it to the user
						// Scenario: multiple domain, then again merge it. The created node is in the graph but not in the tree.
//						if (displayedNodes.indexOf(incomingLinks[i].getSource().getID()) == -1)
//							continue;
						
						alternatives.add(incomingLinks[i]);
					}
				}
			}
		}
		return alternatives;
	}
	
	private void updateLinksStatus() {
		// order of adding lists is important: linksPreferredByUI should be first 
		for (Link e : linksPreferredByUI)
			e.setLinkStatus(LinkStatus.PreferredByUI);
		for (Link e : linksForcedByUser)
			e.setLinkStatus(LinkStatus.ForcedByUser);
	}
	
	private void addUILink(String linkId) {
		Link[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new Link[0]);
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				linksPreferredByUI.add(allLinks[i]);
				logger.debug("link " + linkId + " has been added to preferred UI links.");
				return;
			}
		}
		
		logger.info("link with ID " + linkId + " does not exist in graph.");
	}
	
	private void addUILinksFromTree() {
		linksPreferredByUI.clear();
		
		if (this.steinerTree == null)
			return;
		
		for (Link e: this.steinerTree.edgeSet()) {
			addUILink(e.getID());
		}
	}
	
	private List<Link> buildSelectedLinks() {
		List<Link> selectedLinks = new ArrayList<Link>();

		addUILinksFromTree();
		updateLinksStatus();

		selectedLinks.addAll(linksPreferredByUI);
		selectedLinks.addAll(linksForcedByUser);

		return selectedLinks;
	}
	
	private void align() {
		
//		GraphUtil.printGraph(this.graphBuilder.getGraph());
		long start = System.currentTimeMillis();
		
		logger.info("preparing G Prime for steiner algorithm input ...");
		
		List<Link> selectedLinks = buildSelectedLinks();
		// order of adding lists is important: linksPreferredByUI should be first 
		
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), semanticNodes, selectedLinks );
		UndirectedGraph<Node, Link> undirectedGraph = graphPreProcess.getUndirectedGraph();
//		GraphUtil.printGraph(undirectedGraph);
		List<Node> steinerNodes = graphPreProcess.getSteinerNodes();

		logger.info("computing steiner tree ...");

		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Node, Link> tree = steinerTree.getSteinerTree();
		if (tree == null) {
			logger.info("resulting tree is null ...");
			return;
		}
//		GraphUtil.printGraphSimple(tree);
		
		logger.info("updating link directions ...");
		TreePostProcess treePostProcess = new TreePostProcess(tree);
		removeInvalidForcedLinks(treePostProcess.getDangledVertexList());
		
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
		if (this.steinerTree == null)
			align();
		
		// GraphUtil.printGraph(this.steinerTree);
		return this.steinerTree;
	}

	public DirectedWeightedMultigraph<Node, Link> getAlignmentGraph() {
		return this.graphBuilder.getGraph();
	}
	
	/**** TO BE IMPLEMENTED ***/
	public Link getLinkById(String linkId) {
		return graphBuilder.getIdToLinks().get(linkId);
	}
	
	public List<Link> getLinksByType(String uriString) {
		return graphBuilder.getUriToLinks().get(uriString);
	}
	
	public Node getNodeById(String nodeId) {
		return graphBuilder.getIdToNodes().get(nodeId);
	}
	
	public List<Node> getNodesByType(String uriString) {
		return graphBuilder.getUriToNodes().get(uriString);
	}
	
	// AddNode methods
	
	public ColumnNode addColumnNode(String hNodeId, String columnName) {
		String id = nodeIdFactory.getNodeId(hNodeId);
		Label label = new Label(columnName);
		ColumnNode node = new ColumnNode(id, label, hNodeId);
		if (this.graphBuilder.addNode(node)) return node;
		return null;
	}
	
	public InternalNode addInternalClassNode(Label label) {
		String id = nodeIdFactory.getNodeId(label.getUriString());
		InternalNode node = new InternalNode(id, label);
		if (this.graphBuilder.addNode(node)) return node;
		return null;	
	}
	
	// AddLink methods

	public DataPropertyLink addDataPropertyLink(Node source, Node target, Label label, boolean isPartOfKey) {
		String id = linkIdFactory.getLinkId(label.getUriString());	
		DataPropertyLink link = new DataPropertyLink(id, label, isPartOfKey);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	// Probably we don't need this function in the interface to GUI
	public ObjectPropertyLink addObjectPropertyLink(Node source, Node target, Label label, boolean isPartOfKey) {
		String id = linkIdFactory.getLinkId(label.getUriString());		
		ObjectPropertyLink link = new ObjectPropertyLink(id, label, isPartOfKey);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	// Probably we don't need this function in the interface to GUI
	public SubClassOfLink addSubClassOfLink(Node source, Node target) {
		String id = linkIdFactory.getLinkId(FixedUris.RDFS_SUBCLASS_OF_URI);
		SubClassOfLink link = new SubClassOfLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public ClassLink addClassLink(Node source, Node target, boolean isPartOfKey) {
		String id = linkIdFactory.getLinkId(FixedUris.CLASS_LINK_URI);
		ClassLink link = new ClassLink(id, isPartOfKey);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	public DataPropertyOfColumnLink addDataPropertyOfColumnLink(Node source, Node target) {
		String id = linkIdFactory.getLinkId(FixedUris.DATAPROPERTY_OF_COLUMN_LINK_URI);
		DataPropertyOfColumnLink link = new DataPropertyOfColumnLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}

	public ColumnSubClassOfLink addColumnSubClassOfLink(Node source, Node target) {
		String id = linkIdFactory.getLinkId(FixedUris.COLUMN_SUBCLASS_OF_LINK_URI);
		ColumnSubClassOfLink link = new ColumnSubClassOfLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;	
	}
	
	public UriOfClassLink addURIOfClassLink(Node source, Node target) {
		String id = linkIdFactory.getLinkId(FixedUris.URI_OF_CLASS_LINK_URI);
		UriOfClassLink link = new UriOfClassLink(id);
		if (this.graphBuilder.addLink(source, target, link)) return link;
		return null;
	}
	
	public ColumnNode getColumnNodeByHNodeId(String hNodeId) {
		return null;
	}
	
	public void addLinkAndUpdateAlignment(Link link) {
		
	}
	
	// Used in the case of semantic types
	public void deleteLink(String linkId) {
		
	}
	
	public void changeLinkStatus(String linkId, LinkStatus status) {
		
	}
	
	public List<Node> getAllGraphNodes() {
		return null;
	}
}
