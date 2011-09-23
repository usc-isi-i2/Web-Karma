/**
 * 
 */
package edu.isi.karma.controller.command;

import java.io.PrintWriter;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Entity;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorkspace;

/**
 * Abstract class for all commands.
 * 
 * @author szekely
 * 
 */
public abstract class Command extends Entity {

	public enum HistoryType {
		undo, redo
	}

	public enum CommandType {
		undoable, notUndoable, notInHistory
	}

	public enum JsonKeys {
		commandId, title, description, commandType, historyType
	}

	/**
	 * @return the internal name of this command. Used to communicate between
	 *         the server and the browser.
	 */
	public abstract String getCommandName();

	/**
	 * @return the label shown in the user interface.
	 */
	public abstract String getTitle();

	/**
	 * @return a description of what the command does or did, if it was
	 *         executed.
	 */
	public abstract String getDescription();

	/**
	 * @return the type of this command.
	 */
	public abstract CommandType getCommandType();

	public abstract UpdateContainer doIt(VWorkspace vWorkspace)
			throws CommandException;

	public abstract UpdateContainer undoIt(VWorkspace vWorkspace);

	/**
	 * Has this command been executed already?
	 */
	private boolean isExecuted = false;

	protected Command(String id) {
		super(id);

	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}

	/**
	 * @param prefix
	 * @param pw
	 * @param vWorkspace
	 * @param historyType of the lists where the command is, either undo or redo.
	 */
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace, HistoryType historyType) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref + Util.json(JsonKeys.commandId, getId()));
		pw.println(newPref + Util.json(JsonKeys.title, getTitle()));
		pw.println(newPref + Util.json(JsonKeys.description, getDescription()));
		pw.println(newPref
				+ Util.json(JsonKeys.historyType, historyType.name()));
		pw.println(newPref
				+ Util.jsonLast(JsonKeys.commandType, getCommandType().name()));
		pw.println(prefix + "}");
	}
}
