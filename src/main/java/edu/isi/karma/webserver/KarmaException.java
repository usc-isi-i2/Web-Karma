package edu.isi.karma.webserver;


/**
 * @author Maria Muslea(USC/ISI)
 *
 */
public class KarmaException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6578670561260489674L;

	private final String message;
	
	public KarmaException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
