package edu.isi.karma.modeling.alignment;

import edu.isi.karma.modeling.NameSet;

public class Vertex {

	private String id;
	private NodeType type;
	private NameSet name;
	
	public Vertex(String id) {
		this.id = id;
		this.name = new NameSet(id);
	}
	
	public Vertex(String id, String localName) {
		this.id = id;
		this.name = new NameSet(localName);
	}
	
	public Vertex(String id, String localName, NodeType type) {
		this.id = id;
		this.type = type;
		this.name = new NameSet(localName);
	}

	public Vertex(String id, String ns, String localName, NodeType type) {
		this.id = id;
		this.type = type;
		this.name = new NameSet(ns, localName);
	}

	public Vertex(String id, NameSet name, NodeType type) {
		this.id = id;
		this.type = type;
		this.name = name;
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
	
	public NameSet getName() {
		return name;
	}

	public String getID() {
		return this.id;
	}
	
	public String getLabel() {
		return name.getLabel();
	}
	
	public NodeType getType() {
		return this.type;
	}
	
    public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((Vertex)obj).getID() == this.getID()){
            return true;
        }
        return false;
    }
}
