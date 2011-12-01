package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.WeightedMultigraph;


public class SteinerTree {
	
	static Logger logger = Logger.getLogger(SteinerTree.class);

	UndirectedGraph<Vertex, LabeledWeightedEdge> graph;
	WeightedMultigraph<Vertex, LabeledWeightedEdge> tree;
	List<Vertex> steinerNodes;
	
	public SteinerTree(UndirectedGraph<Vertex, LabeledWeightedEdge> graph, List<Vertex> steinerNodes) {
		this.graph = graph;
		this.steinerNodes = steinerNodes;
		
		runAlgorithm();
	}
	
	private WeightedMultigraph<Vertex, LabeledWeightedEdge> step1() {
		
		logger.debug("<enter");

		WeightedMultigraph<Vertex, LabeledWeightedEdge> g = 
			new WeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		for (int i = 0; i < steinerNodes.size(); i++) {
			g.addVertex(steinerNodes.get(i));
		}
		
		for (LabeledWeightedEdge e: this.graph.edgeSet()) {
			g.addVertex(e.getSource());
			g.addVertex(e.getTarget());
			g.addEdge(e.getSource(), e.getTarget(), e);
		}
		
		
		BellmanFordShortestPath<Vertex, LabeledWeightedEdge> path;
		
		for (int i = 0; i < steinerNodes.size(); i++) {
			path = new BellmanFordShortestPath<Vertex, LabeledWeightedEdge>(this.graph, steinerNodes.get(i));
			
			for (int j = 0; j < steinerNodes.size(); j++) {
				
				if (i == j)
					continue;
				
				if (g.containsEdge(steinerNodes.get(i), steinerNodes.get(j)))
					continue;
				
				LabeledWeightedEdge e = new LabeledWeightedEdge("e" + String.valueOf(i) + String.valueOf(j));
				g.addEdge(steinerNodes.get(i), steinerNodes.get(j), e);
				g.setEdgeWeight(e, path.getCost(steinerNodes.get(j)));
				
			}

		}
		
		logger.debug("exit>");

		return g;

	}
	
	private WeightedMultigraph<Vertex, LabeledWeightedEdge> step2(WeightedMultigraph<Vertex, LabeledWeightedEdge> g1) {

		logger.debug("<enter");

		KruskalMinimumSpanningTree<Vertex, LabeledWeightedEdge> mst =
            new KruskalMinimumSpanningTree<Vertex, LabeledWeightedEdge>(g1);

//    	System.out.println("Total MST Cost: " + mst.getSpanningTreeCost());

        Set<LabeledWeightedEdge> edges = mst.getEdgeSet();

		WeightedMultigraph<Vertex, LabeledWeightedEdge> g2 = 
			new WeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		List<LabeledWeightedEdge> edgesSortedByLabel = new ArrayList<LabeledWeightedEdge>();
		
		for (LabeledWeightedEdge e : edges) 
			edgesSortedByLabel.add(e);
		
		Collections.sort(edgesSortedByLabel, new EdgeComparatorByLabel());
		
		for (LabeledWeightedEdge edge : edgesSortedByLabel) {

//			//just for test, forcing to select another equal minimal spanning tree
//			if (g1.getEdgeSource(edge).getLabel().equalsIgnoreCase("v1") && 
//					g1.getEdgeTarget(edge).getLabel().equalsIgnoreCase("v3") ) {
//				Vertex v2 = steinerNodes.get(1);
//				g2.addVertex(g1.getEdgeTarget(edge));
//				g2.addVertex(v2);
//				LabeledWeightedEdge e = new LabeledWeightedEdge("e");
//				g2.setEdgeWeight(e, g1.getEdgeWeight(edge));
//				g2.addEdge(g1.getEdgeTarget(edge), v2, e);
//			} else 
			{
				g2.addVertex(edge.getSource());
				g2.addVertex(edge.getTarget());
				g2.addEdge( edge.getSource(), edge.getTarget(), edge); 
			}
		}
		
		logger.debug("exit>");

		return g2;
	}
	
	private WeightedMultigraph<Vertex, LabeledWeightedEdge> step3(WeightedMultigraph<Vertex, LabeledWeightedEdge> g2) {
		
		logger.debug("<enter");

		WeightedMultigraph<Vertex, LabeledWeightedEdge> g3 = 
			new WeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		Set<LabeledWeightedEdge> edges = g2.edgeSet();
		DijkstraShortestPath<Vertex, LabeledWeightedEdge> path;
		
		Vertex source, target;
		
		for (LabeledWeightedEdge edge : edges) {
			source = edge.getSource();
			target = edge.getTarget();
			
			path = new DijkstraShortestPath<Vertex, LabeledWeightedEdge>(this.graph, source, target);
			List<LabeledWeightedEdge> pathEdges = path.getPathEdgeList();
			
			if (pathEdges == null)
				continue;
			
			for (int i = 0; i < pathEdges.size(); i++) {
				
				if (g3.edgeSet().contains(pathEdges.get(i)))
					continue;
				
				source = pathEdges.get(i).getSource();
				target = pathEdges.get(i).getTarget();
				
				if (!g3.vertexSet().contains(source) )
					g3.addVertex(source);

				if (!g3.vertexSet().contains(target) )
					g3.addVertex(target);

				g3.addEdge(source, target, pathEdges.get(i));
			}
		}

		logger.debug("exit>");

		return g3;
	}
	
	private WeightedMultigraph<Vertex, LabeledWeightedEdge> step4(WeightedMultigraph<Vertex, LabeledWeightedEdge> g3) {

		logger.debug("<enter");
		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g4 = step2(g3);
		
		logger.debug("exit>");
		
		return g4;
	}
	
