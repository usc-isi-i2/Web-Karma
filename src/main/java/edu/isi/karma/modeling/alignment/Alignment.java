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

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ClassLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnMetaPropertyLink;
import edu.isi.karma.rep.alignment.InternalClassNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.PropertyLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SubclassOfNodeMetaPropertyLink;
import edu.isi.karma.rep.alignment.URI;
import edu.isi.karma.rep.alignment.URIOfClassMetaPropertyLink;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);

	private class SemanticTypeComparator implements Comparator<SemanticType> {
	    public int compare(SemanticType o1, SemanticType o2) {
//	    	String s1 = (o1.getDomain() != null?o1.getDomain().getUriString():"") + o1.getType().getUriString();
//	    	String s2 = (o2.getDomain() != null?o2.getDomain().getUriString():"") + o2.getType().getUriString();
	    	String s1 = o1.getHNodeId();
	    	String s2 = o2.getHNodeId();
//	    	return s1.compareTo(s2);
	    	
	    	s1 = s1.replaceFirst("HN", "");
	    	s2 = s2.replaceFirst("HN", "");
	        
	    	int i1 = Integer.valueOf(s1).intValue();
	    	int i2 = Integer.valueOf(s2).intValue();

	    	if (i1 < i2) return -1;
	    	else if (i1 > i2) return 1;
	    	else return 0;
	    }
	}
	
	private OntologyManager ontologyManager;
	private boolean separateDomainInstancesForSameDataProperties;
	private List<SemanticType> semanticTypes;
	private List<Node> semanticNodes;

	private List<Link> linksForcedByUser;
	private List<Link> linksPreferredByUI;
	
	
	private List<String> duplicatedLinkIds;

	private DirectedWeightedMultigraph<Node, Link> steinerTree = null;
	private Node root = null;
	
	private GraphBuilder graphBuilder;
	
	public Alignment(OntologyManager ontologyManager, List<SemanticType> semanticTypes) {
		this.ontologyManager = ontologyManager;
		this.separateDomainInstancesForSameDataProperties = true;

		this.semanticTypes = semanticTypes;
		Collections.sort(this.semanticTypes, new SemanticTypeComparator());

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, this.semanticTypes, separateDomainInstancesForSameDataProperties);
		
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

	public Alignment(OntologyManager ontologyManager, List<SemanticType> semanticTypes, boolean separateDomainInstancesForSameDataProperties) {
		this.ontologyManager = ontologyManager;
		this.separateDomainInstancesForSameDataProperties = separateDomainInstancesForSameDataProperties;
		
		this.semanticTypes = semanticTypes;
		Collections.sort(this.semanticTypes, new SemanticTypeComparator());

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, this.semanticTypes, separateDomainInstancesForSameDataProperties);
		
		linksForcedByUser = new ArrayList<Link>();
		linksPreferredByUI = new ArrayList<Link>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	public List<SemanticType> getSemanticTypes() {
		return this.semanticTypes;
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
		return null;
	}
	
	public Node getNodeById(String nodeId) {
		return null;
	}
	
	public ColumnNode createColumnNode(String hNodeId) {
		return null;
	}
	
	public InternalClassNode createInternalClassNode(URI uri) {
		return null;
	}

	public PropertyLink createPropertyLink(Node source, Node target, URI uri, boolean isPartOfKey) {
		return null;
	}
	
	public ClassLink createClassLink(Node source, Node target, boolean isPartOfKey) {
		return null;
	}
	
	public DataPropertyOfColumnMetaPropertyLink createDataPropertyOfColumnMetaPropertyLink(Node source, Node target) {
		return null;
	}

	public SubclassOfNodeMetaPropertyLink createSubclassOfNodeMetaPropertyLink(Node source, Node target) {
		return null;
	}
	
	public URIOfClassMetaPropertyLink createURIOfClassMetaPropertyLink(Node source, Node target) {
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
