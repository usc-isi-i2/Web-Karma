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
	private static double MIN_WEIGHT = 0.000001; // need to be fixed later	
	
	public GraphPreProcess(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph, 
			List<Vertex> semanticNodes, List<LabeledWeightedEdge> selectedLinks) {
		this.graph = graph;
		this.semanticNodes = semanticNodes;
		this.selectedLinks = selectedLinks;
		// copy all semantic nodes into steiner nodes
		this.steinerNodes = new ArrayList<Vertex>(semanticNodes); 
		gPrime = createDirectedGPrime();
	}
	
	@SuppressWarnings("unchecked")
	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> createDirectedGPrime() {
		
		logger.debug("<enter");
		
		gPrime = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>)this.graph.clone();
		
		if (selectedLinks == null || selectedLinks.size() == 0) {
			logger.debug("exit>");
			return gPrime;
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
			gPrime.setEdgeWeight(e, MIN_WEIGHT);
			
		}
		
		// if there are 2 DataProperties go to one node, we have to select only one of them. 
		// The target is definitely one of the source columns and we cannot have two classes pointed to that.
		// User can change our selected link later.
		
		for (Vertex v: gPrime.vertexSet()) {
			
			if (v.getType() != NodeType.DataProperty)
				continue;
			
			LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
			if (incomingLinks != null && incomingLinks.length != 0) {
				// keeping only the first link and remove the others.
				for (int i = 1; i < incomingLinks.length; i++)
					gPrime.removeEdge(incomingLinks[i]);
			}
		}
		
		logger.debug("exit>");
		return gPrime;
	}
	
	public List<Vertex> getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public UndirectedGraph<Vertex, LabeledWeightedEdge> getUndirectedGraph() {
		
		return  new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.gPrime);
	}
	
	public static void main(String[] args) {

		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> g = 
			new DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		LabeledWeightedEdge e1 = new LabeledWeightedEdge("e1");
		LabeledWeightedEdge e2 = new LabeledWeightedEdge("e2", "e2", LinkType.HasSubClass);
		LabeledWeightedEdge e3 = new LabeledWeightedEdge("e3");
		LabeledWeightedEdge e4 = new LabeledWeightedEdge("e4");
		LabeledWeightedEdge e5 = new LabeledWeightedEdge("e5");
		LabeledWeightedEdge e6 = new LabeledWeightedEdge("e6");
		LabeledWeightedEdge e7 = new LabeledWeightedEdge("e7");
		LabeledWeightedEdge e8 = new LabeledWeightedEdge("e8");
		LabeledWeightedEdge e9 = new LabeledWeightedEdge("e9");
		LabeledWeightedEdge e10 = new LabeledWeightedEdge("e10");


		Vertex v1 = new Vertex("v1");
		Vertex v2 = new Vertex("v2");
		Vertex v3 = new Vertex("v3");
		Vertex v4 = new Vertex("v4");
		Vertex v5 = new Vertex("v5");
		Vertex v6 = new Vertex("v6");
		Vertex v7 = new Vertex("v7");
		Vertex v8 = new Vertex("v8");
		Vertex v9 = new Vertex("v9");
		
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		g.addVertex(v5);
		g.addVertex(v6);
		g.addVertex(v7);
		g.addVertex(v8);
		g.addVertex(v9);
		
		List<Vertex> semanticNodes = new ArrayList<Vertex>();
		semanticNodes.add(v1);
		semanticNodes.add(v2);
		semanticNodes.add(v3);

		List<LabeledWeightedEdge> selectedLinks = new ArrayList<LabeledWeightedEdge>();
		selectedLinks.add(e2);
		selectedLinks.add(e4);
		selectedLinks.add(e10);

		g.addEdge(v1, v2, e1);
		g.addEdge(v2, v3, e2);
		g.addEdge(v3, v4, e3);
		g.addEdge(v4, v5, e4);
		g.addEdge(v2, v4, e5);
		g.addEdge(v2, v3, e6);
		g.addEdge(v5, v3, e7);
		g.addEdge(v3, v6, e8);
		g.addEdge(v4, v6, e9);
		g.addEdge(v3, v6, e10);

		
		g.setEdgeWeight(e1, 10.0);
		g.setEdgeWeight(e2, 8.0);
		g.setEdgeWeight(e3, 9.0);
		g.setEdgeWeight(e4, 2.0);
		g.setEdgeWeight(e5, 5.0);
		g.setEdgeWeight(e6, 7.1);
		g.setEdgeWeight(e7, 4.3);
		g.setEdgeWeight(e8, 5.5);
		g.setEdgeWeight(e9, 0.01);
		g.setEdgeWeight(e10, 0.033);
		
		GraphPreProcess p = new GraphPreProcess(g, semanticNodes, selectedLinks);
		
		GraphUtil.printGraph(p.getUndirectedGraph());
		
		List<Vertex> steinerNodes = p.getSteinerNodes();
		for (int i = 0; i < steinerNodes.size(); i++)
			System.out.println(steinerNodes.get(i).getLabel());
	}
}
