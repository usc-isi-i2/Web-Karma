package edu.isi.karma.modeling.alignment;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private LinkType linkType;
	private String label;
	private boolean inverse;
	
	public LabeledWeightedEdge(String id) {
		super();
		this.id = id;
		this.linkType = LinkType.None;
		this.label = id;
		this.inverse = false;
	}

	public LabeledWeightedEdge(String id, String label) {
		super();
		this.id = id;
		this.linkType = LinkType.None;
		this.label = label;
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, String label, LinkType linkType) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.label = label;
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, String label, LinkType linkType, boolean inverse) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.label = label;
		this.inverse = inverse;;
	}
	
	public LabeledWeightedEdge(LabeledWeightedEdge e) {
		super();
		this.id = e.id;
		this.linkType = e.linkType;
		this.label = e.label;
		this.inverse = e.inverse;;
	}
	
	public String getLocalID() {
		if (id == null)
			return "";
		
		int index = id.indexOf('#');
		if (index == -1)
			return id;
		
		String result = id.substring(index + 1);
		
		return result;
	}
	
	public String getLocalLabel() {
		if (label == null)
			return "";
		
		int index = label.indexOf('#');
		if (index == -1)
			return label;
		
		String result = label.substring(index + 1);
		
		return result;
	}
	
	public boolean isInverse() {
		return this.inverse;
	}

	public String getID() {
		return this.id;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public LinkType getLinkType() {
		return this.linkType;
	}
	
	public Vertex getSource() {
		return (Vertex)super.getSource();
	}
	
	public Vertex getTarget() {
		return (Vertex)super.getTarget();
	}
	
	public double getWeight() {
		return super.getWeight();
	}
	
    public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((LabeledWeightedEdge)obj).getID() == this.getID()){
            return true;
        }
        return false;
    }
}
