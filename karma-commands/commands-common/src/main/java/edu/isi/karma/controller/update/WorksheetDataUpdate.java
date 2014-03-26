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

import edu.isi.karma.rep.*;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;

public class WorksheetDataUpdate extends AbstractUpdate {
	
	private final String worksheetId;
	private static Logger logger = LoggerFactory.getLogger(WorksheetDataUpdate.class);
	
	private enum JsonKeys {
		worksheetId, rows, columnName, characterLength, hasNestedTable, columnClass,
		displayValue, expandedValue, nestedRows, additionalRowsCount, tableId, nodeId, rowID
	}

	public WorksheetDataUpdate(String worksheetId) {
		super();
		this.worksheetId = worksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		
		try {
			JSONObject response = new JSONObject();
			response.put(JsonKeys.worksheetId.name(), worksheetId);
			response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					this.getClass().getSimpleName());
			
			Worksheet wk = vWorksheet.getWorksheet();
			Table dataTable = wk.getDataTable();
			TablePager pager = vWorksheet.getTopTablePager();
			
			JSONArray rows = getRowsUsingPager(dataTable, vWorkspace.getRepFactory(), 
					pager, vWorksheet, vWorkspace.getPreferences().getIntViewPreferenceValue(
							ViewPreference.maxCharactersInCell));
			int rowsLeft = dataTable.getNumRows() - rows.length();
			rowsLeft = rowsLeft < 0 ? 0 : rowsLeft;
			response.put(JsonKeys.additionalRowsCount.name(), rowsLeft);
			response.put(JsonKeys.tableId.name(), dataTable.getId());
			response.put(JsonKeys.rows.name(), rows);
			
//			logger.debug(response.toString(2));
			pw.println(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONArray getRowsUsingPager(Table dataTable, RepFactory repFactory, 
			TablePager pager, VWorksheet vWorksheet, int maxDataDisplayLength) throws JSONException {
		return getRowsJsonArray(pager.getRows(), vWorksheet, repFactory, maxDataDisplayLength);
	}
	
	public JSONArray getRowsJsonArray(List<Row> rows, VWorksheet vWorksheet, RepFactory repFactory,
			int maxDataDisplayLength) throws JSONException {
		JSONArray rowsArr = new JSONArray();
		
		HTable hTable = null;
		List<String> orderedHnodeIds = null;
		for (Row row:rows) {
			JSONArray rowValueArray = new JSONArray();
			
			// Get the HTable so that we get the values in the correct order of columns
			if (hTable == null) {
				String hTableId = row.getBelongsToTable().getHTableId();
				hTable = repFactory.getHTable(hTableId);
				if (hTable == null) {
					logger.error("No HTable found for a row. This should not happen!");
					continue;
				}
				orderedHnodeIds = hTable.getOrderedNodeIds();
			}
			
			for (String hNodeId : orderedHnodeIds) {
				Node node = row.getNode(hNodeId);
				JSONObject nodeObj = new JSONObject();
				nodeObj.put(JsonKeys.columnClass.name(), 
						WorksheetHeadersUpdate.getColumnClass(hNodeId));
				nodeObj.put(JsonKeys.nodeId.name(), node.getId());
				nodeObj.put(JsonKeys.rowID.name(), row.getId());
				if (node.hasNestedTable()) {
					nodeObj.put(JsonKeys.hasNestedTable.name(), true);
					Table nestedTable = node.getNestedTable();
					JSONArray nestedTableRows = getRowsUsingPager(nestedTable, repFactory, 
							vWorksheet.getNestedTablePager(nestedTable), vWorksheet, 
							maxDataDisplayLength);
					nodeObj.put(JsonKeys.nestedRows.name(), nestedTableRows);
					nodeObj.put(JsonKeys.tableId.name(), node.getNestedTable().getId());
					
					int rowsLeft = nestedTable.getNumRows() - nestedTableRows.length();
					rowsLeft = rowsLeft < 0 ? 0 : rowsLeft;
					nodeObj.put(JsonKeys.additionalRowsCount.name(), rowsLeft);
					
				} else {
					String nodeVal = node.getValue().asString();
					nodeVal = (nodeVal == null) ? "" : nodeVal;
					String displayVal = (nodeVal.length() > maxDataDisplayLength) 
							? nodeVal.substring(0, maxDataDisplayLength) + "..." : nodeVal;
					nodeObj.put(JsonKeys.displayValue.name(), displayVal);
					nodeObj.put(JsonKeys.expandedValue.name(), nodeVal);
					nodeObj.put(JsonKeys.hasNestedTable.name(), false);
				}
				rowValueArray.put(nodeObj);
			}
			rowsArr.put(rowValueArray);
		}
		return rowsArr;
	}
}