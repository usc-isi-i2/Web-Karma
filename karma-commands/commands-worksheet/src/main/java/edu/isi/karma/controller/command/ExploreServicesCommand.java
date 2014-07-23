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
package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.SPARQLGeneratorUtil;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;

import org.apache.http.client.ClientProtocolException;
import org.apache.poi.hssf.record.Margin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class responsible for fetching all the graphs in the tripleStore
 */
public class ExploreServicesCommand extends WorksheetCommand {
	
	private String nodeId;
	
	protected ExploreServicesCommand(String id, String worksheetId, String root) {
		super(id, worksheetId);
		this.nodeId = root;
	}

	private enum JsonKeys {
		worksheetId, updateType, nodeId, services
	}
	
	private static Logger logger = LoggerFactory.getLogger(ExploreServicesCommand.class);
	
	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "ExploreServices";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SPARQLGeneratorUtil sparqlUtil = new SPARQLGeneratorUtil();
		long time_slipts = System.currentTimeMillis();
		String gName = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.graphName);
		String query = sparqlUtil.getExploreServicesQuery(gName, this.nodeId);
		logger.info(query);
		JSONArray final_services = new JSONArray();
		
		try {
			String sData = TripleStoreUtil.invokeSparqlQuery(query, 
					TripleStoreUtil.defaultModelsRepoUrl, "application/sparql-results+json", null);
			JSONObject results = null;
			try {
				results = new JSONObject(sData);
				
			} catch (Exception e) {
				logger.error("Failed to parse JSON result from sparql");
				logger.info("Query to fetch list of columns in the model : " + query);
				return new UpdateContainer(new ErrorUpdate("Error: Failed to fetch columns from the service model"));
			}

			
			JSONArray rows = results.getJSONObject("results").getJSONArray("bindings");
			HashMap<String, JSONObject> services = new HashMap<String, JSONObject>();
			
			JSONObject servObj = null;
			String rowUrl = "";
			for(int i=0; i<rows.length(); i++) {
				JSONObject item = rows.getJSONObject(i);
				rowUrl = item.getJSONObject("s1").getString("value");
				
				if(!services.containsKey(rowUrl)) {
					servObj = new JSONObject();
					servObj.put("s1", item.getJSONObject("s1").getString("value"));
					servObj.put("serviceUrl", item.getJSONObject("serviceUrl").getString("value"));
					servObj.put("serviceRequestMethod", item.getJSONObject("serviceRequestMethod").getString("value"));
					servObj.put("totalArgs", item.getJSONObject("totalArgs").getInt("value"));
					servObj.put("sericeRootNode", item.getJSONObject("sericeRootNode").getString("value"));
					//servicePostMethodType
					servObj.put("servicePostMethodType", item.has("servicePostMethodType") ? item.getJSONObject("servicePostMethodType").getString("value") : false);
					servObj.put("columns", new JSONArray());
					services.put(rowUrl, servObj);
					logger.info("adding new service object for url : " + servObj.getString("s1"));
				}
				servObj = services.get(rowUrl);
				
				JSONObject colObj = new JSONObject();  
				colObj.put("srcPredicate", item.getJSONObject("srcPredicate").getString("value"));
				colObj.put("srcClass", item.getJSONObject("srcClass").getString("value"));
				colObj.put("colName", item.has("colName") ? item.getJSONObject("colName").getString("value") : null);
				colObj.put("srcParentPredicate", item.has("srcParentPredicate") ? item.getJSONObject("srcParentPredicate").getString("value") : null);
				colObj.put("srcParentClass", item.has("srcParentClass") ? item.getJSONObject("srcParentClass").getString("value") : null);
				colObj.put("parentClass", item.has("parentClass") ? item.getJSONObject("parentClass").getString("value") : null);
				colObj.put("parentPredicate", item.has("parentPredicate") ? item.getJSONObject("parentPredicate").getString("value") : null);
				servObj.getJSONArray("columns").put(colObj);
				
			}
			
			if(!services.containsKey(rowUrl)) {
				if(servObj !=null) {
					services.put(servObj.getString("s1"), servObj);
				}
			}
			logger.info("total services found : " + services.size());
					
			int total_args = 0;
			boolean marked_service = true;
			for(String servKey : services.keySet()) {
				marked_service = true;
				total_args = 0;
				JSONObject row = services.get(servKey);
				
				for(int i=0; i<row.getJSONArray("columns").length(); i++) {
					
					JSONObject item =  row.getJSONArray("columns").getJSONObject(i);
					
						// check if parent class and predicate matches
						if(item.has("srcParentClass")) {
							
							if(item.has("parentClass")) {
								if(item.getString("srcParentClass").equalsIgnoreCase(item.getString("parentClass"))
									&& item.getString("srcParentPredicate").equalsIgnoreCase(item.getString("parentPredicate"))) {
									if(item.has("colName") && item.get("colName") != null) {	
										total_args++;
									}
									
								} else {
									logger.info("Relationship or the Classes does not match");
									logger.info("srcParentClass : " + item.getString("srcParentClass") + " (" + "srcParentPredicate : " + item.getString("srcParentPredicate") + ")");
									logger.info("parentClass : " + item.getString("parentClass") + " (" + "parentPredicate : " + item.getString("parentPredicate") + ")");
									marked_service = false;
									break;
									
								}
							} else {
								logger.info("Parent class missing for " + item.getString("srcParentClass"));
								marked_service = false;
								break;
								
							}
						} else {
							// check if column label is present
							if(item.has("colName") && item.get("colName") != null) {	
								total_args++;
							}
						}
						
					}
				if(marked_service && row.getInt("totalArgs") == total_args) {
					// NOTE: comment out the below line if we wish to send all the columns that were matched for this model
					row.remove("columns");
					final_services.put(row);
				}
			}
			
			logger.info("Total services matched : " + final_services.length());
			logger.info("Time take to explore services : " + (System.currentTimeMillis() - time_slipts ) + " ms");
			
			
		} catch (Exception e2) {
			logger.error("Error", e2);
			return new UpdateContainer(new ErrorUpdate("Error: Failed to Explore services that could be invoked"));
		}
		
		final JSONArray results = final_services;
		final String rootNodeId = this.nodeId;
		
		try {
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put(JsonKeys.updateType.name(), "ExploreServices");
						obj.put(JsonKeys.services.name(), results);
						obj.put(JsonKeys.nodeId.name(), rootNodeId);
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Error occurred while preparing json for services!", e);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Error occurred while fetching graphs!", e);
			return new UpdateContainer(new ErrorUpdate("Error occurred while fetching graphs!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
