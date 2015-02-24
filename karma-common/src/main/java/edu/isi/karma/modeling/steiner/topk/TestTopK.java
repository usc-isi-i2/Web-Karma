package edu.isi.karma.modeling.steiner.topk;

import java.util.HashMap;
import java.util.TreeSet;

public class TestTopK {
	public static void main(String[] args) throws Exception {
		
		SteinerNode n1= new SteinerNode("n1");
		SteinerNode n2= new SteinerNode("n2");
		SteinerNode n3= new SteinerNode("n3");
		SteinerNode n4= new SteinerNode("n4");
		SteinerNode n5= new SteinerNode("n5");
		SteinerNode n6= new SteinerNode("n6");
		SteinerNode n7= new SteinerNode("n7");

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

		nodes.put(n5.getNodeId(), n5);
		graph.put(n5, new TreeSet<SteinerEdge>());

		nodes.put(n6.getNodeId(), n6);
		graph.put(n6, new TreeSet<SteinerEdge>());

		nodes.put(n7.getNodeId(), n7);
		graph.put(n7, new TreeSet<SteinerEdge>());

//		int id=0;
//		for(SteinerNode node: graph.keySet()){
//			nodeToId.put(node.name(), id);
//			id++;
//		}
		
		SteinerEdge e12 = new SteinerEdge(n1, "e12", n2, 0.1f);
		SteinerEdge e16 = new SteinerEdge(n1, "e16", n6, 0.3f);
		
		
		SteinerEdge e52 = new SteinerEdge(n5, "e52", n2, 0.3f);

		SteinerEdge e21 = new SteinerEdge(n2, "e21", n1, 0.3f);
		SteinerEdge e31 = new SteinerEdge(n3, "e31", n1, 0.2f);
		SteinerEdge e32 = new SteinerEdge(n3, "e32", n2, 0.7f);
		SteinerEdge e37 = new SteinerEdge(n3, "e37", n7, 0.4f);
		SteinerEdge e14 = new SteinerEdge(n1, "e14", n4, 0.9f);
		SteinerEdge e24 = new SteinerEdge(n2, "e24", n4, 0.4f);
		SteinerEdge e42 = new SteinerEdge(n4, "e42", n2, 0.4f);
		SteinerEdge e43 = new SteinerEdge(n4, "e43", n3, 0.3f);
		SteinerEdge e45 = new SteinerEdge(n4, "e45", n5, 0.3f);
		
		graph.get(n1).add(e21);
		graph.get(n1).add(e31);
//		graph.get(n1).add(e12);
//		graph.get(n1).add(e14);
		
//		graph.get(n2).add(e12);
//		graph.get(n2).add(e52);
//		graph.get(n2).add(e32);
		graph.get(n2).add(e42);
//		graph.get(n2).add(e21);
//		graph.get(n2).add(e24);

		graph.get(n3).add(e43);
//		graph.get(n3).add(e31);
//		graph.get(n3).add(e32);

//		graph.get(n4).add(e14);
//		graph.get(n4).add(e24);
//		graph.get(n4).add(e43);

//		graph.get(n5).add(e15);
		graph.get(n5).add(e45);
//		graph.get(n5).add(e52);

		graph.get(n6).add(e16);

		graph.get(n7).add(e37);

		TreeSet<SteinerNode> terminals= new TreeSet<SteinerNode>();
		
		terminals.add(n6); 
		terminals.add(n7);
		
		
//		STARfromMM N = new STARfromMM(terminals);
//		BANKSIIfromMM N = new BANKSIIfromMM(terminals);
		BANKSfromMM N = new BANKSfromMM(terminals);
//		DNHfromMM N = new DNHfromMM(terminals);
//		DPBFfromMM N = new DPBFfromMM(terminals);
		N.graph = graph;
		N.nodes = nodes;

//		N.writeGraphForSTAR("");
//		N.loadGraphFromFile("STAR.txt");

		long startTime=System.currentTimeMillis();
		N.getTopKTrees(10);
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
