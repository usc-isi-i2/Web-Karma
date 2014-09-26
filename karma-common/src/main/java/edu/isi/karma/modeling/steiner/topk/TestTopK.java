package edu.isi.karma.modeling.steiner.topk;

import java.io.File;
import java.util.HashMap;
import java.util.Queue;
import java.util.TreeSet;

public class TestTopK {
	public static void main(String[] args) throws Exception {
		
		SteinerNode n1= new SteinerNode("n1");
		SteinerNode n2= new SteinerNode("n2");
		SteinerNode n3= new SteinerNode("n3");
		SteinerNode n4= new SteinerNode("n4");

		HashMap<SteinerNode, TreeSet<SteinerEdge>> graph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
		HashMap<String, SteinerNode> nodes = new HashMap<String, SteinerNode>();
		
		graph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
		nodes = new HashMap<String, SteinerNode>();

		nodes.put(n1.getNodeId(), n1);
		graph.put(n1, new TreeSet<SteinerEdge>());

		nodes.put(n2.getNodeId(), n2);
		graph.put(n2, new TreeSet<SteinerEdge>());

		nodes.put(n3.getNodeId(), n3);
		graph.put(n3, new TreeSet<SteinerEdge>());

		nodes.put(n4.getNodeId(), n4);
		graph.put(n4, new TreeSet<SteinerEdge>());

//		int id=0;
//		for(SteinerNode node: graph.keySet()){
//			nodeToId.put(node.name(), id);
//			id++;
//		}
		
		SteinerEdge e12 = new SteinerEdge(n1, "bornIn", n2, 0.6f);
//		SteinerEdge e21 = new SteinerEdge(n2, "diedIn", n1, 0.3f);
		SteinerEdge e13 = new SteinerEdge(n1, "directed", n3, 0.2f);
		SteinerEdge e23 = new SteinerEdge(n2, "discovered", n3, 0.7f);
		SteinerEdge e14 = new SteinerEdge(n1, "describes", n4, 0.9f);
		SteinerEdge e24 = new SteinerEdge(n2, "created", n4, 0.4f);
		SteinerEdge e43 = new SteinerEdge(n4, "actedIn", n3, 0.3f);
		
		graph.get(n1).add(e12);
//		graph.get(n1).add(e21);
		graph.get(n1).add(e13);
		graph.get(n1).add(e14);
		
		graph.get(n2).add(e12);
//		graph.get(n2).add(e21);
		graph.get(n2).add(e23);
		graph.get(n2).add(e24);

		graph.get(n3).add(e13);
		graph.get(n3).add(e23);
		graph.get(n3).add(e43);

		graph.get(n4).add(e14);
		graph.get(n4).add(e24);
		graph.get(n4).add(e43);

		TreeSet<SteinerNode> terminals= new TreeSet<SteinerNode>();
		
		terminals.add(n2); 
		terminals.add(n3);
		
		
//		STARfromMM N = new STARfromMM(terminals);
//		BANKSIIfromMM N = new BANKSIIfromMM(terminals);
//		BANKSfromMM N = new BANKSfromMM(terminals);
//		DNHfromMM N = new DNHfromMM(terminals);
		DPBFfromMM N = new DPBFfromMM(terminals);
		N.graph = graph;
		N.nodes = nodes;

//		N.loadGraphFromFile("STAR.txt");

		int k = 10;
		long startTime=System.currentTimeMillis();
		N.getTopKTrees(3);
		N.writeGraphForSTAR("");
		int count = 0;
		HashMap<Double, ApprSteinerTree> visitedTress = 
				new HashMap<Double, ApprSteinerTree>();
		
		for(ResultGraph tree: N.resultQueue){
			for (Fact f : tree.getFacts()) { 
				System.out.println(f.toString());
			}
			System.out.println(tree.getScore());
//			D.p(tree);
//			D.p(tree.getScore());
//			D.p();
		}
//		D.p(N.accessedEdges);
		long end= System.currentTimeMillis();
		
		System.out.println("done ...");
		
	}

}
