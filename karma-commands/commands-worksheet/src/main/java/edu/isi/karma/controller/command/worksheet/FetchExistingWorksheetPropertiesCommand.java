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

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;

public class FetchExistingWorksheetPropertiesCommand extends WorksheetCommand {
	
	private static Logger logger = LoggerFactory
			.getLogger(FetchExistingWorksheetPropertiesCommand.class);
	
	private enum JsonKeys {
		updateType, properties
	}

	public FetchExistingWorksheetPropertiesCommand(String id, String model, String worksheetId) {
		super(id, model, worksheetId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fetch Worksheet Properties";
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		
		// Check if the model name exists. If not set to a default one
		String graphLabel = worksheet.getMetadataContainer().getWorksheetProperties().
				getPropertyValue(Property.graphLabel); 

		if (graphLabel == null || graphLabel.isEmpty()) {
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.graphLabel, worksheet.getTitle());
				graphLabel = worksheet.getTitle();
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.graphName, WorksheetProperties.createDefaultGraphName(graphLabel));	
		}
		
		String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().
				getPropertyValue(Property.baseURI); 

		if (baseURI == null || baseURI.isEmpty()) {
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.baseURI, "http://localhost:8080/source/");	
		}
		
		String prefix = worksheet.getMetadataContainer().getWorksheetProperties().
				getPropertyValue(Property.prefix); 

		if (prefix == null || prefix.isEmpty()) {
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.prefix, "s");	
		}
		
		
		
		WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
		try {
			final JSONObject propsJson = props.getJSONRepresentation();
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put(JsonKeys.updateType.name(), "ExistingWorksheetProperties");
						obj.put(JsonKeys.properties.name(), propsJson);
						
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Error occurred while fetching worksheet properties!", e);
					}
				}
			});
		} catch (JSONException e) {
			logger.error("Error occurred while fetching worksheet properties!", e);
			return new UpdateContainer(new ErrorUpdate("Error occurred while fetching " +
					"worksheet properties!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
