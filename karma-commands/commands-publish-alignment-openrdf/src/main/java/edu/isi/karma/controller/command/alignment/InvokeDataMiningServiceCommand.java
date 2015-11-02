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
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * @author shri
 * */
public class InvokeDataMiningServiceCommand extends WorksheetCommand {
	private static Logger logger = LoggerFactory.getLogger(InvokeDataMiningServiceCommand.class);
	
	private final String csvFileName;
	private String dataMiningURL;
	
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
	protected InvokeDataMiningServiceCommand(String id, String model, String worksheetId, String miningUrl, String fileName) {
		super(id, model, worksheetId);
		this.dataMiningURL = miningUrl;
		this.csvFileName = fileName;
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
	
	private UpdateContainer processCSV(HttpEntity entity, Workspace workspace) {
		UpdateContainer uc = null;
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		try {
        	BufferedInputStream buf;
        	byte[] buffer = new byte[10240];
        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
        	String ts = sdf.format(Calendar.getInstance().getTime());
        	final String fName = "table_service_results_"+ts;
        	final String fileName = contextParameters.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) + fName; 
        	FileOutputStream fw = new FileOutputStream(fileName);
        	// get the file from the service
        	buf = new BufferedInputStream(entity.getContent());
        	for (int length = 0; (length = buf.read(buffer)) > 0;) {
        		fw.write(buffer, 0, length);
            }
        	fw.close();
        	buf.close();
        	logger.info("Created : " + fileName + " by worksheet Id : " +  this.worksheetId) ;
        	Import impCSV = new CSVFileImport(1, 2, ',', ' ', "UTF-8", -1, new File(fileName), workspace, null);
        	Worksheet wsht = impCSV.generateWorksheet();
        	uc = new UpdateContainer();
            uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
            new File(fileName).delete();

        } catch (Exception e1) {
        	logger.error(e1.getMessage(), e1);
        	uc = new UpdateContainer(new ErrorUpdate(e1.getMessage()));
        }
		return uc;
		
	}
	
	
	private UpdateContainer processJSON(HttpEntity entity, Workspace workspace) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		UpdateContainer uc = null;
		try {
        	BufferedInputStream buf;
        	byte[] buffer = new byte[10240];
        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
        	String ts = sdf.format(Calendar.getInstance().getTime());
        	final String fName = "table_service_results_"+ts;
        	final String fileName = contextParameters.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  fName;
        	FileOutputStream fw = new FileOutputStream(fileName);
        	// get the file from the service
        	buf = new BufferedInputStream(entity.getContent());
        	for (int length = 0; (length = buf.read(buffer)) > 0;) {
        		fw.write(buffer, 0, length);
            }
        	fw.close();
        	buf.close();
        	
			Import impJson = new JsonImport(new File(fileName), fName, workspace, "UTF-8", -1, null,false);
            Worksheet wsht = impJson.generateWorksheet();
//            Worksheet wsht2, wsht3;
            logger.info("Creating worksheet with json : " + wsht.getId());
            uc = new UpdateContainer();
            uc.add(new WorksheetListUpdate());
            uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
        	
        	logger.info("Created : " + fileName);
            new File(fileName).delete();

        } catch (Exception e1) {
        	logger.error(e1.getMessage(), e1);
        	uc = new UpdateContainer(new ErrorUpdate(e1.getMessage()));
        }
		return uc;
		
	}
	
	@Override
	public UpdateContainer doIt(Workspace workspace) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		final String csvFileLocalPath = contextParameters.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  
				this.csvFileName;
		UpdateContainer uc = new UpdateContainer();
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
			Header contentTypeHeader = entity.getContentType();
			String contentType = contentTypeHeader.getValue();
			
			
			if(contentType.equalsIgnoreCase("application/json")) {
				uc = processJSON(entity, workspace);
			} else if(contentType.equalsIgnoreCase("text/csv")) {
				uc = processCSV(entity, workspace);
			} else {
				uc = new UpdateContainer(new ErrorUpdate("Could not parse content type : " + contentType));
			}
					
		} catch (Exception e) {
			logger.error(e.getMessage());
			uc = new UpdateContainer(new ErrorUpdate("Error ! " + e.getMessage()));
		}
		return uc;

	}
	

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
