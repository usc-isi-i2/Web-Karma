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

package edu.isi.karma.controller.command.alignment;

import java.util.HashMap;
import java.util.List;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.FetchR2RMLUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Workspace;

public class FetchR2RMLModelsCommand extends Command {
	
	private String tripleStoreUrl;
	
	public String getTripleStoreUrl() {
		return tripleStoreUrl;
	}

	public void setTripleStoreUrl(String tripleStoreUrl) {
		this.tripleStoreUrl = tripleStoreUrl;
	}

	protected FetchR2RMLModelsCommand(String id, String model, String url) {
		super(id, model);
		if (url == null || url.isEmpty()) {
			url = TripleStoreUtil.defaultServerUrl + "/" + TripleStoreUtil.karma_model_repo;
		}
		this.tripleStoreUrl = url;
	}

	@Override
	public String getCommandName() {
		return FetchR2RMLModelsCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Fetch R2RML from Triple Store";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}
	
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {

		TripleStoreUtil utilObj = new TripleStoreUtil();
		try
		{
			HashMap<String, List<String>> list = utilObj.fetchModelNames(this.tripleStoreUrl);
			return new UpdateContainer(new FetchR2RMLUpdate(list.get("model_names"), list.get("model_urls")));
		}
		catch (Exception e)
		{
			return new UpdateContainer(new ErrorUpdate("Unable to fetch R2RML models: " + e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
