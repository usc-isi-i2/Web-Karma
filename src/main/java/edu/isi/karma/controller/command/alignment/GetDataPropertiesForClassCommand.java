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

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.URI;
import edu.isi.karma.view.VWorkspace;

public class GetDataPropertiesForClassCommand extends Command {

	final private String classURI;

	private static Logger logger = LoggerFactory
			.getLogger(GetDataPropertiesForClassCommand.class.getSimpleName());

	public enum JsonKeys {
		updateType, URI, metadata, data
	}

	public GetDataPropertiesForClassCommand(String id, String uri) {
		super(id);
		this.classURI = uri;
	}

	@Override
	public String getCommandName() {
		return "Get Data Properties For Class";
	}

	@Override
	public String getTitle() {
		return this.getClass().getSimpleName();
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
		final OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		final List<String> properties = ontMgr.getDataPropertiesOfClass(classURI, true);

		// Generate and return the JSON
		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "DataPropertiesForClassUpdate");

					JSONArray dataArray = new JSONArray();

					for (String domain : properties) {
						JSONObject classObject = new JSONObject();

						URI domainURI = ontMgr.getURIFromString(domain);
						if (domainURI == null)
							continue;
						
						classObject.put(JsonKeys.data.name(), domainURI.getLocalNameWithPrefixIfAvailable());
						JSONObject metadataObject = new JSONObject();
						metadataObject.put(JsonKeys.URI.name(), domain);
						classObject.put(JsonKeys.metadata.name(), metadataObject);

						dataArray.put(classObject);
					}
					outputObject.put(JsonKeys.data.name(), dataArray);

					pw.println(outputObject.toString());
				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}

}
