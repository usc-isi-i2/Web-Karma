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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.ColumnMetadata;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class WorksheetHeadersUpdate extends AbstractUpdate {

	private final String worksheetId;

	private enum JsonKeys {
		worksheetId, columns, columnName, characterLength, hasNestedTable, 
		columnClass, hNodeId
	}

	public WorksheetHeadersUpdate(String worksheetId) {
		super();
		this.worksheetId = worksheetId;
	}

	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		
		try {
			JSONObject response = new JSONObject();
			response.put(JsonKeys.worksheetId.name(), worksheetId);
			response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					this.getClass().getSimpleName());
			
			Worksheet wk = vWorksheet.getWorksheet();
			ColumnMetadata colMeta = wk.getMetadataContainer().getColumnMetadata();
			HTable headers = wk.getHeaders();
			
			JSONArray columns = getColumnsJsonArray(headers, colMeta);
			response.put(JsonKeys.columns.name(), columns);
			
			pw.println(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONArray getColumnsJsonArray(HTable headers, ColumnMetadata colMeta) throws JSONException {
		JSONArray colArr = new JSONArray();
		
		List<HNode> hNodes = headers.getSortedHNodes();
		for (HNode hNode:hNodes) {
			colArr.put(getColumnJsonObject(hNode, colMeta));
		}
		
		return colArr;
	}
	
	private JSONObject getColumnJsonObject(HNode hNode, ColumnMetadata colMeta) throws JSONException {
		JSONObject hNodeObj = new JSONObject();
		String columnName = hNode.getColumnName();
		
		hNodeObj.put(JsonKeys.columnName.name(), columnName);
		hNodeObj.put(JsonKeys.columnClass.name(), getColumnClass(hNode.getId()));
		hNodeObj.put(JsonKeys.hNodeId.name(), hNode.getId());
		Integer colLength = colMeta.getColumnPreferredLength(hNode.getId());
		if (colLength == null || colLength == 0) {
			hNodeObj.put(JsonKeys.characterLength.name(), WorksheetCleaningUpdate.DEFAULT_COLUMN_LENGTH);
		} else {
			hNodeObj.put(JsonKeys.characterLength.name(), colLength.intValue());
		}
		
		if (hNode.hasNestedTable()) {
			hNodeObj.put(JsonKeys.hasNestedTable.name(), true);
			
			HTable nestedTable = hNode.getNestedTable();
			hNodeObj.put(JsonKeys.columns.name(), getColumnsJsonArray(nestedTable, colMeta));
		} else {
			hNodeObj.put(JsonKeys.hasNestedTable.name(), false);
		}
		return hNodeObj;
	}
	
	public static String getColumnClass(String hNodeId) {
		return hNodeId + "-class";
	}
}