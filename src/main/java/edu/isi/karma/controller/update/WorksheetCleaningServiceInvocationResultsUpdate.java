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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WorksheetCleaningServiceInvocationResultsUpdate extends
		AbstractUpdate {
	private String worksheetId;

	private static Logger logger = LoggerFactory.getLogger(
			WorksheetCleaningServiceInvocationResultsUpdate.class);
	
	private enum JsonKeys {
		worksheetId, hNodeId, worksheetChartData, chartData, id, value, json
	}
	
	public WorksheetCleaningServiceInvocationResultsUpdate(String worksheetId) {
		this.worksheetId = worksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		Worksheet worksheet = vWorksheet.getWorksheet();
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		
		Map<String, String> cleaningInvocationOutput = new HashMap<String, String>();
		
		for (HNodePath path:columnPaths) {
			Collection<Node> nodes = new ArrayList<Node>();
			worksheet.getDataTable().collectNodes(path, nodes);
			try {
				// Prepare the input data for the cleaning service
				JSONArray requestJsonArray = new JSONArray();  
				for (Node node : nodes) {
					String id = node.getId();
					String originalVal = node.getValue().asString();
					JSONObject jsonRecord = new JSONObject();
					jsonRecord.put(JsonKeys.id.name(), id);
					originalVal = originalVal == null ? "" : originalVal;
					jsonRecord.put(JsonKeys.value.name(), originalVal);
					requestJsonArray.put(jsonRecord);
				}
				if (requestJsonArray.length() == 0) {
					logger.error("Empty values input for path" + path.toColumnNamePath());
					continue;
				}
				
				String jsonString = requestJsonArray.toString();
				
				String cleaningServiceURL = ServletContextParameterMap.getParameterValue(
						ContextParameter.CLEANING_SERVICE_URL);
				
				Map<String, String> formParams = new HashMap<String, String>();
				formParams.put(JsonKeys.json.name(), jsonString);
				                        
				String reqResponse = HTTPUtil.executeHTTPPostRequest(cleaningServiceURL, null,
						null, formParams);
				cleaningInvocationOutput.put(path.getLeaf().getId(), reqResponse);
				
			} catch (Exception e) {
				logger.error("Error while invoking cleaning service", e);
			}
		}
		
		// Prepare the Update that is going to be sent to the browser
		JSONObject response = new JSONObject();
		try {
			response.put(GenericJsonKeys.updateType.name(), this.getClass().getSimpleName());
			response.put(JsonKeys.worksheetId.name(), vWorksheet.getWorksheetId());
			JSONArray chartData = new JSONArray();
			
			for (String hNodeId:cleaningInvocationOutput.keySet()) {
				JSONObject columnChartData = new JSONObject();
				columnChartData.put(JsonKeys.hNodeId.name(), hNodeId);
				try {
					columnChartData.put(JsonKeys.chartData.name(), 
							new JSONObject(cleaningInvocationOutput.get(hNodeId)));
				} catch (JSONException e) {
					logger.error("Error occured with cleaning service for HNode: " + hNodeId, e);
					continue;
				}
				
				chartData.put(columnChartData);
			}
			response.put(JsonKeys.worksheetChartData.name(), chartData);
			pw.print(response.toString());
		} catch (Exception e) {
			logger.error("Error occured while writing to JSON!", e);
			return;
		}
	}
}
