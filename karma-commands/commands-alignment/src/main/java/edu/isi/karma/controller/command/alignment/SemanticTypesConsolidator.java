package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.rep.Workspace;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class SemanticTypesConsolidator extends CommandConsolidator {

	public List<ICommand> consolidateCommand(List<ICommand> commands,
			Workspace workspace) {
		List<ICommand> refinedCommands = new ArrayList<>();
		for (ICommand command : commands) {
			boolean found = false;
			Set<ICommand> commandsToRemove = new HashSet<>();
			if (command instanceof UnassignSemanticTypeCommand) {
				String model = command.getModel();
				Iterator<ICommand> itr = refinedCommands.iterator();
				while(itr.hasNext()) {
					ICommand tmp = itr.next();
					if (tmp.getModel().equals(model)
							&& ((Command)tmp).getOutputColumns().equals(((Command)command).getOutputColumns())
							&& (tmp instanceof SetSemanticTypeCommand || tmp instanceof SetMetaPropertyCommand)) {
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

	@Override
	public Pair<ICommand, JSONArray> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace) {
		if (newCommand instanceof UnassignSemanticTypeCommand) {
			String model = newCommand.getModel();
			Iterator<ICommand> itr = commands.iterator();
			while(itr.hasNext()) {
				ICommand tmp = itr.next();
				if (tmp.getModel().equals(model)
						&& ((Command)tmp).getOutputColumns().equals(((Command)newCommand).getOutputColumns())
						&& (tmp instanceof SetSemanticTypeCommand || tmp instanceof SetMetaPropertyCommand)) {
					return new ImmutablePair<>(tmp, new JSONArray(tmp.getInputParameterJson()));
				}
			}
		}
		return null;
	}

}
