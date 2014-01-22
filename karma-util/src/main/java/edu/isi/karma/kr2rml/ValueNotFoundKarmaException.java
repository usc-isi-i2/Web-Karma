package edu.isi.karma.kr2rml;

/**
 * Class ValueNotFoundKarmaException
 *
 * @since 01/22/2014
 */
class ValueNotFoundKarmaException extends Exception{

	private static final long serialVersionUID = 1L;
	private String offendingColumnHNodeId;

	//constructor without parameters
	public ValueNotFoundKarmaException() {}

	//constructor for exception description
	public ValueNotFoundKarmaException(String description, String offendingColumnHNodeId) {
	    super(description);
	    this.offendingColumnHNodeId = offendingColumnHNodeId;
	}

	public String getOffendingColumnHNodeId() {
		return this.offendingColumnHNodeId;
	}
}
