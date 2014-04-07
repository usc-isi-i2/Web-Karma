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


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
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
		
		final String csvFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  
				this.csvFileName;;
		
		try {
			
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
			String line;
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
				
            	while((line  = buf.readLine()) != null) {
            		responseString.append(line);
                }
			}
			UpdateContainer uc = new UpdateContainer();
			
			JSONObject resutsJson = new JSONObject(responseString.toString());
			Import impJson = new JsonImport(responseString.toString(), resutsJson.optString("model_name", "results_"+this.csvFileName), workspace, "UTF-8", -1);
            Worksheet wsht = impJson.generateWorksheet();
            Worksheet wsht2, wsht3;
            logger.info("Creating worksheet with json : " + wsht.getId());
            uc.add(new WorksheetListUpdate());
            uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
            
            if(resutsJson.has("ConfusionMatrixPath")) {
            // get the matrix file 
	            try {
	            	BufferedInputStream buf;
	            	byte[] buffer = new byte[10240];
	            	final String fileName = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) + 
	            			resutsJson.optString("ConfusionMatrixFileName"); // "martix_"+System.nanoTime();
	            	FileOutputStream fw = new FileOutputStream(fileName);
	            	// get the file from the service
	            	URL url = new URL(resutsJson.optString("ConfusionMatrixPath"));
	            	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            	conn.setRequestMethod("GET");
	            	buf = new BufferedInputStream(conn.getInputStream());
	            	for (int length = 0; (length = buf.read(buffer)) > 0;) {
	            		fw.write(buffer, 0, length);
	                }
	            	fw.close();
	            	buf.close();
	            	logger.info("Created : " + fileName);
	            	Import impCSV = new CSVFileImport(1, 2, ',', ' ', "UTF-8", -1, new File(fileName), workspace);
	            	wsht2 = impCSV.generateWorksheet();
	                uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht2.getId()));
	                new File(fileName).delete();
	
	            } catch (Exception e1) {
	            	logger.error(e1.getMessage(), e1);
	            }
            }
            
            
            if(resutsJson.has("PredictionPath")) {
                // get the matrix file 
    	            try {
    	            	BufferedInputStream buf;
    	            	byte[] buffer = new byte[10240];
    	            	final String fileName = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) + 
    	            			resutsJson.optString("PredictionFileName"); // "martix_"+System.nanoTime();
    	            	FileOutputStream fw = new FileOutputStream(fileName);
    	            	// get the file from the service
    	            	URL url = new URL(resutsJson.optString("PredictionPath"));
    	            	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	            	conn.setRequestMethod("GET");
    	            	buf = new BufferedInputStream(conn.getInputStream());
    	            	for (int length = 0; (length = buf.read(buffer)) > 0;) {
    	            		fw.write(buffer, 0, length);
    	                }
    	            	fw.close();
    	            	buf.close();
    	            	logger.info("Created : " + fileName);
    	            	Import impCSV = new CSVFileImport(1, 2, ',', ' ', "UTF-8", -1, new File(fileName), workspace);
    	            	wsht3 = impCSV.generateWorksheet();
    	            	logger.info(wsht3.getHeaders().toString());
    	                uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht3.getId()));
    	                new File(fileName).delete();
    	
    	            } catch (Exception e1) {
    	            	logger.error(e1.getMessage(), e1);
    	            }
                }
            
            
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
