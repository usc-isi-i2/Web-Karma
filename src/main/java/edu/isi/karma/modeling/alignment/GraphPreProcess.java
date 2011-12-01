package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class GraphPreProcess {

	static Logger logger = Logger.getLogger(GraphPreProcess.class);

	DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph;
	DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> gPrime;
	List<Vertex> semanticNodes;
	List<LabeledWeightedEdge> selectedLinks;
	List<Vertex> steinerNodes;
	
	public GraphPreProcess(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph, 
			List<Vertex> semanticNodes, List<LabeledWeightedEdge> selectedLinks) {
		this.graph = graph;
		this.semanticNodes = semanticNodes;
		this.selectedLinks = selectedLinks;
		// copy all semantic nodes into steiner nodes
		this.steinerNodes = new ArrayList<Vertex>(semanticNodes); 

		createDirectedGPrime();
	}
	
	@SuppressWarnings("unchecked")
	private void createDirectedGPrime() {
		logger.debug("<enter");
		
		gPrime = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>)this.graph.clone();
		
		if (selectedLinks == null || selectedLinks.size() == 0) {
			logger.debug("exit>");
			return;
		}
		LabeledWeightedEdge e;
		
		for (int i = 0; i < selectedLinks.size(); i++) {
			
			Vertex source = selectedLinks.get(i).getSource();
			Vertex target = selectedLinks.get(i).getTarget();
			
			if (!steinerNodes.contains(source))
				steinerNodes.add(source);

			if (!steinerNodes.contains(target))
				steinerNodes.add(target);
			
			e = (LabeledWeightedEdge)selectedLinks.get(i).clone();
			
			// removing all links to target
			LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(target).toArray(new LabeledWeightedEdge[0]); 
			for (LabeledWeightedEdge inLink: incomingLinks) {
				gPrime.removeAllEdges( inLink.getSource(), inLink.getTarget() );
			}
			
			// removing all links from source to target
//			gPrime.removeAllEdges(source, target);
			
			// adding the user selected link
			gPrime.addEdge(source, target, e);
			
			// if it is a subclass link, change the weight to epsilon
			//if (e.getType() == LinkType.HasSubClass)
			gPrime.setEdgeWeight(e, GraphBuilder.MIN_WEIGHT);
			
		}
		
		// if there are 2 DataProperties go to one node, we have to select only one of them. 
		// The target is definitely one of the source columns and we cannot have two classes pointed to that.
		// User can change our selected link later.
		
		for (Vertex v: gPrime.vertexSet()) {
			
			if (v.getNodeType() != NodeType.DataProperty)
				continue;
			
			LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
			if (incomingLinks != null && incomingLinks.length != 0) {
				// keeping only the first link and remove the others.
				for (int i = 1; i < incomingLinks.length; i++)
					gPrime.removeEdge(incomingLinks[i]);
			}
		}
		
		logger.debug("exit>");
//		GraphUtil.printGraphSimple(gPrime);
	}
	
	public List<Vertex> getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public UndirectedGraph<Vertex, LabeledWeightedEdge> getUndirectedGraph() {
		
		return  new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.gPrime);
	}
	
}
