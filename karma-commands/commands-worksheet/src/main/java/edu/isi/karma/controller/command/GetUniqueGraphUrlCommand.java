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

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
 * Class responsible for fetching all the graphs in the tripleStore
 */
public class GetUniqueGraphUrlCommand extends WorksheetCommand {
	private String tripleStoreUrl;
	private String graphUriTobeValidated;
	
	private enum JsonKeys {
		updateType, graphUri, worksheetId
	}
	
	private static Logger logger = LoggerFactory.getLogger(GetUniqueGraphUrlCommand.class);
	
	protected GetUniqueGraphUrlCommand(String id, String model, String worksheetId, String url, String context ){
		super(id, model, worksheetId);
		this.tripleStoreUrl=url;
		this.graphUriTobeValidated = context;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "GetUniqueGraphUrl";
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

		// get the source name for this work sheet
		Worksheet worksheet = workspace.getWorksheet(this.worksheetId);
		
		// prepare the graph url if graphUri is not provided
		if(this.graphUriTobeValidated == null || this.graphUriTobeValidated.isEmpty()) {
			String title = worksheet.getTitle();
			title = title.replace(".", "_");
			this.graphUriTobeValidated = "http://localhost.com/worksheets/"+title;
		}
		
		// check for uniqueness with the existing contexts
		if(!utilObj.isUniqueGraphUri(this.tripleStoreUrl, this.graphUriTobeValidated)) {
			// modify the url if required
			if(this.graphUriTobeValidated.charAt(this.graphUriTobeValidated.length()-1) != '/') {
				this.graphUriTobeValidated += "/";
			}
			this.graphUriTobeValidated += String.valueOf(System.nanoTime());
		}
		final String graphUri = this.graphUriTobeValidated;
		logger.info("GraphUri generated : " + graphUri);
		try {
			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put(JsonKeys.updateType.name(), "GetUniqueGraphUrl");
						obj.put(JsonKeys.graphUri.name(), graphUri);
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Error occurred while creating unique graphUri !", e);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Error occurred while fetching graphs!", e);
			return new UpdateContainer(new ErrorUpdate("Error occurred while fetching graphs!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
