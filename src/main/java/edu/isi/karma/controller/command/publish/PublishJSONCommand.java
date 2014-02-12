package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

public class PublishJSONCommand extends Command {

	protected PublishJSONCommand(String id) {
		super(id);
	}

	@Override
	public String getCommandName() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return null;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		return null;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
}
