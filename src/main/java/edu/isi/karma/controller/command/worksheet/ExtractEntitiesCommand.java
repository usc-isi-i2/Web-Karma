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
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AddColumnUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;
import edu.isi.karma.webserver.KarmaException;

/**
 * Adds extract entities commands to the column menu.
 */

public class ExtractEntitiesCommand extends WorksheetCommand {

	private String hNodeId;
	// add column to this table
	private String hTableId;

	private static Logger logger = LoggerFactory
			.getLogger(ExtractEntitiesCommand.class);

	protected ExtractEntitiesCommand(String id, String worksheetId,
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
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

		ArrayList<Row> rows = worksheet.getDataTable().getRows(0,
				worksheet.getDataTable().getNumRows());

		JSONArray array = new JSONArray();
		Command cmd;
		StringBuffer extractions;

		for (Row row : rows) {
			String id = row.getId();
			JSONArray t = new JSONArray();
			Node node = row.getNode(hNodeId);
			String value = node.getValue().asString();
			JSONObject obj = new JSONObject();
			System.out.println(value);

			// Needs to be replaced by rowHash. Not sure how to compute/fetch
			// rowHash.
			obj.put("rowHash", id);
			obj.put("text", value);
			array.put(obj);
		}

		// POST Request to ExtractEntities API.
		try {

			String url = "http://localhost:8080/myapp/myresource";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");

			// POST content. JSON String
			String urlParameters = array.toString();

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
		JSONArray people = new JSONArray();
		JSONArray dates = new JSONArray();
		JSONArray places = new JSONArray();

		// index for result iteration
		int index = 0;

		for (Row row : rows) {

			if (index < result.length()) {
				JSONObject extraction = (JSONObject) result.getJSONObject(index++).get("extractions");
				System.out.println("test1");
				
				JSONArray peopleExtract = (JSONArray) extraction.get("people");
				JSONArray datesExtract = (JSONArray) extraction.get("dates");
				JSONArray placesExtract = (JSONArray) extraction.get("places");
				
				JSONArray peopleValues = new JSONArray();
				JSONArray datesValues = new JSONArray();
				JSONArray placesValues = new JSONArray();
				
				for(int i=0; i<peopleExtract.length(); i++) {
					peopleValues.put(new JSONObject().put("People", ((JSONObject) peopleExtract.get(i)).getString("extraction"))); 
				}
								
				for(int i=0; i<datesExtract.length(); i++) {
					datesValues.put(new JSONObject().put("Dates", ((JSONObject) datesExtract.get(i)).getString("extraction"))); 
				}
				
				for(int i=0; i<placesExtract.length(); i++) {
					placesValues.put(new JSONObject().put("Places", ((JSONObject) placesExtract.get(i)).getString("extraction"))); 
				}
				
				
				JSONObject peopleObj = new JSONObject();
				peopleObj.put("rowId",row.getId());
				peopleObj.put("values", peopleValues);
				
				JSONObject datesObj = new JSONObject();
				datesObj.put("rowId",row.getId());
				datesObj.put("values", datesValues);
				
				JSONObject placesObj = new JSONObject();
				placesObj.put("rowId",row.getId());
				placesObj.put("values", placesValues);
				
				people.put(peopleObj);
				dates.put(datesObj);
				places.put(placesObj);
				
			}
		}

		JSONObject peopleAddObj = new JSONObject();
		peopleAddObj.put("name", "AddValues");
		peopleAddObj.put("value", people.toString());
		peopleAddObj.put("type", "other");
		JSONArray peopleInput = new JSONArray();
		peopleInput.put(peopleAddObj);

		JSONObject datesAddObj = new JSONObject();
		datesAddObj.put("name", "AddValues");
		datesAddObj.put("value", dates.toString());
		datesAddObj.put("type", "other");
		JSONArray datesInput = new JSONArray();
		datesInput.put(datesAddObj);

		JSONObject placesAddObj = new JSONObject();
		placesAddObj.put("name", "AddValues");
		placesAddObj.put("value", places.toString());
		placesAddObj.put("type", "other");
		JSONArray placesInput = new JSONArray();
		placesInput.put(placesAddObj);

		
		try {
			AddValuesCommandFactory factory = new AddValuesCommandFactory();
			cmd = factory.createCommand(placesInput, workspace, hNodeId, worksheetId,
					hTableId);
			cmd.doIt(workspace);

			cmd = factory.createCommand(datesInput, workspace, hNodeId, worksheetId,
					hTableId);
			cmd.doIt(workspace);

			cmd = factory.createCommand(peopleInput, workspace, hNodeId, worksheetId,
					hTableId);
			cmd.doIt(workspace);
			
			UpdateContainer c = new UpdateContainer();
			c.append(WorksheetUpdateFactory
					.createRegenerateWorksheetUpdates(worksheetId));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
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