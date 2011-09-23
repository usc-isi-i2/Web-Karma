/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorkspace;

/**
 * Announces a command that should be added to the bottom of the history.
 * 
 * @author szekely
 * 
 */
public class HistoryAddCommandUpdate extends AbstractUpdate {

	public enum JsonKeys {
		command
	}

	private final Command command;

	public HistoryAddCommandUpdate(Command command) {
		super();
		this.command = command;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ Util.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + Util.jsonStartObject(JsonKeys.command));
		command.generateJson(newPref + "  ", pw, vWorkspace, Command.HistoryType.undo);
		pw.println(prefix + "}");
	}

}
