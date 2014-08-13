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

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.ExploreServicesCommand;
import edu.isi.karma.controller.command.ExploreServicesCommandFactory;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommandFactory;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.SPARQLGeneratorUtil;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
@author shri
 */
public class FetchColumnCommand extends WorksheetCommand {

	private final String nodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(FetchColumnCommand.class);
	
	public enum JsonKeys {
		worksheetId, alignmentNodeId, tripleStoreUrl, graphUrl, nodeId
	}
	
	protected FetchColumnCommand(String id, String worksheetId, String alignmentId, String sparqlUrl, String graph, String node ) {
		super(id, worksheetId);
		this.nodeId = node;
	}

	@Override
	public String getCommandName() {
		return FetchColumnCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fetch Columns";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		String worksheetName = worksheet.getTitle();
		try {

			// preparing model file name
			final String modelFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
					worksheetName +  "-model.ttl";
			final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(
					ContextParameter.R2RML_PUBLISH_DIR) + modelFileName;
			
			File f = new File(modelFileLocalPath);
			
			// preparing the graphUri where the model is published in the triple store
			String gName = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.graphName);
			final String graphName; 
			if(gName == null || gName.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy-kkmmssS");
				String ts = sdf.format(Calendar.getInstance().getTime());
				graphName = Uris.KM_DEFAULT_PUBLISH_GRAPH_URI +"model/"+ workspace.getWorksheet(worksheetId).getTitle() + "/" + worksheetId + "/" + ts;
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.graphName, graphName);
			} else {
				graphName = gName;
			}
			
			// If the model is not published, publish it!
			if(!f.exists() || !f.isFile()) {
				GenerateR2RMLModelCommandFactory factory = new GenerateR2RMLModelCommandFactory();
				GenerateR2RMLModelCommand cmd = (GenerateR2RMLModelCommand)factory.createCommand(workspace, worksheetId, TripleStoreUtil.defaultModelsRepoUrl, graphName);
				cmd.doIt(workspace);
			} else {
				// if the model was published 30 min ago, publish it again, just to be sure
				long diff = Calendar.getInstance().getTimeInMillis() - f.lastModified();
				if((diff / 1000L / 60L) > 30) {
					f.delete();
					GenerateR2RMLModelCommandFactory factory = new GenerateR2RMLModelCommandFactory();
					GenerateR2RMLModelCommand cmd = (GenerateR2RMLModelCommand)factory.createCommand(workspace, worksheetId, TripleStoreUtil.defaultModelsRepoUrl, graphName);
					cmd.doIt(workspace);
				}
			}
			
			
			SPARQLGeneratorUtil spqrqlUtil = new SPARQLGeneratorUtil();
			String query = spqrqlUtil.get_fetch_column_query(graphName, this.nodeId);
			
			logger.info("Query: " + query);
			long start_time = System.currentTimeMillis();
			String sData = TripleStoreUtil.invokeSparqlQuery(query, 
					TripleStoreUtil.defaultModelsRepoUrl, "application/sparql-results+json", null);
			if (sData == null | sData.isEmpty()) {
				logger.error("Empty response object");
			}
			JSONArray cols = new JSONArray();
			try {
				JSONObject obj1 = new JSONObject(sData);
				JSONArray arr = obj1.getJSONObject("results").getJSONArray("bindings");
				for(int i=0; i<arr.length(); i++) {
					if(arr.getJSONObject(i).has("colName"))
					{
						URL url = new URL(arr.getJSONObject(i).getJSONObject("srcPredicate").getString("value"));
						String colName = arr.getJSONObject(i).getJSONObject("colName").getString("value");
						String colLabel = arr.getJSONObject(i).getJSONObject("colName").getString("value") + " (property: " + url.getRef() + ")";
						String colValue = arr.getJSONObject(i).getJSONObject("srcPredicate").getString("value");
						JSONObject o = new JSONObject();
						o.put("name", colName);
						o.put("url", colValue);
						o.put("label", colLabel);
						cols.put(o);
					}
					
				}
			} catch (Exception e2) {
				logger.error("Error in parsing json response", e2);
			}
			
			
			logger.info("Total Columns fetched : " + cols.length());
			final JSONArray cols_final = cols;
			logger.info("Result Processing time : " + (System.nanoTime() - start_time) + " ms");
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("updateType", "FetchColumnUpdate");
						obj.put("columns", cols_final);
						obj.put("rootId", nodeId);
						obj.put("model_graph", graphName);
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Error occurred while fetching worksheet properties!", e);
					}
				}
			});

		}catch (Exception e ) {
			String msg = "Error occured while fetching columns!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	

}
