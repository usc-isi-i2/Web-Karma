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
package edu.isi.karma.rep;

import edu.isi.karma.controller.command.CommandPreferences;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.metadata.TagsContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all the data to support a single instance of Karma, called a
 * workspace.
 * 
 * @author szekely
 * 
 */
public class Workspace extends Entity {

	/**
	 * Factory to create all the objects in this workspace
	 */
	private final RepFactory factory = new RepFactory();

	/**
	 * History of commands performed in this workspace.
	 */
	private CommandHistory commandHistory = new CommandHistory();

	/**
	 * Record all the worksheets defined in this workspace.
	 */
	private final Map<String, Worksheet> worksheets = new HashMap<String, Worksheet>();
	
	/**
	 * Saves all the tagging information
	 */
	private final TagsContainer tagsContainer = new TagsContainer();

	/**
	 * Manages the model constructed from the imported ontologies
	 */
	private final OntologyManager ontologyManager = new OntologyManager();
	
	/**
	 * The CRF Model for the workspace
	 */
	private final CRFModelHandler crfModelHandler = new CRFModelHandler();
	
	private final CommandPreferences commandPreferences;

	private final String commandPreferencesId;

	private KarmaMetadataManager metadataManager;
	/**
	 * In the future we may need to keep track of user info.
	 */
	protected Workspace(String id) {
		super(id);
		commandPreferences = new CommandPreferences(this.getId());
		commandPreferencesId=this.getId();
	}
	
	protected Workspace(String id, String cachedPreferencesId) {
		super(id);
		this.commandPreferences = new CommandPreferences(cachedPreferencesId);
		this.commandPreferencesId = cachedPreferencesId;
	}

	public CommandHistory getCommandHistory() {
		if(commandHistory == null)
		{
			commandHistory = new CommandHistory();
		}
		return commandHistory;
	}

	void addWorksheet(Worksheet worksheet) {
		worksheets.put(worksheet.getId(), worksheet);
	}

	public void removeWorksheet(String id) {
		Worksheet ws = worksheets.get(id);
		factory.removeWorksheet(id);
		this.worksheets.remove(id);
	}
	
	public Worksheet getWorksheet(String id) {
		return worksheets.get(id);
	}

	public Collection<Worksheet> getWorksheets() {
		return worksheets.values();
	}

	public RepFactory getFactory() {
		return factory;
	}

	public TagsContainer getTagsContainer() {
		return tagsContainer;
	}

	public OntologyManager getOntologyManager() {
		return ontologyManager;
	}

	public CRFModelHandler getCrfModelHandler() {
		return crfModelHandler;
	}

	public CommandPreferences getCommandPreferences() {
		return commandPreferences;
	}

	public String getCommandPreferencesId() {
		return commandPreferencesId;
	}

	public void setMetadataManager(
			KarmaMetadataManager metadataManager) {
		this.metadataManager = metadataManager;
		
	}
	public KarmaMetadataManager getMetadataManager()
	{
		return this.metadataManager;
	}
}
