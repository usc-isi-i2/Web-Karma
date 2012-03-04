package edu.isi.karma.modeling.alignment;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private LinkType linkType;
	private boolean inverse;
	private Name name;
	private LinkStatus linkStatus;
	
//	public LabeledWeightedEdge(String id) {
//		super();
//		this.id = id;
//		this.linkType = LinkType.None;
//		this.label = id;
//		this.inverse = false;
//		this.linkStatus = LinkStatus.None;
//	}
//
//	public LabeledWeightedEdge(String id, String label) {
//		super();
//		this.id = id;
//		this.linkType = LinkType.None;
//		this.label = label;
//		this.inverse = false;
//		this.linkStatus = LinkStatus.None;
//	}
	
	public LabeledWeightedEdge(String id, Name name, LinkType linkType) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.name = name;
		this.inverse = false;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(String id, Name name, LinkType linkType, boolean inverse) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.name = name;
		this.inverse = inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(LabeledWeightedEdge e) {
		super();
		this.id = e.id;
		this.linkType = e.linkType;
		this.name = new Name(e.name);
		this.inverse = e.inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public String getLocalID() {
		String s = this.id;
		s = s.replaceAll(this.name.getNs(), "");
		return s;
	}
	
	public String getLocalLabel() {
		String s = this.name.getUri();
		s = s.replaceAll(this.name.getNs(), "");
		return s;
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
	
	public String getUri() {
		return this.name.getUri();
	}
	
	public String getNs() {
		return this.name.getNs();
	}
	
	public String getPrefix() {
		return this.name.getPrefix();
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
