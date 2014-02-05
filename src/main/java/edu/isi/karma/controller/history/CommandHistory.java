/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.controller.history;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.command.Command.CommandType;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.ResetKarmaCommand;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.update.HistoryAddCommandUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class CommandHistory {

	private final ArrayList<Command> history = new ArrayList<Command>();

	private final ArrayList<Command> redoStack = new ArrayList<Command>();

	/**
	 * If the last command was undo, and then we do a command that goes on the
	 * history, then we need to send the browser the full history BEFORE we send
	 * the update for the command the user just did. The reason is that the
	 * history may contain undoable commands, and the browser does not know how
	 * to reset the history.
	 */
	private boolean lastCommandWasUndo = false;
	
	/**
	 * Used to keep a pointer to the command which require user-interaction
	 * through multiple HTTP requests.
	 */
	private Command currentCommand;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public CommandHistory() {
	}

	public CommandHistory(ArrayList<Command> history,
			ArrayList<Command> redoStack) {
		for (Command c : history) {
			this.history.add(c);
		}
		for (Command c : redoStack) {
			this.redoStack.add(c);
		}
	}

	public boolean isUndoEnabled() {
		return !history.isEmpty();
	}

	public boolean isRedoEnabled() {
		return !redoStack.isEmpty();
	}

	public ArrayList<Command> _getHistory() {
		return history;
	}

	public ArrayList<Command> _getRedoStack() {
		return redoStack;
	}

	public CommandHistory clone() {
		return new CommandHistory(history, redoStack);
	}
	
	public void setCurrentCommand(Command command) {
		this.currentCommand = command;
	}
	
	public Command getCurrentCommand() {
		return this.currentCommand;
	}

	/**
	 * Commands go onto the history when they have all their arguments and are
	 * ready to be executed. If a command needs multiple user interactions to
	 * define the parameters, then the command object must be created,
	 * interacted with and then executed. 
	 * 
	 * @param command
	 * @param vWorkspace
	 * @return UpdateContainer with all the changes done by the command.
	 * @throws CommandException
	 */
	public UpdateContainer doCommand(Command command, Workspace workspace)
			throws CommandException {
		UpdateContainer effects = new UpdateContainer();
		effects.append(command.doIt(workspace));
		command.setExecuted(true);
		
		if(!ModelingConfiguration.getManualAlignment()) {
			if (command.getCommandType() != CommandType.notInHistory) {
				redoStack.clear();
				
				// Send the history before the command we just executed.
				if (lastCommandWasUndo && !(command instanceof UndoRedoCommand)) {
					effects.append(new UpdateContainer(new HistoryUpdate(this)));
				}
				lastCommandWasUndo = false;
				
				history.add(command);
				effects.add(new HistoryAddCommandUpdate(command));
			}
			
			// Save the modeling commands
			if (!(command instanceof ResetKarmaCommand)) {
				CommandHistoryWriter chWriter = new CommandHistoryWriter(history, workspace);
				try {
					chWriter.writeHistoryPerWorksheet();
				} catch (JSONException e) {
					logger.error("Error occured while writing history!" , e);
					e.printStackTrace();
				}
			}
		}
		return effects;
	}

	/**
	 * @param vWorkspace
	 * @param commandId
	 *            is the id of a command that should be either in the undo or
	 *            redo histories. If it is in none, then nothing will be done.
	 * @return the effects of the undone or redone commands.
	 * @throws CommandException
	 */
	public UpdateContainer undoOrRedoCommandsUntil(Workspace workspace,
			String commandId) throws CommandException {
		List<Command> commandsToUndo = getCommandsUntil(history, commandId);
		if (!commandsToUndo.isEmpty()) {
			lastCommandWasUndo = true;
			return undoCommands(workspace, commandsToUndo);
		} else {
			List<Command> commandsToRedo = getCommandsUntil(redoStack,
					commandId);
			if (!commandsToRedo.isEmpty()) {
				return redoCommands(workspace, commandsToRedo);
			} else {
				return new UpdateContainer();
			}
		}
	}

	/**
	 * Undo all commands in the given list.
	 * 
	 * @param vWorkspace
	 * @param commandsToUndo
	 * 
	 * @return UpdateContainer with the effects of all undone commands.
	 */
	private UpdateContainer undoCommands(Workspace workspace,
			List<Command> commandsToUndo) {

		UpdateContainer effects = new UpdateContainer();
		for (Command c : commandsToUndo) {
			history.remove(c);
			redoStack.add(c);
			effects.append(c.undoIt(workspace));
		}
		return effects;
	}

	/**
	 * Redo all the commands in the given list.
	 * 
	 * @param vWorkspace
	 * @param commandsToRedo
	 * @return
	 * @throws CommandException
	 */
	private UpdateContainer redoCommands(Workspace workspace,
			List<Command> commandsToRedo) throws CommandException {
		if (!redoStack.isEmpty()) {

			UpdateContainer effects = new UpdateContainer();
			for (Command c : commandsToRedo) {
				redoStack.remove(c);
				history.add(c);
				effects.append(c.doIt(workspace));
			}
			return effects;
		} else {
			return new UpdateContainer();
		}
	}

	/**
	 * @param commands
	 *            , a list of commands.
	 * @param commandId
	 *            , the id of a command.
	 * @return the sublist of commands from the start until and including a
	 *         command with the given id. If the command with the given id is
	 *         not in the list, return the empty list.
	 */
	private List<Command> getCommandsUntil(ArrayList<Command> commands,
			String commandId) {
		if (commands.isEmpty()) {
			return Collections.emptyList();
		}
		List<Command> result = new LinkedList<Command>();
		boolean foundCommand = false;
		for (int i = commands.size() - 1; i >= 0; i--) {
			Command c = commands.get(i);
			if (c.getCommandType() == Command.CommandType.undoable) {
				result.add(c);
			}
			if (c.getId().equals(commandId)) {
				foundCommand = true;
				break;
			}
		}
		if (foundCommand) {
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	public void generateFullHistoryJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Iterator<Command> histIt = history.iterator();
		while (histIt.hasNext()) {
			histIt.next().generateJson(prefix, pw, vWorkspace,
					Command.HistoryType.undo);
			if (histIt.hasNext() || isRedoEnabled()) {
				pw.println(prefix + ",");
			}
		}

		for(int i = redoStack.size()-1; i>=0; i--) {
			Command redoComm = redoStack.get(i);
			redoComm.generateJson(prefix, pw, vWorkspace,
					Command.HistoryType.redo);
			if(i != 0)
				pw.println(prefix + ",");
		}
	}

	public void removeCommands(CommandTag tag) {
		List<Command> commandsToBeRemoved = new ArrayList<Command>();
		for(Command command: history) {
			if(command.hasTag(tag))
				commandsToBeRemoved.add(command);
		}
		history.removeAll(commandsToBeRemoved);
	}
	
	public Command getCommand(String commandId)
	{
		for(Command  c: this.history)
		{
			if(c.getId().equals(commandId))
			{
				return c;
			}
		}
		return null;
			
	}
}
