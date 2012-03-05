package edu.isi.karma.controller.command;

import java.io.File;
import java.io.PrintWriter;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.view.VWorkspace;

public class ImportOntologyCommand extends Command {
	private File ontologyFile;
	
	private enum JsonKeys {
		Import
	}

	public ImportOntologyCommand(String id) {
		super(id);
	}

	public ImportOntologyCommand(String id, File uploadedFile) {
		super(id);
		this.ontologyFile = uploadedFile;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Import Ontology";
	}

	@Override
	public String getDescription() {
		return ontologyFile.getName();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		OntologyManager ontManager = vWorkspace.getWorkspace().getOntologyManager();
		final boolean success = ontManager.doImport(ontologyFile);
		
		return new UpdateContainer(new AbstractUpdate(){
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				pw.println("{");
				pw.println("	\""+GenericJsonKeys.updateType.name()+"\": \"" + ImportOntologyCommand.class.getSimpleName() + "\",");
				pw.println("	\""+JsonKeys.Import.name()+"\":" + success);
				pw.println("}");
			}
		});
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}

}
