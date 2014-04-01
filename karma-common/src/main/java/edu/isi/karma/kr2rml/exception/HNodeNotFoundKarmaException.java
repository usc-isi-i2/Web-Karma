package edu.isi.karma.kr2rml.exception;

public class HNodeNotFoundKarmaException extends Exception {
	private static final long serialVersionUID = 1L;
	private String offendingColumnName;
	
	//constructor without parameters
	public HNodeNotFoundKarmaException() {}

	//constructor for exception description
	public HNodeNotFoundKarmaException(String description, String offendingColumnName) {
	    super(description);
	    this.offendingColumnName = offendingColumnName;
	}
	
	public String getOffendingColumn() {
		return this.offendingColumnName;
	}	
}