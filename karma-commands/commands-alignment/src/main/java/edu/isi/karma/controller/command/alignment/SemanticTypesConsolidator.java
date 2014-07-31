package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.rep.Workspace;

public class SemanticTypesConsolidator extends CommandConsolidator {

	public List<Command> consolidateCommand(List<Command> commands,
			Workspace workspace) {
		List<Command> refinedCommands = new ArrayList<Command>();
		for (Command command : commands) {
			if (command instanceof UnassignSemanticTypeCommand) {
				Iterator<Command> itr = refinedCommands.iterator();
				while(itr.hasNext()) {
					Command tmp = itr.next();
					if (tmp.getOutputColumns().equals(command.getOutputColumns()) && (tmp instanceof SetSemanticTypeCommand || tmp instanceof SetMetaPropertyCommand)) {
						tmp.getOutputColumns().clear();
						command.getOutputColumns().clear();
					}
				}
			}
			refinedCommands.add(command);
		}
		return refinedCommands;
	}

}
