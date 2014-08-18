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

package edu.isi.karma.controller.command.cleaning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class InvokeCleaningServiceCommand extends WorksheetSelectionCommand {

	private String hNodeId;

	public InvokeCleaningServiceCommand(String id, String hNodeId,
			String worksheetId, String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Invoke cleaning service";
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
		SuperSelection selection = getSuperSelection(worksheet);
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		workspace.getFactory().getWorksheet(worksheetId).getDataTable()
				.collectNodes(selectedPath, nodes, selection);

		try {
			JSONArray requestJsonArray = new JSONArray();
			for (Node node : nodes) {
				String id = node.getId();
				String originalVal = node.getValue().asString();
				JSONObject jsonRecord = new JSONObject();
				jsonRecord.put("id", id);
				originalVal = originalVal == null ? "" : originalVal;
				jsonRecord.put("value", originalVal);
				requestJsonArray.put(jsonRecord);
			}
			String jsonString = null;
			jsonString = requestJsonArray.toString();

			// String url =
			// "http://localhost:8080/cleaningService/IdentifyData";
//			String url = "http://localhost:8070/myWS/IdentifyData";
			String url = ServletContextParameterMap.getParameterValue(
					ContextParameter.CLEANING_SERVICE_URL);

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			HttpResponse response = null;
			HttpEntity entity;
			StringBuffer out = new StringBuffer();

			URI u = null;
			u = new URI(url);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("json", jsonString));

			httppost = new HttpPost(u);
			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			response = httpclient.execute(httppost);
			entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				String line = buf.readLine();
				while (line != null) {
					out.append(line);
					line = buf.readLine();
				}
			}
			// logger.trace(out.toString());
			// logger.info("Connnection success : " + url + " Successful.");
			final JSONObject data1 = new JSONObject(out.toString());
			// logger.trace("Data--->" + data1);
			return new UpdateContainer(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject response = new JSONObject();
					// logger.trace("Reached here");
					try {
						response.put("updateType", "CleaningServiceOutput");
						response.put("chartData", data1);
						response.put("hNodeId", hNodeId);
						// logger.trace(response.toString(4));
					} catch (JSONException e) {
						pw.print("Error");
					}

					pw.print(response.toString());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate("Error!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
