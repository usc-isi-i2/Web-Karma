package edu.isi.karma.controller.history;
import java.util.List;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.rep.Workspace;
public abstract class CommandConsolidator {
	public abstract List<Command> consolidateCommand(List<Command> commands, Workspace workspace);
}
