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
package edu.isi.karma.controller.command;

import java.io.File;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.view.VWorkspace;

public class ImportOntologyCommand extends Command {
	private File ontologyFile;
	
	private static Logger logger = Logger.getLogger(ImportOntologyCommand.class);
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
		logger.info("Loading ontology: " + ontologyFile.getAbsolutePath());
		final boolean success = ontManager.doImportAndUpdateCache(ontologyFile);
		logger.info("Done loading ontology: " + ontologyFile.getAbsolutePath());
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
