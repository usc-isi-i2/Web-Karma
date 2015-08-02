package edu.isi.karma.modeling.steiner.topk;

import java.util.Set;


/**
 * This class represents an edge in a steiner tree. It has a source node, a sink node, 
 * an edge label and a weight.
 * @author kasneci
 *
 */

public class SteinerEdge extends Fact {
	
	
	protected SteinerNode sourceNode;
	protected SteinerNode sinkNode;
	
	boolean isForced;
	protected Set<String> modelIds;

	
	public SteinerEdge(SteinerNode n1, String l, SteinerNode n2, float weight){
		super(new Entity(n1.getNodeId()),new Entity(n2.getNodeId()),
		    new Relation(l),weight);
		
		sourceNode= n1;//n1-->n2
		sinkNode= n2;
		isForced = false;
	}
	
	public int hashCode(){
		return sourceNode.hashCode()^
			 label.name.hashCode()^
			sinkNode.hashCode();
	}
	
	public String getEdgeLabel(){
		return label.name;
	}
	
	public SteinerNode getSourceNode(){
		return sourceNode;
	}
	
	public SteinerNode getSinkNode(){
		return sinkNode;
	}

	public Set<String> getModelIds() {
		return modelIds;
	}

	public void setModelIds(Set<String> modelIds) {
		this.modelIds = modelIds;
	}

	public boolean isForced() {
		return isForced;
	}

	public void setForced(boolean isForced) {
		this.isForced = isForced;
	}	
	
	
}

