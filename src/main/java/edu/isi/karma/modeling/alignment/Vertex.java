package edu.isi.karma.modeling.alignment;

import edu.isi.karma.rep.semantictypes.SemanticType;

public class Vertex {

	private String id;
	private NodeType nodeType;
	private String label;
	private SemanticType semanticType;
	
	public Vertex(String id) {
		this.id = id;
		this.label = id;
	}
	
	public Vertex(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public Vertex(String id, SemanticType semanticType, NodeType nodeType) {
		this.id = id;
		this.nodeType = nodeType;
		this.semanticType = semanticType;
		this.label = semanticType.getType();
	}
	
	public Vertex(String id, String label, NodeType nodeType) {
		this.id = id;
		this.label = label;
		this.nodeType = nodeType;
		this.semanticType = null;
	}
		
	public Vertex(Vertex v) {
		this.id = v.id;
		this.label = v.label;
		this.nodeType = v.nodeType;
		this.semanticType = v.semanticType;
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
	
	public String getID() {
		return this.id;
	}
	
	public String getLabel() {
		return this.label;
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
