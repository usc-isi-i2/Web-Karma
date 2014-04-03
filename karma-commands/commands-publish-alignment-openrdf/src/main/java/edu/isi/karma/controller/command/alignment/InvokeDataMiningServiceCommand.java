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


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * @author shri
 * */
public class InvokeDataMiningServiceCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(InvokeDataMiningServiceCommand.class);
	private final String worksheetId;
	
	private final String csvFileName;
	private String dataMiningURL;
	private boolean isTestingPhase;
	
	public String getDataMiningURL() {
		return dataMiningURL;
	}

	public void setDataMiningURL(String dataMiningURL) {
		this.dataMiningURL = dataMiningURL;
	}

	/**
	 * @param id
	 * @param worksheetId
	 * @param miningUrl
	 * @param csvFileName
	 * @param isTesting A boolean flag to identify if it is the training or testing phase
	 * */
	protected InvokeDataMiningServiceCommand(String id, String worksheetId, String miningUrl, String fileName, boolean isTesting) {
		super(id);
		this.worksheetId = worksheetId;
		this.dataMiningURL = miningUrl;
		this.csvFileName = fileName;
		this.isTestingPhase = isTesting;
	}

	@Override
	public String getCommandName() {
		return InvokeDataMiningServiceCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Invoke Data Mining Service";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}
	
	@Override
	public UpdateContainer doIt(Workspace workspace) {
		
		final String csvFileLocalPath = ServletContextParameterMap.getParameterValue(
				ContextParameter.USER_DIRECTORY_PATH) +  "publish/CSV/" + this.csvFileName;
		
		try {
			
			// post the results 
			//TODO : integrate the service with karma
			
			// Prepare the headers
			HttpPost httpPost = new HttpPost(this.dataMiningURL);
	
			FileEntity file = new FileEntity(new File(csvFileLocalPath));
			httpPost.setEntity(file);
			HttpClient httpClient = new DefaultHttpClient();
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			
			// Execute the request
			HttpResponse response = httpClient.execute(httpPost);
			
			// Parse the response and store it in a String
			HttpEntity entity = response.getEntity();
			StringBuilder responseString = new StringBuilder();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
				
				String line = buf.readLine();
				while(line != null) {
					responseString.append(line);
					line = buf.readLine();
				}
			}
			logger.info(responseString.toString());
			final String modelFileName = responseString.toString(); 
			UpdateContainer uc = new UpdateContainer();
			uc.add(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,	
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put("updateType", "InvokeDataMiningServiceUpdate");
						outputObject.put("model_name", modelFileName);
						if(isTestingPhase) {
							JSONObject obj = new JSONObject(modelFileName);
								outputObject.put("results", obj);
								outputObject.put("isTestingPhase", true);
							}
							else {
								outputObject.put("model_name", modelFileName);
							}
						pw.println(outputObject.toString());
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
			return uc;
					
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new UpdateContainer(new ErrorUpdate("Error !"));
		}

	}
	

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
