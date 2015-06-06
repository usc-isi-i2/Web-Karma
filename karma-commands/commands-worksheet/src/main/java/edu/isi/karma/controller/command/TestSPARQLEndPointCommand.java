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
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
 * Class responsible for fetching all the graphs in the tripleStore
 */
public class TestSPARQLEndPointCommand extends Command {
	private String tripleStoreUrl;
	
	private enum JsonKeys {
		tripleStoreUrl, connectionStatus, updateType
	}
	
	private static Logger logger = LoggerFactory.getLogger(TestSPARQLEndPointCommand.class);
	
	protected TestSPARQLEndPointCommand(String id, String model, String url){
		super(id, model);
		this.tripleStoreUrl=url;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "TestSPARQLEndPoint";
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
		if(TripleStoreUtil.checkConnection(this.tripleStoreUrl)) {
			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put(JsonKeys.updateType.name(), "TestSPARQLEndPoint");
						obj.put(JsonKeys.connectionStatus.name(), 1);
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Error occurred while performing connection test for sparql endpoint!", e);
					}
				}
			});
		}
		
		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				try {
					obj.put(JsonKeys.updateType.name(), "TestSPARQLEndPoint");
					obj.put(JsonKeys.connectionStatus.name(), 0);
					pw.println(obj.toString());
				} catch (JSONException e) {
					logger.error("Error occurred while performing connection test for sparql endpoint!", e);
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace vorkspace) {
		return null;
	}

}
