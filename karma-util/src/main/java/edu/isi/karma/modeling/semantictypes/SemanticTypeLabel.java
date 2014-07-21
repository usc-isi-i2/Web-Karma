package edu.isi.karma.modeling.semantictypes;

public class SemanticTypeLabel {

	private String label;
	private float score;
	
	public SemanticTypeLabel(String label, float score) {
		this.label = label;
		this.score = score;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public float getScore() {
		return this.score;
	}
}
