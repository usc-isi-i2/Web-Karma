package edu.isi.karma.modeling.alignment;

public class Vertex {

	private String label;
	
	public Vertex(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}
	
    public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((Vertex)obj).getLabel() == this.getLabel()){
            return true;
        }
        return false;
    }
}
