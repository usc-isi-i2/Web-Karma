package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand.HistoryType;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

public class HistoryLastCommandUpdate extends AbstractUpdate {

	public enum JsonKeys {
		command
	}

	private final Command command;

	public HistoryLastCommandUpdate(Command command) {
		super();
		this.command = command;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.jsonStartObject(JsonKeys.command));
		command.generateJson(newPref, pw, vWorkspace, HistoryType.undo);
		pw.println(prefix + "}");
	}
	
	public boolean equals(Object o) {
		if (o instanceof HistoryUpdate) {
			return true;
		}
		return false;
	}

}
