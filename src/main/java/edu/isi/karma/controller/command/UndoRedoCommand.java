package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

public class UndoRedoCommand extends Command {

	private final String commandIdArg;

	UndoRedoCommand(String id, String commandIdArg) {
		super(id);
		this.commandIdArg = commandIdArg;
	}

	@Override
	public String getCommandName() {
		return "Undo/Redo";
	}

	@Override
	public String getTitle() {
		return "Undo/Redo";
	}

	@Override
	public String getDescription() {
		return "Undo/Redo " + commandIdArg;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer undoEffects = vWorkspace.getWorkspace().getCommandHistory().undoOrRedoCommandsUntil(
				vWorkspace, commandIdArg);
		UpdateContainer result = new UpdateContainer(new HistoryUpdate(
				vWorkspace.getWorkspace().getCommandHistory()));
		result.append(undoEffects);
		return result;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// not undoable.
		return new UpdateContainer();
	}

}