	private WeightedMultigraph<Vertex, LabeledWeightedEdge> step5(WeightedMultigraph<Vertex, LabeledWeightedEdge> g4) {
		
		logger.debug("<enter");

		WeightedMultigraph<Vertex, LabeledWeightedEdge> g5 = g4; 

		List<Vertex> nonSteinerLeaves = new ArrayList<Vertex>();
		
		Set<Vertex> vertexSet = g4.vertexSet();
		for (Vertex vertex : vertexSet) {
			if (g5.degreeOf(vertex) == 1 && steinerNodes.indexOf(vertex) == -1) {
				nonSteinerLeaves.add(vertex);
			}
		}
		
		Vertex source, target;
		for (int i = 0; i < nonSteinerLeaves.size(); i++) {
			source = nonSteinerLeaves.get(i);
			do {
				LabeledWeightedEdge e = g5.edgesOf(source).toArray(new LabeledWeightedEdge[0])[0];
				target = this.graph.getEdgeTarget(e);
				
				// this should not happen, but just in case of ...
				if (target.equals(source)) 
					target = e.getSource();
				
				g5.removeVertex(source);
				source = target;
			} while(g5.degreeOf(source) == 1 && steinerNodes.indexOf(source) == -1);
			
		}
		
		logger.debug("exit>");

		return g5;
	}
	
	private void runAlgorithm() {
		
		logger.debug("<enter");

		WeightedMultigraph<Vertex, LabeledWeightedEdge> g1 = step1();
//		GraphUtil.printGraph(g1);
//		GraphUtil.printGraphSimple(g1);
		
		if (g1.vertexSet().size() < 2) {
			this.tree = g1;
			return;
		}
		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g2 = step2(g1);
//		GraphUtil.printGraph(g2);
//		GraphUtil.printGraphSimple(g2);

		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g3 = step3(g2);
//		GraphUtil.printGraph(g3);
//		GraphUtil.printGraphSimple(g3);
		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g4 = step4(g3);
//		GraphUtil.printGraph(g4);
//		GraphUtil.printGraphSimple(g4);

		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g5 = step5(g4);
//		GraphUtil.printGraph(g5);
//		GraphUtil.printGraphSimple(g5);
		
		this.tree = g5;
		logger.debug("exit>");

	}
	
	public WeightedMultigraph<Vertex, LabeledWeightedEdge> getSteinerTree() {
		return this.tree;
	}
	
	public static void main(String[] args) {
		
		WeightedMultigraph<Vertex, LabeledWeightedEdge> g = 
			new WeightedMultigraph<Vertex, LabeledWeightedEdge>(LabeledWeightedEdge.class);
		
		LabeledWeightedEdge e1 = new LabeledWeightedEdge("e1");
		LabeledWeightedEdge e2 = new LabeledWeightedEdge("e2");
		LabeledWeightedEdge e3 = new LabeledWeightedEdge("e3");
		LabeledWeightedEdge e4 = new LabeledWeightedEdge("e4");
		LabeledWeightedEdge e5 = new LabeledWeightedEdge("e5");
		LabeledWeightedEdge e6 = new LabeledWeightedEdge("e6");
		LabeledWeightedEdge e7 = new LabeledWeightedEdge("e7");
		LabeledWeightedEdge e8 = new LabeledWeightedEdge("e8");
		LabeledWeightedEdge e9 = new LabeledWeightedEdge("e9");
		LabeledWeightedEdge e10 = new LabeledWeightedEdge("e10");
		LabeledWeightedEdge e11 = new LabeledWeightedEdge("e11");
		LabeledWeightedEdge e12 = new LabeledWeightedEdge("e12");

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
		
		List<Vertex> steinerNodes = new ArrayList<Vertex>();
		steinerNodes.add(v1);
		steinerNodes.add(v2);
		steinerNodes.add(v3);
		steinerNodes.add(v4);
		
		g.addEdge(v1, v2, e1);
		g.addEdge(v2, v3, e2);
		g.addEdge(v3, v4, e3);
		g.addEdge(v4, v5, e4);
		g.addEdge(v5, v6, e5);
		g.addEdge(v6, v7, e6);
		g.addEdge(v7, v8, e7);
		g.addEdge(v8, v9, e8);
		g.addEdge(v9, v1, e9);
		g.addEdge(v5, v9, e10);
		g.addEdge(v2, v6, e11);
		g.addEdge(v3, v5, e12);
		
		g.setEdgeWeight(e1, 10.0);
		g.setEdgeWeight(e2, 8.0);
		g.setEdgeWeight(e3, 9.0);
		g.setEdgeWeight(e4, 2.0);
		g.setEdgeWeight(e5, 1.0);
		g.setEdgeWeight(e6, 1.1);
		g.setEdgeWeight(e7, 0.3);
		g.setEdgeWeight(e8, 0.5);
		g.setEdgeWeight(e9, 1.0);
		g.setEdgeWeight(e10, 1.0);
		g.setEdgeWeight(e11, 1.0);
		g.setEdgeWeight(e12, 2.0);

		SteinerTree st = new SteinerTree(g, steinerNodes);
		WeightedMultigraph<Vertex, LabeledWeightedEdge> steiner = st.getSteinerTree();
		double sum = 0.0;
		for (LabeledWeightedEdge edge : steiner.edgeSet()) {
			sum += steiner.getEdgeWeight(edge);
        }
		
		System.out.println("Steiner Cost: " + sum);

	}
}
