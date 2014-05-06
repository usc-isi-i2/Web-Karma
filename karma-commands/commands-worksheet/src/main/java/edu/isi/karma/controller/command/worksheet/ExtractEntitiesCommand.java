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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;

/**
 * Adds extract entities commands to the column menu.
 */

public class ExtractEntitiesCommand extends WorksheetCommand {

	private String hNodeId;
	// add column to this table
	private String hTableId;
	
	//URL for Extraction Service as input by the user
	private String extractionURL;
	//Entities that the user wants to extract
	private String entitiesToBeExt;

	private static Logger logger = LoggerFactory
			.getLogger(ExtractEntitiesCommand.class);

	protected ExtractEntitiesCommand(String id, String worksheetId,
			String hTableId, String hNodeId, String extractionURL, String entitiesToBeExt) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		this.extractionURL = extractionURL;
		this.entitiesToBeExt = entitiesToBeExt;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return ExtractEntitiesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Extract Entities";
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
		System.out.println("in do it");
		System.out.println(extractionURL);

		String[] entities = entitiesToBeExt.split(",");
		HashSet<String> entitiesReqd = new HashSet<String>();
		
		entitiesReqd.addAll(Arrays.asList(entities));
		
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0,
				worksheet.getDataTable().getNumRows());

		JSONArray array = new JSONArray();
		AddValuesCommand cmd;
		StringBuffer extractions;

		for (Row row : rows) {
			String id = row.getId();
			JSONArray t = new JSONArray();
			Node node = row.getNode(hNodeId);
			String value = node.getValue().asString();
			JSONObject obj = new JSONObject();
			System.out.println(value);

			obj.put("rowId", id);
			obj.put("text", value);
			array.put(obj);
		}

		// POST Request to ExtractEntities API.
		try {

			//String url = "http://karmanlp.isi.edu:8080/ExtractionService/myresource";
			String url = extractionURL;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset","utf-8");

			// POST content. JSON String
			String urlParameters = array.toString();
			urlParameters = new String(urlParameters.getBytes(Charset.forName("UTF-8")), Charset.forName("ISO-8859-1"));

			// Send POST request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			extractions = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				extractions.append(inputLine);
			}
			in.close();

		} catch (Exception e) {
			logger.error("Error in ExtractEntitiesCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}

		// print result
		System.out.println(extractions.toString());

		JSONArray result = (JSONArray) JSONUtil.createJson(extractions
				.toString());

		//Final Data for AddValuesCommand
		JSONArray rowData = new JSONArray();

		// index for result iteration
		int index = 0;

		for (Row row : rows) {

			if (index < result.length()) {
				JSONObject extraction = (JSONObject) result.getJSONObject(index++).get("extractions");
				System.out.println("test1");
				
				JSONObject extractionValues = new JSONObject();
				
				//Check if the user wants People entities
				if(entitiesReqd.contains("People")) {
				//***Extracting People***
				JSONArray peopleExtract = (JSONArray) extraction.get("people");
				JSONArray peopleValues = new JSONArray();
				
				
				for(int i=0; i<peopleExtract.length(); i++) {
					peopleValues.put(new JSONObject().put("extraction", ((JSONObject)peopleExtract.get(i)).getString("extraction"))); 
				}
				
				extractionValues.put("People", peopleValues);
				}
								
				
				//Check if the user wants Places entities
				if(entitiesReqd.contains("Places")) {
				//***Extracting Places***
				
				JSONArray placesExtract = (JSONArray) extraction.get("places");
				JSONArray placesValues = new JSONArray();
				
				
				for(int i=0; i<placesExtract.length(); i++) {
					placesValues.put(new JSONObject().put("extraction", ((JSONObject)placesExtract.get(i)).getString("extraction"))); 
				}
				
				
				extractionValues.put("Places", placesValues);
				}
				
				//Check if the user wants Date entities
				if(entitiesReqd.contains("Dates")) {
				//***Extracting People***
				
				JSONArray datesExtract = (JSONArray) extraction.get("dates");
				JSONArray datesValues = new JSONArray();
					
				
				for(int i=0; i<datesExtract.length(); i++) {
					datesValues.put(new JSONObject().put("extraction", ((JSONObject)datesExtract.get(i)).getString("extraction"))); 
				}
				
				extractionValues.put("Dates", datesValues);
				}
				
				JSONObject extractionsObj = new JSONObject();
				extractionsObj.put("extractions", extractionValues);
				
				JSONObject rowDataObject = new JSONObject();
				rowDataObject.put("values", extractionsObj);
				rowDataObject.put("rowId", row.getId());
				rowData.put(rowDataObject);
			}
		}

		JSONObject addValuesObj = new JSONObject();
		addValuesObj.put("name", "AddValues");
		addValuesObj.put("value", rowData.toString());
		addValuesObj.put("type", "other");
		JSONArray addValues = new JSONArray();
		addValues.put(addValuesObj);

		System.out.println(JSONUtil.prettyPrintJson(addValues.toString()));

		
		try {
			AddValuesCommandFactory factory = new AddValuesCommandFactory();
			cmd = (AddValuesCommand) factory.createCommand(addValues, workspace, hNodeId, worksheetId,
					hTableId);
			
			HNode hnode = worksheet.getHeaders().getHNode(hNodeId);
			cmd.setColumnName(hnode.getColumnName()+" Extractions");
			cmd.doIt(workspace);

			UpdateContainer c = new UpdateContainer(new InfoUpdate("Extracted Entities"));
			c.append(WorksheetUpdateFactory
					.createRegenerateWorksheetUpdates(worksheetId));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			//c.append(new InfoUpdate("Extracted Entities"));
			return c;
		} catch (Exception e) {
			logger.error("Error in ExtractEntitiesCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}


		// return new UpdateContainer(new InfoUpdate("Extracted Entities"));

	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {

		return WorksheetUpdateFactory
				.createRegenerateWorksheetUpdates(worksheetId);
	}

}

// mvn clean compile -D jetty.port=9999 jetty:run