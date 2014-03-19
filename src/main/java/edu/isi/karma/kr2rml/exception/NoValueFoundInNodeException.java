package edu.isi.karma.kr2rml.exception;

public class NoValueFoundInNodeException extends Exception{

	private static final long serialVersionUID = 1L;
	
	//constructor without parameters
	public NoValueFoundInNodeException() {}

	//constructor for exception description
	public NoValueFoundInNodeException(String description) {
	    super(description);
	}
}

