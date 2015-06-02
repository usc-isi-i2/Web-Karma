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

import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 * Class responsible for fetching saved preferences for the command "commandName"
 */
public class FetchPreferencesCommand extends Command {
	//the command for which we retrieve the preferences
	private String commandName;
	

	protected FetchPreferencesCommand(String id, String model, String commandName){
		super(id, model);
		this.commandName=commandName;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "FetchPreferences";
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		/*
		logger.debug("FetchPreferences....");
		
		logger.debug("I get 1.....");
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		logger.debug("I get 1....."+prefObject1);
		 */
		UpdateContainer c = new UpdateContainer();
		c.add(new FetchPreferencesUpdate( commandName+"Preferences", this.getId()));
		return c;
		
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
