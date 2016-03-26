package edu.isi.karma.modeling.steiner.topk;


import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class represents a loose path, that is a path in which all intermediate nodes
 * have a degree 2, its end nodes have a degree > 2 (they are fixed nodes).
 * 
 * @author kasneci
 *
 */

public class LoosePath extends SteinerSubTree implements Cloneable {
	
	//list of steiner nodes belonging to this loose path
	protected LinkedList<SteinerNode> pathNodes;
	
	public LoosePath(LinkedList<SteinerNode> pathNodes){
		this.pathNodes=pathNodes;
		setScoreWithFunction(null);
	}
	
	
	public Set<SteinerNode> getNodes(){
		Set<SteinerNode> nodeSet= new TreeSet<>();
		nodeSet.addAll(pathNodes);
		return nodeSet;
	}
	
	// copying this loose path
	public SteinerSubTree clone(){
		LinkedList<SteinerNode> l= new LinkedList<>();
		for(SteinerNode n: pathNodes){
			l.add(n.copy());
		}
		return new LoosePath(l);
	}
	
	
	// adding an edge to the first node of the path
	public void addEdgeToFirstNode(SteinerEdge edge){
		if(pathNodes.getFirst().equals(edge.sourceNode)){
			pathNodes.getFirst().addEdge(edge.sinkNode, 
					false, edge.label().name, edge.weight());
			pathNodes.addFirst(edge.sinkNode);
		}
		if(pathNodes.getFirst().equals(edge.sinkNode)){
			pathNodes.getFirst().addEdge(edge.sourceNode, 
					true, edge.label().name, edge.weight());
			pathNodes.addFirst(edge.sourceNode);
		}
	}
	
	public void addEdgeToFirstNode(SteinerNode n, boolean isSourceNode, String l, float weight){
		if(isSourceNode)
			addEdgeToFirstNode(new SteinerEdge(n, l, pathNodes.getFirst(),weight));
		else
			addEdgeToFirstNode(new SteinerEdge(pathNodes.getFirst(), l, n,weight));
	}
	
	
	// adding an edge to the last node of the path
	public void addEdgeToLastNode(SteinerEdge edge){
		if(pathNodes.getLast().equals(edge.sourceNode)){
			pathNodes.getLast().addEdge(edge.sinkNode, false, edge.label().name,edge.weight());
			pathNodes.addLast(edge.sinkNode);
		}
		if(pathNodes.getLast().equals(edge.sinkNode)){
			pathNodes.getLast().addEdge(edge.sourceNode, true, edge.label().name, edge.weight());
			pathNodes.addLast(edge.sourceNode);
		}
	}
	
	public void addEdgeToLastNode(SteinerNode n, boolean isSourceNode, String l, float weight){
		if(isSourceNode)
			addEdgeToLastNode(new SteinerEdge(n, l, pathNodes.getFirst(),weight));
		else
			addEdgeToLastNode(new SteinerEdge(pathNodes.getFirst(), l, n,weight));
	}

	/**
	 * 
	 * @return the LinkedList of SteinerNodes representing this loose path
	 */
	public LinkedList<SteinerNode> getPath(){
		return pathNodes;
	}
	
	
	public SteinerNode getFirstNode(){
		return pathNodes.getFirst();
	}
	
	public SteinerNode getLastNode(){
		return pathNodes.getLast();
	}
	
	
	
	public boolean equals(Object o){
		return this.compareTo((LoosePath)o)==0;
	}

}
