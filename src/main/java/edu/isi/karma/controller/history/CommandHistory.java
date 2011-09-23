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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.Command.CommandType;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.update.HistoryAddCommandUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
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
	public UpdateContainer doCommand(Command command, VWorkspace vWorkspace)
			throws CommandException {
		UpdateContainer effects = new UpdateContainer();
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
		effects.append(command.doIt(vWorkspace));
		command.setExecuted(true);
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
	public UpdateContainer undoOrRedoCommandsUntil(VWorkspace vWorkspace,
			String commandId) throws CommandException {
		List<Command> commandsToUndo = getCommandsUntil(history, commandId);
		if (!commandsToUndo.isEmpty()) {
			lastCommandWasUndo = true;
			return undoCommands(vWorkspace, commandsToUndo);
		} else {
			List<Command> commandsToRedo = getCommandsUntil(redoStack,
					commandId);
			if (!commandsToRedo.isEmpty()) {
				return redoCommands(vWorkspace, commandsToRedo);
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
	private UpdateContainer undoCommands(VWorkspace vWorkspace,
			List<Command> commandsToUndo) {

		UpdateContainer effects = new UpdateContainer();
		for (Command c : commandsToUndo) {
			history.remove(c);
			redoStack.add(c);
			effects.append(c.undoIt(vWorkspace));
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
	private UpdateContainer redoCommands(VWorkspace vWorkspace,
			List<Command> commandsToRedo) throws CommandException {
		if (!redoStack.isEmpty()) {

			UpdateContainer effects = new UpdateContainer();
			for (Command c : commandsToRedo) {
				redoStack.remove(c);
				history.add(c);
				effects.append(c.doIt(vWorkspace));
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

		Iterator<Command> redoIt = redoStack.iterator();
		while (redoIt.hasNext()) {
			redoIt.next().generateJson(prefix, pw, vWorkspace,
					Command.HistoryType.redo);
			if (redoIt.hasNext()) {
				pw.println(prefix + ",");
			}
		}
	}
}
