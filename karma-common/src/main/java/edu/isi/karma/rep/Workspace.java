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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandPreferences;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.rep.metadata.TagsContainer;
import edu.isi.karma.semantictypes.typinghandler.HybridSTModelHandler;
import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;

/**
 * Contains all the data to support a single instance of Karma, called a
 * workspace.
 * 
 * @author szekely
 * 
 */
public class Workspace extends Entity {

	private static final Logger logger = LoggerFactory.getLogger(Workspace.class);
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
	private final Map<String, Worksheet> worksheets = new ConcurrentHashMap<>();
	
	/**
	 * Saves all the tagging information
	 */
	private final TagsContainer tagsContainer = new TagsContainer();

	/**
	 * Manages the model constructed from the imported ontologies
	 */
	private  OntologyManager ontologyManager;
	
	/**
	 * The CRF Model for the workspace
	 */
	private final ISemanticTypeModelHandler semTypeModelHandler;
	
	private final CommandPreferences commandPreferences;

	private final String commandPreferencesId;
	
	private final String contextId;

	/**
	 * In the future we may need to keep track of user info.
	 */
	protected Workspace(String id, String contextId) {
		super(id);
		commandPreferences = new CommandPreferences(this.getId(), contextId);
		commandPreferencesId=this.getId();
		this.contextId = contextId;
		this.semTypeModelHandler = new LuceneBasedSTModelHandler(contextId);
	}
	
	protected Workspace(String id, String cachedPreferencesId, String contextId) {
		super(id);
		this.commandPreferences = new CommandPreferences(cachedPreferencesId, contextId);
		this.commandPreferencesId = cachedPreferencesId;
		this.contextId = contextId;
		this.semTypeModelHandler = new HybridSTModelHandler(contextId);
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
		factory.removeWorksheet(id, commandHistory);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				getId(), id);
		AlignmentManager.Instance().removeAlignment(alignmentId);
		this.commandHistory.removeWorksheetHistory(id);
		this.worksheets.remove(id);
	}
	
	public Worksheet getWorksheet(String id) {
		return worksheets.get(id);
	}

	public Collection<Worksheet> getWorksheets() {
		return new ArrayList<>(worksheets.values());
	}

	public RepFactory getFactory() {
		return factory;
	}

	public TagsContainer getTagsContainer() {
		return tagsContainer;
	}

	public OntologyManager getOntologyManager() {
		if(ontologyManager == null)
		{
			ontologyManager = new OntologyManager(contextId);
		}
		return ontologyManager;
	}

	public ISemanticTypeModelHandler getSemanticTypeModelHandler() {
		return semTypeModelHandler;
	}

	public CommandPreferences getCommandPreferences() {
		return commandPreferences;
	}

	public String getCommandPreferencesId() {
		return commandPreferencesId;
	}
	public String getContextId()
	{
		return contextId;
	}
}
