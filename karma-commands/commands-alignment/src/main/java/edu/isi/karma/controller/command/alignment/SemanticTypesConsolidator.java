package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.rep.Workspace;

import java.util.*;

public class SemanticTypesConsolidator extends CommandConsolidator {

	public List<Command> consolidateCommand(List<Command> commands,
			Workspace workspace) {
		List<Command> refinedCommands = new ArrayList<>();
		for (Command command : commands) {
			boolean found = false;
			Set<Command> commandsToRemove = new HashSet<>();
			if (command instanceof UnassignSemanticTypeCommand) {
				String model = command.getModel();
				Iterator<Command> itr = refinedCommands.iterator();
				while(itr.hasNext()) {
					Command tmp = itr.next();
					if (tmp.getModel().equals(model) && tmp.getOutputColumns().equals(command.getOutputColumns()) && (tmp instanceof SetSemanticTypeCommand || tmp instanceof SetMetaPropertyCommand)) {
						tmp.getOutputColumns().clear();
						command.getOutputColumns().clear();
						commandsToRemove.add(tmp);
						found = true;
					}
				}
			}
			if (!found) {
				refinedCommands.add(command);
			}
			else {
				refinedCommands.removeAll(commandsToRemove);
			}
		}
		return refinedCommands;
	}

}
