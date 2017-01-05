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

package edu.isi.karma.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;
import edu.isi.karma.webserver.WorkspaceRegistry;

public abstract class RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(RdfGenerator.class);
	protected String selectionName;
	public RdfGenerator(String selectionName) {
		this.selectionName = selectionName;
	}
	protected Workspace initializeWorkspace(ServletContextParameterMap contextParameters) {
		
		Workspace workspace = WorkspaceManager.getInstance().createWorkspace(contextParameters.getId());
		WorkspaceRegistry.getInstance().register(new ExecutionController(workspace));
		WorkspaceKarmaHomeRegistry.getInstance().register(workspace.getId(), contextParameters.getKarmaHome());
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());        
        modelingConfiguration.setManualAlignment();

        return workspace;
        
	}

	protected void removeWorkspace(Workspace workspace) {
		WorkspaceManager.getInstance().removeWorkspace(workspace.getId());
	    WorkspaceRegistry.getInstance().deregister(workspace.getId());
	    WorkspaceKarmaHomeRegistry.getInstance().deregister(workspace.getId());
	}

	protected void applyHistoryToWorksheet(Workspace workspace, Worksheet worksheet,
			KR2RMLMapping mapping) throws JSONException {
		WorksheetCommandHistoryExecutor wchr = new WorksheetCommandHistoryExecutor(worksheet.getId(), workspace);
		try
		{
			List<CommandTag> tags = new ArrayList<>();
			tags.add(CommandTag.Selection);
			tags.add(CommandTag.Transformation);
			
			List<CommandTag> ignoreTags = new ArrayList<>();
			ignoreTags.add(CommandTag.IgnoreInBatch);
			wchr.executeCommandsByTags(tags, 
					ignoreTags,
					new JSONArray(mapping.getWorksheetHistoryString()));
		}
		catch (CommandException | KarmaException e)
		{
			logger.error("Unable to execute column transformations", e);
		}
	}
	
}
