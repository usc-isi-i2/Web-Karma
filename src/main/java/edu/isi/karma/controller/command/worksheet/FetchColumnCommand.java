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

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
@author shri
 */
public class FetchColumnCommand extends WorksheetCommand {

	private final String alignmentNodeId;
	private final String tripleStoreUrl;
	private final String graphUrl;
	private final String nodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(FetchColumnCommand.class);
	
	public enum JsonKeys {
		worksheetId, alignmentNodeId, tripleStoreUrl, graphUrl, nodeId
	}
	
	protected FetchColumnCommand(String id, String worksheetId, String alignmentId, String sparqlUrl, String graph, String node ) {
		super(id, worksheetId);
		this.alignmentNodeId = alignmentId;
		this.tripleStoreUrl = sparqlUrl;
		this.graphUrl = graph;
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
		try {
			
			TripleStoreUtil tUtil = new TripleStoreUtil();
			StringBuffer query = new StringBuffer("prefix rr: <http://www.w3.org/ns/r2rml#> prefix km-dev: <http://isi.edu/integration/karma/dev#> ");
	
			query.append("select distinct ?column where { ");
			if(this.graphUrl != null && !this.graphUrl.trim().isEmpty()) {
				query.append(" graph  <" + this.graphUrl + "> { ");
			}
			query.append("{ ?x1 rr:subjectMap/km-dev:alignmentNodeId \"")
				.append(this.nodeId)
				.append("\" . ?x1 rr:predicateObjectMap/rr:objectMap/rr:column ?column .")
				.append(" } UNION { ")
				.append("?x1 rr:subjectMap/km-dev:alignmentNodeId \"")
				.append(this.nodeId)
				.append("\" . ?x1 (rr:predicateObjectMap/rr:objectMap/rr:parentTriplesMap)*/rr:predicateObjectMap/rr:objectMap/rr:column ?column .")
				.append(" } }");
			if(this.graphUrl != null && !this.graphUrl.trim().isEmpty()) {
				query.append(" } ");
			}
			logger.info("Query: " + query.toString());
			String sData = TripleStoreUtil.invokeSparqlQuery(query.toString(), 
					this.tripleStoreUrl, "application/json", null);
			if (sData == null | sData.isEmpty()) {
				logger.error("Empty response object from query : " + query);
			}
			JSONArray cols = new JSONArray();
			try {
				JSONObject obj1 = new JSONObject(sData);
				JSONArray arr = obj1.getJSONObject("results").getJSONArray("bindings");
				for(int i=0; i<arr.length(); i++) {
					cols.put(arr.getJSONObject(i).getJSONObject("column").getString("value"));
				}
			} catch (Exception e2) {
				logger.error("Error in parsing json response", e2);
			}
			
			logger.info("Columns fetched : " + sData);
			final JSONArray columns = cols;
			return new UpdateContainer(new AbstractUpdate() {
				
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("updateType", "FetchColumnUpdate");
						obj.put("columns", columns);
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
