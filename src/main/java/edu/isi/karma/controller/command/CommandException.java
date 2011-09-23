/**
 * 
 */
package edu.isi.karma.controller.command;


/**
 * @author szekely
 *
 */
public class CommandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6578670561260489674L;

	private final Command command;
	
	private final String message;
	
	CommandException(Command command, String message) {
		super();
		this.command = command;
		this.message = message;
	}

	public Command getCommand() {
		return command;
	}

	public String getMessage() {
		return message;
	}
	
}
