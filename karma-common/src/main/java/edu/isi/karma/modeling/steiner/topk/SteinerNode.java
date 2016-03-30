package edu.isi.karma.modeling.steiner.topk;


import java.util.Set;
import java.util.TreeSet;

/**
 * This class serves as the main building block of a Steiner tree.
 * Additionally to the the class Entity a SteinerNode has also the edges that
 * contain it and a predecessor node from which the SteinerNode is reached.
 * @author kasneci
 *
 */
public class SteinerNode extends Entity {
	
	/*
	 * Each edge has this Steiner Node either as sink or as source node.
	 */
	protected TreeSet<SteinerEdge> edges;
	
	/*
	 * needed to reconstruct paths
	 */
	protected SteinerNode predecessor ;
	protected String relationToPredecessor;
	protected float weightToPredecessor;
	protected boolean wasArg1;
	protected double [] distancesToSources=new double [2];
	
	protected SteinerEdge predecessorLink;
	/*
	 * for BANKS II
	 */
	
	public SteinerNode(String id){
		super(id);
		edges= new TreeSet<>();		
	}
	
	/**
	 * copies only the name of the node
	 * @return
	 */
	public SteinerNode copy(){
		
		SteinerNode newNode = new SteinerNode(this.name);
		return newNode;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	
	
	/**
	 * sets n as the predecessor of this node, during a search process...
	 * ...needed for rebuilding issues...
	 * @param n
	 */
	public void setPredecessor(SteinerNode n){
		predecessor=n;
	}
	
	/**
	 * @param e
	 * @return the neighbor connected to this node through the steiner edge e
	 */
	public SteinerNode getNeighborInEdge(SteinerEdge e){
		if(e.sourceNode.equals(this))
			return e.sinkNode;
		else if(e.sinkNode.equals(this))
			return e.sourceNode;
		else return null;
	}
	
	/**
	 * @param n
	 * @return the steiner edge that connects this node to n
	 */
	public SteinerEdge getEdgeToNode(SteinerNode n){
		SteinerEdge e=null;
		for(SteinerEdge edge: edges){
			if(edge.sourceNode.equals(n)||edge.sinkNode.equals(n)){
				e=edge;
				break;
			}
		}
		return e;
	}
	
	public String getEdgeLabelToNode(SteinerNode n){
		return getEdgeToNode(n).label().name;
	}
	
	/**
	 * 
	 * @return all neighbors of this steiner node
	 */
	public TreeSet<SteinerNode> getNeighbors(){
		TreeSet<SteinerNode> ts = new TreeSet<>();
		for(SteinerEdge e: edges){
			if(e.sourceNode.equals(this))
				ts.add(e.sinkNode);
			if(e.sinkNode.equals(this))
				ts.add(e.sourceNode);
		}
		return ts;
	}
	
	/**
	 * 
	 * @param exceptForEdge
	 * @return return all neighbors of this node except for the node connected to this node
	 * through exceptForEdge
	 */
	public TreeSet<SteinerNode> getNeighbors(SteinerEdge exceptForEdge){
		TreeSet<SteinerNode> ts = new TreeSet<>();
		if(exceptForEdge==null){
			return getNeighbors();
		}
		else
			for(SteinerEdge e: edges)
				if(!e.equals(exceptForEdge)){
					if(e.sourceNode.equals(this))
						ts.add(e.sinkNode);
					if(e.sinkNode.equals(this))
						ts.add(e.sourceNode);
					
				}
		return ts;
	}
	
	public void addEdge(SteinerEdge edge){
		if(edge.sourceNode.equals(this)){
			edges.add(edge);
			edge.sinkNode.edges.add(edge);
		}
		if( edge.sinkNode.equals(this)){
			edges.add(edge);
			edge.sourceNode.edges.add(edge);
		}
	}
	
	/**
	 * 
	 * @param n the new node connected to this node through the new steiner edge to be added
	 * @param isSourceNode boolean variable indicating whether n is the source node
	 * in the new steiner edge (which is going to be added)
	 * @param l label of the new edge
	 * @param weight weight of the new edge
	 */
	public void addEdge(SteinerNode n, boolean isSourceNode, String l, float weight){
		if(isSourceNode) addEdge(new SteinerEdge(n,l,this,weight));
		else addEdge(new SteinerEdge(this, l, n,weight));
	}
	
	/**
	 * deletes all edges of this node
	 *
	 */
	public void deleteEdges(){
		edges= new TreeSet<>();
	}
	
	/**
	 * @param e steiner edge to be removed
	 */
	public void deleteEdge(SteinerEdge e){
		Set<SteinerEdge> badEdges= new TreeSet<>();
		for(SteinerEdge e1: edges)
			if(e1.sinkNode.equals(e.sinkNode)&& e1.sourceNode.equals(e.sourceNode))
				badEdges.add(e1);
		edges.removeAll(badEdges);
	}
	
	
	/**
	 * 
	 * @return number of edges this node is involved in
	 */
	public int getDegree(){
		return edges.size();
	}
	
	/**
	 * 
	 * @return true if this node has more than two edges
	 */
	public boolean isFixedNode(){
		return edges.size()>=3;
	}
	
	
	public String getNodeId(){
		return name;
	}


	public String getRelationToPredecessor() {
		return relationToPredecessor;
	}

	public void setRelationToPredecessor(String relationToPredecessor) {
		this.relationToPredecessor = relationToPredecessor;
	}

	public boolean isWasArg1() {
		return wasArg1;
	}

	public void setWasArg1(boolean wasArg1) {
		this.wasArg1 = wasArg1;
	}

	public float getWeightToPredecessor() {
		return weightToPredecessor;
	}

	public void setWeightToPredecessor(float weightToPredecessor) {
		this.weightToPredecessor = weightToPredecessor;
	}

	public SteinerNode getPredecessor() {
		return predecessor;
	}

	public TreeSet<SteinerEdge> getEdges() {
		return edges;
	}
}
