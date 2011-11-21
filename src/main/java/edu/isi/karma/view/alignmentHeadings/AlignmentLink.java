package edu.isi.karma.view.alignmentHeadings;

public class AlignmentLink {
	final private String id;
	final private String label;
	
	public AlignmentLink(String id, String label) {
		super();
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
}
