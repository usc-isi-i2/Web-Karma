/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Reports all the commands in the history including undo and redo commands.
 * They are ordered so that undo commands come first and redo commands are last.
 * 
 * @author szekely
 * 
 */
public class HistoryUpdate extends AbstractUpdate {

	public enum JsonKeys {
		commands
	}

	private final CommandHistory commandHistory;

	public HistoryUpdate(CommandHistory commandHistory) {
		super();
		this.commandHistory = commandHistory.clone();
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.jsonStartList(JsonKeys.commands));
		commandHistory.generateFullHistoryJson(newPref + "  ", pw, vWorkspace);
		pw.println(newPref + "]");
		pw.println(prefix + "}");
	}

}
