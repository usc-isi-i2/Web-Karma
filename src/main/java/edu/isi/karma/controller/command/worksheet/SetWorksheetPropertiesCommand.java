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

import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;

public class SetWorksheetPropertiesCommand extends Command {
	final private String worksheetId;
	final private String properties;
	private Worksheet worksheet;

	public SetWorksheetPropertiesCommand(String id, String worksheetId, String properties) {
		super(id);
		this.worksheetId = worksheetId;
		this.properties = properties;
		
		addTag(CommandTag.Modeling);
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
		return worksheet.getTitle();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		worksheet = workspace.getWorksheet(worksheetId);
		
		JSONObject propertiesJson = null;
		try {
			propertiesJson = new JSONObject(properties);
			
			WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
			if (props == null) {
				props = new WorksheetProperties();
				worksheet.getMetadataContainer().setWorksheetProperties(props);
			}
			
			// Parse the properties and set WorksheetProperties data structure
			String modelName = propertiesJson.getString(Property.graphName.name());
			props.setPropertyValue(Property.graphName, modelName);
			
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
						.equals(HttpMethod.POST.name())) {
					props.setPropertyValue(Property.serviceDataPostMethod, 
							propertiesJson.getString(Property.serviceDataPostMethod.name()));
				}
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
