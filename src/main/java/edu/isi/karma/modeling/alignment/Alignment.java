package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import edu.isi.karma.rep.semantictypes.SemanticType;



public class Alignment {

	static Logger logger = Logger.getLogger(Alignment.class);

	private List<SemanticType> semanticTypes;
	private List<Vertex> semanticNodes;

	private List<LabeledWeightedEdge> linksForcedByUser;
	private List<LabeledWeightedEdge> linksForcedByDomain;
	private List<LabeledWeightedEdge> linksPreferredByUI;

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree = null;
	private Vertex root = null;
	
	private GraphBuilder graphBuilder;
	
	public Alignment(List<SemanticType> semanticTypes) {
		this.semanticTypes = semanticTypes;

		logger.info("building initial graph ...");
		graphBuilder = new GraphBuilder(this.semanticTypes);
		linksForcedByDomain = graphBuilder.getLinksForcedByDomain();
		
		linksForcedByUser = new ArrayList<LabeledWeightedEdge>();
		linksPreferredByUI = new ArrayList<LabeledWeightedEdge>();
		
		semanticNodes = graphBuilder.getSemanticNodes();
		
	}
	
	public List<SemanticType> getSemanticTypes() {
		return this.semanticTypes;
	}
	
	public void addUserLink(String linkId) {
		LabeledWeightedEdge[] allLinks =  this.graphBuilder.getGraph().edgeSet().toArray(new LabeledWeightedEdge[0]);
		for (int i = 0; i < allLinks.length; i++) {
			if (allLinks[i].getID().equalsIgnoreCase(linkId)) {
				linksForcedByUser.add(allLinks[i]);
				logger.info("link " + linkId + " has been added to user selected links.");
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
					linksForcedByUser.add(allLinks[i]);
					found = true;
					logger.info("link " + linkIds.get(j) + " has been added to user selected links.");
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
				
//				GraphUtil.printGraph(this.graphBuilder.getGraph());
				linksForcedByUser.add(this.graphBuilder.getGraph().getEdge(v, target));
				
				// do we need to keep the outgoing links of the source which are already in the tree? 
				
				logger.info("domain of the link " + linkId + " has been replicated and graph has been changed successfully.");
				align();
				return;
				
			}
		}
		
		logger.info("link with ID " + linkId + " does not exist in graph.");
	}
	
	public void reset() {
		
		graphBuilder = new GraphBuilder(this.semanticTypes);
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
	
	private void updateLinksStatus() {
		for (LabeledWeightedEdge e : linksForcedByUser)
			e.setLinkStatus(LinkStatus.ForcedByUser);
		for (LabeledWeightedEdge e : linksForcedByDomain)
			e.setLinkStatus(LinkStatus.ForcedByDomain);
		for (LabeledWeightedEdge e : linksPreferredByUI)
			e.setLinkStatus(LinkStatus.PreferredByUI);
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
	
	private void align() {
		
//		GraphUtil.printGraph(this.graph);

		long start = System.currentTimeMillis();
		
		logger.info("preparing G Prime for steiner algorithm input ...");
		
		addUILinksFromTree();
		updateLinksStatus();
		List<LabeledWeightedEdge> selectedLinks = new ArrayList<LabeledWeightedEdge>();
		// order of adding lists is important: linksPreferredByUI should be first 
		selectedLinks.addAll(linksPreferredByUI);
		selectedLinks.addAll(linksForcedByDomain);
		selectedLinks.addAll(linksForcedByUser);
		
		GraphPreProcess graphPreProcess = new GraphPreProcess(this.graphBuilder.getGraph(), semanticNodes, selectedLinks );
		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph = graphPreProcess.getUndirectedGraph();
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
		
		this.steinerTree = treePostProcess.getTree();
		this.root = treePostProcess.getRoot();

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		logger.info("time to compute steiner tree: " + elapsedTimeSec);
	}

	public Vertex GetTreeRoot() {
		return this.root;
	}
	
	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getSteinerTree() {
		if (this.steinerTree == null)
			align();
		
//		GraphUtil.printGraph(this.graphBuilder.getGraph());
		return this.steinerTree;
	}

}
