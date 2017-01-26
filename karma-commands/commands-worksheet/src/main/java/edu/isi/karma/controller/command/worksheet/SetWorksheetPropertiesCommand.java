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

package edu.isi.karma.controller.command.worksheet;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.common.HttpMethods;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;

public class SetWorksheetPropertiesCommand extends WorksheetCommand {
	final private String properties;
	private Worksheet worksheet;

	public SetWorksheetPropertiesCommand(String id, String model, String worksheetId, String properties) {
		super(id, model, worksheetId);
		this.properties = properties;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set Worksheet Properties";
	}

	@Override
	public String getDescription() {
		JSONObject propertiesJson = new JSONObject(properties);
		String desc = "";
		String sep = "";
		if (propertiesJson.has(Property.hasServiceProperties.name()) && propertiesJson.getBoolean(Property.hasServiceProperties.name())) {
			desc = "Service";
			sep = ", ";
		}
		if (propertiesJson.has(Property.graphLabel.name()) && 
				!propertiesJson.getString(Property.graphLabel.name()).isEmpty()) {
			desc = desc + sep + "Model Name: " + propertiesJson.getString(Property.graphLabel.name());
			sep = ", ";
		}
		if (propertiesJson.has("hasPrefix") && propertiesJson.getBoolean("hasPrefix")) {
			desc = desc + sep + "Prefix: " + propertiesJson.getString(Property.prefix.name());
			sep = ", ";
		}
		if (propertiesJson.has("hasBaseURI") && propertiesJson.getBoolean("hasBaseURI")) {
			desc = desc + sep + "Base URI: " + propertiesJson.getString(Property.baseURI.name());
			sep = ", ";
		}
		if (propertiesJson.has("hasGithubURL") && propertiesJson.getBoolean("hasGithubURL")) {
			desc = desc + sep + "Github URL: " + propertiesJson.getString(Property.GithubURL.name());
			sep = ", ";
		}
		return desc;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		worksheet = workspace.getWorksheet(worksheetId);
		
		JSONObject propertiesJson;
		try {
			propertiesJson = new JSONObject(properties);
			WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
			if (props == null) {
				props = new WorksheetProperties();
				worksheet.getMetadataContainer().setWorksheetProperties(props);
			}
			
			// Parse the properties and set WorksheetProperties data structure
			String graphLabel = propertiesJson.getString(Property.graphLabel.name());
			if (!graphLabel.trim().isEmpty()) {
				props.setPropertyValue(Property.graphLabel, graphLabel);
				props.setPropertyValue(Property.graphName, WorksheetProperties.createDefaultGraphName(graphLabel));
			}
			
			if (propertiesJson.getBoolean(Property.hasServiceProperties.name())) {
				props.setHasServiceProperties(true);
				// Service URL
				props.setPropertyValue(Property.serviceUrl, 
						propertiesJson.getString(Property.serviceUrl.name()));
				// Service method
				props.setPropertyValue(Property.serviceRequestMethod, 
						propertiesJson.getString(Property.serviceRequestMethod.name()));
				// Set the service invocation style if http method is POST 
				if (propertiesJson.getString(Property.serviceRequestMethod.name())
						.equals(HttpMethods.POST.name())) {
					props.setPropertyValue(Property.serviceDataPostMethod, 
							propertiesJson.getString(Property.serviceDataPostMethod.name()));
				}
			}

			if (propertiesJson.has(Property.GithubURL.name()) && !propertiesJson.getString(Property.GithubURL.name()).isEmpty())
				props.setPropertyValue(Property.GithubURL, propertiesJson.getString(Property.GithubURL.name()));
			
			if (propertiesJson.getBoolean("hasPrefix")) {
				props.setPropertyValue(Property.prefix, 
						propertiesJson.getString(Property.prefix.name()));
			}
			if (propertiesJson.getBoolean("hasBaseURI")) {
				props.setPropertyValue(Property.baseURI, 
						propertiesJson.getString(Property.baseURI.name()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate("Malformed properties object received"));
		}
		
		return new UpdateContainer(new InfoUpdate("Properties set successfully"));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}

}
