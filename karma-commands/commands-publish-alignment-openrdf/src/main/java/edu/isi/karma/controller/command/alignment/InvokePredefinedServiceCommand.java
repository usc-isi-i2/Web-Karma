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
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.indexer.ast.NWhile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.worksheet.ExportCSVCommand;
import edu.isi.karma.controller.command.worksheet.ExportCSVCommandFactory;
import edu.isi.karma.controller.command.worksheet.ExportCSVCommand.JsonKeys;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.ExportCSVUtil;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
 * @author shri
 * */
public class InvokePredefinedServiceCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(InvokePredefinedServiceCommand.class);
	
	private final String worksheetId;
	private final String tripleStoreUrl;
	private final String graphUrl;
	private final String rootNodeId;
	private final String serviceUrl;
	private final String method;
	private final String postOption;
	private TripleStoreUtil sparqlUtil;
	
	/**
	 * @param id
	 * @param worksheetId
	 * @param miningUrl
	 * @param csvFileName
	 * @param isTesting A boolean flag to identify if it is the training or testing phase
	 * */
	protected InvokePredefinedServiceCommand(String id, String worksheetId, String  tripleStoreUrl, 
			String graphUrl, String rootNodeId, String serviceUrl, String  method, String postOption) {
		super(id);
		this.worksheetId = worksheetId;
		this.rootNodeId = rootNodeId;
		if(graphUrl.equalsIgnoreCase("000")) {
			this.graphUrl = null;
		} else {
			this.graphUrl = graphUrl;
		}
		this.tripleStoreUrl = tripleStoreUrl;
		this.serviceUrl = serviceUrl;
		this.method = method;
		this.postOption = postOption;
		this.sparqlUtil = new TripleStoreUtil();
	}
	
	private enum JsonKeys {
		worksheetId, updateType, serviceUrl
	}

	@Override
	public String getCommandName() {
		return InvokePredefinedServiceCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Invoke Predefined Services";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}
	
//	private UpdateContainer processCSV(HttpEntity entity, Workspace workspace) {
//		UpdateContainer uc = null;
//		try {
//        	BufferedInputStream buf;
//        	byte[] buffer = new byte[10240];
//        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
//        	String ts = sdf.format(Calendar.getInstance().getTime());
//        	final String fName = "table_service_results_"+ts;
//        	final String fileName = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) + fName; 
//        	FileOutputStream fw = new FileOutputStream(fileName);
//        	// get the file from the service
//        	buf = new BufferedInputStream(entity.getContent());
//        	for (int length = 0; (length = buf.read(buffer)) > 0;) {
//        		fw.write(buffer, 0, length);
//            }
//        	fw.close();
//        	buf.close();
//        	logger.info("Created : " + fileName + " by worksheet Id : " +  this.worksheetId) ;
//        	Import impCSV = new CSVFileImport(1, 2, ',', ' ', "UTF-8", -1, new File(fileName), workspace);
//        	Worksheet wsht = impCSV.generateWorksheet();
//        	uc = new UpdateContainer();
//            uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
//            new File(fileName).delete();
//
//        } catch (Exception e1) {
//        	logger.error(e1.getMessage(), e1);
//        	uc = new UpdateContainer(new ErrorUpdate(e1.getMessage()));
//        }
//		return uc;
//		
//	}
//	
//	
//	private UpdateContainer processJSON(HttpEntity entity, Workspace workspace) {
//		UpdateContainer uc = null;
//		try {
//        	BufferedInputStream buf;
//        	byte[] buffer = new byte[10240];
//        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
//        	String ts = sdf.format(Calendar.getInstance().getTime());
//        	final String fName = "table_service_results_"+ts;
//        	final String fileName = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  fName;
//        	FileOutputStream fw = new FileOutputStream(fileName);
//        	// get the file from the service
//        	buf = new BufferedInputStream(entity.getContent());
//        	for (int length = 0; (length = buf.read(buffer)) > 0;) {
//        		fw.write(buffer, 0, length);
//            }
//        	fw.close();
//        	buf.close();
//        	
//			Import impJson = new JsonImport(new File(fileName), fName, workspace, "UTF-8", -1);
//            Worksheet wsht = impJson.generateWorksheet();
////            Worksheet wsht2, wsht3;
//            logger.info("Creating worksheet with json : " + wsht.getId());
//            uc = new UpdateContainer();
//            uc.add(new WorksheetListUpdate());
//            uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
//        	
//        	logger.info("Created : " + fileName);
//            new File(fileName).delete();
//
//        } catch (Exception e1) {
//        	logger.error(e1.getMessage(), e1);
//        	uc = new UpdateContainer(new ErrorUpdate(e1.getMessage()));
//        }
//		return uc;
//		
//	}
	
	@Override
	public UpdateContainer doIt(Workspace workspace) {
		
		if(this.method.equalsIgnoreCase("POST")) {

			if(this.postOption.equalsIgnoreCase("invokeWithWholeWorksheet")) {
				// export csv
				// invoke dm service
				return invokePOSTwholeWorksheetService(workspace);
				
			} else {
				
				// invoke row by row
				return invokePOSTrowByrowWorksheetService(workspace);
				
			}
		} else if(this.method.equalsIgnoreCase("GET")) {
			// invoke row by row, get service
			return invokeGETService(workspace);
		}
		return null;
		
//		final String csvFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  
//				this.csvFileName;
//		UpdateContainer uc = new UpdateContainer();
//		try {
//			
//			// Prepare the headers
//			HttpPost httpPost = new HttpPost(this.dataMiningURL);
//	
//			FileEntity file = new FileEntity(new File(csvFileLocalPath));
//			httpPost.setEntity(file);
//			HttpClient httpClient = new DefaultHttpClient();
//			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//			
//			// Execute the request
//			HttpResponse response = httpClient.execute(httpPost);
//			
//			// Parse the response and store it in a String
//			HttpEntity entity = response.getEntity();
//			Header contentTypeHeader = entity.getContentType();
//			String contentType = contentTypeHeader.getValue();
//			
//			
//			if(contentType.equalsIgnoreCase("application/json")) {
//				uc = processJSON(entity, workspace);
//			} else if(contentType.equalsIgnoreCase("text/csv")) {
//				uc = processCSV(entity, workspace);
//			} else {
//				uc = new UpdateContainer(new ErrorUpdate("Could not parse content type : " + contentType));
//			}
//					
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//			uc = new UpdateContainer(new ErrorUpdate("Error ! " + e.getMessage()));
//		}
//		return uc;

	}
	
	
	
	

	
	private UpdateContainer invokePOSTwholeWorksheetService(Workspace workspace) {
		
		logger.info("Invoking POST, wholeWorksheet service");
		Worksheet worksheet = workspace.getWorksheet(this.worksheetId);
		
		// Prepare the model file path and names
		final String csvFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle().replaceAll("\\.", "_") +  "-export"+".csv"; 
		final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR) +  
				csvFileName;

		ExportCSVUtil.generateCSVFile(workspace, this.worksheetId, this.rootNodeId, 
						this.sparqlUtil.getAllInputColumnsForServiceNode(this.serviceUrl), this.graphUrl, this.tripleStoreUrl, modelFileLocalPath, null);

		InvokeDataMiningServiceCommandFactory dmFactory = new InvokeDataMiningServiceCommandFactory();
		InvokeDataMiningServiceCommand cmd = (InvokeDataMiningServiceCommand)dmFactory
				.createCommand(workspace, worksheetId, csvFileName, this.serviceUrl );						
		return  cmd.doIt(workspace);
		
	}
	
	private UpdateContainer invokePOSTrowByrowWorksheetService(Workspace workspace) {
		
		return invokeRowByRowService(workspace, false);
	}
	
	private UpdateContainer invokeGETService(Workspace workspace) {
		
		return invokeRowByRowService(workspace, true);
	}
	
	private UpdateContainer invokeRowByRowService(Workspace workspace, boolean isGET) {	
		Worksheet wk = workspace.getWorksheet(this.worksheetId);
		RepFactory factory = workspace.getFactory();
		UpdateContainer uc = new UpdateContainer();
		
		// get all the column names from the service model to be invoked
		String gName = wk.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.graphName);
		HashMap<String, String> columnLabelMappings = this.sparqlUtil.getServiceColumnLabelsForCurrentWorksheet(this.rootNodeId, gName, this.serviceUrl);

		
		// find all the hNodeIds for the column names in the service model
		String lasthNodeId = "";
		Collection<HNode> hNodes = wk.getHeaders().getHNodes();
		ArrayList<HNode> hNodes2 = new ArrayList<HNode>();
		for(HNode hn: hNodes) {
			if(columnLabelMappings.containsKey(hn.getColumnName()))  {
				hNodes2.add(hn);
				lasthNodeId = hn.getId();
			} 
		}
		logger.info("Total hNodes fetched for service :" + hNodes2.size());
		
		// adding a new column
		HTable hTable = workspace.getFactory().getHTable(wk.getDataTable().getHTableId());
		HNode ndid = null;
		try {
			ndid = hTable.addNewHNodeAfter(lasthNodeId, HNodeType.Transformation, workspace.getFactory(), "ServiceResults"+System.currentTimeMillis(), wk,true);
			logger.info("Adding new column to worksheet");
			if(ndid == null)
			{
				throw new KarmaException("Unable to add new HNode!");
			}
		} catch (Exception e1) {
			logger.error("Unable to add new HNode!", e1);
		}
		
		// for each row, prepare the post data and invoke the service
		Map<String, String> formparams = null;
		int numRows = wk.getDataTable().getNumRows();
		List<Row> rows = wk.getDataTable().getRows(0, numRows);
		for(Row r : rows) {
			try {
				Thread.sleep(50);
				String responseString = "";
				if(isGET) {
					StringBuffer urlParams = new StringBuffer();
					try {
						for(HNode hn : hNodes2) {
							String paramName = columnLabelMappings.get(hn.getColumnName());
							Node valNode = r.getNode(hn.getId());
							CellValue cellVal = valNode.getValue();
							String param = URIUtil.encodeQuery(cellVal.asString());
							logger.info(param);
							urlParams.append(paramName).append("=").append(param).append("&");
						}
						responseString = HTTPUtil.executeHTTPGetRequest(this.serviceUrl+"?"+urlParams.toString(), "text/plain");
					} catch (Exception e2) {
						logger.error("Failed to invoke service.",e2);
					}
					
					
				} else {
					
					formparams = new HashMap<String, String>();
					for(HNode hn : hNodes2) {
						formparams.put(columnLabelMappings.get(hn.getColumnName()), r.getNode(hn.getId()).getValue().asString());
					}
					responseString = HTTPUtil.executeHTTPPostRequest( this.serviceUrl, "application/x-www-form-urlencoded", "text/plain", formparams);
					logger.info(responseString);
					
				}
				r.setValue(ndid.getId(), responseString, factory);
				logger.info("Got response : " + responseString);
				
			} catch (Exception e2) {
				logger.error("Error while invoking service for params : " + formparams.toString() , e2);
			}
		}
		
		uc.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		uc.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return uc;
		
	}
	

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public UpdateContainer computeAlignmentAndSemanticTypesAndCreateUpdates(Workspace workspace)
	{
		Alignment alignment = getAlignmentOrCreateIt(workspace);
		return WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(worksheetId, workspace, alignment);
	}
	
	private Alignment getAlignmentOrCreateIt(Workspace workspace)
	{
		return AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), worksheetId, workspace.getOntologyManager());
	}

}
