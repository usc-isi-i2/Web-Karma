package edu.isi.karma.modeling.alignment;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String label;
	
	public LabeledWeightedEdge(String label) {
		super();
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
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
