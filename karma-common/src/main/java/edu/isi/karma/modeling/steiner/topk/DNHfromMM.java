package edu.isi.karma.modeling.steiner.topk;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;



/**
 * This class represents an implementation of the Distenace Network Heuristic for
 * Steiner tree approximation.
 * 
 * 
 * @author kasneci
 *
 */
public class DNHfromMM extends TopKSteinertrees{
	
	public DNHfromMM(){
	}
	/**
	 * 
	 * @param terminals given terminal nodes
	 * @throws Exception
	 */
	public DNHfromMM(TreeSet<SteinerNode> terminals) throws Exception{
			super(terminals);
		
	}
	
	/**
	 * builds the Distance Network ...
	 * @return Distance Network (graph that contains shortest paths between
	 * the terminals)
	 */
	public Map<String, SteinerNode> getDistanceNetworkOnTerminals(){
	
		Map<String, SteinerNode> distanceNetwork = new HashMap<String, SteinerNode>();
		Map<String, SteinerNode> processedNodes = new HashMap<String, SteinerNode>();
		Map<String, SteinerNode> visitedNodes = new HashMap<String, SteinerNode>();
		Queue<SteinerNode> queue= new LinkedList<SteinerNode>();
		Set<SteinerNode> processedTerminals = new TreeSet<SteinerNode>();

		Set<SteinerNode> copyOfTerminals= new TreeSet<SteinerNode>();
		
		
		for(SteinerNode n: terminalNodes){
			queue = new LinkedList<SteinerNode>();
			processedNodes = new HashMap<String, SteinerNode>();
			visitedNodes = new HashMap<String, SteinerNode>();
			copyOfTerminals= new TreeSet<SteinerNode>();
			processedTerminals.add(n);
			for(SteinerNode node: terminalNodes)
				if(!processedTerminals.contains(node))
					copyOfTerminals.add(node.copy());
			
			
			
			copyOfTerminals.remove(n);
			queue.offer(n);
			
			while (!queue.isEmpty() && !copyOfTerminals.isEmpty()) {
				
				SteinerNode node =queue.poll();
				visitedNodes.put(node.name(), node);
				processedNodes.put(node.name(), node);
				
				if(copyOfTerminals.contains(node)){
				
					copyOfTerminals.remove(node);
				
					
					continue;
				}
				
				for(SteinerEdge e: graph.get(node)){
					accessedEdges++;
					SteinerNode newNode = null;
					if(!e.sourceNode.equals(node)){
						newNode=new SteinerNode(e.sourceNode.name());
						newNode.wasArg1=true;
					}
					else{
						newNode=new SteinerNode(e.sinkNode.name());
						newNode.wasArg1=false;
					}
					
					if(processedNodes.containsKey(newNode.name()));
					
					newNode.relationToPredecessor=e.getEdgeLabel();
					newNode.weightToPredecessor=e.getWeight();
					
					//check whether n has been visited
					SteinerNode v = visitedNodes.get(newNode.name());
					if(v!=null){ 
						newNode=v;
						if(newNode.distancesToSources[0]>node.distancesToSources[0]+e.getWeight()){
							newNode.distancesToSources[0]=node.distancesToSources[0]+e.getWeight();
							newNode.predecessor= node;
							newNode.relationToPredecessor=e.getEdgeLabel();
							newNode.weightToPredecessor=e.getWeight();
							
						}	
					}
					
					// in case n has not been visited
					else{
						newNode.distancesToSources[0]=node.distancesToSources[0]+e.getWeight();
						newNode.predecessor= node;
						queue.offer(newNode);
						visitedNodes.put(newNode.name(), newNode);
					}
					
					
				}
				
			}
			
			stabilizeNodes(visitedNodes, distanceNetwork);
			
		}
		
		return distanceNetwork;
	}
	
	/**
	 * reconstructs the paths of the distance network
	 * @param visitedNodes nodes visited by the single source shortest paths iterator
	 * @param dnhGraph the distance network with reconstructed paths
	 */
	protected void stabilizeNodes(Map<String, SteinerNode> visitedNodes, Map<String, SteinerNode>dnhGraph){
		for(SteinerNode node: terminalNodes){
			if(visitedNodes.containsKey(node.name())&& 
					visitedNodes.get(node.name()).predecessor!=null){
				SteinerNode startNode = visitedNodes.get(node.name());
				
				
				while(startNode.predecessor!=null){
					
					if(!dnhGraph.containsKey(startNode.name())){
						dnhGraph.put(startNode.name(), startNode);
					}
					
					if(!dnhGraph.containsKey(startNode.predecessor.name())){
						dnhGraph.put(startNode.predecessor.name(), startNode.predecessor);
					}
					
					dnhGraph.get(startNode.name()).addEdge(dnhGraph.get(
							startNode.predecessor.name()), 
							!startNode.wasArg1, startNode.relationToPredecessor, 
							startNode.weightToPredecessor);
					
					startNode=startNode.predecessor;
					
					if(terminalNodes.contains(startNode))break;
				}
			}
		}
	}
	
	
	/**
	 * retrieves the induced graph of a subgraph of an underlying graph :-)
	 * (i.e. given a subgraph G' of an underlying graph G all edges of G that are connected
	 * to nodes of G' are included in G') 
	 * @param dnhGraph subgraph that is going to be extended (induced w.r.t. 
	 * the underlying graph)
	 * @return induced graph
	 */
	
