package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class CloseWorkspaceCommand extends Command {
	private final String workspaceId;

	protected CloseWorkspaceCommand(String id, String workspaceId) {
		super(id);
		this.workspaceId = workspaceId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Close Workspace";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		WorkspaceManager mgr = WorkspaceManager.getInstance();
		// Remove it from the rep factory
		RepFactory f = mgr.getFactory();
		f.removeWorkspace(workspaceId);
		
		// Remove any alignments from the AlignmentManager
		AlignmentManager.Instance().removeWorkspaceAlignments(vWorkspace.getWorkspace().getId());
		
		// Remove it from the workspace registry
		WorkspaceRegistry.getInstance().deregister(workspaceId);
		
		return new UpdateContainer();
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}

}
