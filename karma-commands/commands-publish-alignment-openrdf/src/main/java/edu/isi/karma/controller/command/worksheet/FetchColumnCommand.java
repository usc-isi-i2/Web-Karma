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
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommandFactory;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

/**
@author shri
 */
public class FetchColumnCommand extends WorksheetSelectionCommand {

//	private final String alignmentNodeId;
//	private final String tripleStoreUrl;
//	private final String graphUrl;
	private final String nodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(FetchColumnCommand.class);
	
	public enum JsonKeys {
		worksheetId, alignmentNodeId, tripleStoreUrl, graphUrl, nodeId
	}
	
	protected FetchColumnCommand(String id, String model, String worksheetId, String alignmentId, 
			String sparqlUrl, String graph, String node, 
			String selectionId) {
		super(id, model, worksheetId, selectionId);
//		this.alignmentNodeId = alignmentId;
//		this.tripleStoreUrl = sparqlUrl;
//		this.graphUrl = graph;
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
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		String worksheetName = worksheet.getTitle();
		try {

			// preparing model file name
			final String modelFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
					worksheetName +  "-model.ttl";
			final String modelFileLocalPath = contextParameters.getParameterValue(
					ContextParameter.R2RML_PUBLISH_DIR) + modelFileName;
			
			File f = new File(modelFileLocalPath);
			
			// preparing the graphUri where the model is published in the triple store
			String graphName = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.graphName);
			if(graphName == null || graphName.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy-kkmmssS");
				String ts = sdf.format(Calendar.getInstance().getTime());
				graphName = "http://localhost/"+workspace.getCommandPreferencesId() + "/" + worksheetId + "/model/" + ts;
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.graphName, graphName);
			}
			
			// If the model is not published, publish it!
			if(!f.exists() || !f.isFile()) {
				GenerateR2RMLModelCommandFactory factory = new GenerateR2RMLModelCommandFactory();
				GenerateR2RMLModelCommand cmd = (GenerateR2RMLModelCommand)factory.createCommand(model, workspace, worksheetId, TripleStoreUtil.defaultModelsRepoUrl, selection.getName(), graphName);
				cmd.doIt(workspace);
			} else {
				// if the model was published 30 min ago, publish it again, just to be sure
				long diff = Calendar.getInstance().getTimeInMillis() - f.lastModified();
				if((diff / 1000L / 60L) > 30) {
					f.delete();
					GenerateR2RMLModelCommandFactory factory = new GenerateR2RMLModelCommandFactory();
					GenerateR2RMLModelCommand cmd = (GenerateR2RMLModelCommand)factory.createCommand(model, workspace, worksheetId, TripleStoreUtil.defaultModelsRepoUrl, selection.getName(), graphName);
					cmd.doIt(workspace);
				}
			}
			
//			TripleStoreUtil tUtil = new TripleStoreUtil();
			StringBuffer query = new StringBuffer("prefix rr: <http://www.w3.org/ns/r2rml#> prefix km-dev: <http://isi.edu/integration/karma/dev#> ");
	
			
			/* ****** this is the query for the list of columns.
			 
				PREFIX km-dev: <http://isi.edu/integration/karma/dev#>
				PREFIX rr: <http://www.w3.org/ns/r2rml#>
				
				select distinct ?class where  {
				  {
				    ?x1 rr:subjectMap/km-dev:alignmentNodeId "------- The full url of the column/class --------".
				    ?x1 rr:predicateObjectMap/rr:objectMap/rr:column ?column .
					?x1 rr:subjectMap/rr:predicate ?class .
				  }
				  UNION
				  {
				    ?x1 rr:subjectMap/km-dev:alignmentNodeId "------- The full url of the column/class --------".
					?x1 (rr:predicateObjectMap/rr:objectMap/rr:parentTriplesMap)* ?x2 .
					?x2 rr:predicateObjectMap/rr:objectMap/rr:column ?column .
					?x2 rr:predicateObjectMap/rr:predicate ?class .
				  }
				}
			 * */
			
			query.append("select distinct ?class ?column where { ");
			if(graphName != null && !graphName.trim().isEmpty()) {
				query.append(" graph  <" + graphName + "> { ");
			}
			query.append("{ ?x1 rr:subjectMap/km-dev:alignmentNodeId \"")
				.append(this.nodeId)
				.append("\" . ?x1 rr:predicateObjectMap/rr:objectMap/rr:column ?column . ?x1 rr:subjectMap/rr:predicate ?class .")
				.append(" } UNION { ")
				.append("?x1 rr:subjectMap/km-dev:alignmentNodeId \"")
				.append(this.nodeId)
				.append("\" . ?x1 (rr:predicateObjectMap/rr:objectMap/rr:parentTriplesMap)* ?x2 .")
				.append(" ?x2 rr:predicateObjectMap ?x3 . ")
				.append(" ?x3 rr:objectMap/rr:column ?column . ?x3 rr:predicate ?class .")
				.append(" } }");
			if(graphName != null && !graphName.trim().isEmpty()) {
				query.append(" } ");
			}
			logger.info("Query: " + query.toString());
			String sData = TripleStoreUtil.invokeSparqlQuery(query.toString(), 
					TripleStoreUtil.defaultModelsRepoUrl, "application/json", null);
			if (sData == null | sData.isEmpty()) {
				logger.error("Empty response object from query : " + query);
			}
			HashMap<String,String> cols = new HashMap<>();
			try {
				JSONObject obj1 = new JSONObject(sData);
				JSONArray arr = obj1.getJSONObject("results").getJSONArray("bindings");
				for(int i=0; i<arr.length(); i++) {
					String colName = arr.getJSONObject(i).getJSONObject("column").getString("value");
					String colValue = arr.getJSONObject(i).getJSONObject("class").getString("value");
					if(cols.containsKey(colName)) {
						logger.error("Duplicate Column <-> property mapping. " + colName + " <=> " + colValue);
					} else {
						cols.put(colName, colValue);
					}
				}
			} catch (Exception e2) {
				logger.error("Error in parsing json response", e2);
			}
			
			logger.info("Total Columns fetched : " + cols.size());
			final HashMap<String,String> columns = cols;
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						Iterator<String> itr =  columns.keySet().iterator();
						JSONArray colList = new JSONArray();
						while(itr.hasNext()) {
							JSONObject o = new JSONObject();
							String k = itr.next();
							o.put("name", k);
							o.put("url", columns.get(k));
							colList.put(o);
						}
						obj.put("updateType", "FetchColumnUpdate");
						obj.put("columns", colList);
						obj.put("rootId", nodeId);
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
