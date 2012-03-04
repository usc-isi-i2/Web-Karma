package edu.isi.karma.modeling.alignment;

import edu.isi.karma.rep.semantictypes.SemanticType;

public class Vertex {

	private String id;
	private NodeType nodeType;
	private Name name;
	private SemanticType semanticType;
	
	public Vertex(String id, Name name, NodeType nodeType) {
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.semanticType = null;
	}
		
	public Vertex(Vertex v) {
		this.id = v.id;
		this.name = new Name(v.name);
		this.nodeType = v.nodeType;
		this.semanticType = v.semanticType;
	}
	
	public String getLocalID() {
		String s = id;
		s = s.replaceAll(this.name.getNs(), "");
		return s;
	}

	public String getLocalLabel() {
		String s = this.name.getUri();
		s = s.replaceAll(this.name.getNs(), "");
		return s;
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
	
	public NodeType getNodeType() {
		return this.nodeType;
	}
	
	public SemanticType getSemanticType() {
		return this.semanticType;
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
