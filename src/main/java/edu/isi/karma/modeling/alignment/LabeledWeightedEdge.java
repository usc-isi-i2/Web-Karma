package edu.isi.karma.modeling.alignment;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String label;
	private EdgeType type;
	
	public LabeledWeightedEdge(String label) {
		super();
		this.label = label;
		this.type = EdgeType.None;
	}
	
	public LabeledWeightedEdge(String label, EdgeType type) {
		super();
		this.label = label;
		this.type = type;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public EdgeType getType() {
		return this.type;
	}
	
    public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((LabeledWeightedEdge)obj).getLabel() == this.getLabel()){
            return true;
        }
        return false;
    }
}
