package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.rep.Workspace;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;

import java.util.List;
public abstract class CommandConsolidator {

	public String getConsolidatorName() {
		return this.getClass().getSimpleName();
	}
	public abstract List<ICommand> consolidateCommand(List<ICommand> commands, Workspace workspace);
	public abstract Pair<ICommand, JSONArray> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace);
}
