package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.DataPropertyListUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

public class GetDataPropertyListCommand extends Command {

	protected GetDataPropertyListCommand(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Data Property List";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		c.add(new DataPropertyListUpdate());
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
