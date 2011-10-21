package edu.isi.karma.modeling.alignment;

import org.jgrapht.graph.DefaultWeightedEdge;

import edu.isi.karma.modeling.NameSet;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private LinkType type;
	private NameSet name;
	private boolean inverse;
	
	public LabeledWeightedEdge(String id) {
		super();
		this.id = id;
		this.type = LinkType.None;
		this.name = new NameSet(id);
		this.inverse = false;
	}

	public LabeledWeightedEdge(String id, String localName) {
		super();
		this.id = id;
		this.type = LinkType.None;
		this.name = new NameSet(localName);
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, String localName, LinkType type) {
		super();
		this.id = id;
		this.type = type;
		this.name = new NameSet(localName);
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, String ns, String localName, LinkType type) {
		super();
		this.id = id;
		this.type = type;
		this.name = new NameSet(ns, localName);
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, String ns, String localName, LinkType type, boolean inverse) {
		super();
		this.id = id;
		this.type = type;
		this.name = new NameSet(ns, localName);
		this.inverse = inverse;
	}
	
	public LabeledWeightedEdge(String id, NameSet name, LinkType type) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.inverse = false;
	}
	
	public LabeledWeightedEdge(String id, NameSet name, LinkType type, boolean inverse) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.inverse = inverse;;
	}
	
	public String getLocalID() {
		if (id == null)
			return "";
		
		int index = id.indexOf('#');
		if (index == -1)
			return id;
		
		String result = id.substring(index + 1);
		
		// FIXME
		if (this.inverse) {
			//if (this.type == LinkType.HasSubClass)
				//result = "isa";
			//else
				result = "inverseOf(" + result + ")";
		}
		
		return result;
	}
	
	public boolean isInverse() {
		return this.inverse;
	}

	public NameSet getName() {
		return name;
	}

	public String getID() {
		return this.id;
	}
	
	public String getLabel() {
		return name.getLabel();
	}
	
	public LinkType getType() {
		return this.type;
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
