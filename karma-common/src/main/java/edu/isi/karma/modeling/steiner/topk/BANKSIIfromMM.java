package edu.isi.karma.modeling.steiner.topk;


import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;

/**
 * This class is an implementation of the bidirectional search BANKS (or BANKS II) heuristic
 * by Kacholia et al. (VLDB 2005)
 * @author kasneci
 *
 */

public class BANKSIIfromMM extends BANKSfromMM {
	
	protected int topK=0;
	
	public BANKSIIfromMM() {
		// TODO Auto-generated constructor stub
	}
	
	
	public BANKSIIfromMM(TreeSet<SteinerNode> terminals) throws Exception {
		super(terminals);
	}
	
	
	/**
	 * A BANKSIINode has a predecessor for each terminal, a distance from each terminal,
	 * an activation from each terminal node.
	 * @author kasneci
	 *
	 */
	class BANKSIINode{
		SteinerNode node;
		Map<SteinerNode, BANKSIINode> predecessorsFromTerminals;
		Map<SteinerNode, Double> distancesToTerminals;
		Map<SteinerNode, Double> activationFromTerminals;
		
		BANKSIINode(SteinerNode nodeName){
			node= new SteinerNode(nodeName.name());
			predecessorsFromTerminals= new HashMap<SteinerNode, BANKSIINode>();
			distancesToTerminals= new HashMap<SteinerNode, Double>();
			activationFromTerminals= new HashMap<SteinerNode, Double>();
			for(SteinerNode terminal: terminalNodes){
				distancesToTerminals.put(terminal, Double.MAX_VALUE);
				activationFromTerminals.put(terminal, 0.0);
			}
		}
		
		double getActivation(){
			double activation=0;
			for(double d: activationFromTerminals.values()){
				activation=activation+d;
			}
			return activation;
		}
		
		boolean isComplete(){
			for(SteinerNode terminal: terminalNodes){
				if(distancesToTerminals.get(terminal)==Double.MAX_VALUE)
					return false;
			}
			return true;
		}		
	}
	
	
	/**
	 * Retrieves top-k trees with the bidirectional seach heuristic as described in the paper
	 * by Kacholia et al. (VLDB 2005)
	 * @param k number of result trees
	 */
	public void bidirectionalSearch( int k ){
		topK=k;
		
		// iterator for all nodes that point to the current node
		Queue<BANKSIINode> qIn =  new PriorityQueue<BANKSIINode>(10, new Comparator<BANKSIINode>(){
			public int compare(BANKSIINode n1, BANKSIINode n2){
				if(n1.getActivation()<n2.getActivation())return 1;
				else if (n1.getActivation()>n2.getActivation())return -1;
				else return 0;
			}
		});
		
		// stores all nodes visited by both iterators
		Map<SteinerNode, BANKSIINode> visitedNodes= new HashMap<SteinerNode, BANKSIINode>();
		
		// all terminal nodes are activated and inserted into qIn
		for(SteinerNode node: terminalNodes){
			BANKSIINode n=new BANKSIINode(node);
			n.distancesToTerminals.put(node, 0.0);
			n.activationFromTerminals.put(node, 1.0/graph.get(node).size());
			qIn.offer(n);
			visitedNodes.put(n.node, n);
		}
		
		// iterator for all nodes pointed to by the current node
		Queue<BANKSIINode> qOut =  new PriorityQueue<BANKSIINode>(10, new Comparator<BANKSIINode>(){
			public int compare(BANKSIINode n1, BANKSIINode n2){
				if(n1.getActivation()<n2.getActivation())return 1;
				else if (n1.getActivation()>n2.getActivation())return -1;
				else return 0;
			}
		});
		
		//nodes processed by qIn (polled from qIn)
		Map<SteinerNode, BANKSIINode> xIn= new HashMap<SteinerNode, BANKSIINode>();
		
		//nodes processed by qOut (polled from qOut)
		Map<SteinerNode, BANKSIINode> xOut= new HashMap<SteinerNode, BANKSIINode>();
		
		
		
		while (!qIn.isEmpty() || !qOut.isEmpty()){
			
			if(resultQueue.size()>=k)break;
			
			// decide which queue to take
			// take the one that has the node with the highest activation
			@SuppressWarnings("unused")
			Queue<BANKSIINode> queue= null;
			boolean isQIn=false;
			if(qIn.peek()== null){
				queue=qOut;
			}
			else if(qOut.peek()==null){
				queue= qIn;
				isQIn=true;
			}
			else{
				if(qIn.peek().getActivation() > qOut.peek().getActivation()){
					queue=qIn;
					isQIn=true;
				}
				else queue=qOut; 
			}
			
			
			if(isQIn){
				BANKSIINode v= qIn.poll();
				xIn.put(v.node, v);
				if(v.isComplete()) emit(v);
				for(SteinerEdge e: graph.get(v.node)){
					accessedEdges++;
					BANKSIINode u= null;
					if(e.sinkNode.equals(v.node)){
						
						u= visitedNodes.get(e.sourceNode);
						if(u==null){
							u= new BANKSIINode(e.sourceNode);
						}
					}
					else{
						u= visitedNodes.get(e.sinkNode);
						if(u==null){
							u= new BANKSIINode(e.sinkNode);
						}
					}
					exploreEdge(u,v,e, qIn, visitedNodes);
					if(resultQueue.size()>=k)break;
					if(xIn.get(u.node)==null){
						if(visitedNodes.containsKey(u.node)){
							qIn.remove(u);
							qIn.offer(u);
						}
						else qIn.offer(u);
						visitedNodes.put(u.node, u);
					}
					if(!xOut.containsKey(v.node)){
						if(visitedNodes.containsKey(v.node)){
							qOut.remove(v);
							qOut.offer(v);
						}
						else qOut.offer(v);
						visitedNodes.put(v.node, v);
					}
						
					visitedNodes.put(u.node, u);
				}
			}
			else{
				BANKSIINode u= qOut.poll();
				xOut.put(u.node, u);
				if(u.isComplete()) emit(u);
				for(SteinerEdge e: graph.get(u.node)){
					BANKSIINode v=null;
					if(e.sourceNode.equals(u.node)){
						v= visitedNodes.get(e.sinkNode);
						if(v==null){
							v= new BANKSIINode(e.sinkNode);
						}
					}
					else{
						v= visitedNodes.get(e.sourceNode);
						if(v==null){
							v= new BANKSIINode(e.sourceNode);
						}
					}
					exploreEdge(u,v,e, qIn, visitedNodes);
					if(resultQueue.size()>=k)break;
					if(xOut.get(v.node)==null){
						if(visitedNodes.containsKey(v.node)){
							qOut.remove(v);
							qOut.offer(v);
						}
						else qOut.offer(v);
						visitedNodes.put(v.node, v);
					}
					visitedNodes.put(v.node, v);
				}
			}
		}
	}
	
