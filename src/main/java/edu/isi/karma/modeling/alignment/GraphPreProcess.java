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
		LinkStatus status;
		
		for (int i = 0; i < selectedLinks.size(); i++) {
			
			e = (LabeledWeightedEdge)selectedLinks.get(i);
			status = e.getLinkStatus();
			
			if (status == LinkStatus.PreferredByUI) {
				gPrime.setEdgeWeight(e, GraphBuilder.DEFAULT_WEIGHT - GraphBuilder.MIN_WEIGHT);
				
			} else if (status == LinkStatus.ForcedByUser || status == LinkStatus.ForcedByDomain) {
				
				e = (LabeledWeightedEdge)selectedLinks.get(i);
				
				Vertex source = selectedLinks.get(i).getSource();
				Vertex target = selectedLinks.get(i).getTarget();
				
				if (!steinerNodes.contains(source))
					steinerNodes.add(source);
	
				if (!steinerNodes.contains(target))
					steinerNodes.add(target);
				
				// removing all links to target
				LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(target).toArray(new LabeledWeightedEdge[0]); 
				for (LabeledWeightedEdge inLink: incomingLinks) {
					gPrime.removeAllEdges( inLink.getSource(), inLink.getTarget() );
				}
	
				// adding the user selected link
				gPrime.addEdge(source, target, e);
				
				// if it is a subclass link, change the weight to epsilon
				//if (e.getType() == LinkType.HasSubClass)
				gPrime.setEdgeWeight(e, GraphBuilder.MIN_WEIGHT);
			}			
		}
		
//		GraphUtil.printGraph(gPrime);
		
		// if there are 2 DataProperties go to one node, we have to select only one of them. 
		// The target is definitely one of the source columns and we cannot have two classes pointed to that.
		// User can change our selected link later.
		
//		for (Vertex v: gPrime.vertexSet()) {
//			
//			if (v.getNodeType() != NodeType.DataProperty)
//				continue;
//			
//			double weight;
//			int minIndex;
//			double minWeight;
//			
//			LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
//			if (incomingLinks != null && incomingLinks.length != 0) {
//				
//				minWeight = GraphBuilder.MAX_WEIGHT;
//				minIndex = 0;
//				// keeping only the link with minimum weight and remove the others.
//				// we select minimum to prefer the UI links in previous model.
//				for (int i = 0; i < incomingLinks.length; i++) {
//					weight = gPrime.getEdgeWeight(incomingLinks[i]);
//					if (weight < minWeight) {
//						minWeight = weight;
//						minIndex = i;
//					}
//				}
//				for (int i = 0; i < incomingLinks.length; i++)
//					if (i != minIndex)
//					gPrime.removeEdge(incomingLinks[i]);
//			}
//		}
		
		logger.debug("exit>");
//		GraphUtil.printGraph(gPrime);
	}
	
	public List<Vertex> getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public UndirectedGraph<Vertex, LabeledWeightedEdge> getUndirectedGraph() {
		
		return  new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.gPrime);
	}
	
}
