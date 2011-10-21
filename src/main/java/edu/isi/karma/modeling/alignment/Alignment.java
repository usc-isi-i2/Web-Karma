package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import edu.isi.karma.modeling.NameSet;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);

	private List<NameSet> semanticTypes;
	private List<Vertex> semanticNodes;

	private List<LabeledWeightedEdge> selectedLinks;

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = null;
	
	private GraphBuilder graphBuilder;
	
	public Alignment(List<NameSet> semanticTypes) {
		this.semanticTypes = semanticTypes;

		logger.debug("building initial graph ...");
		graphBuilder = new GraphBuilder(this.semanticTypes);
		
		selectedLinks = new ArrayList<LabeledWeightedEdge>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	
	public List<NameSet> getSemanticTypes() {
		return this.semanticTypes;
	}
	
	public void addUserLink(String linkId) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				selectedLinks.add(allLinks[i]);
				logger.debug("link " + linkId + " has been added.");
				align();
				return;
			}
		}
		
		logger.debug("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void addUserLinks(List<String> linkIds) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		for (int j = 0; j < linkIds.size(); j++) {
			boolean found = false;
			for (int i = 0; i < allLinks.length; i++) {
				if (allLinks[i].getID().equalsIgnoreCase(linkIds.get(j))) {
					selectedLinks.add(allLinks[i]);
					found = true;
					logger.debug("link " + linkIds.get(j) + " has been added.");
				}
			}
			if (!found)
				logger.debug("link with ID " + linkIds.get(j) + " does not exist in graph.");
		}
		align();
	}
	
	public void clearUserLink(String linkId) {
		for (int i = 0; i < selectedLinks.size(); i++) {
			if (selectedLinks.get(i).getID().equalsIgnoreCase(linkId)) {
				selectedLinks.remove(i);
				logger.debug("link " + linkId + " has been removed.");
				align();
				return;
			}
		}
	}
	
	public void clearUserLinks(List<String> linkIds) {
		for (int j = 0; j < linkIds.size(); j++) {
			for (int i = 0; i < selectedLinks.size(); i++) {
				if (selectedLinks.get(i).getID().equalsIgnoreCase(linkIds.get(j))) {
					selectedLinks.remove(i);
					logger.debug("link " + linkIds.get(j) + " has been removed.");
				}
			}
		}
		align();
	}
	
	public void clearAllUserLinks() {
		selectedLinks.clear();
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
				
//				GraphUtil.printGraph(this.graphBuilder.getGraph());
				selectedLinks.add(this.graphBuilder.getGraph().getEdge(v, target));
				
				// do we need to keep the outgoing links of the source which are already in the tree? 
				
				logger.debug("domain of the link " + linkId + " has been replicated and graph has been changed successfully.");
				align();
				return;
				
			}
		}
		
		logger.debug("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void reset() {
		
		graphBuilder = new GraphBuilder(this.semanticTypes);
		selectedLinks.clear();
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
		
		for (Vertex v : this.graphBuilder.getGraph().vertexSet()) {
			if (v.getID().equalsIgnoreCase(nodeId)) {
				LabeledWeightedEdge[] incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
				if (incomingLinks != null && incomingLinks.length > 0) {
					
					for (int i = 0; i < incomingLinks.length; i++) {
						if (!includeAssignedLink) {
							if (assignedLink.getID().equalsIgnoreCase(incomingLinks[i].getID()))
								continue;
						}
						alternatives.add(incomingLinks[i]);
					}
				}
			}
		}
		return alternatives;
	}
	
	private void align() {
		
//		GraphUtil.printGraph(this.graph);

		logger.debug("preparing G Prime for steiner algorithm input ...");
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), semanticNodes, selectedLinks);
		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph = graphPreProcess.getUndirectedGraph();
		List<Vertex> steinerNodes = graphPreProcess.getSteinerNodes();
//		GraphUtil.printGraph(undirectedGraph);

		logger.debug("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		WeightedMultigraph<Vertex, LabeledWeightedEdge> tree = steinerTree.getSteinerTree();
		
		logger.debug("updating link directions ...");
		TreePostProcess treePostProcess = new TreePostProcess(tree);
		
		this.steinerTree = treePostProcess.getTree();
	}

	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getSteinerTree() {
		if (this.steinerTree == null)
			align();
		
		return this.steinerTree;
	}
}
