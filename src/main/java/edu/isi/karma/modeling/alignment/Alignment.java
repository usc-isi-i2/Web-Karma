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
import edu.isi.karma.rep.semantictypes.SemanticType;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);

	private class SemanticTypeComparator implements Comparator<SemanticType> {
	    @Override
	    public int compare(SemanticType o1, SemanticType o2) {
	    	String s1 = (o1.getDomain() != null?o1.getDomain().getUriString():"") + o1.getType().getUriString();
	    	String s2 = (o2.getDomain() != null?o2.getDomain().getUriString():"") + o2.getType().getUriString();
	        return s1.compareTo(s2);
	    }
	}
	
	private OntologyManager ontologyManager;
	private boolean separateDomainInstancesForSameDataProperties;
	private List<SemanticType> semanticTypes;
	private List<Vertex> semanticNodes;

	private List<LabeledWeightedEdge> linksForcedByUser;
	private List<LabeledWeightedEdge> linksPreferredByUI;

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = null;
	private Vertex root = null;
	
	private GraphBuilder graphBuilder;
	
	public Alignment(OntologyManager ontologyManager, List<SemanticType> semanticTypes) {
		this.ontologyManager = ontologyManager;
		this.separateDomainInstancesForSameDataProperties = true;

		this.semanticTypes = semanticTypes;
		Collections.sort(this.semanticTypes, new SemanticTypeComparator());

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, this.semanticTypes, separateDomainInstancesForSameDataProperties);
		
		linksForcedByUser = new ArrayList<LabeledWeightedEdge>();
		linksPreferredByUI = new ArrayList<LabeledWeightedEdge>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	
	public List<LabeledWeightedEdge> getLinksForcedByUser() {
		return linksForcedByUser;
	}
	
	public Alignment(OntologyManager ontologyManager, List<SemanticType> semanticTypes, boolean separateDomainInstancesForSameDataProperties) {
		this.ontologyManager = ontologyManager;
		this.separateDomainInstancesForSameDataProperties = separateDomainInstancesForSameDataProperties;
		
		this.semanticTypes = semanticTypes;
		Collections.sort(this.semanticTypes, new SemanticTypeComparator());

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(ontologyManager, this.semanticTypes, separateDomainInstancesForSameDataProperties);
		
		linksForcedByUser = new ArrayList<LabeledWeightedEdge>();
		linksPreferredByUI = new ArrayList<LabeledWeightedEdge>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	public List<SemanticType> getSemanticTypes() {
		return this.semanticTypes;
	}
	
	private void addToLinksForcedByUserList(LabeledWeightedEdge e) {
		LabeledWeightedEdge[] links = linksForcedByUser.toArray(new LabeledWeightedEdge[0]);
		for (LabeledWeightedEdge link : links) {
			if (link.getTarget().getID().equalsIgnoreCase(e.getTarget().getID()))
				clearUserLink(link.getID());
		}
		linksForcedByUser.add(e);
		logger.info("link " + e.getID() + " has been added to user selected links.");
	}
	
	private void removeInvalidForcedLinks(List<Vertex> dangledVertexList) {
		LabeledWeightedEdge[] links = linksForcedByUser.toArray(new LabeledWeightedEdge[0]);
		for (LabeledWeightedEdge link : links) {
			for (Vertex v : dangledVertexList) {
				if (link.getTarget().getID().equalsIgnoreCase(v.getID()) || 
						link.getSource().getID().equalsIgnoreCase(v.getID()))
					clearUserLink(link.getID());
			}
		}
	}
	
	public void addUserLink(String linkId) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				addToLinksForcedByUserList(allLinks[i]);
				align();
				return;
			}
		}
		
		logger.info("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void addUserLinks(List<String> linkIds) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		for (int j = 0; j < linkIds.size(); j++) {
			boolean found = false;
			for (int i = 0; i < allLinks.length; i++) {
				if (allLinks[i].getID().equalsIgnoreCase(linkIds.get(j))) {
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
	
	
	public void duplicateDomainOfLink(String linkId) {
		
//		GraphUtil.printGraph(this.graphBuilder.getGraph());

		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		Vertex source, target;
		
		
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				
				source = allLinks[i].getSource();
				target = allLinks[i].getTarget();
				
				Vertex v = this.graphBuilder.copyNode(source);
				this.graphBuilder.copyLinks(source, v);
				target.setDomainVertexId(v.getID());
				
//				GraphUtil.printGraph(this.graphBuilder.getGraph());
				addToLinksForcedByUserList(this.graphBuilder.getGraph().getEdge(v, target));
				
				// do we need to keep the outgoing links of the source which are already in the tree? 
				
				logger.info("domain of the link " + linkId + " has been replicated and graph has been changed successfully.");
				align();
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
	
	public LabeledWeightedEdge getAssignedLink(String nodeId) {
		
		for (Vertex v : this.steinerTree.vertexSet()) {
			if (v.getID().equalsIgnoreCase(nodeId)) {
				LabeledWeightedEdge[] incomingLinks = this.steinerTree.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
				if (incomingLinks != null && incomingLinks.length == 1)
					return incomingLinks[0];
			}
		}
		return null;
	}
	
	public List<LabeledWeightedEdge> getAlternatives(String nodeId, boolean includeAssignedLink) {
		
		List<LabeledWeightedEdge> alternatives = new ArrayList<LabeledWeightedEdge>();
		LabeledWeightedEdge assignedLink = null;
		
		if (!includeAssignedLink)
			assignedLink = getAssignedLink(nodeId);

		List<String> displayedNodes = new ArrayList<String>();
		for (Vertex v : this.steinerTree.vertexSet())
			displayedNodes.add(v.getID());
		
		for (Vertex v : this.graphBuilder.getGraph().vertexSet()) {
			if (v.getID().equalsIgnoreCase(nodeId)) {
				LabeledWeightedEdge[] incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
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
		for (LabeledWeightedEdge e : linksPreferredByUI)
			e.setLinkStatus(LinkStatus.PreferredByUI);
		for (LabeledWeightedEdge e : linksForcedByUser)
			e.setLinkStatus(LinkStatus.ForcedByUser);
	}
	
	private void addUILink(String linkId) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
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
		
		for (LabeledWeightedEdge e: this.steinerTree.edgeSet()) {
			addUILink(e.getID());
		}
	}
	
	private List<LabeledWeightedEdge> buildSelectedLinks() {
		List<LabeledWeightedEdge> selectedLinks = new ArrayList<LabeledWeightedEdge>();

		addUILinksFromTree();
		updateLinksStatus();

		selectedLinks.addAll(linksPreferredByUI);
		selectedLinks.addAll(linksForcedByUser);

		return selectedLinks;
	}
	
	private void align() {
		

		long start = System.currentTimeMillis();
		
		logger.info("preparing G Prime for steiner algorithm input ...");
		
		List<LabeledWeightedEdge> selectedLinks = buildSelectedLinks();
		// order of adding lists is important: linksPreferredByUI should be first 
		
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), semanticNodes, selectedLinks );
		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph = graphPreProcess.getUndirectedGraph();
//		GraphUtil.printGraph(undirectedGraph);
		List<Vertex> steinerNodes = graphPreProcess.getSteinerNodes();

		logger.info("computing steiner tree ...");

		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Vertex, LabeledWeightedEdge> tree = steinerTree.getSteinerTree();
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

	public Vertex GetTreeRoot() {
		return this.root;
	}
	
	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getSteinerTree() {
		if (this.steinerTree == null)
			align();
		
		// GraphUtil.printGraph(this.steinerTree);
		return this.steinerTree;
	}

	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getAlignmentGraph() {
		return this.graphBuilder.getGraph();
	}
	
	// Reversing the inverse links
	//THIS IS AN IMPORTANT METHOD. DO NOT REMOVE!!!!! (mariam)
	public void updateLinksDirections(Vertex root, LabeledWeightedEdge e, DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone) {

		if (root == null)
			return;
		Vertex source, target;
		LabeledWeightedEdge inLink;
		
		LabeledWeightedEdge[] incomingLinks = treeClone.incomingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);
		if (incomingLinks != null && incomingLinks.length != 0) {
			for (int i = 0; i < incomingLinks.length; i++) {
				
				inLink = incomingLinks[i];
				source = inLink.getSource();
				target = inLink.getTarget();
				// don't remove the incoming link from parent to this node
				if (inLink.getID().equalsIgnoreCase(e.getID())){
					continue;
				}
				
				LabeledWeightedEdge inverseLink = new LabeledWeightedEdge(inLink.getID(), new URI(inLink.getUriString(), inLink.getNs(), inLink.getPrefix()), inLink.getLinkType(), true);
				treeClone.addEdge(target, source, inverseLink);
				treeClone.setEdgeWeight(inverseLink, inLink.getWeight());
				treeClone.removeEdge(inLink);
				GraphUtil.printGraph(treeClone);
			}
		}

		LabeledWeightedEdge[] outgoingLinks = treeClone.outgoingEdgesOf(root).toArray(new LabeledWeightedEdge[0]);

		if (outgoingLinks == null || outgoingLinks.length == 0)
			return;
		for (int i = 0; i < outgoingLinks.length; i++) {
			target = outgoingLinks[i].getTarget();
			updateLinksDirections(target, outgoingLinks[i], treeClone);
		}
	}	

}
