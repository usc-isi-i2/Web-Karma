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
/*
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class InvokeCleaningServiceCommand extends Command {
	private String hNodeId;
	private String vWorksheetId;
	private static Logger logger = LoggerFactory.getLogger(InvokeCleaningServiceCommand.class);

	public InvokeCleaningServiceCommand(String id, String hNodeId, String vWorksheetId) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		String worksheetId = vWorkspace.getViewFactory().getVWorksheet(this.vWorksheetId).getWorksheetId();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();

		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) { 
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		vWorkspace.getRepFactory().getWorksheet(worksheetId).getDataTable()
		.collectNodes(selectedPath, nodes);
		try {
			JSONArray requestJsonArray = new JSONArray();  
			for (Node node : nodes) {
				String id = node.getId();
				String originalVal = node.getValue().asString();
				JSONObject jsonRecord = new JSONObject();
				jsonRecord.put("id", id);
				jsonRecord.put("value", originalVal);
				requestJsonArray.put(jsonRecord);
				//System.out.println(id + " " + originalVal);
			}
			String jsonString = null;
			jsonString = requestJsonArray.toString();
			//System.out.println(jsonString);

			String url = "http://localhost:8080/cleaningService/IdentifyData";
			//String url = "http://localhost:8070/myWS/IdentifyData";
			//System.out.println(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			HttpResponse response = null;
			HttpEntity entity;
			StringBuffer out = new StringBuffer();
			//logger.info(url);
			URI u = null ;
			u = new URI(url) ;
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("json",jsonString));

			httppost = new HttpPost(u);
			httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			response = httpclient.execute(httppost);
			entity = response.getEntity();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
				String line = buf.readLine();
				while(line != null) {
					out.append(line);
					line = buf.readLine();
				}
			}	
			//System.out.println(out.toString());
			//logger.info("Connnection success : " + url + " Successful.");
			//System.out.println("Connnection success : " + url + " Successful.");
			final JSONObject data1  = new JSONObject(out.toString());
			//System.out.println("Data--->" + data1);
			return new UpdateContainer(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject response = new JSONObject();
					//System.out.println("Reached here");
					try {
						response.put("updateType", "CleaningServiceOutput");
						response.put("chartData", data1);
						response.put("hNodeId", hNodeId); 
						//System.out.print(response.toString(4));
					} catch (JSONException e) {
						pw.print("Error");
					}

					pw.print(response.toString());
				}
			});
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate("Error!"));
		}
	} 

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
