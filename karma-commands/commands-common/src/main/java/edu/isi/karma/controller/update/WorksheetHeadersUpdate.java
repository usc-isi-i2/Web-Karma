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

import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.ColumnMetadata;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class WorksheetHeadersUpdate extends AbstractUpdate {

	private final String worksheetId;
	private Workspace workspace;
	private SuperSelection selection;
	private enum JsonKeys {
		worksheetId, columns, columnName, characterLength, hasNestedTable, 
		columnClass, hNodeId, pythonTransformation, previousCommandId, 
		columnDerivedFrom, hNodeType, status, onError, selectionPyCode
	}

	public WorksheetHeadersUpdate(String worksheetId, SuperSelection selection) {
		super();
		this.worksheetId = worksheetId;
		this.selection = selection;
	}

	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		workspace = vWorkspace.getWorkspace();
		try {
			JSONObject response = new JSONObject();
			response.put(JsonKeys.worksheetId.name(), worksheetId);
			response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					this.getClass().getSimpleName());
			
			Worksheet wk = vWorksheet.getWorksheet();
			ColumnMetadata colMeta = wk.getMetadataContainer().getColumnMetadata();
			List<VHNode> viewHeaders = vWorksheet.getHeaderViewNodes();
			
			JSONArray columns = getColumnsJsonArray(viewHeaders, colMeta);
			response.put(JsonKeys.columns.name(), columns);
			
			pw.println(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONArray getColumnsJsonArray(List<VHNode> viewHeaders, ColumnMetadata colMeta) throws JSONException {
		JSONArray colArr = new JSONArray();
		
		for (VHNode hNode:viewHeaders) {
			if(hNode.isVisible()) {
				colArr.put(getColumnJsonObject(hNode, colMeta));
			}
		}
		
		return colArr;
	}
	
	private JSONObject getColumnJsonObject(VHNode hNode, ColumnMetadata colMeta) throws JSONException {
		JSONObject hNodeObj = new JSONObject();
		String columnName = hNode.getColumnName();
		
		hNodeObj.put(JsonKeys.columnName.name(), columnName);
		hNodeObj.put(JsonKeys.columnClass.name(), getColumnClass(hNode.getId()));
		hNodeObj.put(JsonKeys.hNodeId.name(), hNode.getId());
		HNode t = workspace.getFactory().getHNode(hNode.getId());
		if (t.hasNestedTable()) {
			Selection sel = selection.getSelection(t.getNestedTable().getId());
			if (sel != null)
			{
				hNodeObj.put(JsonKeys.status.name(), sel.getStatus().name());
				hNodeObj.put(JsonKeys.selectionPyCode.name(), colMeta.getSelectionPythonCode(t.getNestedTable().getId()));
			}
		}
		hNodeObj.put(JsonKeys.hNodeType.name(), t.getHNodeType().name());
		Integer colLength = colMeta.getColumnPreferredLength(hNode.getId());
		if (colLength == null || colLength == 0) {
			hNodeObj.put(JsonKeys.characterLength.name(), WorksheetCleaningUpdate.DEFAULT_COLUMN_LENGTH);
		} else {
			hNodeObj.put(JsonKeys.characterLength.name(), colLength.intValue());
		}
		
		if (hNode.hasNestedTable()) {
			hNodeObj.put(JsonKeys.hasNestedTable.name(), true);
			
			List<VHNode> nestedHeaders = hNode.getNestedNodes();
			hNodeObj.put(JsonKeys.columns.name(), getColumnsJsonArray(nestedHeaders, colMeta));
		} else {
			hNodeObj.put(JsonKeys.hasNestedTable.name(), false);
		}
		String pythonTransformation = colMeta.getColumnPython(hNode.getId());
		if(pythonTransformation != null)
		{
			hNodeObj.put(JsonKeys.pythonTransformation.name(), pythonTransformation);
		}
		String previousCommandId = colMeta.getColumnPreviousCommandId(hNode.getId());
		if(previousCommandId != null)
		{
			hNodeObj.put(JsonKeys.previousCommandId.name(), previousCommandId);
		}
		String columnDerivedFrom = colMeta.getColumnDerivedFrom(hNode.getId());
		if(columnDerivedFrom != null)
		{
			hNodeObj.put(JsonKeys.columnDerivedFrom.name(), columnDerivedFrom);
		}
		
		Boolean onError = colMeta.getColumnOnError(hNode.getId());
		if(onError != null)
		{
			hNodeObj.put(JsonKeys.onError.name(), onError);
		}
				
		return hNodeObj;
	}
	
	public static String getColumnClass(String hNodeId) {
		return hNodeId + "-class";
	}
	
	public boolean equals(Object o) {
		if (o instanceof WorksheetHeadersUpdate) {
			WorksheetHeadersUpdate t = (WorksheetHeadersUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}
}