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
	private LinkStatus linkStatus;
	
	public LabeledWeightedEdge(String id) {
		super();
		this.id = id;
		this.linkType = LinkType.None;
		this.label = id;
		this.inverse = false;
		this.linkStatus = LinkStatus.None;
	}

	public LabeledWeightedEdge(String id, String label) {
		super();
		this.id = id;
		this.linkType = LinkType.None;
		this.label = label;
		this.inverse = false;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(String id, String label, LinkType linkType) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.label = label;
		this.inverse = false;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(String id, String label, LinkType linkType, boolean inverse) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.label = label;
		this.inverse = inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(LabeledWeightedEdge e) {
		super();
		this.id = e.id;
		this.linkType = e.linkType;
		this.label = e.label;
		this.inverse = e.inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public String getLocalID() {
		if (id == null)
			return "";
		
		String result = "";
		String temp = id;
		
		if (temp.endsWith("/"))
			temp = temp.substring(0, temp.length() - 1);
		
		int index = temp.indexOf('#');
		if (index == -1) {
			index = temp.lastIndexOf('/');
			if (index == -1)
				return temp;
			result = temp.substring(index + 1);
		} else
			result = temp.substring(index + 1);
		
		return result;
	}
	
	public String getLocalLabel() {
		if (label == null)
			return "";

		String result = "";
		String temp = label;
		
		if (temp.endsWith("/"))
			temp = temp.substring(0, temp.length() - 1);
		
		int index = temp.indexOf('#');
		if (index == -1) {
			index = temp.lastIndexOf('/');
			if (index == -1)
				return temp;
			result = temp.substring(index + 1);
		} else
			result = temp.substring(index + 1);
		
		return result;
	}
	
	
	public LinkStatus getLinkStatus() {
		return linkStatus;
	}

	public void setLinkStatus(LinkStatus linkStatus) {
		this.linkStatus = linkStatus;
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
