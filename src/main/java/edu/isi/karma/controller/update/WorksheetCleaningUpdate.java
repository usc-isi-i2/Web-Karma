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

import edu.isi.karma.rep.ColumnMetadata;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WorksheetCleaningUpdate extends
		AbstractUpdate {
	
	private String	worksheetId;
	private boolean	forceUpdates;

	private static Logger logger = LoggerFactory.getLogger(
			WorksheetCleaningUpdate.class);
	
	public static int DEFAULT_COLUMN_LENGTH = 10;
	public static int MIN_COLUMN_LENGTH = 10;
	
	private enum JsonKeys {
		worksheetId, hNodeId, worksheetChartData, chartData, id, value, json, Preferred_Length
	}
	
	public WorksheetCleaningUpdate(String worksheetId, boolean forceUpdates) {
		this.worksheetId = worksheetId;
		this.forceUpdates = forceUpdates;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		Worksheet worksheet = vWorksheet.getWorksheet();
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		ColumnMetadata colMetadata = worksheet.getMetadataContainer().getColumnMetadata();
		
		List<String> columnsInvoked = new ArrayList<String>();
		
		for (HNodePath path:columnPaths) {
			String leafHNodeId = path.getLeaf().getId();
			Collection<Node> nodes = new ArrayList<Node>();
			worksheet.getDataTable().collectNodes(path, nodes);
			try {
				// Check if the column metadata doesn't contains the cleaning information
				if (colMetadata.getColumnHistogramData(leafHNodeId) == null 
						|| forceUpdates) {
					// Prepare the input data for the cleaning service
					JSONArray requestJsonArray = new JSONArray();  
					for (Node node : nodes) {
						JSONObject jsonRecord = new JSONObject();
						jsonRecord.put(JsonKeys.id.name(), node.getId());
						String originalVal = node.getValue().asString();
						originalVal = originalVal == null ? "" : originalVal;
						jsonRecord.put(JsonKeys.value.name(), originalVal);
						requestJsonArray.put(jsonRecord);
					}
					if (requestJsonArray.length() == 0) {
						logger.error("Empty values input for path" + path.toColumnNamePath());
						continue;
					}
					String cleaningServiceURL = ServletContextParameterMap.getParameterValue(
							ContextParameter.CLEANING_SERVICE_URL);
					
					Map<String, String> formParams = new HashMap<String, String>();
					formParams.put(JsonKeys.json.name(), requestJsonArray.toString());
					
					String reqResponse = HTTPUtil.executeHTTPPostRequest(cleaningServiceURL, null,
							null, formParams);
//					
//					System.out.println("***");
//					System.out.println(path.getLeaf().getColumnName());
//					System.out.println(reqResponse);
					try {
						// Test if the output is valid JSON object. Throws exception if not.
						JSONObject output = new JSONObject(reqResponse);
						
						// Add to the metadata if valid
						colMetadata.addColumnHistogramData(leafHNodeId, output);
	
						// Parse the request response to populate the column metadata for the worksheet
						int colLength = getColumnLength(path.getLeaf(), output, 
								vWorkspace.getPreferences().getIntViewPreferenceValue(
										ViewPreference.maxCharactersInCell));
						colMetadata.addColumnPreferredLength(leafHNodeId, colLength);
						
						// Add the hNodeId to the list for which we invoked successfully
						columnsInvoked.add(leafHNodeId);
					} catch (JSONException e) {
						logger.error("Error occured with cleaning service for HNode: " 
								+ path.toColumnNamePath(), e);
						
						// Set to a default column word length
						colMetadata.addColumnPreferredLength(leafHNodeId, DEFAULT_COLUMN_LENGTH);
						continue;
					}	
				}
			} catch (Exception e) {
				logger.error("Error while invoking cleaning service", e);
			}
		}
		
		// Prepare the Update that is going to be sent to the browser
		JSONObject response = new JSONObject();
		try {
			response.put(GenericJsonKeys.updateType.name(), this.getClass().getSimpleName());
			response.put(JsonKeys.worksheetId.name(), vWorksheet.getId());
			JSONArray chartData = new JSONArray();
			
			for (String hNodeId:columnsInvoked) {
				JSONObject columnChartData = new JSONObject();
				columnChartData.put(JsonKeys.hNodeId.name(), hNodeId);
				try {
					columnChartData.put(JsonKeys.chartData.name(), 
							colMetadata.getColumnHistogramData(hNodeId));
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
	
	private int getColumnLength(HNode hNode, JSONObject serviceResults, int maxColumnWidth)
			throws JSONException {
		int colLength = serviceResults.getInt(JsonKeys.Preferred_Length.name());
		colLength = (colLength == -1 || colLength == 0) ? DEFAULT_COLUMN_LENGTH : colLength;
		
		// Check if it is greater that max column data length
		colLength = (colLength > maxColumnWidth) ? maxColumnWidth : colLength;
		
		// Check if it is lesser than minimum required for the cleaning chart
		colLength = (colLength < MIN_COLUMN_LENGTH) ? MIN_COLUMN_LENGTH : colLength;
		
		// Check if column name requires more characters
		String colName = hNode.getColumnName();
		colLength = (colLength < colName.length()) ? colName.length() : colLength; 
		return colLength;
	}
}
