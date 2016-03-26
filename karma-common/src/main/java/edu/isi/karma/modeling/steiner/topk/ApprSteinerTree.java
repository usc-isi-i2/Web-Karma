package edu.isi.karma.modeling.steiner.topk;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents an (intermediate) result of a Steiner tree query.
 * Such a result is a tree which may be improved to a better result when ever this is possible
 * (see paper). It consists of treeNodes (all the nodes) and terminalNodes (query nodes).
 * @author kasneci
 *
 */
public class ApprSteinerTree extends SteinerSubTree implements Cloneable {
	
	// all the nodes of the result tree
	protected Set<SteinerNode> treeNodes;
	
	// nodes representing the query terms
	protected Set<SteinerNode> terminalNodes;
	
	// needed for a dynamic programming implementation
	protected SteinerNode root;
	
	public ApprSteinerTree(Set<SteinerNode>terminalNodes, Set<SteinerNode>treeNodes){
		super(treeNodes);
		this.terminalNodes=terminalNodes;
		this.treeNodes=treeNodes;
		
	}
	
	/**
	 * copying this ApprSteinerTree
	 * @return copy of this ApprSteinerTree
	 */
	public ApprSteinerTree clone(){
		Set<SteinerNode> newTerminals= new TreeSet<>();
		
		Set<SteinerNode> newTreeNodes= new TreeSet<>();
		Map<String,SteinerNode> hms= new HashMap<>();
		
		// copy nodes (only nodeIds: SteinerNode.copy())
		for(SteinerNode n: treeNodes){
			hms.put(n.getNodeId(), n.copy());
		}
		
		// copy edges of each node
		for(SteinerNode n: treeNodes){
			for(SteinerEdge e: n.edges){
				if((hms.get(e.sourceNode.getNodeId())!=null)&&(hms.get(e.sinkNode.getNodeId())!=null)){
					hms.get(e.sourceNode.getNodeId()).addEdge(
							hms.get(e.sinkNode.getNodeId()), false, e.label().name, e.weight());
					
					hms.get(e.sinkNode.getNodeId()).addEdge(
							hms.get(e.sourceNode.getNodeId()), true, e.label().name, e.weight());
				}
			}
		}
		
		// add terminal nodes
		for(SteinerNode n: hms.values()){
			if(terminalNodes.contains(n)){
				newTerminals.add(n);
			}
			newTreeNodes.add(n);
		}
		return new ApprSteinerTree(newTerminals,newTreeNodes);
	}
	
	
	/**
	 * retrieves all nodes that have degree >= 3 and the terminals (fixedNodes).
	 * @return TreeSet<SteinerNode> of all nodes that have degree >= 3 and the terminals
	 */
	public Set<SteinerNode> getFixedNodes(){
		Set<SteinerNode> fixedNodes= new TreeSet<>();
		// first add all terminals
		fixedNodes.addAll(terminalNodes);
		
		// than add all fixedNodes
		for(SteinerNode node: treeNodes){
			if(node.isFixedNode()){
				fixedNodes.add(node);
			}
		}
		return fixedNodes;
	}
	
	/**
	 * 
	 * @param p loose path to be removed
	 * @return the subtrees resulting from the removal of p
	 */
	public List<Set<SteinerNode>> getNodeSetPartitioning(LoosePath p){
		List<Set<SteinerNode>> listOfPartitions = new ArrayList<>();
		SteinerNode n1 = p.getFirstNode();
		SteinerNode n2 = p.getLastNode();
		for(SteinerNode node:treeNodes){
			if(node.equals(n1))
				/*get all nodes that can be reached by following all edges connected
				  to n1's pendant in the tree (i.e. node) except for the first edge of n1.*/
				listOfPartitions.add(getNodesInBreadthFirstFrom(node, n1.edges.first()));
			if(node.equals(n2))
				listOfPartitions.add(getNodesInBreadthFirstFrom(node, n2.edges.first()));
		}
		return listOfPartitions;
	}
	
	/**
	 * 
	 * @param node the steiner node from which the breadth first search (BFS) should start.
	 * @param exceptForEdge the edge that should not be considered in the BFS
	 * @return the nodes reached by the BFS
	 */
	public Set<SteinerNode> getNodesInBreadthFirstFrom(SteinerNode node, SteinerEdge exceptForEdge){
		Set<SteinerNode> ts = new TreeSet<>();
		Queue<SteinerNode> queue= new LinkedList<>();
		queue.offer(node);
		while(!queue.isEmpty()){
			SteinerNode n=queue.poll();
			ts.add(n);
			for(SteinerNode n1: n.getNeighbors(exceptForEdge)){
				if(!ts.contains(n1)){
					queue.offer(n1);
					//ts.add(n1);
					
				}
			}
		}
		
		//removing bad edges...		
		for(SteinerNode neighbor:node.getNeighbors())
			if(!ts.contains(neighbor))
				node.deleteEdge(node.getEdgeToNode(neighbor));
		return ts;
	}
	
	/**
	 * retireves all loose paths connected to fixed node
	 * @param fixedNode steiner node from which to start the search for loose paths
	 * @return PriorityQueue of all loose paths connected to fixedNode
	 */
	public PriorityQueue<LoosePath> getLoosePaths(SteinerNode fixedNode){
		PriorityQueue<LoosePath> loosePaths= new PriorityQueue<>();
		
		for(SteinerEdge edge: fixedNode.edges){
			SteinerEdge e2=edge;
			SteinerNode n1=new SteinerNode(fixedNode.getNodeId());
			SteinerNode n= fixedNode.getNeighborInEdge(edge);
			SteinerNode n2 = new SteinerNode(n.getNodeId());
			if(edge.sourceNode.equals(n)){
				n1.addEdge(n2, true, edge.getEdgeLabel(), edge.weight());
			}
			else{
				n1.addEdge(n2, false, edge.getEdgeLabel(), edge.weight());
			}
			LinkedList<SteinerNode> pathNodes = new LinkedList<>();
			pathNodes.add(n1);pathNodes.add(n2);
			// following a loose path
			while(!n.isFixedNode()&&!terminalNodes.contains(n)){
				for(SteinerEdge e: n.edges)
					if(!e2.equals(e)){
						e2=e;
						break;
					}
				n=n.getNeighborInEdge(e2);
				SteinerNode n3= new SteinerNode(n.getNodeId());
				if(e2.sourceNode.equals(n)){
					n3.addEdge(n2, false, e2.getEdgeLabel(), e2.weight());
				}
				else{
					n3.addEdge(n2, true, e2.getEdgeLabel(), e2.weight());
				}
				n2=n3;
				pathNodes.add(n2);
			}
			loosePaths.add(new LoosePath(pathNodes));
		}
		return loosePaths;
	}
	
	
	/**
	 * 
	 * @return priority queue of all loose paths in a tree (ranked by their scores) 
	 */
	public Set<LoosePath> getLoosePaths(){
		Set<LoosePath> loosePaths= new TreeSet<>();
		for(SteinerNode node: treeNodes){
			if(node.isFixedNode()||terminalNodes.contains(node)){
				loosePaths.addAll(getLoosePaths(node));
			}
		}
		return loosePaths;
	}
	

   public Set<SteinerNode> getTreeNodes() {
      return treeNodes;
    }

}
	