	/**
	 * v forwards activation an distance from terminals to u
	 * @param u
	 * @param v
	 * @param e
	 * @param qIn
	 * @param visitedNodes
	 */
	private void exploreEdge(BANKSIINode u, BANKSIINode v, SteinerEdge e, Queue<BANKSIINode>qIn,
							Map<SteinerNode, BANKSIINode> visitedNodes){
		for(SteinerNode n: terminalNodes){
			
			if(u.distancesToTerminals.get(n)>v.distancesToTerminals.get(n)+e.weight()){
				
				// needed for the activation component
				u.distancesToTerminals.put(n,v.distancesToTerminals.get(n)+e.weight());
				
				// needed to rebuild trees
				v.node.wasArg1=e.sourceNode.equals(v.node);
				v.node.weightToPredecessor=e.weight();
				v.node.relationToPredecessor=e.getEdgeLabel();
				u.predecessorsFromTerminals.put(n, v);
				
				// set activation of u
				if(u.activationFromTerminals.get(n)<0.4*1/u.distancesToTerminals.get(n)+0.6*v.activationFromTerminals.get(n)/graph.get(u.node).size())
					u.activationFromTerminals.put(n,0.4*1/u.distancesToTerminals.get(n)+0.6*v.activationFromTerminals.get(n)/graph.get(u.node).size());
				if(u.isComplete())emit(u);
				if(resultQueue.size()>=topK) return;
				
			}
			visitedNodes.put(u.node, u);
		}
	}
	
	/**
	 * builds the tree rooted at v
	 * @param v root of a new result tree
	 */
	private void emit(BANKSIINode v){
		// will contain the edges of the new tree
		TreeSet<SteinerEdge> trEdges = new TreeSet<SteinerEdge>();
		
		// will contain the nodes of the new tree
		Map<String, SteinerNode> nodesToClean= new HashMap<String, SteinerNode>();
		
		// will contain nodes dequeued from the queue
		Map<SteinerNode, BANKSIINode> processedNodes= new HashMap<SteinerNode, BANKSIINode>();
		
		// needed to construct th new tree in breadth first manner
		Queue<BANKSIINode> queue = new LinkedList<BANKSIINode>();
		queue.add(v);
		
		// construct the new tree in breadth first manner
		while (!queue.isEmpty()) {
			BANKSIINode n= queue.poll();
			SteinerNode root= new SteinerNode(n.node.name());
			processedNodes.put(root, n);
			nodesToClean.put(root.name(), root);
			if(n.predecessorsFromTerminals.isEmpty()) continue;

			// needed to manage predecessors
			TreeSet<BANKSIINode> predecessors= new TreeSet<BANKSIINode>(new Comparator<BANKSIINode>(){
				public int compare(BANKSIINode n1, BANKSIINode n2){
					return n1.node.name().compareTo(n2.node.name());
				}
			});
			
			// add predecessors only once into the queue
			for(BANKSIINode bN: n.predecessorsFromTerminals.values()){
				if(!processedNodes.containsKey(bN.node))
					predecessors.add(bN);
			}
			
			if(predecessors.contains(n))predecessors.remove(n);
			for(BANKSIINode bN: predecessors){
				
				if(processedNodes.containsKey(bN.node))continue;
				SteinerEdge e= null;
				if(bN.node.isWasArg1())
					e=new SteinerEdge(bN.node,bN.node.relationToPredecessor, root, bN.node.weightToPredecessor );
				else e= new SteinerEdge(root,bN.node.relationToPredecessor, bN.node, bN.node.weightToPredecessor );
				trEdges.add(e);
				processedNodes.put(bN.node, bN);
				queue.add(bN);
			}
		}
		
		for(SteinerEdge e: trEdges){
			nodesToClean.get(e.sourceNode.name()).addEdge(
					nodesToClean.get(e.sinkNode.name()), false, e.getEdgeLabel(), e.weight());
		}
		TreeSet<SteinerNode > trNodes = new TreeSet<SteinerNode>();
		clean(nodesToClean);
		trNodes.addAll(nodesToClean.values());
		
		ApprSteinerTree t= new ApprSteinerTree(terminalNodes, trNodes);
		
		resultQueue.add(t.toResultGraph());
	}

	public Queue<ResultGraph> getTopKTrees(int k) throws Exception {
		// TODO Auto-generated method stub
		bidirectionalSearch(k);
		return resultQueue;
	}
	
	public void getBestTree()throws Exception{
		bidirectionalSearch(1);
	}

}
