package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.OntologyClassHieararchyUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

public class GetOntologyClassHierarchyCommand extends Command {

	protected GetOntologyClassHierarchyCommand(String id) {
		super(id);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Ontology Class Hierarchy";
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
		c.add(new OntologyClassHieararchyUpdate());
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