	public Map<String, SteinerNode> getInducedGraph (Map<String, SteinerNode> dnhGraph){
		
		Map<String, SteinerNode> copyOfCompleteGraph = new HashMap<String, SteinerNode>();
		for(String name: dnhGraph.keySet()){
			copyOfCompleteGraph.put(name, dnhGraph.get(name).copy());
		}
		
		for(SteinerNode n: copyOfCompleteGraph.values())
			for(SteinerEdge e: graph.get(n)){
				accessedEdges++;
				if(!dnhGraph.containsKey(e.sourceNode.name())){
					dnhGraph.put(e.sourceNode.name(), new SteinerNode(e.sourceNode.name()));
				}
				if(!dnhGraph.containsKey(e.sinkNode.name())){
					dnhGraph.put(e.sinkNode.name(), new SteinerNode(e.sinkNode.name()));
				}
				dnhGraph.get(e.sourceNode.name()).addEdge(dnhGraph.get(e.sinkNode.name()),
						false, e.getEdgeLabel(), e.getWeight());
			}
		clean(dnhGraph);
		return dnhGraph;
	}
	
	
	/**
	 * finds the MST of a given graph by exploiting Prim's algorithm
	 * @param inducedGraph the given graph
	 * @return MST
	 */
	public Map<String, SteinerNode> getMinSpanningTree(Map<String, SteinerNode> inducedGraph){
		//alg by prim
		
		// initialize an empty MST
		Map <String, SteinerNode> minSpanningTree = new HashMap<String, SteinerNode>();
		
		// start with any node of the induced graph
		SteinerNode startNode = new SteinerNode(((TreeSet<SteinerNode>)terminalNodes).first().name());
		minSpanningTree.put(startNode.name(), startNode);
		
		
		/** Input: A connected weighted graph with vertices V and edges E.
	    * 	Initialize: Vnew = {x}, where x is an arbitrary node (starting point) from V, Enew= {}
	    * 	repeat until Vnew=V:
	          	Choose edge (u,v) from E with minimal weight such that u is in Vnew and v is not (if there are multiple edges with the same weight, choose arbitrarily)
	          	Add v to Vnew, add (u,v) to Enew
	    * 	Output: Vnew and Enew describe the minimal spanning tree
	    */
		while(minSpanningTree.size()<inducedGraph.size()){
			Queue<SteinerEdge> edgesToConsider= new PriorityQueue<SteinerEdge>(3000, new Comparator<SteinerEdge>(){
				public int compare(SteinerEdge e1, SteinerEdge e2){
					if(e1.getWeight()<e2.getWeight())return -1;
					if(e1.getWeight()==e2.getWeight())return 0;
					else return 1;
				}
				
			});
			
			for(SteinerNode n: minSpanningTree.values()){
				for(SteinerEdge e: inducedGraph.get(n.name()).edges){
					accessedEdges++;
					if(!minSpanningTree.containsKey(n.getNeighborInEdge(e).name())){
							edgesToConsider.offer(e);
					}
				}
			}
			
			
			
			
			SteinerEdge e = edgesToConsider.poll();
				
					
			SteinerNode neighbor=null;
			boolean isNeighborArg1=false;
			if(minSpanningTree.containsKey(e.sourceNode.name())
						&& !minSpanningTree.containsKey(e.sinkNode.name())){
				neighbor=new SteinerNode(e.sinkNode.name());
				isNeighborArg1=false;
			}
					
			if(minSpanningTree.containsKey(e.sinkNode.name())
						&& !minSpanningTree.containsKey(e.sourceNode.name())){
				neighbor=new SteinerNode(e.sourceNode.name());
				isNeighborArg1=true;
			}
	
			minSpanningTree.put(neighbor.name(), neighbor);	
				
			minSpanningTree.get(neighbor.getNeighborInEdge(e).name()).addEdge(
						minSpanningTree.get(neighbor.name()), isNeighborArg1,
							e.getEdgeLabel(), e.getWeight());
			
			edgesToConsider=null;
					
		}
		clean(minSpanningTree);
		return minSpanningTree;
	}
	
	
	
	public void clean (Map<String, SteinerNode> map){
		LinkedList<SteinerNode> outliers = new LinkedList<SteinerNode>();
		for(SteinerNode n: map.values())
			if(!terminalNodes.contains(n)&& n.edges.size()==1)
				outliers.add(n);
		for(SteinerNode n: outliers)
			while(n.edges.size()<2 && !terminalNodes.contains(n)){
				SteinerNode node=n.getNeighbors().first();
				node.edges.remove(node.getEdgeToNode(n));
				n.edges.remove(n.getEdgeToNode(node));
				map.remove(n.getNodeId());
				n=node;
			}
				
	}
	
	
	/**
	 * 
	 * @return the approximation Steiner tree by the Distance Network Heuristic
	 */
	public ApprSteinerTree getDNHApprTree(){
		Map<String, SteinerNode> tree= getMinSpanningTree(
				getInducedGraph(getMinSpanningTree(getDistanceNetworkOnTerminals())));
		TreeSet<SteinerNode> steinerNodes= new TreeSet<SteinerNode>();
		steinerNodes.addAll(tree.values());
		
		return new ApprSteinerTree(terminalNodes, steinerNodes);
	
	}
	
	
	/**
	 * finds the best tree and puts it into the resultQueue as a resultGraph
	 *
	 */
	public void getBestTree(){
		resultQueue.add(getDNHApprTree().toResultGraph());
	}

	@Override
	public void buildTaxonomicGraph() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Queue<ResultGraph> getTopKTrees(int k) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
