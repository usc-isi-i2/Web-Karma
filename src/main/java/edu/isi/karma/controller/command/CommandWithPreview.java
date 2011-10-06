package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

public abstract class CommandWithPreview extends Command {

	protected CommandWithPreview(String id) {
		super(id);
	}

	public abstract UpdateContainer showPreview(VWorkspace vWorkspace)
			throws CommandException;

	public abstract UpdateContainer handleUserActions(HttpServletRequest request);
}
